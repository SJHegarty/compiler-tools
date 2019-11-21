package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Modifiers implements TokenTree{
	private static final Type TYPE = new Type(
		"modifiers",
		TokenLayer.SEMANTIC,
		Collections.emptySet()
	);

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
				new StringToken(value, Collections.singleton(TYPE))
			)
		);
	}

	@Override
	public Token tail(){
		return CLOSING;
	}

	@Override
	public Token trim(){
		return this;
	}

	@Override
	public TokenLayer layer(){
		return TokenLayer.SEMANTIC;
	}
}
