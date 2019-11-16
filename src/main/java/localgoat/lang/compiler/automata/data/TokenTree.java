package localgoat.lang.compiler.automata.data;

import localgoat.lang.compiler.automata.data.Token;

import java.util.List;

public interface TokenTree extends Token{
	List<Token> children();
}
