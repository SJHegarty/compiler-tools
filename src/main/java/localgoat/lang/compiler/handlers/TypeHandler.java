package localgoat.lang.compiler.handlers;

import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.TokenType;
import localgoat.util.functional.CharPredicate;
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
		final var builder = new StringBuilder();
		final CharPredicate baseChars = c -> LOWER.test(c) || NUMERIC.test(c);
		for(;;){
			final char segHead[] = source.peek(2);
			if(segHead.length == 2 && UPPER.test(segHead[0]) && baseChars.test(segHead[1])){
				source.skip(2);
				builder.append(segHead);
				//TODO - Read a bunch of chars, run through counting the matches, and reset with a delta of the match count.
				for(;;){
					char c = source.peek();
					if(baseChars.test(c)){
						source.skip(1);
						builder.append(c);
					}
					else{
						break;
					}
				}
			}
			else{
				break;
			}

		}
		if(builder.length() == 0){
			if(UPPER.test(source.peek())){
				return new Token(new String(source.read(1)), TokenType.HANGING);
			}
			exceptInvalid();
		}
		return new Token(builder.toString(), TokenType.TYPE);
	}
}
