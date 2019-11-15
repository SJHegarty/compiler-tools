package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.TypeState;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.MutableNode;
import localgoat.lang.compiler.automata.data.Token;
import localgoat.util.CollectionUtils;
import localgoat.util.functional.operation.UnaryOperation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class Not<T extends Token> implements UnaryOperation<Automaton<T>>{
	private final Set<T> baseAlphabet;

	public Not(Set<T> alphabet){
		this.baseAlphabet = alphabet;
	}

	public Not(){
		this(Collections.emptySet());
	}

	@Override
	public DFA<T> apply(Automaton<T> source){
		final var complete = new Complete<>(baseAlphabet).apply(source);
		final var builder = new Builder<T>(complete.tokens());
		builder.copy(complete, s -> s.negate());
		IntStream.range(0, complete.nodeCount())
			.filter(
				i -> {
					final boolean srcNT = !complete.node(i).isTerminating();
					final boolean dstNT = !builder.nodeBuilder(i).isTerminating();
					return srcNT && dstNT;
				}
			)
			.mapToObj(i -> builder.nodeBuilder(i))
			.forEach(
				node -> node.addState(TypeState.TERMINATING)
			);

		return builder.buildDFA();
	}
}
