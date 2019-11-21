package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.structure.Type;
import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StringToken implements Token{
	private final String value;
	private final Set<Type> types;

	public StringToken(String value, Set<Type> types){
		this.value = value;
		this.types = types;
	}

	public StringToken(List<Symbol> symbols){
		this(symbols, Collections.emptySet());
	}

	public StringToken(List<Symbol> symbols, Set<Type> types){
		this(ESupplier.from(symbols).concatenate(), types);
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
	public Set<Type> types(){
		return Collections.unmodifiableSet(types);
	}

	@Override
	public boolean equals(Object o){
		return null != ESupplier.of(o)
			.mapOrNull(e -> (StringToken)o)
			.retain(t -> value.equals(t.value))
			.get();
	}

	@Override
	public Token filter(TokenLayer layer){
		if(layer.ordinal() < layer().ordinal()){
			return null;
		}
		return this;
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
