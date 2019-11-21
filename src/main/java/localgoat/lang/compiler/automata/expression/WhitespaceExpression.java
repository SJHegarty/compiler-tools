package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenLayer;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WhitespaceExpression implements Token{
	private final Type TYPE = new Type(
		"white-space",
		TokenLayer.AESTHETIC,
		Collections.singleton("white-space")
	);

	private final String value;

	public WhitespaceExpression(List<Symbol> symbols, int index){
		final var builder = new StringBuilder();
		while(true){
			final char c = symbols.get(index + builder.length()).charValue();
			if("\n \t".indexOf(c) != -1){
				builder.append(c);
			}
			else{
				break;
			}
		}
		if(builder.length() == 0){
			throw new IllegalArgumentException();
		}
		this.value = builder.toString();
	}

	@Override
	public String toString(){
		return value;
	}

	@Override
	public int length(){
		return value.length();
	}

	@Override
	public String value(){
		return value;
	}

	@Override
	public Set<Type> types(){
		return Collections.singleton(TYPE);
	}
}
