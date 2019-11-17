package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.Parser;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionParser implements Parser<Symbol, Token>{

	@Override
	public List<Token> parse(List<Symbol> tokens){
		return Arrays.asList(parseSeries(tokens, 0));
	}

	public Token parseSeries(List<Symbol> symbols, int index){
		if(index == symbols.size()){
			return new TokenSeries();
		}
		final var segments = new ArrayList<Token>();
		while(index < symbols.size()){
			final var seg = parseSegment(symbols, index);
			if(seg == null){
				break;
			}
			segments.add(seg);
			index += seg.length();
		}
		return (segments.size() == 1) ? segments.get(0) : new TokenSeries(segments);
	}

	public Token parseSegment(List<Symbol> symbols, int index){
		final char c = symbols.get(index).charValue();
		switch(c){
			case '@': case '+': case '&': case '*': case '!': case '?': case '~':{
				return new FunctionExpression(this, symbols, index);
			}
			case '.': case '^':{
				return new Symbol(c);
			}
			case '\n': case '\t': case ' ':{
				return new WhitespaceExpression(symbols, index);
			}
			case '\'':{
				return new LiteralExpression(symbols, index);
			}
			default:{
				if(('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')){
					return new Symbol(c);
				}
				return null;
			}
		}

	}


}
