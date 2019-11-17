package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.data.ReadMode;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;

public class LiteralExpression implements Token{
	static final DFA DFA;

	static{
		final var converter = new Converter();

		converter.addClass('d', c -> c < 0x100);
		converter.addClass('q', c -> c == '\'');
		converter.addClass('e', c -> c == '\\');

		DFA = converter.buildDFA("q*+(~q, eq)q");
	}

	private final String wrapped;

	public LiteralExpression(String s, int index){
		final var tokens = Symbol.from(s);
		final var result = DFA.read(ReadMode.GREEDY, index, tokens);
		if(result == null){
			throw new IllegalArgumentException(s.substring(index));
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
