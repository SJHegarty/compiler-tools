package localgoat.lang.compiler.automata.utility;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.structure.*;
import localgoat.util.ESupplier;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Builder<T extends Token>{
	final Set<T> tokens;
	final List<NodeBuilder<T>> nodes = new ArrayList<>();

	public Builder(Set<T> tokens){
		this.tokens = tokens;
	}

	public NodeBuilder<T> addNode(TypeState...classes){
		return addNode(new HashSet<>(Arrays.asList(classes)));
	}

	public NodeBuilder<T> addNode(Set<TypeState> classes){
		final var rv = new NodeBuilder<T>(nodes.size(), classes);
		nodes.add(rv);
		return rv;
	}

	public NodeBuilder<T> addNode(boolean terminating){
		final var rv = new NodeBuilder<T>(nodes.size(), terminating);
		nodes.add(rv);
		return rv;
	}

	public void copy(Automaton<T> a, UnaryOperator<TypeState> stateOp){
		final int offset = nodes.size();
		final int nodecount = a.nodeCount();

		for(int i = 0; i < nodecount; i++){
			addNode(
				ESupplier.from(a.node(i).typeStates())
					.map(stateOp)
					.exclude(ts -> ts.type() == null && !ts.isTerminating())
					.toStream()
					.collect(Collectors.toSet())
			);
		}
		for(int i = 0; i < nodecount; i++){
			final var srcnode = a.node(i);
			final var builder = nodeBuilder(i + offset);
			srcnode.transitions().forEach(
				(token, srctransitions) -> srctransitions.stream()
					.map(t -> t.node())
					.forEach(
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

	public NodeBuilder<T> nodeBuilder(int index){
		return nodes.get(index);
	}

	public NFA<T> buildNFA(){
		return new NFA<T>(this);
	}

	public DFA<T> buildDFA(){
		return new DFA<T>(this);
	}

	//TODO: implement build based upon determinism check, rather than passed in type.
	public Automaton<T> build(Class<? extends Automaton> type){
		if(type == DFA.class){
			return buildDFA();
		}
		if(type == NFA.class){
			return buildNFA();
		}
		throw new UnsupportedOperationException();
	}

	public int nodeCount(){
		return nodes.size();
	}

	public Set<T> tokens(){
		return tokens;
	}

	public List<NodeBuilder<T>> nodes(){
		return Collections.unmodifiableList(nodes);
	}
}
