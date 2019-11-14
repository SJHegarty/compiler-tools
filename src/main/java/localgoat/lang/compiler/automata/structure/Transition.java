package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.automata.data.Token;

public class Transition<T extends Token>{
	private final Node<T> destination;

	Transition(Node<T> destination){
		this.destination = destination;
	}

	public Node<T> node(){
		return destination;
	}
}
