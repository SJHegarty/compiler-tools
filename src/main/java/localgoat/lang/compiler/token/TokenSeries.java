package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.expression.WhitespaceExpression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TokenSeries implements TokenTree{
	private final Token[] segments;

	public TokenSeries(List<Token> segments){
		if(segments.size() == 1 && segments.get(0) instanceof WhitespaceExpression){
			throw new IllegalArgumentException();
		}
		this.segments = segments.stream().toArray(Token[]::new);
	}

	public TokenSeries(){
		this.segments = new Token[0];
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
}
