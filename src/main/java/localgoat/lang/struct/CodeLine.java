package localgoat.lang.struct;

import localgoat.util.io.CharSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CodeLine{

	public static final int TAB_WIDTH = 4;
	public final int lineindex;
	public final List<Token> tokens;

	CodeLine(String line, int index){
		this.lineindex = index;
		final var tokens = new ArrayList<Token>();
		this.tokens = Collections.unmodifiableList(tokens);

		try(var source = new CharSource(line)){
			tokens.add(
				Handlers.WHITESPACE_HANDLER.extract(source)
			);

			outer: for(;;){
				final char c = source.peek();
				if(c == CharSource.STREAM_END){
					break;
				}
				for(var handler: Handlers.HANDLERS){
					if(handler.handles(c)){
						tokens.add(handler.extract(source));
						continue outer;
					}
				}
				tokens.add(
					new Token(
						new String(source.read(1)),
						TokenType.UNHANDLED
					)
				);
			}
		}
	}

	public List<Token> contentTokens(){
		var deque = new ArrayDeque<>(tokens);
		deque.pollFirst();
		for(;!deque.isEmpty() && deque.peekFirst().type.ignored; deque.pollFirst());
		for(;!deque.isEmpty() && deque.peekLast().type.ignored; deque.pollLast());
		return Collections.unmodifiableList(
			new ArrayList<>(deque)
		);
	}

	public Token last(Predicate<Token> filter){
		for(int i = tokens.size() - 1; i > 0; i--){
			var token = tokens.get(i);
			if(filter.test(token)){
				return token;
			}
		}
		return null;
	}

	public Token first(Predicate<Token> filter){
		for(var token: tokens){
			if(filter.test(token)){
				return token;
			}
		}
		return null;
	}

	public List<Token> all(Predicate<Token> filter){
		return Collections.unmodifiableList(
			tokens.stream()
				.filter(filter)
				.collect(Collectors.toList())
		);
	}

	public String content(){
		var builder = new StringBuilder();
		for(Token t: contentTokens()){
			builder.append(t);
		}
		return builder.toString();
	}

	public String prefix(){
		return tokens.get(0).value;
	}

	public String suffix(){
		final var stack = new ArrayDeque<Token>();
		for(int index = tokens.size() - 1 ;; index--){
			final var token = tokens.get(index);
			if(token.type.ignored){
				stack.push(token);
			}
			else{
				break;
			}
		}
		final var builder = new StringBuilder();
		while(!stack.isEmpty()){
			builder.append(stack.pop());
		}
		return builder.toString();
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var token: tokens){
			builder.append(token);
		}
		return builder.toString();
	}

	public int depth(){
		int sum = 0;
		for(char c: tokens.get(0).value.toCharArray()){
			switch(c){
				case '\t':{
					sum += TAB_WIDTH - (sum % TAB_WIDTH);
					break;
				}
				case ' ':{
					sum += 1;
					break;
				}
				default:{
					//TODO: Figure out if this happens, and if so what characters cause it
				}
			}
		}
		return sum/TAB_WIDTH;
	}
}
