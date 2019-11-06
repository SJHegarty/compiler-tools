package localgoat.lang.compiler.automata;

import java.util.AbstractList;
import java.util.List;
import java.util.Set;

public interface Automaton<T extends Token>{

	enum BinaryOperation{
		OR,
		AND,
		CONCATENATE
	}

	enum UnaryOperation{
		NOT,
		KLEENE
	}

	int nodeCount();
	Node<T> node(int index);
	Set<T> tokens();

	default List<Node<T>> nodes(){
		return new AbstractList<>(){

			@Override
			public int size(){
				return nodeCount();
			}

			@Override
			public Node<T> get(int index){
				return node(index);
			}
		};
	}

	boolean isDeterministic();

}
