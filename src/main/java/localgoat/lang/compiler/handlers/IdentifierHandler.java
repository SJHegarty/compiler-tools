package localgoat.lang.compiler.handlers;

import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.TokenType;
import localgoat.util.io.CharSource;

public class IdentifierHandler implements Handler{
	public static final Handler INSTANCE = new IdentifierHandler();

	private IdentifierHandler(){

	}

	@Override
	public boolean handles(char head){
		return LOWER.test(head);
	}

	@Override
	public Token extract(CharSource source){
		return extractCompound(
			LOWER,
			c -> LOWER.test(c) || NUMERICAL.test(c),
			c -> c == '-',
			source,
			TokenType.IDENTIFIER
		);
	}
}
