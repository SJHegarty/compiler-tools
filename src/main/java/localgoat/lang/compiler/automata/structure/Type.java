package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.token.TokenLayer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Type{

	private final String type;
	private final Set<String> flags;
	private final TokenLayer layer;

	public Type(String type, TokenLayer layer, Set<String> flags){
		this.type = type;
		this.layer = layer;
		this.flags = new HashSet<>(flags);
	}

	public String name(){
		return type;
	}

	public boolean hasFlag(String flag){
		return flags.contains(flag);
	}

	public String toString(){
		return type + (flags.isEmpty()?"":flags);
	}

	public TokenLayer layer(){
		return layer;
	}
}
