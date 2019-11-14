package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.MutableNode;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.util.CollectionUtils;
import localgoat.util.ESupplier;

import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

public class Complete<T extends Token> implements UnaryOperator<DFA<T>>{
	private final Set<T> alphabet;

	public Complete(Set<T> alphabet){
		this.alphabet = alphabet;
	}

	@Override
	public DFA<T> apply(DFA<T> dfa){
		final var tokens = CollectionUtils.union(alphabet, dfa.tokens());

		if(dfa.isComplete(tokens)){
			return dfa;
		}

		final var builder = new Builder<T>(tokens);
		builder.copy(dfa, s -> s);
		final var sink = builder.addNode();

		for(var node: builder.nodes()){
			node.addTransitions(
				CollectionUtils.exclusion(tokens, node.tokens()),
				sink
			);
		}

		return builder.buildDFA();
	}
}
