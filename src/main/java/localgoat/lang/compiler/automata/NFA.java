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


	private NFA(Builder<T> builder){
		this.tokens = new HashSet<>(builder.tokens);

		this.nodes = builder.nodes.stream()
			.map(nbuilder -> nbuilder.initialise(this))
			.toArray(MutableNode[]::new);

		for(var n: builder.nodes){
			n.finalise();
		}

	}

	private final MutableNode<T>[] nodes;
	private final Set<T> tokens;


	NFA(Automaton<T> a, UnaryOperation op){
		if(op == UnaryOperation.KLEENE_PLUS || op == UnaryOperation.KLEENE_STAR){
			this.tokens = new HashSet<>(a.tokens());

			//noinspection unchecked
			this.nodes = new MutableNode[a.nodeCount()];
			final IntFunction<MutableNode<T>> builder;
			switch(op){
				case KLEENE_PLUS:{
					builder = i -> new MutableNode<>(this, i, a.node(i).isTerminating());
					this.nodes[0] = new MutableNode<>(this, 0, a.node(0).isTerminating());
					break;
				}
				case KLEENE_STAR:{
					builder = i -> new MutableNode<>(this, i, false);
					this.nodes[0] = new MutableNode<>(this, 0, true);
					break;
				}
				default:{
					throw new IllegalStateException();
				}
			}
			for(int i = 1; i < nodes.length; i++){
				this.nodes[i] = builder.apply(i);
			}


			IntStream.range(0, nodes.length)
				.filter(i -> a.node(i).isTerminating())
				.mapToObj(i -> nodes[i])
				.forEach(node -> node.addTransition(null, nodes[0]));

			IntStream.range(0, nodes.length)
				.forEach(
					i -> {
						final var srcNode = a.node(i);
						final var node = nodes[i];
						srcNode.transitions().forEach(
							(token, destinations) -> destinations.stream()
								.map(srcdest -> nodes[srcdest.index()])
								.forEach(dest -> node.addTransition(token, dest))
						);
					}
				);
		}
		else{
			throw new UnsupportedOperationException();
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
