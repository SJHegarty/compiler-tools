package localgoat.lang.compiler.automata.expression.handlers;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.expression.Expression;
import localgoat.lang.compiler.automata.expression.Symbol;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.DFA;

import java.util.function.Function;

public class SymbolHandler implements Function<Expression, Automaton<Token<Character>>>{

	private final Converter converter;

	public SymbolHandler(Converter converter){
		this.converter = converter;
	}

	@Override
	public Automaton<Token<Character>> apply(Expression expression){
		final var symbol = (Symbol)expression;
		final char c = symbol.value();
		if(c == '^'){
			return DFA.lambda();
		}
		if((c & 0xffffff00) == 0){
			if('A' <= c && c <= 'Z'){
				final var expr = converter.substitutions(c);
				if(expr != null){
					return converter.buildDFA(expr);
				}
			}
			else{
				final char[] chars = converter.chars(c);
				if(chars != null){
					return DFA.of(Token.from(chars));
				}
			}
			System.err.println(String.format("No character class defined for symbol '%s' using literal interpretation.", c));
			return DFA.of(Token.of(c));
		}
		throw new IllegalStateException();
	}
}
