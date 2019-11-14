package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.operation.*;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.NFA;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.util.ESupplier;
import localgoat.util.functional.CharPredicate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Converter{
	private final char[][] classes = new char[256][];
	private final Expression[] substitutions = new Expression[256];
	private final Set<Token<Character>> alphabet = new HashSet<>();


	private static final DFA<Token<Character>> NAME_PARSER;
	static{
		final var converter = new Converter();
		converter.addClass('a', c -> 'a' <= c && c <= 'z');
		converter.addSubstitution('S', "*<1+>a*('-'*<1+>a)");
		converter.addSubstitution('N', "S");
		converter.addSubstitution('P', "'--'S");
		NAME_PARSER = converter.buildDFA("+(N,P,' ')");
	}

	private Type classFor(String name){
		final var tokens = NAME_PARSER.tokenise(Token.from(name))
			.map(token -> token.value())
			.retain(s -> s.indexOf(' ') == -1)
			.toArray(String[]::new);

		if(tokens.length == 0 || tokens[0].startsWith("--")){
			throw new IllegalArgumentException(name);
		}
		final Set<String> flags = new HashSet<>();
		for(int i = 1; i < tokens.length; i++){
			if(!tokens[i].startsWith("--")){
				throw new IllegalArgumentException(name);
			}
			flags.add(tokens[i].substring(2));
		}
		return new Type(tokens[0], flags);
	}

	public void addClass(char sub, CharPredicate members){
		if(('a' <= sub && sub <= 'z')){
			if(classes[sub] != null){
				throw new IllegalStateException(String.format("Unavailable class substitution character: '%s'.", sub));
			}
			addClassP(sub, members);
		}
		else{
			throw new IllegalArgumentException(String.format("Unsupported class substitution character: '%s'.", sub));
		}
	}

	public void addSubstitution(char sub, String expression){
		if('A' <= sub && sub <= 'Z'){
			if(substitutions[sub] != null){
				throw new IllegalStateException(String.format("Unavailable expression substitution character: '%s'.", sub));
			}
			final var expr = Expression.parse(expression);
			final Queue<Expression> loopCheck = new ArrayDeque<>();
			loopCheck.add(expr);
			while(!loopCheck.isEmpty()){
				var embedded = ESupplier.of(loopCheck.poll())
					.branchingMap(
						true,
						e -> {
							if(e instanceof ExpressionTree){
								return ESupplier.from(((ExpressionTree) e).children());
							}
							else return ESupplier.empty();
						}
					)
					.retain(e -> e instanceof Symbol)
					.map(e -> (Symbol) e)
					.retain(
						s -> {
							final char c = s.value();
							return c == sub || (c < 0x100 && substitutions[c] != null);
						}
					)
					.toArray(Symbol[]::new);

				for(Symbol s: embedded){
					if(s.value() == sub){
						throw new IllegalArgumentException(
							String.format(
								"Recursively defined substitution in expression \"%s\" for character '%s'",
								expression,
								s
							)
						);
					}
					else loopCheck.add(substitutions[s.value()]);
				}
			}
			substitutions[sub] = expr;
			build(expr);
		}
		else{
			throw new IllegalArgumentException(String.format("Unsupported expression substitution character '%s'", sub));
		}
	}

	private void addClassP(char sub, CharPredicate members){
		final char[] buffer = new char[256];
		int size = 0;
		for(int i = 0; i < 256; i++){
			final char c = (char)i;
			if(members.test(c)){
				buffer[size++] = c;
			}
		}
		this.classes[sub] = new char[size];
		System.arraycopy(buffer, 0, classes[sub], 0, size);
		alphabet.addAll(Arrays.asList(Token.from(classes[sub])));
	}

	private Map<Class<? extends Expression>, Function<Expression, Automaton<Token<Character>>>> handlers;

	public static void main(String...args){
		final String identifier = "@<identifier>(*<1+>l*(h*<1+>l))";
		final String className = "@<class-name>(*<1+>(U*l))";
		final String pattern = String.format("+(%s, %s)", identifier, className);
		final var converter = new Converter();
		final var series = converter.build(pattern);
		System.err.print(series);

	}

	public Converter(){
		this.handlers = new HashMap<>();
		addClassP('.', c -> true);
		handlers.put(
			LiteralExpression.class,
			expression -> {
				final var literal = (LiteralExpression)expression;
				final var tokens = Token.from(literal.value());
				final var machines = Stream.of(tokens)
					.map(t -> DFA.of(t))
					.toArray(DFA[]::new);

				return new Concatenate<Token<Character>>().apply(machines);
			}
		);
		handlers.put(
			FunctionExpression.class,
			expression -> {
				final var function = (FunctionExpression)expression;
				final char identifier = function.identifier();
				switch(identifier){
					case '!':{
						final var children = function.children();
						if(children.size() != 1){
							throw new IllegalStateException("! function takes a single parameter.");
						}
						final var not = new Not<Token<Character>>(alphabet);
						return not.apply(buildDFA(children.get(0)));
					}
					case '~':{
						final var children = function.children();
						final var accepted = new HashSet<Token<Character>>(alphabet);
						for(var c: function.children()){
							handled:
							{
								if(c instanceof Symbol){
									final char symbol = ((Symbol) c).value();
									if(classes[symbol] == null){
										System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
										accepted.remove(Token.of(symbol));
									}
									else{
										accepted.removeAll(
											Arrays.asList(
												Token.from(classes[symbol])
											)
										);
									}
								}
								else if(c instanceof LiteralExpression && c.length() == 3){
									final char symbol = ((LiteralExpression)c).value().charAt(0);
									accepted.remove(Token.of(symbol));
								}
								else{
									throw new UnsupportedOperationException(
										String.format("All children of ~ operation must be a single symbol (%s is not).", c)
									);
								}
							}
						}
						return DFA.of(accepted.stream().toArray(Token[]::new));

					}
					case '+':{
						final var children = function.children().stream()
							.map(expr -> build(expr))
							.collect(Collectors.toList());
						final var or = new Or<Token<Character>>();
						return or.apply(children);
					}
					case '@':{
						final var name = function.modifiers();
						if(name == null){
							throw new IllegalStateException("@ function requires defined modifiers.");
						}
						final var children = function.children();
						if(children.size() != 1){
							throw new IllegalStateException("@ function only supports a single argument.");
						}
						final var naming = new Name<Token<Character>>(classFor(name));
						return naming.apply(build(children.get(0)));
					}
					case '*':{
						final int minCount;
						final int maxCount = -1;
						{
							final String modifiers = function.modifiers();
							if(modifiers == null){
								minCount = 0;
							}
							else{
								if(!modifiers.equals("1+")){
									throw new UnsupportedOperationException("Not yet implemented: " + modifiers);
								}
								minCount = 1;
							}

						}
						final var children = function.children();
						if(children.size() != 1){
							throw new IllegalStateException("Kleene functions only support a single argument.");
						}
						final var kleen = new Kleene<Token<Character>>((minCount == 0) ? Kleene.Op.STAR : Kleene.Op.PLUS);
						return kleen.apply(build(children.get(0)));
					}
					default:{
						throw new UnsupportedOperationException("Unknown function identifier: " + identifier);
					}
				}
			}
		);

		handlers.put(
			ExpressionSeries.class,
			expression -> {
				final var series = (ExpressionSeries)expression;
				final var children = ESupplier.from(series.children())
					.map(seg -> build(seg))
					.toStream()
					.collect(Collectors.toList());

				final var concat = new Concatenate<Token<Character>>();
				return concat.apply(children);
			}
		);

		handlers.put(
			SpacesExpression.class,
			expression -> null
		);

		handlers.put(
			Symbol.class,
			expression -> {
				final var symbol = (Symbol)expression;
				final char c = symbol.value();
				if(c == '^'){
					return DFA.lambda();
				}
				if((c & 0xffffff00) == 0){
					if('A' <= c && c <= 'Z'){
						final var expr = substitutions[c];
						if(expr != null){
							return buildDFA(expr);
						}
					}
					else{
						final char[] chars = classes[c];
						if(chars != null){
							return DFA.of(Token.from(chars));
						}
					}
					System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
					return DFA.of(Token.of(c));
				}
				throw new IllegalStateException();
			}
		);
	}

	public DFA<Token<Character>> buildDFA(String pattern){
		return buildDFA(Expression.parse(pattern));
	}

	public DFA<Token<Character>> buildDFA(Expression expression){
		final var a = build(expression);
		return (a instanceof DFA) ? (DFA)a : new Convert<>().apply((NFA)a);
	}

	public Automaton<Token<Character>> build(String pattern){
		return build(Expression.parse(pattern));
	}

	private Automaton<Token<Character>> build(Expression expression){
		final var type = expression.getClass();
		final var handler = handlers.get(type);
		if(handler == null){
			throw new UnsupportedOperationException("No handler provided for expression class " + type.getName());
		}
		return handler.apply(expression);
	}
}
