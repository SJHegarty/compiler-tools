package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.State;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.automata.structure.TypeState;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.util.functional.operation.UnaryOperation;

public class Name implements UnaryOperation<Automaton>{
	private final Type name;

	public Name(Type name){
		this.name = name;
	}

	@Override
	public Automaton apply(Automaton automaton){
		final var builder = new Builder(automaton.tokens());
		final TypeState terminating = new TypeState(name, State.TERMINATING);

		builder.copy(automaton, state -> state.drop());

		automaton.nodes().stream()
			.filter(n -> n.isTerminating())
			.mapToInt(n -> n.index())
			.mapToObj(i -> builder.nodeBuilder(i))
			.forEach(nbuilder -> nbuilder.addState(terminating));

		builder.nodeBuilder(0).addState(new TypeState(name, State.INITIALISING));
		final var type = automaton.getClass();
		return builder.build(automaton.getClass());
	}
}
