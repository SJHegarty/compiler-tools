package localgoat.lang.compiler.automata;

import localgoat.util.ESupplier;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public interface Node<T extends Token>{
	Automaton<T> automaton();
	int index();

	Set<Transition<T>> transitions(T token);
	Map<T, Set<Transition<T>>> transitions();
	Set<Node<T>> neighbours();
	Set<T> tokens();
	Set<TypeState> typeStates();

	default	Set<Type> types(){
		return typeStates().stream()
			.filter(ts -> ts.isTerminating())
			.map(ts -> ts.type())
			.collect(Collectors.toSet());
	}

	default boolean isTerminating(){
		return !types().isEmpty();
	}

	default Transition<T> transition(T token){
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
