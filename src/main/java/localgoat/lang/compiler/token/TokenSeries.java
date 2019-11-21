package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.expression.WhitespaceExpression;
import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TokenSeries implements TokenTree{
	private final Token[] segments;

	public TokenSeries(List<? extends Token> segments){
		if(segments.size() == 1 && segments.get(0) instanceof WhitespaceExpression){
			throw new IllegalArgumentException();
		}
		this.segments = segments.stream().toArray(Token[]::new);
	}

	public TokenSeries(){
		this.segments = new Token[0];
	}

	private TokenSeries(Token[] segments){
		this.segments = segments;
	}

	@Override
	public String toString(){
		return value();
	}

	@Override
	public Token head(){
		return null;
	}

	@Override
	public List<Token> children(){
		return Collections.unmodifiableList(Arrays.asList(segments));
	}

	@Override
	public Token tail(){
		return null;
	}

	@Override
	public Token filter(TokenLayer layer){
		var tokens = ESupplier.from(segments)
			.map(s -> s.filter(layer))
			.toArray(Token[]::new);

		switch(tokens.length){
			case 0: return null;
			case 1: return tokens[0];
			default: return new TokenSeries(tokens);
		}
	}

}
