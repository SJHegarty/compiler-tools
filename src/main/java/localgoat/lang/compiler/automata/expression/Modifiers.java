package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.data.TokenTree;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Modifiers implements TokenTree{
	private static final Symbol OPENING = new Symbol('<');
	private static final Symbol CLOSING = new Symbol('>');

	private final String value;

	public Modifiers(String value){
		this.value = value;
	}

	@Override
	public Token head(){
		return OPENING;
	}

	@Override
	public List<Token> children(){
		return Collections.unmodifiableList(
			Arrays.asList(
				Token.of(value)
			)
		);
	}

	@Override
	public Token tail(){
		return CLOSING;
	}
}
