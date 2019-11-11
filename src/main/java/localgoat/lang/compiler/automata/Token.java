package localgoat.lang.compiler.automata;

import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public interface Token<T>{

	public static void main(String...args){
		final String identifier = "@<identifier>(*<1^>l*(h*<1^>l))";
		final String className = "@<class-name>(*<1^>(U*l))";
		final String pattern = String.format("+(%s, %s)", identifier, className);
		final var series = readSeries(pattern);
		System.err.println(series);

	}

	interface Segment{
		int index();
		int length();
	}

	static class Series implements Segment{
		private final int index;
		private final Segment[] segments;

		public Series(List<Segment> segments){
			this.index = segments.get(0).index();
			this.segments = segments.stream().toArray(Segment[]::new);
		}
		public Series(int index){
			this.index = index;
			this.segments = new Segment[0];
		}

		@Override
		public int index(){
			return index;
		}

		@Override
		public int length(){
			return Stream.of(segments)
				.mapToInt(s -> s.length())
				.sum();
		}

		@Override
		public String toString(){
			final var builder = new StringBuilder();
			for(var s: segments){
				builder.append(s);
			}
			return builder.toString();
		}
	}

	class Symbol implements Segment{

		private final int index;
		private final char c;

		public Symbol(int index, char c){
			this.index = index;
			this.c = c;
		}

		@Override
		public int index(){
			return index;
		}

		@Override
		public int length(){
			return 1;
		}

		@Override
		public String toString(){
			return Character.toString(c);
		}
	}

	static class Function implements Segment{

		private final int index;
		private final char identifier;
		private final String modifiers;
		private final Segment[] children;

		public Function(String source, int index){
			this.index = index;
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
				final var segments = new ArrayList<Segment>();
				loop: while(true){
					final var seg = readSeries(source, ++index);
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
				this.children = segments.stream().toArray(Segment[]::new);
			}
			else{
				this.children = new Segment[]{
					readSegment(source, index)
				};
			}
		}

		@Override
		public int index(){
			return index;
		}

		@Override
		public int length(){
			int rv = 1;
			if(modifiers != null){
				rv += 2 + modifiers.length();
			}
			if(children.length == 1 && !(children[0] instanceof Series)){
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
			if(children.length == 1 && !(children[0] instanceof Series)){
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
	}
	static Segment readSeries(String s){
		final var builder = new StringBuilder();
		for(int i = 0; i < s.length(); i++){
			final char c = s.charAt(i);
			if(!Character.isWhitespace(c)){
				builder.append(c);
			}
		}
		return readSeries(builder.toString(), 0);
	}

	static Segment readSeries(String s, int index){
		if(index == s.length()){
			return new Series(index);
		}
		final var segments = new ArrayList<Segment>();
		while(index < s.length() && "),".indexOf(s.charAt(index)) == -1){
			final var seg = readSegment(s, index);
			segments.add(seg);
			index += seg.length();
		}
		return (segments.size() == 1) ? segments.get(0) : new Series(segments);
	}

	static Segment readSegment(String s, int index){
		final char c = s.charAt(index);
		switch(c){
			case '@':{
				//return a named segment
			}
			case '+':{
				//return or of list
			}
			case '&':{
				//return not of or of nots of list
			}
			case '*':{
				//return kleene, optionally modified, of next
			}
			case '!':{
				//return not of next
			}
			case '?':{
				//return optional of next
				return new Function(s, index);
			}
			case '^':{
				//return lambda lang
				return new Symbol(index, c);
			}

			default:{
				if(('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')){
					return new Symbol(index, c);
				}
				throw new UnsupportedOperationException(String.format("Unsupported segment head '%s'.", c));
			}
		}

	}

	static Token<Character>[] from(String s){
		final var chars = s.toCharArray();
		final Token<Character> rv[] = new Token[chars.length];
		for(int i = 0; i < rv.length; i++){
			rv[i] = of(chars[i]);
		}
		return rv;
	}

	static Token<Character> of(char c){
		return new Token<Character>(){
			@Override
			public Character value(){
				return c;
			}
		};
	}

	T value();
}
