package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.data.TokenTree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ExpressionSeries implements TokenTree{
	private final Token[] segments;

	public ExpressionSeries(List<Token> segments){
		if(segments.size() == 1 && segments.get(0) instanceof WhitespaceExpression){
			throw new IllegalArgumentException();
		}
		this.segments = segments.stream().toArray(Token[]::new);
	}

	public ExpressionSeries(){
		this.segments = new Token[0];
	}

	@Override
	public int length(){
		return Stream.of(segments)
			.mapToInt(s -> s.length())
			.sum();
	}

	@Override
	public String value(){
		final var builder = new StringBuilder();
		for(var s: segments){
			builder.append(s);
		}
		return builder.toString();
	}

	@Override
	public String toString(){
		return value();
	}

	@Override
	public List<Token> children(){
		return Collections.unmodifiableList(Arrays.asList(segments));
	}
}
