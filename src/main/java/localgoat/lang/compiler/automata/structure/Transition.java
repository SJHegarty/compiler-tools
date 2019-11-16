package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.automata.data.Token;

public class Transition{
	private final Node destination;

	Transition(Node destination){
		this.destination = destination;
	}

	public Node node(){
		return destination;
	}
}
