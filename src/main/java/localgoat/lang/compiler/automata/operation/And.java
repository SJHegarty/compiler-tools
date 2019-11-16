package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.NFA;
import localgoat.lang.compiler.automata.data.Token;
import localgoat.util.functional.operation.PolyOperation;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class And implements PolyOperation<Automaton>{

	@Override
	public Automaton apply(List<Automaton> values){
		final Set<Token> alphabet = values.stream()
			.flatMap(a -> a.tokens().stream())
			.collect(Collectors.toSet());

		final Not not = new Not(alphabet);
		final Automaton or = new Or().apply(
			values.stream()
				.map(a -> not.apply(a))
				.collect(Collectors.toList())
		);
		return not.apply(or);
	}
}
