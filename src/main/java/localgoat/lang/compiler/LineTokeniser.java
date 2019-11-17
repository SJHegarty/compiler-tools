package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.AutomatonUtils;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.TokenString;

import java.util.*;

public class LineTokeniser{




	public static final int TAB_WIDTH = 4;

	private final Automaton automaton;
	private final AutomatonUtils utils;
	public LineTokeniser(Automaton automaton){
		this.automaton = automaton;
		this.utils = new AutomatonUtils(automaton);
	}

	public CodeLine tokenise(String line, int index){
		return new CodeLine(
			index,
			Arrays.asList(
				utils.tokenise(Symbol.from(line))
					.toStream()
					.toArray(TokenString[]::new)
			)
		);
	}

}
