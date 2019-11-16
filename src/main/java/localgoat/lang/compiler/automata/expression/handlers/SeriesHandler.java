package localgoat.lang.compiler.automata.expression.handlers;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.data.TokenSeries;
import localgoat.lang.compiler.automata.operation.Concatenate;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.util.ESupplier;

import java.util.function.Function;
import java.util.stream.Collectors;

public class SeriesHandler implements Function<Token, Automaton>{

	private final Converter converter;

	public SeriesHandler(Converter converter){
		this.converter = converter;
	}

	@Override
	public Automaton apply(Token expression){
		final var series = (TokenSeries)expression;
		final var children = ESupplier.from(series.children())
			.map(seg -> converter.build(seg))
			.toStream()
			.collect(Collectors.toList());

		final var concat = new Concatenate();
		return concat.apply(children);
	}
}
