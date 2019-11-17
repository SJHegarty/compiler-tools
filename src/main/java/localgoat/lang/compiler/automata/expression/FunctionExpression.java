package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.token.*;
import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionExpression implements TokenTree{

	private final ExpressionParser parser;
	private final Token head;
	private final Token[] children;
	private final Token tail;

	public FunctionExpression(ExpressionParser parser, List<Symbol> symbols, int index){
		this.parser = parser;
		final List<Token> head = new ArrayList<>();
		head.add(symbols.get(index++));

		if(symbols.get(index).charValue() == '<'){
			final var builder = new StringBuilder();
			while(true){
				final char c = symbols.get(++index).charValue();
				if(c == '>'){
					head.add(new Modifiers(builder.toString()));
					index++;
					break;
				}
				builder.append(c);
			}
		}

		if(symbols.get(index).charValue() == '('){
			head.add(new Symbol('('));
			final var segments = new ArrayList<Token>();
			loop: while(true){
				final var seg = parser.parseSeries(symbols, ++index);
				segments.add(seg);
				index+=seg.length();
				final char c = symbols.get(index).charValue();
				switch(c){
					case ')':{
						this.tail = new Symbol(')');
						break loop;
					}
					case ',': continue loop;
					default: throw new IllegalArgumentException(
						String.format("Unexpected token: '%s' in %s", c, Symbol.toString(symbols, index))
					);
				}
			}
			if(segments.size() == 0){
				throw new IllegalArgumentException();
			}
			this.children = segments.stream().toArray(Token[]::new);
		}
		else{
			this.children = new Token[]{
				parser.parseSegment(symbols, index)
			};
			this.tail = null;
		}
		if(head.size() == 1){
			this.head = head.get(0);
		}
		else{
			this.head = new TokenSeries(head);
		}
	}

	@Override
	public String toString(){
		return value();
	}

	public char identifier(){
		return ((Symbol)headTokens().get()).charValue();
	}

	private ESupplier<? extends Token> headTokens(){
		if(head instanceof TokenTree){
			return ESupplier.of((TokenTree)head)
				.flatMap(tree -> ESupplier.from(tree.children()));
		}
		return ESupplier.of(head);
	}

	public String modifiers(){
		return headTokens().retain(t -> t instanceof Modifiers)
			.map(t -> (Modifiers)t)
			.map(m -> m.childrenString())
			.get();
	}

	@Override
	public Token head(){
		return head;
	}

	@Override
	public List<Token> children(){
		return ESupplier.from(children)
			.interleave(new IgnoredToken(","))
			.toStream()
			.collect(Collectors.toList());
	}

	@Override
	public Token tail(){
		return tail;
	}

}
