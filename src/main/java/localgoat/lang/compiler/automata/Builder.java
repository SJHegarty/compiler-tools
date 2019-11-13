package localgoat.lang.compiler.automata;

import localgoat.util.ESupplier;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Builder<T extends Token>{
	final Set<T> tokens;
	final List<MutableNode.Builder<T>> nodes = new ArrayList<>();

	public Builder(Set<T> tokens){
		this.tokens = tokens;
	}

	public MutableNode.Builder<T> addNode(TypeState...classes){
		return addNode(new HashSet<>(Arrays.asList(classes)));
	}

	public MutableNode.Builder<T> addNode(Set<TypeState> classes){
		final var rv = new MutableNode.Builder<T>(nodes.size(), classes);
		nodes.add(rv);
		return rv;
	}

	public MutableNode.Builder<T> addNode(boolean terminating){
		final var rv = new MutableNode.Builder<T>(nodes.size(), terminating);
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
					.exclude(ts -> ts.type() == null && ts.depth() < 0)
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
