package localgoat.lang.compiler.handlers;

import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.TokenType;
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
			if(c == '$'){
				source.read(1);
				final var peek = source.peek(2);
				switch(peek.length){
					case 2:{
						if(peek[0] == '-' && IdentifierHandler.INSTANCE.handles(peek[1])){
							source.read(1);
							return new Token(
								"$$-" + IdentifierHandler.INSTANCE.extract(source),
								TokenType.HANDLED_COMMENT
							);
						}
					}
					case 1:{
						if(peek[0] == '$'){
							var builder = new StringBuilder().append("$$");
							for(char cn = source.read(); cn != CharSource.STREAM_END; cn = source.read()){
								builder.append(cn);
							}
							return new Token(builder.toString(), TokenType.LINE_COMMENT);
						}
					}
				}
				return new Token("$$", TokenType.STRUCTURED_COMMENT);
			}
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
