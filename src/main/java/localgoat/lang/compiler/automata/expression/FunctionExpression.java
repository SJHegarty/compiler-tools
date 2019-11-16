package localgoat.lang.compiler.automata.expression;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.data.TokenTree;
import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FunctionExpression implements TokenTree{

	private final boolean bracketed;
	private final char identifier;
	private final String modifiers;
	private final Token[] children;

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
			bracketed = true;
			final var segments = new ArrayList<Token>();
			loop: while(true){
				final var seg = ExpressionParser.parseSeries(source, ++index);
				segments.add(seg);
				index+=seg.length();
				final char c = source.charAt(index);
				switch(c){
					case ')': break loop;
					case ',': continue loop;
					default: throw new IllegalArgumentException(
						String.format("Unexpected token: '%s' in %s", c, source.substring(index))
					);
				}
			}
			if(segments.size() == 0){
				throw new IllegalArgumentException();
			}
			this.children = segments.stream().toArray(Token[]::new);
		}
		else{
			bracketed = false;
			this.children = new Token[]{
				ExpressionParser.parseSegment(source, index)
			};
		}
	}

	@Override
	public int length(){
		int rv = 1;
		if(modifiers != null){
			rv += 2 + modifiers.length();
		}
		if(bracketed){
			rv += 2;
		}
		rv += children.length - 1;
		for(var c: children){
			rv += c.length();
		}
		return rv;
	}

	@Override
	public String value(){
		final var builder = new StringBuilder();
		builder.append(identifier);
		if(modifiers != null){
			builder.append('<').append(modifiers).append('>');
		}
		if(bracketed){
			builder.append('(');
		}

		ESupplier.from(children)
			.map(c -> c.toString())
			.interleave(",")
			.forEach(s -> builder.append(s));

		if(bracketed){
			builder.append(')');
		}
		return builder.toString();
	}

	@Override
	public String toString(){
		return value();
	}

	public char identifier(){
		return identifier;
	}

	public String modifiers(){
		return modifiers;
	}

	@Override
	public List<Token> children(){
		return Collections.unmodifiableList(Arrays.asList(children));
	}

}
