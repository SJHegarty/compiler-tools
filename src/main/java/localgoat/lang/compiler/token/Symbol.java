package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.Token;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

public class Symbol implements Token{

	private final char c;
	private final TokenLayer layer;

	public Symbol(char c){
		this(c, TokenLayer.SEMANTIC);
	}

	public Symbol(char c, TokenLayer layer){
		this.c = c;
		this.layer = layer;
	}

	public static Symbol[] from(char...chars){
		return IntStream.range(0, chars.length)
			.mapToObj(i -> new Symbol(chars[i]))
			.toArray(Symbol[]::new);
	}

	public static Symbol[] from(String s){
		return IntStream.range(0, s.length())
			.mapToObj(i -> new Symbol(s.charAt(i)))
			.toArray(Symbol[]::new);
	}

	public static String toString(List<Symbol> symbols){
		return toString(symbols, 0);
	}

	public static String toString(List<Symbol> symbols, int index){
		final var builder = new StringBuilder();
		IntStream.range(index, symbols.size())
			.forEach(i -> builder.append(symbols.get(i)));
		return builder.toString();
	}

	@Override
	public Symbol filter(FilteringContext context){
		return (Symbol)Token.super.filter(context);
	}

	@Override
	public int length(){
		return 1;
	}

	@Override
	public String toString(){
		return Character.toString(c);
	}

	public String value(){
		return Character.toString(c);
	}

	@Override
	public Set<Type> types(){
		return Collections.emptySet();
	}

	@Override
	public TokenLayer layer(){
		return layer;
	}

	public char charValue(){
		return c;
	}

	public boolean equals(Object o){
		if(o.getClass() == getClass()){
			return c == ((Symbol)o).c;
		}
		return false;
	}

	public int hashCode(){
		return c;
	}
}
