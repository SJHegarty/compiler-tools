package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.token.Token;

public class WhitespaceExpression implements Token{
	private final String value;

	public WhitespaceExpression(String source, int index){
		final var builder = new StringBuilder();
		while(true){
			final char c = source.charAt(index + builder.length());
			if("\n \t".indexOf(c) != -1){
				builder.append(c);
			}
			else{
				break;
			}
		}
		if(builder.length() == 0){
			throw new IllegalArgumentException();
		}
		this.value = builder.toString();
	}

	@Override
	public String toString(){
		return value;
	}

	@Override
	public int length(){
		return value.length();
	}

	@Override
	public String value(){
		return value;
	}
}
