package localgoat.lang.compiler.automata;

import java.util.HashSet;
import java.util.Set;

public interface TokenSet<T extends Token>{

	static <T extends Token> TokenSet<T> union(TokenSet<T> s0, TokenSet<T> s1){
		final var tokens = new HashSet<>(s0.tokens());
		tokens.addAll(s1.tokens());
		return () -> tokens;
	}

	Set<T> tokens();

	default boolean contains(T token){
		return tokens().contains(token);
	}
}
