package localgoat.lang.compiler.automata;

import localgoat.util.CollectionUtils;

import java.util.*;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NFA<T extends Token> implements Automaton<T>{

	public static class Builder<T extends Token>{
		private final Set<T> tokens;
		private final List<MutableNode.Builder<T>> nodes = new ArrayList<>();

		public Builder(Set<T> tokens){
			this.tokens = tokens;
		}

		public MutableNode.Builder<T> addNode(boolean terminating){
			final var rv = new MutableNode.Builder<T>(nodes.size(), terminating);
			nodes.add(rv);
			return rv;
		}

		public MutableNode.Builder<T> nodeBuilder(int index){
			return nodes.get(index);
		}

		public NFA<T> build(){
			return new NFA<T>(this);
		}
	}

	private final MutableNode<T>[] nodes;
	private final Set<T> tokens;

	private NFA(Builder<T> builder){
		this.tokens = new HashSet<>(builder.tokens);

		this.nodes = builder.nodes.stream()
			.map(nbuilder -> nbuilder.initialise(this))
			.toArray(MutableNode[]::new);

		for(var n: builder.nodes){
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
