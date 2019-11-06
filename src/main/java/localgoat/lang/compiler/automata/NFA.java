package localgoat.lang.compiler.automata;

import localgoat.util.CollectionUtils;

import java.util.Collections;
import java.util.Set;
import java.util.stream.IntStream;

public class NFA<T extends Token> implements Automaton<T>{

	public static <T extends Token> NFA<T> build(BinaryOperation op, Automaton<T>...automata){
		return build(op, automata, 0, automata.length);
	}

	private static <T extends Token> NFA<T> build(BinaryOperation op, Automaton<T>[] automata, int index, int length){
		switch(op){
			case AND: case OR: case CONCATENATE:{
				final Automaton<T> a0;
				final Automaton<T> a1;

				switch(length){
					case 0: case 1:{
						throw new IllegalArgumentException(
							String.format(
								"Cannot perform %s %s on fewer than two %s",
								op,
								BinaryOperation.class.getSimpleName(),
								Automaton.class.getSimpleName()
							)
						);
					}
					case 2:{
						a0 = automata[index];
						a1 = automata[index + 1];
						break;
					}
					default:{
						final int hlength0 = length >> 1;
						a0 = (hlength0 == 1) ? automata[index] : NFA.build(op, automata, index, hlength0);
						a1 = NFA.build(op, automata, index + hlength0, length - hlength0);
						break;
					}
				}

				return new NFA<>(op, a0, a1);
			}
			default:{
				throw new IllegalStateException();
			}
		}
	}

	private final MutableNode<T>[] nodes;
	private final Set<T> tokens;

	NFA(BinaryOperation op, Automaton<T> a0, Automaton<T> a1){
		switch(op){
			case OR:{
				tokens = CollectionUtils.union(a0.tokens(), a1.tokens());
				final var left = a0.nodes();
				final var right = a1.nodes();

				//noinspection unchecked
				nodes = new MutableNode[1 + left.size() + right.size()];
				nodes[0] = new MutableNode<>(this, 0, false);

				{
					final int loff = 1;

					IntStream.range(0, left.size())
						.forEach(
							lindex -> {
								final int index = loff + lindex;
								nodes[index] = new MutableNode<>(this, index, left.get(lindex).isTerminating());
							}
						);

					nodes[0].addTransition(null, nodes[loff]);
					for(var l : left){
						final var node = nodes[loff + l.index()];
						l.transitions().forEach(
							(token, ldests) -> {
								ldests.forEach(
									ldest -> node.addTransition(token, nodes[loff + ldest.index()])
								);
							}
						);
					}
				}
				{
					final int roff = 1 + left.size();

					IntStream.range(0, right.size())
						.forEach(
							rindex -> {
								final int index = roff + rindex;
								nodes[index] = new MutableNode<>(this, index, right.get(rindex).isTerminating());
							}
						);


					nodes[0].addTransition(null, nodes[roff]);
					for(var r : right){
						final var node = nodes[roff + r.index()];
						r.transitions().forEach(
							(token, rdests) -> {
								rdests.forEach(
									ldest -> node.addTransition(token, nodes[roff + ldest.index()])
								);
							}
						);
					}
				}
				break;
			}
			case CONCATENATE:{

			}
			default:{
				throw new UnsupportedOperationException();
			}
		}
	}

	@Override
	public Node node(int index){
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
