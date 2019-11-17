package localgoat.lang.compiler.automata.expression.handlers;

import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.automata.expression.LiteralExpression;
import localgoat.lang.compiler.automata.operation.Concatenate;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.DFA;

import java.util.function.Function;
import java.util.stream.Stream;

public class LiteralHandler implements Function<Token, Automaton>{
	@Override
	public Automaton apply(Token expression){
		final var literal = (LiteralExpression)expression;
		final var tokens = Symbol.from(literal.wrapped());
		final var machines = Stream.of(tokens)
			.map(t -> DFA.of(t))
			.toArray(DFA[]::new);

		return new Concatenate().apply(machines);
	}
}
