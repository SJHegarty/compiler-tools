package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.structure.Node;
import localgoat.lang.compiler.automata.structure.Type;

import java.util.*;
import java.util.stream.Collectors;

public interface Automaton{

	int nodeCount();
	Node node(int index);
	Set<Token> tokens();

	default List<Node> nodes(){
		return new AbstractList<>(){

			@Override
			public int size(){
				return nodeCount();
			}

			@Override
			public Node get(int index){
				return node(index);
			}
		};
	}

	default Set<Type> types(){
		return nodes().stream()
			.flatMap(n -> n.types().stream())
			.collect(
				Collectors.toCollection(
					() -> new TreeSet<>(Comparator.comparing(t -> String.valueOf(t)))
				)
			);
	}

	default boolean isDeterministic(){
		for(var n: nodes()){
			final var transitions = n.transitions();
			if(transitions.containsKey(null)){
				return false;
			}
			for(var e: transitions.entrySet()){
				if(e.getValue().size() != 1){
					return false;
				}
			}
		}
		return true;
	}

}
