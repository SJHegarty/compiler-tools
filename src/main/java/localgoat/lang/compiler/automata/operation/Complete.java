package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.NFA;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.util.CollectionUtils;

import java.util.Set;
import java.util.function.UnaryOperator;

public class Complete implements UnaryOperator<Automaton>{
	private final Set<Token> alphabet;

	public Complete(Set<Token> alphabet){
		this.alphabet = alphabet;
	}

	@Override
	public DFA apply(Automaton a){
		final DFA dfa;
		if(a instanceof DFA){
			dfa = (DFA)a;
		}
		else{
			dfa = new Convert().apply((NFA)a);
		}
		final var tokens = CollectionUtils.union(alphabet, dfa.tokens());

		if(dfa.isComplete(tokens)){
			return dfa;
		}

		final var builder = new Builder(tokens);
		builder.copy(dfa, s -> s);
		final var sink = builder.addNode();

		for(var node: builder.nodes()){
			node.addTransitions(
				CollectionUtils.exclusion(tokens, node.tokens()),
				sink
			);
		}

		return builder.buildDFA();
	}
}
