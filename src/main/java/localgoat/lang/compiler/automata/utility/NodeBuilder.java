package localgoat.lang.compiler.automata.utility;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.automata.structure.MutableNode;
import localgoat.lang.compiler.automata.structure.TypeState;

import java.util.*;

public class NodeBuilder<T extends Token>{
	private final int index;
	private final Map<T, Set<NodeBuilder<T>>> transitions;
	private final Set<TypeState> typestates;
	private Automaton automaton;
	private MutableNode node;

	public NodeBuilder(int index, boolean terminating){
		this(index, terminating ? new TypeState[]{TypeState.TERMINATING} : new TypeState[0]);
	}

	public NodeBuilder(int index, TypeState...classes){
		this(index, new HashSet<>(Arrays.asList(classes)));
	}

	public NodeBuilder(int index, Set<TypeState> classes){
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


	public void addTransitions(Set<T> tokens, NodeBuilder<T> destination){
		tokens.forEach(t -> addTransition(t, destination));
	}

	public void addTransition(T token, NodeBuilder<T> destination){
		var set = transitions.get(token);
		if(set == null){
			set = new HashSet<>();
			transitions.put(token, set);
		}
		set.add(destination);
	}

	public MutableNode initialise(Automaton automaton){
		this.automaton = automaton;
		this.node = new MutableNode(automaton, index, typestates);
		return node;
	}

	public void finalise(){
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

	public Set<T> tokens(){
		return transitions.keySet();
	}

	public void addState(TypeState ts){
		typestates.add(ts);
	}

	public boolean isTerminating(){
		for(var ts: typestates){
			if(ts.isTerminating()){
				return true;
			}
		}
		return false;
	}
}
