package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.Parser;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenSeries;

import java.util.ArrayList;

public class ExpressionParser implements Parser{

	@Override
	public Token parse(String s){
		return parseSeries(s, 0);
	}

	public Token parseSeries(String s, int index){
		if(index == s.length()){
			return new TokenSeries();
		}
		final var segments = new ArrayList<Token>();
		while(index < s.length()){
			final var seg = parseSegment(s, index);
			if(seg == null){
				break;
			}
			segments.add(seg);
			index += seg.length();
		}
		return (segments.size() == 1) ? segments.get(0) : new TokenSeries(segments);
	}

	public Token parseSegment(String s, int index){
		final char c = s.charAt(index);
		switch(c){
			case '@': case '+': case '&': case '*': case '!': case '?': case '~':{
				return new FunctionExpression(this, s, index);
			}
			case '.': case '^':{
				return new Symbol(c);
			}
			case '\n': case '\t': case ' ':{
				return new WhitespaceExpression(s, index);
			}
			case '\'':{
				return new LiteralExpression(s, index);
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
