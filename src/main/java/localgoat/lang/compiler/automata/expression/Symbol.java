package localgoat.lang.compiler.automata.expression;

public class Symbol implements Expression{

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

	public char value(){
		return c;
	}
}
