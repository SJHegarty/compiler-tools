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

	private static final Symbol OPENING = new Symbol('<', TokenLayer.SYNTACTIC);
	private static final Symbol CLOSING = new Symbol('>', TokenLayer.SYNTACTIC);

	private final Symbol opening;
	private final StringToken value;
	private final Symbol closing;

	public Modifiers(String value){
		this(
			OPENING,
			new StringToken(value, Collections.singleton(TYPE)),
			CLOSING
		);
	}

	private Modifiers(Symbol opening, StringToken value, Symbol closing){
		this.opening = opening;
		this.value = value;
		this.closing = closing;
	}

	@Override
	public Symbol head(){
		return opening;
	}

	@Override
	public List<Token> children(){
		return Collections.unmodifiableList(
			Arrays.asList(value)
		);
	}

	@Override
	public Symbol tail(){
		return closing;
	}

	@Override
	public Token filter(FilteringContext context){
		final boolean semantic = context.layer() == TokenLayer.SEMANTIC;
		return new Modifiers(
			semantic ? null : OPENING,
			value,
			semantic ? null : CLOSING
		);
	}

	@Override
	public TokenLayer filteringLayer(){
		return (opening == null) ? TokenLayer.SEMANTIC : TokenLayer.SYNTACTIC;
	}

	@Override
	public TokenLayer layer(){
		return TokenLayer.SEMANTIC;
	}
}
