package localgoat.lang.compiler.automata;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class Builder<T extends Token>{
	final Set<T> tokens;
	final List<MutableNode.Builder<T>> nodes = new ArrayList<>();

	public Builder(Set<T> tokens){
		this.tokens = tokens;
	}

	public MutableNode.Builder<T> addNode(boolean terminating){
		final var rv = new MutableNode.Builder<T>(nodes.size(), terminating);
		nodes.add(rv);
		return rv;
	}

	public void copy(Automaton<T> a){
		copy(a, node -> node.isTerminating());
	}

	public void copy(Automaton<T> a, Predicate<Node<T>> terminating){
		final int offset = nodes.size();
		final int nodecount = a.nodeCount();

		for(int i = 0; i < nodecount; i++){
			addNode(terminating.test(a.node(i)));
		}
		for(int i = 0; i < nodecount; i++){
			final var srcnode = a.node(i);
			final var builder = nodeBuilder(i);
			srcnode.transitions().forEach(
				(token, srcdests) -> srcdests.forEach(
					srcdest -> {
						builder.addTransition(
							token,
							nodeBuilder(offset + srcdest.index())
						);
					}
				)
			);
		}
	}
	public MutableNode.Builder<T> nodeBuilder(int index){
		return nodes.get(index);
	}

	public NFA<T> buildNFA(){
		return new NFA<T>(this);
	}

	public DFA<T> buildDFA(){
		return new DFA<T>(this);
	}
}
