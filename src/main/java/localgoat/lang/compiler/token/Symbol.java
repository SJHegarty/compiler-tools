package localgoat.lang.compiler.token;

import localgoat.lang.compiler.token.Token;

import java.util.stream.IntStream;

public class Symbol implements Token{

	private final char c;

	public Symbol(char c){
		this.c = c;
	}

	public static Token[] from(char...chars){
		return IntStream.range(0, chars.length)
			.mapToObj(i -> new Symbol(chars[i]))
			.toArray(Token[]::new);
	}

	public static Token[] from(String s){
		return IntStream.range(0, s.length())
			.mapToObj(i -> new Symbol(s.charAt(i)))
			.toArray(Token[]::new);
	}

	@Override
	public int length(){
		return 1;
	}

	@Override
	public String toString(){
		return Character.toString(c);
	}

	public String value(){
		return Character.toString(c);
	}

	public char charValue(){
		return c;
	}

	public boolean equals(Object o){
		if(o.getClass() == getClass()){
			return c == ((Symbol)o).c;
		}
		return false;
	}

	public int hashCode(){
		return c;
	}
}
