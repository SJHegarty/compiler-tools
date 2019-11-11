package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.Automaton;
import localgoat.lang.compiler.automata.DFA;
import localgoat.lang.compiler.automata.NFA;
import localgoat.lang.compiler.automata.Token;
import localgoat.lang.compiler.automata.operation.Concatenate;
import localgoat.lang.compiler.automata.operation.Kleene;
import localgoat.lang.compiler.automata.operation.Name;
import localgoat.lang.compiler.automata.operation.Or;
import localgoat.util.functional.CharPredicate;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Converter{
	private final char[][] classes = new char[256][];

	public void addClass(char sub, CharPredicate members){
		if(('a' <= sub && sub <= 'z') || ('A' <= sub && sub <= 'Z')){
			if(classes[sub] != null){
				throw new IllegalStateException(String.format("Unavailable class substitution character: '%s'.", sub));
			}
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
		}
		else{
			throw new IllegalArgumentException(String.format("Unsupported class substitution character: '%s'.", sub));
		}
	}

	private Map<Class<? extends Expression>, Function<Expression, Automaton<Token<Character>>>> handlers;

	public static void main(String...args){
		//*<0...>+(a, b)
		final String identifier = "@<identifier>(*<1+>l*(h*<1+>l))";
		final String className = "@<class-name>(*<1+>(U*l))";
		final String pattern = String.format("+(%s, %s)", identifier, className);
		final var converter = new Converter();
		final var series = converter.build(pattern);
		System.err.print(series);

	}

	public Converter(){
		this.handlers = new HashMap<>();
		handlers.put(
			FunctionExpression.class,
			expression -> {
				final var function = (FunctionExpression)expression;
				final char identifier = function.identifier();
				switch(identifier){
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
						final var naming = new Name<Token<Character>>(name);
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
				final var children = series.segments().stream()
					.map(seg -> build(seg))
					.collect(Collectors.toList());

				final var concat = new Concatenate<Token<Character>>();
				return concat.apply(children);
			}
		);

		handlers.put(
			Symbol.class,
			expression -> {
				final var symbol = (Symbol)expression;
				final char c = symbol.value();
				if(c == '^'){
					return new DFA<Token<Character>>();
				}
				if((c & 0xffffff00) == 0){
					final char[] chars = classes[c];
					if(chars == null){
						System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
						return new DFA<Token<Character>>(Token.of(c));
					}
					else{
						return new DFA<Token<Character>>(Token.from(chars));
					}

				}
				throw new IllegalStateException();
			}
		);
	}

	public DFA<Token<Character>> buildDFA(String pattern){
		final var a = build(pattern);
		return (a instanceof DFA) ? (DFA)a : new DFA<Token<Character>>((NFA)a);
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
