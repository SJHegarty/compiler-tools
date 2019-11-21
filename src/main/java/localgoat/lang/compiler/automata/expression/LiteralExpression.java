package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.ReadMode;
import localgoat.lang.compiler.automata.structure.AutomatonUtils;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.*;

import java.util.*;

public class LiteralExpression implements TokenTree{

	private static final Symbol MARK = new Symbol('\'', TokenLayer.SYNTACTIC);

	static final AutomatonUtils UTILS;
	static final Type LITERAL = new Type(
		"literal",
		TokenLayer.SEMANTIC,
		Collections.emptySet()
	);

	static{
		final var converter = new Converter();

		converter.addClass('d', c -> c < 0x100);
		converter.addClass('q', c -> c == '\'');
		converter.addClass('e', c -> c == '\\');

		UTILS = new AutomatonUtils(converter.build("q*+(~q, eq)q"));
	}

	private final Token value;
	private final Symbol head;
	private final Symbol tail;

	private LiteralExpression(Symbol head, Token value, Symbol tail){
		this.head = head;
		this.value = value;
		this.tail = tail;
	}

	public LiteralExpression(List<Symbol> symbols, int index){
		//final var tokens = Symbol.from(s);
		final var result = UTILS.read(ReadMode.GREEDY, index, symbols);
		if(result == null){
			throw new IllegalArgumentException(Symbol.toString(symbols, index));
		}
		final var quoted = result.value();
		final var extracted = quoted.substring(1, quoted.length() - 1);

		this.value = new Token(){
			@Override
			public int length(){
				return extracted.length();
			}

			@Override
			public String value(){
				return extracted;
			}

			@Override
			public Set<Type> types(){
				return Collections.singleton(LITERAL);
			}
		};
		this.head = MARK;
		this.tail = MARK;
	}

	@Override
	public Token head(){
		return head;
	}

	@Override
	public List<? extends Token> children(){
		return Collections.unmodifiableList(
			Arrays.asList(this.value)
		);
	}

	@Override
	public Token tail(){
		return tail;
	}

	@Override
	public Token filter(FilteringContext layer){
		final Symbol mark = (layer.layer() == TokenLayer.SEMANTIC) ? null : MARK;
		return new LiteralExpression(mark, value, mark);
	}

	@Override
	public TokenLayer filteringLayer(){
		return (head == null) ? TokenLayer.SEMANTIC : TokenLayer.SYNTACTIC;
	}

	@Override
	public Set<Type> types(){
		return Collections.singleton(LITERAL);
	}

	@Override
	public TokenLayer layer(){
		return TokenLayer.SEMANTIC;
	}

	@Override
	public String toString(){
		return value();
	}

}
