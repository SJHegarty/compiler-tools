package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.automata.Automaton;
import localgoat.lang.compiler.automata.DFA;
import localgoat.lang.compiler.automata.NFA;
import localgoat.lang.compiler.automata.TokenA;
import localgoat.lang.compiler.automata.operation.*;
import localgoat.util.functional.CharPredicate;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Converter{
	private final char[][] classes = new char[256][];
	private final Set<TokenA<Character>> alphabet = new HashSet<>();

	public void addClass(char sub, CharPredicate members){
		if(('a' <= sub && sub <= 'z') || ('A' <= sub && sub <= 'Z')){
			if(classes[sub] != null){
				throw new IllegalStateException(String.format("Unavailable class substitution character: '%s'.", sub));
			}
			addClassP(sub, members);
		}
		else{
			throw new IllegalArgumentException(String.format("Unsupported class substitution character: '%s'.", sub));
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
		alphabet.addAll(Arrays.asList(TokenA.from(classes[sub])));
	}

	private Map<Class<? extends Expression>, Function<Expression, Automaton<TokenA<Character>>>> handlers;

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
				final var tokens = TokenA.from(literal.value());
				final var machines = Stream.of(tokens)
					.map(t -> new DFA<>(t))
					.toArray(DFA[]::new);

				return new Concatenate<TokenA<Character>>().apply(machines);
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
						final var not = new Not<TokenA<Character>>(alphabet);
						return not.apply(buildDFA(children.get(0)));
					}
					case '~':{
						final var children = function.children();
						final var accepted = new HashSet<TokenA<Character>>(alphabet);
						for(var c: function.children()){
							if(c instanceof Symbol){
								final char symbol = ((Symbol)c).value();
								if(classes[symbol] == null){
									System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
									accepted.remove(TokenA.of(symbol));
								}
								else{
									accepted.removeAll(
										Arrays.asList(
											TokenA.from(classes[symbol])
										)
									);
								}
							}
							else{
								throw new UnsupportedOperationException("All children of ~ operation must be a single symbol.");
							}
						}
						return new DFA<TokenA<Character>>(accepted.stream().toArray(TokenA[]::new));

					}
					case '+':{
						final var children = function.children().stream()
							.map(expr -> build(expr))
							.collect(Collectors.toList());
						final var or = new Or<TokenA<Character>>();
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
						final var naming = new Name<TokenA<Character>>(name);
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
						final var kleen = new Kleene<TokenA<Character>>((minCount == 0) ? Kleene.Op.STAR : Kleene.Op.PLUS);
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
				final var children = series.segments().stream()
					.map(seg -> build(seg))
					.collect(Collectors.toList());

				final var concat = new Concatenate<TokenA<Character>>();
				return concat.apply(children);
			}
		);

		handlers.put(
			Symbol.class,
			expression -> {
				final var symbol = (Symbol)expression;
				final char c = symbol.value();
				if(c == '^'){
					return new DFA<TokenA<Character>>();
				}
				if((c & 0xffffff00) == 0){
					final char[] chars = classes[c];
					if(chars == null){
						System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
						return new DFA<TokenA<Character>>(TokenA.of(c));
					}
					else{
						return new DFA<TokenA<Character>>(TokenA.from(chars));
					}

				}
				throw new IllegalStateException();
			}
		);
	}

	public DFA<TokenA<Character>> buildDFA(String pattern){
		return buildDFA(Expression.parse(pattern));
	}

	public DFA<TokenA<Character>> buildDFA(Expression expression){
		final var a = build(expression);
		return (a instanceof DFA) ? (DFA)a : new DFA<TokenA<Character>>((NFA)a);
	}

	public Automaton<TokenA<Character>> build(String pattern){
		return build(Expression.parse(pattern));
	}

	private Automaton<TokenA<Character>> build(Expression expression){
		final var type = expression.getClass();
		final var handler = handlers.get(type);
		if(handler == null){
			throw new UnsupportedOperationException("No handler provided for expression class " + type.getName());
		}
		return handler.apply(expression);
	}
}
