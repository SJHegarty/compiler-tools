package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.NFA;
import localgoat.lang.compiler.automata.structure.TypeState;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.util.functional.operation.UnaryOperation;

public class Kleene<T extends Token> implements UnaryOperation<Automaton<T>>{

	public enum Op{
		STAR,
		PLUS
	}

	private final Op op;

	public Kleene(Op op){
		this.op = op;
	}

	@Override
	public NFA<T> apply(Automaton<T> a){
		final var builder = new Builder<T>(a.tokens());
		builder.copy(a, s -> s.drop());

		final var node0 = builder.nodeBuilder(0);
		a.nodes().stream()
			.filter(n -> n.isTerminating())
			.mapToInt(n -> n.index())
			.mapToObj(i -> builder.nodeBuilder(i))
			.forEach(node -> node.addTransition(null, node0));

		switch(op){
			case PLUS:{
				a.nodes().stream()
					.filter(n -> n.isTerminating())
					.mapToInt(n -> n.index())
					.mapToObj(i -> builder.nodeBuilder(i))
					.forEach(node -> node.addState(TypeState.TERMINATING));

				break;
			}
			case STAR:{
				node0.addState(TypeState.TERMINATING);
				break;
			}
			default:{
				throw new IllegalStateException();
			}
		}
		return builder.buildNFA();
	}

}
