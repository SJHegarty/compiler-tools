package localgoat.lang.compiler.automata;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class TokenString<T> implements Token{

	private final List<T> tokens;
	private final Set<String> classes;

	public TokenString(Set<String> classes, List<T> tokens){
		this.tokens = Collections.unmodifiableList(tokens);
		this.classes = Collections.unmodifiableSet(classes);
	}

	public String toString(){
		return classes + ": " + value().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
	}

	public String value(){
		final var builder = new StringBuilder();
		for(var t: tokens){
			builder.append(t);
		}
		return builder.toString();
	}

	public Set<String> classes(){
		return classes;
	}

	public List<T> tokens(){
		return tokens;
	}
}
