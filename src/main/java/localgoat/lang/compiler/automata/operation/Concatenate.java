package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.*;
import localgoat.util.CollectionUtils;
import localgoat.util.functional.operation.AssociativeOperation;

import java.util.stream.IntStream;

public class Concatenate<T extends TokenA> implements AssociativeOperation<Automaton<T>>{
	@Override
	public NFA<T> apply(Automaton<T> a0, Automaton<T> a1){
		final var builder = new Builder<T>(
			CollectionUtils.union(
				a0.tokens(),
				a1.tokens()
			)
		);

		builder.copy(a0, n -> false);
		builder.copy(a1);

		final int nodeCount0 = a0.nodeCount();
		final var builder10 = builder.nodeBuilder(nodeCount0);

		IntStream.range(0, nodeCount0)
			.filter(i -> a0.node(i).isTerminating())
			.mapToObj(i -> builder.nodeBuilder(i))
			.forEach(nbuilder -> nbuilder.addTransition(null, builder10));

		return builder.buildNFA();
	}
}
