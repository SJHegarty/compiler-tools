package localgoat.lang.struct.handlers;

import localgoat.lang.struct.Token;
import localgoat.lang.struct.TokenType;
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
