package localgoat.lang.compiler.automata;

public interface Token<T>{

	Token<Character> LINE_FEED = of('\n');

	static Token<Character>[] from(String s){
		return from(s.toCharArray());
	}

	static Token<Character>[] from(char...chars){
		final Token<Character> rv[] = new Token[chars.length];
		for(int i = 0; i < rv.length; i++){
			rv[i] = of(chars[i]);
		}
		return rv;
	}

	static Token<Character> of(char c){
		return new Token<Character>(){
			@Override
			public Character value(){
				return c;
			}

			@Override
			public boolean equals(Object o){
				if(o.getClass() == getClass()){
					final var t = (Token<Character>)o;
					return c == t.value();
				}
				return false;
			}

			@Override
			public int hashCode(){
				return c;
			}

			@Override
			public String toString(){
				return Character.toString(c);
			}
		};
	}

	T value();
}
