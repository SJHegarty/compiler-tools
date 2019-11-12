package localgoat.lang.compiler.automata;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class StringClass{
	public static final StringClass NONE = new StringClass(null, Collections.emptySet());

	private final String type;
	private final Set<String> flags;

	public StringClass(String type, Set<String> flags){
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
