package localgoat.lang.compiler.automata.expression.handlers;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.expression.Expression;
import localgoat.lang.compiler.automata.expression.ExpressionSeries;
import localgoat.lang.compiler.automata.operation.Concatenate;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.util.ESupplier;

import java.util.function.Function;
import java.util.stream.Collectors;

public class SeriesHandler implements Function<Expression, Automaton<Token<Character>>>{

	private final Converter converter;

	public SeriesHandler(Converter converter){
		this.converter = converter;
	}

	@Override
	public Automaton<Token<Character>> apply(Expression expression){
		final var series = (ExpressionSeries)expression;
		final var children = ESupplier.from(series.children())
			.map(seg -> converter.build(seg))
			.toStream()
			.collect(Collectors.toList());

		final var concat = new Concatenate<Token<Character>>();
		return concat.apply(children);
	}
}
