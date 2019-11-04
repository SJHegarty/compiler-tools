package localgoat.lang.compiler.handlers;

import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.TokenType;
import localgoat.util.io.CharSource;

public class TypeHandler implements Handler{

	public static final Handler INSTANCE = new TypeHandler();

	private TypeHandler(){

	}

	@Override
	public boolean handles(char head){
		return UPPER.test(head);
	}

	@Override
	public Token extract(CharSource source){
		return this.extractCompound(
			UPPER,
			c -> LOWER.test(c) || NUMERIC.test(c),
			UPPER,
			source,
			TokenType.TYPE
		);
	}
}
