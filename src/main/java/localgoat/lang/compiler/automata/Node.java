package localgoat.lang.compiler.automata;

import java.util.List;
import java.util.Map;

public interface Node<T extends Token>{
	Automaton<T> automaton();
	int index();
	List<Node<T>> transitions(T token);
	Map<T, List<Node<T>>> transitions();
}
