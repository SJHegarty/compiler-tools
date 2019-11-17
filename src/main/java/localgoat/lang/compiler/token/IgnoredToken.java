package localgoat.lang.compiler.token;

import localgoat.lang.compiler.token.Token;

public class IgnoredToken implements Token{
	private final String value;

	public IgnoredToken(String value){
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
