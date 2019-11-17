package localgoat.lang.compiler.token;

public class StringToken implements Token{
	private final String value;

	public StringToken(String value){
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

	@Override
	public boolean equals(Object o){
		if(o.getClass() == getClass()){
			final var t = (Token)o;
			return value.equals(t.value());
		}
		return false;
	}

	@Override
	public int hashCode(){
		return toString().hashCode() ^ getClass().hashCode();
	}

	@Override
	public String toString(){
		return value;
	}
}
