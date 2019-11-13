package localgoat.lang.compiler.automata;

import java.util.*;
import java.util.stream.Collectors;

public class MutableNode<T extends Token> implements Node<T>{

	public static class Builder<T extends Token>{
		private final int index;
		private final Map<T, Set<Builder<T>>> transitions;
		private final Set<TypeState> typestates;
		private Automaton<T> automaton;
		private MutableNode<T> node;

		public Builder(int index, boolean terminating){
			this(index, terminating ? new TypeState[]{TypeState.TERMINATING} : new TypeState[0]);
		}

		public Builder(int index, TypeState...classes){
			this(index, new HashSet<>(Arrays.asList(classes)));
		}

		public Builder(int index, Set<TypeState> classes){
			this.index = index;
			this.typestates = new HashSet<>(classes);
			this.transitions = new HashMap<>();
			for(var v: classes){
				if(!(v instanceof TypeState)){
					throw new IllegalStateException();
				}
			}
		}

		public int index(){
			return index;
		}

		public void addTransition(T token, Builder<T> destination){
			var set = transitions.get(token);
			if(set == null){
				set = new HashSet<>();
				transitions.put(token, set);
			}
			set.add(destination);
		}

		MutableNode<T> initialise(Automaton<T> automaton){
			this.automaton = automaton;
			this.node = new MutableNode<T>(automaton, index, typestates);
			return node;
		}

		void finalise(){
			if(automaton == null){
				throw new IllegalStateException();
			}
			transitions.forEach(
				(token, builders) -> builders.forEach(
					builder -> node.addTransition(
						token,
						automaton.node(builder.index)
					)
				)
			);
		}

		public void addState(TypeState ts){
			typestates.add(ts);
		}
	}
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

	MutableNode(Automaton<T> automaton, int index, Set<TypeState> typestates){
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

	void addTransition(T token, Node<T> destination){
		addTransition(token, new Transition<>(destination));
	}
	void addTransition(T token, Transition<T> transition){
		final var destination = transition.node();
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
		Set<Transition<T>> set = transitions.get(token);
		if(set == null){
			set = new HashSet<>();
			transitions.put(token, set);
		}
		else if(automaton.isDeterministic()){
			throw new IllegalArgumentException("Transitions from the same node to different destinations on the same token are only permitted in non-deterministic automata.");
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
