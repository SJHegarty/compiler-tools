package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.*;
import localgoat.util.functional.operation.UnaryOperation;

import java.util.Collections;

public class Name<T extends Token> implements UnaryOperation<Automaton<T>>{
	private final Type name;

	public Name(Type name){
		this.name = name;
	}

	@Override
	public Automaton<T> apply(Automaton<T> automaton){
		final var builder = new Builder<T>(automaton.tokens());
		final TypeState terminating = new TypeState(name, State.TERMINATING);

		builder.copy(automaton, state -> state.drop());

		automaton.nodes().stream()
			.filter(n -> n.isTerminating())
			.mapToInt(n -> n.index())
			.mapToObj(i -> builder.nodeBuilder(i))
			.forEach(nbuilder -> nbuilder.addState(terminating));

		final var type = automaton.getClass();
		return builder.build(automaton.getClass());
	}
}
