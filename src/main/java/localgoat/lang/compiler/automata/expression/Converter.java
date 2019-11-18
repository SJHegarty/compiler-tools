package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenSeries;
import localgoat.lang.compiler.token.TokenTree;
import localgoat.lang.compiler.automata.expression.handlers.FunctionHandler;
import localgoat.lang.compiler.automata.expression.handlers.LiteralHandler;
import localgoat.lang.compiler.automata.expression.handlers.SeriesHandler;
import localgoat.lang.compiler.automata.expression.handlers.SymbolHandler;
import localgoat.util.ESupplier;
import localgoat.util.functional.CharPredicate;

import java.util.*;
import java.util.function.Function;

public class Converter{
	private final char[][] classes = new char[256][];
	private final Token[] substitutions = new Token[256];
	private final Set<Token> alphabet = new HashSet<>();
	private final ExpressionParser parser = new ExpressionParser();

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
			final var expr = parser.parse(Symbol.from(expression)).get(0);
			final Queue<Token> loopCheck = new ArrayDeque<>();
			loopCheck.add(expr);
			while(!loopCheck.isEmpty()){
				var embedded = ESupplier.of(loopCheck.poll())
					.branchBreadthFirst(
						true,
						e -> {
							if(e instanceof TokenTree){
								return ESupplier.from(((TokenTree) e).children());
							}
							else return ESupplier.empty();
						}
					)
					.retain(e -> e instanceof Symbol)
					.map(e -> (Symbol) e)
					.retain(
						s -> {
							final char c = s.charValue();
							return c == sub || (c < 0x100 && substitutions[c] != null);
						}
					)
					.toArray(Symbol[]::new);

				for(Symbol s: embedded){
					if(s.charValue() == sub){
						throw new IllegalArgumentException(
							String.format(
								"Recursively defined substitution in expression \"%s\" for character '%s'",
								expression,
								s
							)
						);
					}
					else loopCheck.add(substitutions[s.charValue()]);
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
		alphabet.addAll(Arrays.asList(Symbol.from(classes[sub])));
	}

	private Map<Class<? extends Token>, Function<Token, Automaton>> handlers;

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
			new LiteralHandler()
		);
		handlers.put(
			FunctionExpression.class,
			new FunctionHandler(this)
		);

		handlers.put(
			TokenSeries.class,
			new SeriesHandler(this)
		);

		handlers.put(
			WhitespaceExpression.class,
			expression -> null
		);

		handlers.put(
			Symbol.class,
			new SymbolHandler(this)
		);
	}

	public Token parse(String pattern){
		final var expression = parser.parse(Symbol.from(pattern)).get(0);
		final var rebuilt = expression.value();
		if(!rebuilt.equals(pattern)){
			throw new IllegalStateException(rebuilt + " != " + pattern);
		}
		return expression;
	}

	public Automaton build(String pattern){
		return build(parse(pattern));
	}

	public Automaton build(Token expression){
		final var type = expression.getClass();
		final var handler = handlers.get(type);
		if(handler == null){
			throw new UnsupportedOperationException("No handler provided for expression class " + type.getName());
		}
		return handler.apply((expression instanceof TokenTree) ? (((TokenTree)expression).trim()) : expression);
	}

	public Set<Token> alphabet(){
		return Collections.unmodifiableSet(alphabet);
	}

	public char[] chars(char symbol){
		return classes[symbol];
	}

	public Token substitutions(char c){
		return substitutions[c];
	}
}
