package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.ReadMode;
import localgoat.lang.compiler.automata.structure.AutomatonUtils;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;

import java.util.List;

public class LiteralExpression implements Token{
	static final AutomatonUtils UTILS;

	static{
		final var converter = new Converter();

		converter.addClass('d', c -> c < 0x100);
		converter.addClass('q', c -> c == '\'');
		converter.addClass('e', c -> c == '\\');

		UTILS = new AutomatonUtils(converter.build("q*+(~q, eq)q"));
	}

	private final String wrapped;

	public LiteralExpression(List<Symbol> symbols, int index){
		//final var tokens = Symbol.from(s);
		final var result = UTILS.read(ReadMode.GREEDY, index, symbols);
		if(result == null){
			throw new IllegalArgumentException(Symbol.toString(symbols, index));
		}
		final var extracted = result.value();
		this.wrapped = extracted.substring(1, extracted.length() - 1);
	}

	@Override
	public int length(){
		return 2 + wrapped.length();
	}

	@Override
	public String value(){
		return "'" + wrapped + "'";
	}

	@Override
	public String toString(){
		return value();
	}

	public String wrapped(){
		return wrapped;
	}
}
