package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.*;
import localgoat.util.CollectionUtils;
import localgoat.util.functional.operation.AssociativeOperation;

import java.util.stream.IntStream;

public class Or<T extends Token> implements AssociativeOperation<Automaton<T>>{

	@Override
	public NFA<T> apply(Automaton<T> a0, Automaton<T> a1){

		final var tokens = CollectionUtils.union(a0.tokens(), a1.tokens());
		final var left = a0.nodes();
		final var right = a1.nodes();
		final var builder = new Builder<T>(tokens);

		final var nbuilder0 = builder.addNode(false);

		{
			final int loff = 1;

			IntStream.range(0, left.size())
				.forEach(
					index -> {
						builder.addNode(left.get(index).isTerminating());
					}
				);

			nbuilder0.addTransition(null, builder.nodeBuilder(loff));
			for(var l : left){
				final var node = builder.nodeBuilder(loff + l.index());
				l.transitions().forEach(
					(token, ldests) -> {
						ldests.forEach(
							ldest -> node.addTransition(token, builder.nodeBuilder(loff + ldest.index()))
						);
					}
				);
			}
		}
		{
			final int roff = 1 + left.size();

			IntStream.range(0, right.size())
				.forEach(
					index -> {
						builder.addNode(right.get(index).isTerminating());
					}
				);


			nbuilder0.addTransition(null, builder.nodeBuilder(roff));
			for(var r : right){
				final var node = builder.nodeBuilder(roff + r.index());
				r.transitions().forEach(
					(token, rdests) -> {
						rdests.forEach(
							ldest -> node.addTransition(token, builder.nodeBuilder(roff + ldest.index()))
						);
					}
				);
			}
		}
		return builder.buildNFA();
	}
}
