package localgoat.lang.compiler.automata.expression;

import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FunctionExpression implements Expression{

	private final char identifier;
	private final String modifiers;
	private final Expression[] children;

	public FunctionExpression(String source, int index){
		this.identifier = source.charAt(index++);

		if(source.charAt(index) == '<'){
			final var builder = new StringBuilder();
			while(true){
				final char c = source.charAt(++index);
				if(c == '>'){
					modifiers = builder.toString();
					index++;
					break;
				}
				builder.append(c);
			}
		}
		else{
			modifiers = null;
		}

		if(source.charAt(index) == '('){
			final var segments = new ArrayList<Expression>();
			loop: while(true){
				final var seg = Expression.parseSeries(source, ++index);
				segments.add(seg);
				index+=seg.length();
				final char c = source.charAt(index);
				switch(c){
					case ')': break loop;
					case ',': continue loop;
					default: throw new IllegalArgumentException(
						String.format("Unexpected token: '%s'", c)
					);
				}
			}
			if(segments.size() == 0){
				throw new IllegalArgumentException();
			}
			this.children = segments.stream().toArray(Expression[]::new);
		}
		else{
			this.children = new Expression[]{
				Expression.parseSegment(source, index)
			};
		}
	}

	@Override
	public int length(){
		int rv = 1;
		if(modifiers != null){
			rv += 2 + modifiers.length();
		}
		if(children.length == 1 && !(children[0] instanceof ExpressionSeries)){
			rv += children[0].length();
		}
		else{
			rv += 1 + children.length;
			for(var c: children){
				rv += c.length();
			}
		}
		return rv;
	}

	@Override
	public String toString(){
		final var builder = new StringBuilder();
		builder.append(identifier);
		if(modifiers != null){
			builder.append('<').append(modifiers).append('>');
		}
		if(children.length == 1 && !(children[0] instanceof ExpressionSeries)){
			builder.append(children[0]);
		}
		else{
			builder.append('(');
			ESupplier.from(children)
				.map(c -> c.toString())
				.interleave(",")
				.forEach(s -> builder.append(s));
			builder.append(')');
		}
		return builder.toString();
	}

	public char identifier(){
		return identifier;
	}

	public String modifiers(){
		return modifiers;
	}

	public List<Expression> children(){
		return Collections.unmodifiableList(Arrays.asList(children));
	}

}
