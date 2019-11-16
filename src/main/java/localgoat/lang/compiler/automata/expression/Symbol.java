package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.data.Token;

public class Symbol implements Token{

	private final char c;

	public Symbol(char c){
		this.c = c;
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
}
