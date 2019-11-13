package localgoat.lang.compiler.automata;

import localgoat.util.ESupplier;

import java.util.*;
import java.util.stream.Collectors;

public interface Automaton<T extends Token>{



	enum UnaryOperation{
		NOT,
		KLEENE_STAR,
		KLEENE_PLUS
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

	default Set<StringClass> getStringClasses(){
		return nodes().stream()
			.flatMap(n -> n.classes().stream())
			.collect(
				Collectors.toCollection(
					() -> new TreeSet<>(Comparator.comparing(c -> c.name()))
				)
			);
	}

	boolean isDeterministic();

}
