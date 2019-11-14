package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.MutableNode;
import localgoat.lang.compiler.automata.data.Token;
import localgoat.util.CollectionUtils;
import localgoat.util.functional.operation.UnaryOperation;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class Not<T extends Token> implements UnaryOperation<DFA<T>>{
	private final Set<T> baseAlphabet;

	public Not(Set<T> alphabet){
		this.baseAlphabet = alphabet;
	}

	@Override
	public DFA<T> apply(DFA<T> source){
		final var complete = source.isComplete(baseAlphabet) ? source : new DFA<T>(baseAlphabet, source);
		final var tokens = (baseAlphabet == null) ? new HashSet<>(source.tokens()) : CollectionUtils.union(source.tokens(), baseAlphabet);

		final var builder = new Builder<T>(tokens);

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
						.map(t -> t.node())
						.map(srcdest -> nbuilders[srcdest.index()])
						.forEach(dest -> nbuilder.addTransition(token, dest));
				}
			);
		}


		return builder.buildDFA();
	}
}
