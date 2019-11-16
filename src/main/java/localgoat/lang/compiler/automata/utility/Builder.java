package localgoat.lang.compiler.automata.utility;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.structure.*;
import localgoat.util.ESupplier;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Builder{
	final Set<Token> tokens;
	final List<NodeBuilder<Token>> nodes = new ArrayList<>();

	public Builder(Set<Token> tokens){
		this.tokens = tokens;
	}

	public NodeBuilder<Token> addNode(TypeState...classes){
		return addNode(new HashSet<>(Arrays.asList(classes)));
	}

	public NodeBuilder<Token> addNode(Set<TypeState> classes){
		final var rv = new NodeBuilder<Token>(nodes.size(), classes);
		nodes.add(rv);
		return rv;
	}

	public NodeBuilder<Token> addNode(boolean terminating){
		final var rv = new NodeBuilder<Token>(nodes.size(), terminating);
		nodes.add(rv);
		return rv;
	}

	public void copy(Automaton a, UnaryOperator<TypeState> stateOp){
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

	public NodeBuilder<Token> nodeBuilder(int index){
		return nodes.get(index);
	}

	public NFA buildNFA(){
		return new NFA(this);
	}

	public DFA buildDFA(){
		return new DFA(this);
	}

	//TODO: implement build based upon determinism check, rather than passed in type.
	public Automaton build(Class<? extends Automaton> type){
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

	public Set tokens(){
		return tokens;
	}

	public List<NodeBuilder> nodes(){
		return Collections.unmodifiableList(nodes);
	}
}
