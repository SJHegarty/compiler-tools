package localgoat.lang.struct;

public class Token{

	public final String value;
	public final TokenType type;

	public Token(String value, TokenType type){
		this.value = value;
		this.type = type;
	}

	public TokenType type(){
		return type;
	}

	public String value(){
		return value;
	}

	public String toString(){
		return value();
	}

	public boolean ignored(){
		return type.ignored;
	}
}
