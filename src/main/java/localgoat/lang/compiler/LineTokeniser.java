package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.data.TokenString;

import java.util.*;

public class LineTokeniser{


	public static final String WHITE_SPACE = "white-space";
	public static final String IGNORED = "ignored";

	public static final TokenString<Token<Character>> LINE_FEED = new TokenString<>(
		Collections.singleton(
			new Type(
				"line-feed",
				Collections.singleton(WHITE_SPACE)
			)
		),
		Collections.singletonList(
			Token.LINE_FEED
		)
	);

	public static final int TAB_WIDTH = 4;

	private final DFA<Token<Character>> dfa;

	public LineTokeniser(DFA<Token<Character>> dfa){
		this.dfa = dfa;
	}

	public CodeLine tokenise(String line, int index){
		return new CodeLine(
			index,
			Arrays.asList(
				dfa.tokenise(Token.from(line))
					.toStream()
					.toArray(TokenString[]::new)
			)
		);
	}

}
