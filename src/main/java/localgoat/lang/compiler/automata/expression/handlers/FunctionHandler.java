package localgoat.lang.compiler.automata.expression.handlers;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.AutomatonUtils;
import localgoat.lang.compiler.token.IgnoredToken;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.automata.expression.*;
import localgoat.lang.compiler.automata.operation.Kleene;
import localgoat.lang.compiler.automata.operation.Naming;
import localgoat.lang.compiler.automata.operation.Not;
import localgoat.lang.compiler.automata.operation.Or;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.TokenLayer;
import localgoat.util.ESupplier;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunctionHandler implements Function<Token, Automaton>{

	private static final AutomatonUtils NAME_PARSER;
	static{
		final var converter = new Converter();
		converter.addClass('a', c -> 'a' <= c && c <= 'z');
		converter.addSubstitution('S', "*<1+>a*('-'*<1+>a)");
		converter.addSubstitution('N', "S");
		converter.addSubstitution('P', "'--'S");
		NAME_PARSER = new AutomatonUtils(converter.build("+(N,P,' ')"));
	}

	private Type classFor(String name){
		final var tokens = NAME_PARSER.tokenise(Symbol.from(name))
			.map(token -> token.value())
			.retain(s -> s.indexOf(' ') == -1)
			.toArray(String[]::new);

		if(tokens.length == 0 || tokens[0].startsWith("--")){
			throw new IllegalArgumentException(name);
		}
		final Set<String> flags = new HashSet<>();

		for(int i = 1; i < tokens.length; i++){
			if(tokens[i].startsWith("--")){
				flags.add(tokens[i].substring(2));
			}
		}
		return new Type(tokens[0], converter.layerFor(tokens[0]), flags);
	}

	private final Converter converter;
	private final Map<Character, Function<FunctionExpression, Automaton>> subhandlers = new TreeMap<>();
	public FunctionHandler(final Converter converter){
		this.converter = converter;

		subhandlers.put(
			'?',
			function -> {
				return new Or().apply(
					ESupplier.concat(
						ESupplier.of(Automaton.lambda()),
						ESupplier.from(function.children())
							.map(token -> converter.build(token))
					)
				);
			}
		);

		subhandlers.put(
			'!',
			function -> {
				final var children = function.children();
				if(children.size() != 1){
					throw new IllegalStateException("! function takes a single parameter.");
				}
				final var not = new Not(converter.alphabet());
				return not.apply(converter.build(children.get(0)));
			}
		);

		subhandlers.put(
			'~',
			function -> {
				final var alphabet = converter.alphabet();
				final var children = function.children().stream().filter(c -> !(c instanceof IgnoredToken)).collect(Collectors.toList());
				final var accepted = new HashSet<Token>(alphabet);
				for(var c: children){
					handled:
					{
						if(c instanceof Symbol){
							final char symbol = ((Symbol) c).charValue();
							final var chars = converter.chars(symbol);
							if(chars == null){
								System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
								accepted.remove(new Symbol(symbol));
							}
							else{
								accepted.removeAll(
									Arrays.asList(
										Symbol.from(chars)
									)
								);
							}
						}
						else if(c instanceof LiteralExpression && c.value().length() == 1){
							final char symbol = c.value().charAt(0);
							accepted.remove(new Symbol(symbol));
						}
						else{
							throw new UnsupportedOperationException(
								String.format("All children of ~ operation must be a single symbol (%s is not).", c)
							);
						}
					}
				}
				return Automaton.of(accepted.stream().toArray(Token[]::new));
			}
		);

		subhandlers.put(
			'+',
			function -> {
				final var children = function.children().stream()
					.filter(t -> !(t instanceof IgnoredToken))
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
				final var naming = new Naming(classFor(name));
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
	public Automaton apply(Token expression){
		final var function = (FunctionExpression)expression;
		final char identifier = function.identifier();
		final var handler = subhandlers.get(identifier);
		if(handler == null){
			throw new UnsupportedOperationException("Unknown function identifier: " + identifier);
		}
		return handler.apply(function);
	}




}
