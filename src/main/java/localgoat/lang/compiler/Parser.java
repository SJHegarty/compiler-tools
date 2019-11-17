package localgoat.lang.compiler;

import localgoat.lang.compiler.token.Token;

public interface Parser{
	public Token parse(String value);
}
