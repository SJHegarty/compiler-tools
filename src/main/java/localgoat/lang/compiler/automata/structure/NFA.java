package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.lang.compiler.automata.data.Token;

import java.util.*;

public class NFA<T extends Token> extends AbstractAutomaton<T>{

	public NFA(Builder<T> builder){
		super(builder);
	}

}