package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.DFA;
import localgoat.lang.compiler.automata.MutableNode;
import localgoat.lang.compiler.automata.Token;
import localgoat.util.functional.operation.UnaryOperation;

import java.util.HashSet;
import java.util.stream.IntStream;

public class Not<T extends Token> implements UnaryOperation<DFA<T>>{
	@Override
	public DFA<T> apply(DFA<T> source){
		final var complete = source.complete();
		final var tokens = new HashSet<>(source.tokens());

		final var builder = new DFA.Builder<T>(tokens);

		//noinspection unchecked
		var nbuilders = IntStream.range(0, complete.nodeCount())
			.mapToObj(
				i -> builder.addNode(!complete.node(i).isTerminating())
			)
			.toArray(MutableNode.Builder[]::new);

		for(var nbuilder: nbuilders){
			var srcnode = complete.node(nbuilder.index());
			srcnode.tokens().forEach(
				token -> {
					final var transitions = srcnode.transitions(token);
					if(transitions.size() != 1){
						throw new IllegalStateException();
					}
					transitions.stream()
						.map(srcdest -> nbuilders[srcdest.index()])
						.forEach(dest -> nbuilder.addTransition(token, dest));
				}
			);
		}

		return builder.build();
	}
}