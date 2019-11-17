package localgoat.lang.compiler.automata.expression.handlers;

import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.token.Symbol;

import java.util.function.Function;

public class SymbolHandler implements Function<Token, Automaton>{

	private final Converter converter;

	public SymbolHandler(Converter converter){
		this.converter = converter;
	}

	@Override
	public Automaton apply(Token expression){
		final var symbol = (Symbol)expression;
		final char c = symbol.charValue();
		if(c == '^'){
			return Automaton.lambda();
		}
		if((c & 0xffffff00) == 0){
			if('A' <= c && c <= 'Z'){
				final var expr = converter.substitutions(c);
				if(expr != null){
					return converter.build(expr);
				}
			}
			else{
				final char[] chars = converter.chars(c);
				if(chars != null){
					return Automaton.of(Symbol.from(chars));
				}
			}
			System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
			return Automaton.of(new Symbol(c));
		}
		throw new IllegalStateException();
	}
}
