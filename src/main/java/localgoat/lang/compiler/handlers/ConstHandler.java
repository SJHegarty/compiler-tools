package localgoat.lang.compiler.handlers;

import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.TokenType;
import localgoat.util.functional.CharPredicate;
import localgoat.util.io.CharSource;

public class ConstHandler implements Handler{

	public static final ConstHandler INSTANCE = new ConstHandler();

	private ConstHandler(){

	}

	@Override
	public boolean handles(char head){
		return head == '@';
	}

	@Override
	public Token extract(CharSource source){
		if(source.read() != '@'){
			exceptInvalid();
		}
		final var builder = new StringBuilder();
		if(!UPPER.test(source.peek())){
			return new Token("@", TokenType.HANGING);
		}
		final CharPredicate baseChar = c -> UPPER.test(c) || NUMERIC.test(c);
		loop: for(;;){
			char[] chars = source.peek(2);
			switch(chars.length){
				case 0:{
					break loop;
				}
				case 2:{
					if(chars[0] == '_'){
						if(baseChar.test(chars[1])){
							source.skip(2);
							builder.append(chars);
							continue loop;
						}
						break loop;
					}
				}
				case 1:{
					if(baseChar.test(chars[0])){
						source.skip(1);
						builder.append(chars[0]);
						continue loop;
					}
					break loop;
				}
			}
		}
		return new Token("@" + builder.toString(), TokenType.CONST);
	}
}
