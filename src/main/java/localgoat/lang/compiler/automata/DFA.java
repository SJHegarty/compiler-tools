package localgoat.lang.compiler.automata;

import localgoat.util.ESupplier;

public class DFA<T extends Token> implements Automaton<T>{

	public DFA(NFA<T> nfa){
		for(var node: nfa.nodes()){
			ESupplier.of(node)
				.branchingMap(n -> ESupplier.from(n.transitions(null)))
				.map(n -> n.toString())
				.interlace(", ")
				.toStream().reduce((s0, s1) -> s0 + s1);
		}
	}

	@Override
	public int nodeCount(){
		throw new UnsupportedOperationException();
	}

	@Override
	public Node<T> node(int index){
		throw new UnsupportedOperationException();
	}

	@Override
	public TokenSet<T> tokens(){
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isDeterministic(){
		throw new UnsupportedOperationException();
	}
}
