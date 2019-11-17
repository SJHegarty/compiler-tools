package localgoat.lang.compiler.automata.structure;

public class Transition{
	private final Node destination;

	Transition(Node destination){
		this.destination = destination;
	}

	public Node node(){
		return destination;
	}
}
