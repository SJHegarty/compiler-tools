package localgoat.lang.compiler.automata.structure;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Type{

	private final String type;
	private final Set<String> flags;

	public Type(String type, Set<String> flags){
		this.type = type;
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

}