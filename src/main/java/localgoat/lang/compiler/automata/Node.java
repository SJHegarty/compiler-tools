package localgoat.lang.compiler.automata;

import localgoat.util.ESupplier;

import java.util.Map;
import java.util.Set;

public interface Node<T extends Token>{
	Automaton<T> automaton();
	int index();

	Set<Node<T>> transitions(T token);
	Map<T, Set<Node<T>>> transitions();
	Set<Node<T>> neighbours();
	Set<T> tokens();
	boolean isTerminating();

	default Node<T> transition(T token){
		final var tokenTransitions = transitions(token);
		switch(tokenTransitions.size()){
			case 0: return null;
			case 1: return tokenTransitions.iterator().next();
			default:{
				throw new UnsupportedOperationException();
			}
		}
	}

	default boolean isTerminable(){
		return null != ESupplier.of(this)
			.branchingMap(true, node -> ESupplier.from(node.neighbours()))
			.retain(node -> node.isTerminating())
			.get();
	}

}
