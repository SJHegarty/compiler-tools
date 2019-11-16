package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.data.TokenString;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CodeLine{
	public final int lineindex;
	public final List<TokenString> tokens;

	public CodeLine(int index, List<TokenString> tokens){
		this.lineindex = index;
		this.tokens = Collections.unmodifiableList(tokens);
	}

	public TokenString last(Predicate<TokenString> filter){
		for(int i = tokens.size() - 1; i >= 0; i--){
			var token = tokens.get(i);
			if(filter.test(token)){
				return token;
			}
		}
		return null;
	}

	public TokenString first(Predicate<TokenString> filter){
		for(var token : tokens){
			if(filter.test(token)){
				return token;
			}
		}
		return null;
	}

	public List<TokenString> all(Predicate<TokenString> filter){
		return Collections.unmodifiableList(
			tokens.stream()
				.filter(filter)
				.collect(Collectors.toList())
		);
	}

	public String prefix(){
		handler:
		{
			if(tokens.size() == 0){
				break handler;
			}
			final var token = tokens.get(0);
			if(!token.hasClass(s -> s.hasFlag(LineTokeniser.IGNORED))){
				break handler;
			}
			return token.value();
		}
		return "";
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var token : tokens){
			builder.append(token.value());
		}
		return builder.toString();
	}

	public int depth(){
		int sum = 0;
		for(char c : prefix().toCharArray()){
			switch(c){
				case '\t':{
					sum += LineTokeniser.TAB_WIDTH - (sum % LineTokeniser.TAB_WIDTH);
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
		return sum / LineTokeniser.TAB_WIDTH;
	}

	@Override
	public String toString(){
		final var builder = new StringBuilder();
		for(var t : tokens){
			builder.append(t);
		}
		return builder.toString();
	}

	public CodeLine effective(){
		if(tokens.isEmpty()){
			return null;
		}
		final var tokens = new ArrayList<TokenString>();
		tokens.addAll(this.tokens);
		while(tokens.get(tokens.size() - 1).hasClass(t -> t.hasFlag(LineTokeniser.IGNORED))){
			tokens.remove(tokens.size() - 1);
			if(tokens.isEmpty()){
				return null;
			}
		}
		return new CodeLine(lineindex, tokens);
	}
}
