package localgoat.lang.struct.handlers;

import localgoat.lang.struct.Token;
import localgoat.lang.struct.TokenType;
import localgoat.util.io.CharSource;

public class StringHandler implements Handler{

	char STRING_HEAD = '\"';

	@Override
	public boolean handles(char head){
		return head == STRING_HEAD;
	}

	@Override
	public Token extract(CharSource source){
		if(source.read() != STRING_HEAD){
			exceptInvalid();
		}
		final var builder = new StringBuilder().append('\"');
		for(;;){
			char c = source.read();
			hanging:{
				switch(c){
					case CharSource.STREAM_END:{
						break;
					}
					case '\"':{
						return new Token(builder.append('\"').toString(), TokenType.STRING);
					}
					case '\\':{
						builder.append(c);
						final char escaped = source.read();
						if(escaped == CharSource.STREAM_END){
							break;
						}
						c = escaped;
					}
					default:{
						builder.append(c);
						break hanging;
					}
				}
				//TODO: make hanging a property of the Token, rather than a token type - this should be a hanging STRING token.
				return new Token(builder.toString(), TokenType.HANGING);
			}
		}
	}
}
