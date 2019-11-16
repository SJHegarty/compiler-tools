package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.data.Token;

public class FormattingExpression implements Token{
	private final String value;

	public FormattingExpression(String value){
		this.value = value;
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
