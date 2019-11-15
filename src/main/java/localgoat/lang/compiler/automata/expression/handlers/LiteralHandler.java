package localgoat.lang.compiler.automata.expression.handlers;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.expression.Expression;
import localgoat.lang.compiler.automata.expression.LiteralExpression;
import localgoat.lang.compiler.automata.operation.Concatenate;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.DFA;

import java.util.function.Function;
import java.util.stream.Stream;

public class LiteralHandler implements Function<Expression, Automaton<Token<Character>>>{
	@Override
	public Automaton<Token<Character>> apply(Expression expression){
		final var literal = (LiteralExpression)expression;
		final var tokens = Token.from(literal.value());
		final var machines = Stream.of(tokens)
			.map(t -> DFA.of(t))
			.toArray(DFA[]::new);

		return new Concatenate<Token<Character>>().apply(machines);
	}
}
