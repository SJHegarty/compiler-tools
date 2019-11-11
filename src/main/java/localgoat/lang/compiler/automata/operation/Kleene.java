package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.*;
import localgoat.util.functional.operation.UnaryOperation;

import java.util.HashSet;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class Kleene<T extends TokenA> implements UnaryOperation<Automaton<T>>{

	public enum Op{
		STAR,
		PLUS
	}

	private final Op op;

	public Kleene(Op op){
		this.op = op;
	}

	@Override
	public NFA<T> apply(Automaton<T> a){

		final var tokens = new HashSet<>(a.tokens());
		final var abuilder = new Builder<T>(tokens);
		final IntFunction<MutableNode.Builder<T>> builder;
		final MutableNode.Builder<T> nbuilder0;

		switch(op){
			case PLUS:{
				nbuilder0 = abuilder.addNode(a.node(0).isTerminating());
				builder = i -> abuilder.addNode(a.node(i).isTerminating());
				break;
			}
			case STAR:{
				nbuilder0 = abuilder.addNode(true);
				builder = i -> abuilder.addNode(false);
				break;
			}
			default:{
				throw new IllegalStateException();
			}
		}
		for(int i = 1; i < a.nodeCount(); i++){
			builder.apply(i);
		}

		IntStream.range(0, a.nodeCount())
			.forEach(
				i -> {
					final var srcNode = a.node(i);
					final var node = abuilder.nodeBuilder(i);
					srcNode.transitions().forEach(
						(token, destinations) -> destinations.stream()
							.map(srcdest -> abuilder.nodeBuilder(srcdest.index()))
							.forEach(dest -> node.addTransition(token, dest))
					);
				}
			);

		IntStream.range(0, a.nodeCount())
			.filter(i -> a.node(i).isTerminating())
			.mapToObj(i -> abuilder.nodeBuilder(i))
			.forEach(node -> node.addTransition(null, nbuilder0));

		return abuilder.buildNFA();
	}

}
