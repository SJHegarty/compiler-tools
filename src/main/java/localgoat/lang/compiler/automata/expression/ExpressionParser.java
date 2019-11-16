package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.data.Token;

import java.util.ArrayList;

public class ExpressionParser{

	public static Token parse(String s){
		return parseSeries(s, 0);
	}

	public static Token parseSeries(String s, int index){
		if(index == s.length()){
			return new ExpressionSeries();
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
		return (segments.size() == 1) ? segments.get(0) : new ExpressionSeries(segments);
	}

	public static Token parseSegment(String s, int index){
		final char c = s.charAt(index);
		switch(c){
			case '@': case '+': case '&': case '*': case '!': case '?': case '~':{
				return new FunctionExpression(s, index);
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
