package localgoat.lang.compiler.automata.data;

public interface Token{

	Token LINE_FEED = of('\n');

	static Token[] from(String s){
		return from(s.toCharArray());
	}

	static Token[] from(char...chars){
		final Token rv[] = new Token[chars.length];
		for(int i = 0; i < rv.length; i++){
			rv[i] = of(chars[i]);
		}
		return rv;
	}

	static Token of(char c){
		return of(Character.toString(c));
	}

	static Token of(String s){
		return new Token(){
			@Override
			public int length(){
				return s.length();
			}

			@Override
			public String value(){
				return s;
			}

			@Override
			public boolean equals(Object o){
				if(o.getClass() == getClass()){
					final var t = (Token)o;
					return s.equals(t.value());
				}
				return false;
			}

			@Override
			public int hashCode(){
				return toString().hashCode() ^ getClass().hashCode();
			}

			@Override
			public String toString(){
				return s;
			}
		};
	}

	int length();
	String value();
}
