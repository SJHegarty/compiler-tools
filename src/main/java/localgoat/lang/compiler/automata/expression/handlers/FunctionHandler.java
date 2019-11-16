package localgoat.lang.compiler.automata.expression.handlers;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.expression.Expression;
import localgoat.lang.compiler.automata.expression.FunctionExpression;
import localgoat.lang.compiler.automata.expression.LiteralExpression;
import localgoat.lang.compiler.automata.expression.Symbol;
import localgoat.lang.compiler.automata.operation.Kleene;
import localgoat.lang.compiler.automata.operation.Name;
import localgoat.lang.compiler.automata.operation.Not;
import localgoat.lang.compiler.automata.operation.Or;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.Type;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionHandler implements Function<Expression, Automaton>{

	private static final DFA NAME_PARSER;
	static{
		final var converter = new Converter();
		converter.addClass('a', c -> 'a' <= c && c <= 'z');
		converter.addSubstitution('S', "*<1+>a*('-'*<1+>a)");
		converter.addSubstitution('N', "S");
		converter.addSubstitution('P', "'--'S");
		NAME_PARSER = converter.buildDFA("+(N,P,' ')");
	}

	private static Type classFor(String name){
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

	private final Converter converter;
	private final Map<Character, Function<FunctionExpression, Automaton>> subhandlers = new TreeMap<>();
	public FunctionHandler(final Converter converter){
		this.converter = converter;

		subhandlers.put(
			'!',
			function -> {
				final var children = function.children();
				if(children.size() != 1){
					throw new IllegalStateException("! function takes a single parameter.");
				}
				final var not = new Not(converter.alphabet());
				return not.apply(converter.buildDFA(children.get(0)));
			}
		);

		subhandlers.put(
			'~',
			function -> {
				final var alphabet = converter.alphabet();
				final var children = function.children();
				final var accepted = new HashSet<Token>(alphabet);
				for(var c: function.children()){
					handled:
					{
						if(c instanceof Symbol){
							final char symbol = ((Symbol) c).value();
							final var chars = converter.chars(symbol);
							if(chars == null){
								System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
								accepted.remove(Token.of(symbol));
							}
							else{
								accepted.removeAll(
									Arrays.asList(
										Token.from(chars)
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
		);

		subhandlers.put(
			'+',
			function -> {
				final var children = function.children().stream()
					.map(expr -> converter.build(expr))
					.collect(Collectors.toList());
				final var or = new Or();
				return or.apply(children);
			}
		);

		subhandlers.put(
			'@',
			function -> {
				final var name = function.modifiers();
				if(name == null){
					throw new IllegalStateException("@ function requires defined modifiers.");
				}
				final var children = function.children();
				if(children.size() != 1){
					throw new IllegalStateException("@ function only supports a single argument.");
				}
				final var naming = new Name(classFor(name));
				return naming.apply(converter.build(children.get(0)));
			}
		);

		subhandlers.put(
			'*',
			function -> {
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
				final var kleen = new Kleene((minCount == 0) ? Kleene.Op.STAR : Kleene.Op.PLUS);
				return kleen.apply(converter.build(children.get(0)));
			}
		);
	}

	@Override
	public Automaton apply(Expression expression){
		final var function = (FunctionExpression)expression;
		final char identifier = function.identifier();
		final var handler = subhandlers.get(identifier);
		if(handler == null){
			throw new UnsupportedOperationException("Unknown function identifier: " + identifier);
		}
		return handler.apply(function);
	}




}
