package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.*;
import localgoat.util.CollectionUtils;
import localgoat.util.functional.operation.AssociativeOperation;

import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class Concatenate<T extends Token> implements AssociativeOperation<Automaton<T>>{
	@Override
	public NFA<T> apply(Automaton<T> a0, Automaton<T> a1){
		final var builder = new Builder<T>(
			CollectionUtils.union(
				a0.tokens(),
				a1.tokens()
			)
		);

		builder.copy(a0, state -> state.drop());
		builder.copy(a1, state -> state.drop());

		final int nodeCount0 = a0.nodeCount();
		final var builder10 = builder.nodeBuilder(nodeCount0);

		IntStream.range(0, nodeCount0)
			.filter(i -> a0.node(i).isTerminating())
			.mapToObj(i -> builder.nodeBuilder(i))
			.forEach(
				nbuilder -> nbuilder.addTransition(null, builder10)
			);

		IntStream.range(0, a1.nodeCount())
			.filter(i -> a1.node(i).isTerminating())
			.mapToObj(i -> builder.nodeBuilder(i + nodeCount0))
			.forEach(
				nbuilder -> nbuilder.addState(TypeState.TERMINATING)
			);
		
		return builder.buildNFA();
	}
}
