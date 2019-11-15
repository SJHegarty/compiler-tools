package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.expression.handlers.FunctionHandler;
import localgoat.lang.compiler.automata.expression.handlers.LiteralHandler;
import localgoat.lang.compiler.automata.expression.handlers.SeriesHandler;
import localgoat.lang.compiler.automata.expression.handlers.SymbolHandler;
import localgoat.lang.compiler.automata.operation.*;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.NFA;
import localgoat.util.ESupplier;
import localgoat.util.functional.CharPredicate;

import java.util.*;
import java.util.function.Function;

public class Converter{
	private final char[][] classes = new char[256][];
	private final Expression[] substitutions = new Expression[256];
	private final Set<Token<Character>> alphabet = new HashSet<>();

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
			new LiteralHandler()
		);
		handlers.put(
			FunctionExpression.class,
			new FunctionHandler(this)
		);

		handlers.put(
			ExpressionSeries.class,
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

	public Automaton<Token<Character>> build(Expression expression){
		final var type = expression.getClass();
		final var handler = handlers.get(type);
		if(handler == null){
			throw new UnsupportedOperationException("No handler provided for expression class " + type.getName());
		}
		return handler.apply(expression);
	}

	public Set<Token<Character>> alphabet(){
		return Collections.unmodifiableSet(alphabet);
	}

	public char[] chars(char symbol){
		return classes[symbol];
	}

	public Expression substitutions(char c){
		return substitutions[c];
	}
}
