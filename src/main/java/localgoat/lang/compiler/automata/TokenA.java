package localgoat.lang.compiler.automata;

public interface TokenA<T>{

	static TokenA<Character>[] from(String s){
		return from(s.toCharArray());
	}

	static TokenA<Character>[] from(char...chars){
		final TokenA<Character> rv[] = new TokenA[chars.length];
		for(int i = 0; i < rv.length; i++){
			rv[i] = of(chars[i]);
		}
		return rv;
	}

	static TokenA<Character> of(char c){
		return new TokenA<Character>(){
			@Override
			public Character value(){
				return c;
			}

			@Override
			public boolean equals(Object o){
				if(o.getClass() == getClass()){
					final var t = (TokenA<Character>)o;
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
