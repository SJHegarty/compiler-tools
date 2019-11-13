package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.*;
import localgoat.util.functional.operation.UnaryOperation;

import java.util.Collections;

public class Name<T extends Token> implements UnaryOperation<Automaton<T>>{
	private final StringClass name;

	public Name(StringClass name){
		this.name = name;
	}

	@Override
	public Automaton<T> apply(Automaton<T> automaton){
		final var builder = new Builder(automaton.tokens());
		for(var node: automaton.nodes()){
			builder.addNode(node.isTerminating() ? Collections.singleton(name) : Collections.emptySet());
		}
		for(var node: automaton.nodes()){
			var nbuilder = builder.nodeBuilder(node.index());
			node.transitions().forEach(
				(token, transitions) -> {
					transitions.stream()
						.map(t -> t.node())
						.mapToInt(dest -> dest.index())
						.mapToObj(index -> builder.nodeBuilder(index))
						.forEach(
							dest -> {
								nbuilder.addTransition(token, dest);
							}
						);
				}
			);
		}
		final var type = automaton.getClass();
		if(type == DFA.class){
			return builder.buildDFA();
		}
		if(type == NFA.class){
			return builder.buildNFA();
		}
		throw new IllegalStateException("Unknown automata class " + type.getName());
	}
}
