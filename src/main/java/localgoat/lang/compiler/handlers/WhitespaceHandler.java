package localgoat.lang.compiler.handlers;

import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.TokenType;
import localgoat.util.io.CharSource;

public class WhitespaceHandler implements Handler{

	public static final Handler INSTANCE = new WhitespaceHandler();

	private WhitespaceHandler(){

	}

	@Override
	public boolean handles(char head){
		return Character.isWhitespace(head);
	}

	@Override
	public Token extract(CharSource source){
		final var builder = new StringBuilder();
		for(;;){
			final char c = source.peek();
			if(c == CharSource.STREAM_END){
				break;
			}
			else if(Character.isWhitespace(c)){
				builder.append(c);
				source.read();
			}
			else break;
		}
		return new Token(builder.toString(), TokenType.WHITESPACE);
	}
}
