package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.TypeState;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.token.Token;
import localgoat.util.functional.operation.UnaryOperation;

import java.util.Collections;
import java.util.Set;
import java.util.stream.IntStream;

public class Not implements UnaryOperation<Automaton>{
	private final Set<Token> baseAlphabet;

	public Not(Set<Token> alphabet){
		this.baseAlphabet = alphabet;
	}

	public Not(){
		this(Collections.emptySet());
	}

	@Override
	public DFA apply(Automaton source){
		final var complete = new Complete(baseAlphabet).apply(source);
		final var builder = new Builder(complete.tokens());
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
