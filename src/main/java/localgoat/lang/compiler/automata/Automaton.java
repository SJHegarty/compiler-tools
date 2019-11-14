package localgoat.lang.compiler.automata;

import java.util.*;
import java.util.stream.Collectors;

public interface Automaton<T extends Token>{

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

	default Set<Type> types(){
		return nodes().stream()
			.flatMap(n -> n.types().stream())
			.collect(Collectors.toSet());
	}

	boolean isDeterministic();

}
