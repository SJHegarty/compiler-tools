package localgoat.lang.compiler.automata;

import localgoat.util.CollectionUtils;
import localgoat.util.ESupplier;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;

public class DFA<T extends Token> implements Automaton<T>{

	private final MutableNode<T>[] nodes;
	private final Set<T> tokens;
	private CachedBoolean complete = CachedBoolean.UNCACHED;

	public DFA(NFA<T> nfa){
		for(var node: nfa.nodes()){
			ESupplier.of(node)
				.branchingMap(true, n -> ESupplier.from(n.transitions(null)))
				.map(n -> n.toString())
				.interlace(", ")
				.toStream().reduce((s0, s1) -> s0 + s1);
		}
		throw new UnsupportedOperationException();
	}

	public DFA(T...tokens){
		this.tokens = new HashSet<>(Arrays.asList(tokens));
		//noinspection unchecked
		this.nodes = new MutableNode[]{
			new MutableNode<>(this, 0, false),
			new MutableNode<>(this, 0, true)
		};
		nodes[0].addTransitions(this.tokens, nodes[1]);
	}

	public DFA(DFA<T> source){
		this.tokens = new HashSet<>(source.tokens);
		//noinspection unchecked
		this.nodes = IntStream.range(0, source.nodes.length + (source.isComplete() ? 0 : 1))
			.mapToObj(i -> new MutableNode<>(this, i, source.nodes[i].isTerminating()))
			.toArray(MutableNode[]::new);

		for(int i = 0; i < source.nodes.length; i++){
			final var node = nodes[i];
			source.nodes[node.index()].transitions().forEach(
				(token, srcdests) -> {
					if(srcdests.size() != 1){
						throw new IllegalStateException();
					}
					srcdests.forEach(
						srcdest -> node.addTransition(token, nodes[srcdest.index()])
					);
				}
			);
		}

		if(!source.isComplete()){
			var sink = nodes[source.nodes.length];
			for(var node: nodes){
				node.addTransitions(
					CollectionUtils.exclusion(tokens, node.tokens()),
					sink
				);
			}
		}
		
		complete = CachedBoolean.TRUE;
	}

	public boolean isComplete(){
		if(complete == CachedBoolean.UNCACHED){
			this.complete = CachedBoolean.of(
				null == ESupplier.from(nodes)
					.exclude(
						node -> node.tokens().equals(tokens)
					)
					.get()
			);
		}
		return complete.asBoolean();
	}

	@Override
	public int nodeCount(){
		return nodes.length;
	}

	@Override
	public Node<T> node(int index){
		return nodes[index];
	}

	@Override
	public Set<T> tokens(){
		return Collections.unmodifiableSet(tokens);
	}

	@Override
	public boolean isDeterministic(){
		return true;
	}
}
