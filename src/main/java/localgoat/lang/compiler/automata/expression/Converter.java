package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.Automaton;
import localgoat.lang.compiler.automata.Token;
import localgoat.lang.compiler.automata.operation.Concatenate;
import localgoat.lang.compiler.automata.operation.Kleene;
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
						System.err.println("Currently unsupported feature - string class extraction.");
						final var children = function.children();
						if(children.size() != 1){
							throw new IllegalStateException("@ function only supports a single argument.");
						}
						return build(children.get(0));
					}
					case '*':{
						if(function.modifiers() != null){
							throw new UnsupportedOperationException("Modifiers not yet implemented (" + function.modifiers() + ").");
						}
						final var children = function.children();
						if(children.size() != 1){
							throw new IllegalStateException("Kleene functions only support a single argument.");
						}
						final var kleen = new Kleene<Token<Character>>(Kleene.Op.STAR);
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
