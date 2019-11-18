package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.token.Token;
import localgoat.util.ESupplier;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public interface Node{
	Automaton automaton();
	int index();

	Set<Transition> transitions(Token token);
	Map<Token, Set<Transition>> transitions();
	Set<Node> neighbours();
	Set<Token> tokens();
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

	default Transition transition(Token token){
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
			.branchBreadthFirst(true, node -> ESupplier.from(node.neighbours()))
			.retain(node -> node.isTerminating())
			.get();
	}

}
