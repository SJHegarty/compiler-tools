package localgoat.lang.compiler.automata;

import java.util.*;
import java.util.stream.Collectors;

public class MutableNode<T extends Token> implements Node<T>{

	private final Automaton<T> automaton;
	private final int index;
	private final Map<T, Set<Node<T>>> transitions;
	private final boolean terminating;

	MutableNode(Automaton automaton, int index, boolean terminating){
		this.automaton = automaton;
		this.index = index;
		this.transitions = new HashMap<>();
		this.terminating = terminating;
	}

	void addTransitions(Set<T> tokens, Node<T> destination){
		for(var token: tokens){
			addTransition(token, destination);
		}
	}

	void addTransition(T token, Node<T> destination){
		if(destination.automaton() != automaton){
			throw new IllegalArgumentException("Destination Node does not belong to the same automaton as this Node.");
		}
		if(token == null && automaton.isDeterministic()){
			throw new IllegalArgumentException("Null transitions are only permitted in non-deterministic automata.");
		}
		if(token != null && !automaton.tokens().contains(token)){
			throw new IllegalArgumentException(
				String.format("Token %s is not in the set of legal transition tokens", token)
			);
		}
		Set<Node<T>> set = transitions.get(token);
		if(set == null){
			set = new HashSet<>();
			transitions.put(token, set);
		}
		else if(automaton.isDeterministic()){
			throw new IllegalArgumentException("Transitions from the same node to different destinations on the same token are only permitted in non-deterministic automata.");
		}
		terminable = CachedBoolean.UNCACHED;
		set.add(destination);
	}

	@Override
	public boolean isTerminating(){
		return terminating;
	}

	private CachedBoolean terminable = CachedBoolean.UNCACHED;

	@Override
	public boolean isTerminable(){
		if(terminable == CachedBoolean.UNCACHED){
			terminable = CachedBoolean.of(Node.super.isTerminable());
		}
		return terminable.asBoolean();
	}

	@Override
	public Set<T> tokens(){
		return Collections.unmodifiableSet(transitions.keySet());
	}

	@Override
	public Set<Node<T>> neighbours(){
		return Collections.unmodifiableSet(
			transitions.values().stream()
				.flatMap(nodes -> nodes.stream())
				.collect(Collectors.toSet())
		);
	}

	@Override
	public Set<Node<T>> transitions(T token){
		return Optional.ofNullable(transitions.get(token))
			.map(Collections::unmodifiableSet)
			.orElseGet(Collections::emptySet);
	}

	@Override
	public Map<T, Set<Node<T>>> transitions(){
		return Collections.unmodifiableMap(
			transitions.entrySet().stream()
			.collect(
				Collectors.toMap(
					e -> e.getKey(),
					e -> Collections.unmodifiableSet(e.getValue())
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
