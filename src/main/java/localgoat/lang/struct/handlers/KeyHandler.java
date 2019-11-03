package localgoat.lang.struct.handlers;

import localgoat.lang.struct.Token;
import localgoat.lang.struct.TokenType;
import localgoat.util.io.CharSource;

public class KeyHandler implements Handler{

	static final char KEY_HEAD = '$';
	static final String KEY_HEAD_STRING = KEY_HEAD + "";
	static final Token HANGING_KEY_HEAD = new Token(KEY_HEAD_STRING, TokenType.HANGING);

	@Override
	public boolean handles(char head){
		return head == KEY_HEAD;
	}

	@Override
	public Token extract(CharSource source){
		if(source.read() != KEY_HEAD){
			exceptInvalid();
		}
		final var c = source.peek();
		if(c != CharSource.STREAM_END){
			if(LOWER.test(c)){
				return new Token(
					KEY_HEAD_STRING + IdentifierHandler.INSTANCE.extract(source),
					TokenType.KEYWORD
				);
			}
			if(SymbolHandler.SYMBOLIC.test(c)){
				return new Token(
					KEY_HEAD_STRING  + SymbolHandler.INSTANCE.extract(source),
					TokenType.KEYSYMBOL
				);
			}
		}
		return HANGING_KEY_HEAD;
	}
}
