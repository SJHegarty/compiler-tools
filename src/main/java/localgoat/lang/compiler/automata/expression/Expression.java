package localgoat.lang.compiler.automata.expression;

import java.util.ArrayList;

public interface Expression{

	int length();

	static Expression parse(String s){
		return parseSeries(s, 0);
	}

	static Expression parseSeries(String s, int index){
		if(index == s.length()){
			return new ExpressionSeries();
		}
		final var segments = new ArrayList<Expression>();
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

	static Expression parseSegment(String s, int index){
		final char c = s.charAt(index);
		switch(c){
			case '@': case '+': case '&': case '*': case '!': case '?': case '~':{
				return new FunctionExpression(s, index);
			}
			case '.': case '^':{
				return new Symbol(c);
			}
			case ' ':{
				return new SpacesExpression(s, index);
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
