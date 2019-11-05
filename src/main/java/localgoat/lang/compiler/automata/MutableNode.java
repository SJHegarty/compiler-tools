package localgoat.lang.compiler.automata;

import java.util.*;
import java.util.stream.Collectors;

public class MutableNode<T extends Token> implements Node<T>{

	private final Automaton<T> automaton;
	private final int index;
	private final Map<T, List<Node<T>>> transitions;

	MutableNode(Automaton automaton, int index){
		this.automaton = automaton;
		this.index = index;
		this.transitions = new HashMap<>();
	}

	void addTransition(T token, Node<T> destination){
		if(destination.automaton() != automaton){
			throw new IllegalArgumentException("Destination Node does not belong to the same automaton as this Node.");
		}
		if(token == null && automaton.isDeterministic()){
			throw new IllegalArgumentException("Null transitions are only permitted in non-deterministic automata.");
		}
		if(automaton.tokens().contains(token)){
			throw new IllegalArgumentException(
				String.format("Token %s is not in the set of legal transition tokens", token)
			);
		}
		List<Node<T>> list = transitions.get(token);
		if(list == null){
			list = new ArrayList<>();
		}
		else if(automaton.isDeterministic()){
			throw new IllegalArgumentException("Transitions from the same node to different destinations on the same token are only permitted in non-deterministic automata.");
		}
		list.add(destination);
	}

	@Override
	public List<Node<T>> transitions(T token){
		return Collections.unmodifiableList(transitions.get(token));
	}

	@Override
	public Map<T, List<Node<T>>> transitions(){
		return Collections.unmodifiableMap(
			transitions.entrySet().stream()
			.collect(
				Collectors.toMap(
					e -> e.getKey(),
					e -> Collections.unmodifiableList(e.getValue())
				)
			)
		);
	}

	@Override
	public Automaton<T> automaton(){
		return automaton;
	}

	@Override
	public int index(){
		return index;
	}
}
