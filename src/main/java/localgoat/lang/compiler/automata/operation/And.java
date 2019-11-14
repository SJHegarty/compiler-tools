//package localgoat.lang.compiler.automata.operation;
//
//import localgoat.lang.compiler.automata.structure.Automaton;
//import localgoat.lang.compiler.automata.structure.DFA;
//import localgoat.lang.compiler.automata.structure.NFA;
//import localgoat.lang.compiler.automata.data.Token;
//import localgoat.util.functional.operation.PolyOperation;
//
//import java.util.List;
//import java.util.Set;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//public class And<T extends Token> implements PolyOperation<Automaton<T>>{
//
//	@Override
//	public Automaton<T> apply(List<Automaton<T>> values){
//		final Set<T> alphabet = values.stream()
//			.flatMap(a -> a.tokens().stream())
//			.collect(Collectors.toSet());
//
//		final Not<T> not = new Not<>(alphabet);
//		final List<DFA<T>> negatable
//		return negator.apply(
//			or.apply(
//				values.stream()
//					.map(negator)
//					.collect(Collectors.toList())
//			)
//		);
//	}
//}
