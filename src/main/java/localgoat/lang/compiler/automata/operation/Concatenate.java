package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.NFA;
import localgoat.lang.compiler.automata.structure.TypeState;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.util.CollectionUtils;
import localgoat.util.functional.operation.AssociativeOperation;

import java.util.stream.IntStream;

public class Concatenate implements AssociativeOperation<Automaton>{
	@Override
	public NFA apply(Automaton a0, Automaton a1){
		final var builder = new Builder(
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
