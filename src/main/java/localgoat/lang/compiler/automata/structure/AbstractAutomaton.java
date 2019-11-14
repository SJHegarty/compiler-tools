package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.utility.Builder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAutomaton<T extends Token> implements Automaton<T>{
	protected final MutableNode<T>[] nodes;
	protected final Set<T> tokens;

	public AbstractAutomaton(Builder<T> builder){
		this.tokens = new HashSet<>(builder.tokens());
		this.nodes = builder.nodes().stream()
			.map(nbuilder -> nbuilder.initialise(this))
			.toArray(MutableNode[]::new);

		builder.nodes().stream()
			.forEach(nbuilder -> nbuilder.finalise());

	}

	@Override
	public final Node<T> node(int index){
		return nodes[index];
	}

	@Override
	public final int nodeCount(){
		return nodes.length;
	}

	@Override
	public final Set<T> tokens(){
		return Collections.unmodifiableSet(tokens);
	}
}
