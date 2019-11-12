//package localgoat.lang.compiler.automata.operation;
//
//import localgoat.lang.compiler.automata.Automaton;
//import localgoat.lang.compiler.automata.DFA;
//import localgoat.lang.compiler.automata.NFA;
//import localgoat.lang.compiler.automata.TokenA;
//import localgoat.util.functional.operation.PolyOperation;
//
//import java.util.List;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//public class And<T extends TokenA> implements PolyOperation<Automaton<T>>{
//
//	private final Not<T> not = new Not<>();
//	private final Or<T> or = new Or<>();
//	private final Function<Automaton<T>, DFA<T>> negator = a -> {
//		DFA<T> dfa;
//		if(a instanceof NFA){
//			dfa = new DFA<T>((NFA)a);
//		}
//		else if(a instanceof DFA){
//			dfa = (DFA)a;
//		}
//		else{
//			throw new IllegalStateException();
//		}
//		return not.apply(dfa);
//	};
//
//	@Override
//	public Automaton<T> apply(List<Automaton<T>> values){
//		return negator.apply(
//			or.apply(
//				values.stream()
//					.map(negator)
//					.collect(Collectors.toList())
//			)
//		);
//	}
//}
