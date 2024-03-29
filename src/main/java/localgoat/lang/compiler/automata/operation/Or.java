package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.util.functional.operation.PolyOperation;

import java.util.List;
import java.util.stream.Collectors;

public class Or implements PolyOperation<Automaton>{

	@Override
	public Automaton apply(List<Automaton> automata){

		final var tokens = automata.stream()
			.flatMap(a -> a.tokens().stream())
			.collect(Collectors.toSet());

		final var builder = new Builder(tokens);
		final var node0 = builder.addNode(false);

		for(var a: automata){
			final int index = builder.nodeCount();
			builder.copy(a, s -> s);
			node0.addTransition(null, builder.nodeBuilder(index));
		}

		return new Automaton(builder);
	}
}
