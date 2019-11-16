package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.data.ReadMode;
import localgoat.lang.compiler.automata.data.Token;

public class LiteralExpression implements Expression{
	static final DFA DFA;

	static{
		final var converter = new Converter();

		converter.addClass('d', c -> c < 0x100);
		converter.addClass('q', c -> c == '\'');
		converter.addClass('e', c -> c == '\\');

		DFA = converter.buildDFA("q*+(~q, eq)q");
	}

	private final String value;

	public LiteralExpression(String s, int index){
		final var tokens = Token.from(s);
		final var result = DFA.read(ReadMode.GREEDY, index, tokens);
		if(result == null){
			throw new IllegalArgumentException(s.substring(index));
		}
		final var extracted = result.value();
		this.value = extracted.substring(1, extracted.length() - 1);
	}

	@Override
	public int length(){
		return 2 + value.length();
	}

	@Override
	public String toString(){
		return "'" + value + "'";
	}

	public String value(){
		return value;
	}
}
