package localgoat.lang.compiler.automata.expression;

import java.util.Arrays;

public class SpacesExpression implements Expression{
	private final int length;

	public SpacesExpression(String source, int index){
		int count = 0;
		while(source.charAt(index + count) == ' '){
			count++;
		}
		if(count == 0){
			throw new IllegalArgumentException();
		}
		this.length = count;

	}

	@Override
	public String toString(){
		final char[] chars = new char[length];
		Arrays.fill(chars, ' ');
		return new String(chars);
	}

	@Override
	public int length(){
		return length;
	}
}
