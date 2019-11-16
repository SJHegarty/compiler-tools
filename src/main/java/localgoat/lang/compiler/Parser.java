package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.data.Token;

public interface Parser{
	public Token parse(String value);
}
