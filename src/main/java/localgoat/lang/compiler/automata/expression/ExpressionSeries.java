package localgoat.lang.compiler.automata.expression;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ExpressionSeries implements Expression{
	private final Expression[] segments;

	public ExpressionSeries(List<Expression> segments){
		this.segments = segments.stream().toArray(Expression[]::new);
	}

	public ExpressionSeries(){
		this.segments = new Expression[0];
	}

	@Override
	public int length(){
		return Stream.of(segments)
			.mapToInt(s -> s.length())
			.sum();
	}

	@Override
	public String toString(){
		final var builder = new StringBuilder();
		for(var s: segments){
			builder.append(s);
		}
		return builder.toString();
	}

	public List<Expression> segments(){
		return Collections.unmodifiableList(Arrays.asList(segments));
	}
}
