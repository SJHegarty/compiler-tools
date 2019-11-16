package localgoat.lang.compiler.automata.data;

import localgoat.lang.compiler.automata.structure.Type;

import java.util.*;
import java.util.function.Predicate;

public class TokenString implements Token{

	private final List<Token> tokens;
	private final Set<Type> classes;

	public TokenString(Set<Type> classes, List<Token> tokens){
		this.tokens = Collections.unmodifiableList(new ArrayList<>(tokens));
		this.classes = Collections.unmodifiableSet(new HashSet<>(classes));
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

	@Override
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

	public List<Token> tokens(){
		return tokens;
	}
}
