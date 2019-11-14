package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.lang.compiler.automata.data.Token;

import java.util.*;

public class NFA<T extends Token> implements Automaton<T>{

	private final MutableNode<T>[] nodes;
	private final Set<T> tokens;

	public NFA(Builder<T> builder){
		this.tokens = new HashSet<>(builder.tokens());

		this.nodes = builder.nodes().stream()
			.map(nbuilder -> nbuilder.initialise(this))
			.toArray(MutableNode[]::new);

		for(var n: builder.nodes()){
			n.finalise();
		}
	}

	@Override
	public Node<T> node(int index){
		return nodes[index];
	}

	@Override
	public int nodeCount(){
		return nodes.length;
	}

	@Override
	public Set<T> tokens(){
		return Collections.unmodifiableSet(tokens);
	}

	@Override
	public boolean isDeterministic(){
		return false;
	}

}
