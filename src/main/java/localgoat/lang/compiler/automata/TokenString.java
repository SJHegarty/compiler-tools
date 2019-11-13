package localgoat.lang.compiler.automata;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class TokenString<T> implements Token{

	private final List<T> tokens;
	private final Set<Type> classes;

	public TokenString(Set<Type> classes, List<T> tokens){
		this.tokens = Collections.unmodifiableList(tokens);
		this.classes = Collections.unmodifiableSet(classes);
		for(var c: classes){
			if(c != null && !(c instanceof Type)){
				throw new IllegalStateException();
			}
		}
	}

	public String toString(){
		return String.format(
			"[%s%s]",
			classes,
			value().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n").replaceAll("\t", "\\\\t")
		);
	}

	public String value(){
		final var builder = new StringBuilder();
		for(var t: tokens){
			builder.append(t);
		}
		return builder.toString();
	}

	public boolean hasClass(Predicate<Type> predicate){
		return null != classes.stream()
			.filter(predicate)
			.findFirst()
			.orElse(null);
	}
	public Set<Type> classes(){
		return classes;
	}

	public List<T> tokens(){
		return tokens;
	}
}
