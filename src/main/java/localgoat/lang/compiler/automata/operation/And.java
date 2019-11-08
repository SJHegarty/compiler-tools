package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.Automaton;
import localgoat.lang.compiler.automata.DFA;
import localgoat.lang.compiler.automata.NFA;
import localgoat.lang.compiler.automata.Token;
import localgoat.util.functional.operation.AssociativeOperation;

import java.util.function.Function;

public class And<T extends Token> implements AssociativeOperation<Automaton<T>>{

	private final Not<T> not = new Not<>();
	private final Or<T> or = new Or<>();
	private final Function<Automaton<T>, DFA<T>> negator = a -> {
		DFA<T> dfa;
		if(a instanceof NFA){
			dfa = new DFA<T>((NFA)a);
		}
		else if(a instanceof DFA){
			dfa = (DFA)a;
		}
		else{
			throw new IllegalStateException();
		}
		return not.apply(dfa);
	};

	@Override
	public Automaton<T> apply(Automaton<T> a0, Automaton<T> a1){
		return negator.apply(
			or.apply(
				negator.apply(a0),
				negator.apply(a1)
			)
		);
	}
}
