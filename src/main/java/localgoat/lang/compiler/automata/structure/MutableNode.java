package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.utility.CachedBoolean;

import java.util.*;
import java.util.stream.Collectors;

public class MutableNode<T extends Token> implements Node<T>{

	private final Automaton<T> automaton;
	private final int index;
	private final Map<T, Set<Transition<T>>> transitions;
	private final Set<TypeState> typeStates;

	MutableNode(Automaton<T> automaton, int index, boolean terminating){
		this(automaton, index, terminating ? Collections.singleton(TypeState.TERMINATING) : Collections.emptySet());
	}

	MutableNode(Automaton<T> automaton, int index, TypeState...typestates){
		this(automaton, index, new HashSet<>(Arrays.asList(typestates)));
	}

	public MutableNode(Automaton<T> automaton, int index, Set<TypeState> typestates){
		this.automaton = automaton;
		this.index = index;
		this.transitions = new HashMap<>();
		this.typeStates = typestates;
	}

	void addTransitions(Set<T> tokens, Node<T> destination){
		for(var token: tokens){
			addTransition(token, destination);
		}
	}

	public void addTransition(T token, Node<T> destination){
		addTransition(token, new Transition<>(destination));
	}

	void addTransition(T token, Transition<T> transition){
		final var destination = transition.node();
		if(destination.automaton() != automaton){
			throw new IllegalArgumentException("Destination Node does not belong to the same automaton as this Node.");
		}
		if(token != null && !automaton.tokens().contains(token)){
			throw new IllegalArgumentException(
				String.format("Token %s is not in the set of legal transition tokens", token)
			);
		}
		Set<Transition<T>> set = transitions.get(token);
		if(set == null){
			set = new HashSet<>();
			transitions.put(token, set);
		}
		terminable = CachedBoolean.UNCACHED;
		set.add(transition);
	}

	@Override
	public Set<TypeState> typeStates(){
		return Collections.unmodifiableSet(typeStates);
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
				.map(t -> t.node())
				.collect(Collectors.toSet())
		);
	}

	@Override
	public Set<Transition<T>> transitions(T token){
		return Optional.ofNullable(transitions.get(token))
			.map(Collections::unmodifiableSet)
			.orElseGet(Collections::emptySet);
	}

	@Override
	public Map<T, Set<Transition<T>>> transitions(){
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
