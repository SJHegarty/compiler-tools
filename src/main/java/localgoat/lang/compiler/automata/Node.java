package localgoat.lang.compiler.automata;

import localgoat.util.ESupplier;

import java.util.Map;
import java.util.Set;

public interface Node<T extends Token>{
	Automaton<T> automaton();
	int index();
	Set<Node<T>> transitions(T token);
	Map<T, Set<Node<T>>> transitions();
	Set<Node<T>> neighbours();
	boolean isTerminating();

	default boolean isTerminable(){
		return null != ESupplier.of(this)
			.branchingMap(true, node -> ESupplier.from(node.neighbours()))
			.retain(node -> node.isTerminating())
			.get();
	}

}
