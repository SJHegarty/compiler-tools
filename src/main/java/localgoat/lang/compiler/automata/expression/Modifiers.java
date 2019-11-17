package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.token.StringToken;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenTree;

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
				new StringToken(value)
			)
		);
	}

	@Override
	public Token tail(){
		return CLOSING;
	}
}
