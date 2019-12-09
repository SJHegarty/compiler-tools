package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.expression.WhitespaceExpression;
import localgoat.util.streaming.ESupplier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class TokenSeries implements TokenTree{
	private final Token[] segments;
	private final TokenLayer filteringLayer;

	public TokenSeries(List<? extends Token> segments){
		this(segments, TokenLayer.AESTHETIC);
	}

	private TokenSeries(List<? extends Token> segments, TokenLayer filteringLayer){
		if(segments.size() == 1 && segments.get(0) instanceof WhitespaceExpression){
			throw new IllegalArgumentException();
		}
		this.segments = segments.stream().toArray(Token[]::new);
		this.filteringLayer = filteringLayer;
	}

	public TokenSeries(){
		this.segments = new Token[0];
		this.filteringLayer = TokenLayer.AESTHETIC;
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
	public Token filter(FilteringContext context){
		var tokens = ESupplier.from(segments)
			.map(s -> s.filter(context))
			.toStream()
			.collect(Collectors.toList());

		switch(tokens.size()){
			case 0: return null;
			case 1: return tokens.get(0);
			default: return new TokenSeries(tokens, context.layer());
		}
	}

	@Override
	public TokenLayer filteringLayer(){
		return filteringLayer;
	}

}
