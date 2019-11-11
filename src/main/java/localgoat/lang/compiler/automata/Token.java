package localgoat.lang.compiler.automata;

import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface Token<T>{

	static Token<Character>[] from(String s){
		final var chars = s.toCharArray();
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
		};
	}

	T value();
}
