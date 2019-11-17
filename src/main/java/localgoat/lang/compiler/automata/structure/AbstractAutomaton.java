package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.automata.utility.Builder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractAutomaton implements Automaton{
	protected final MutableNode[] nodes;
	protected final Set<Token> tokens;

	public AbstractAutomaton(Builder builder){
		this.tokens = new HashSet<>(builder.tokens());
		this.nodes = builder.nodes().stream()
			.map(nbuilder -> nbuilder.initialise(this))
			.toArray(MutableNode[]::new);

		builder.nodes().stream()
			.forEach(nbuilder -> nbuilder.finalise());

	}

	@Override
	public final Node node(int index){
		return nodes[index];
	}

	@Override
	public final int nodeCount(){
		return nodes.length;
	}

	@Override
	public final Set<Token> tokens(){
		return Collections.unmodifiableSet(tokens);
	}
}
