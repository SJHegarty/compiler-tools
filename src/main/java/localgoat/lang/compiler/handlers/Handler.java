package localgoat.lang.compiler.handlers;

import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.TokenType;
import localgoat.util.functional.CharPredicate;
import localgoat.util.io.CharSource;

public interface Handler{

	CharPredicate LOWER = c -> 'a' <= c && c <= 'z';
	CharPredicate UPPER = c -> 'A' <= c && c <= 'Z';
	CharPredicate NUMERIC = c -> '0' <= c && c <= '9';
	CharPredicate ALPHANUMERIC = c -> LOWER.test(c) || UPPER.test(c) || NUMERIC.test(c);

	boolean handles(char head);
	Token extract(CharSource source);


	default void exceptInvalid(){
		throw new IllegalStateException("Should never be called with an invalid first character.");
	}

	default Token extractCompound(
		CharPredicate headChar,
		CharPredicate baseChar,
		CharPredicate combiner,
		CharSource source,
		TokenType type
	){
		final var builder = new StringBuilder();
		{
			final char c = source.read();
			builder.append(c);
			if(!headChar.test(c)){
				exceptInvalid();
			}
		}
		loop:for(;;){
			final char[] chars = source.peek(2);
			switch(chars.length){
				case 0:{
					break loop;
				}
				case 2:{
					if(combiner.test(chars[0])){
						if(baseChar.test(chars[1])){
							builder.append(chars);
							source.read(2);
							continue loop;
						}
						break loop;
					}
				}
				case 1:{
					if(baseChar.test(chars[0])){
						builder.append(chars[0]);
						source.read();
						continue loop;
					}
					break loop;
				}
			}
		}
		return new Token(builder.toString(), type);
	};

}
