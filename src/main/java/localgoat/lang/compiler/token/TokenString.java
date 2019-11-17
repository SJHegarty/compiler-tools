package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.structure.Type;

import java.util.*;
import java.util.function.Predicate;

public class TokenString implements TokenTree{

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

	@Override
	public int length(){
		return tokens.stream()
			.mapToInt(s -> s.length())
			.sum();
	}

	public boolean hasClass(String name){
		return hasClass(t -> Objects.equals(name, t.name()));
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

	@Override
	public Token head(){
		return null;
	}

	@Override
	public List<Token> children(){
		return tokens;
	}

	@Override
	public Token tail(){
		return null;
	}
}
