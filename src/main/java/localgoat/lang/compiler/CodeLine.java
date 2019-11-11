package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.DFA;
import localgoat.lang.compiler.automata.TokenA;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.util.ESupplier;
import localgoat.util.io.CharSource;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CodeLine{

	static final DFA<TokenA<Character>> DFA;
	static final String WHITE_SPACE = "white-space";
	static final String CLASS_NAME = "class-name";
	static final String CONSTANT = "constant";
	static final String IDENTIFIER = "identifier";

	static{
		final var converter = new Converter();
		converter.addClass('u', c -> 'A' <= c && c <= 'Z');
		converter.addClass('l', c -> 'a' <= c && c <= 'z');
		converter.addClass('w', c -> c == ' ' || c == '\t');
		converter.addClass('h', c -> c == '-');
		converter.addClass('s', c -> c == '_');

		final var expressions = new HashMap<>();
		expressions.put(WHITE_SPACE, "*<1+>w");
		expressions.put(CLASS_NAME, "*<1+>(u*l)");
		expressions.put(CONSTANT, "*<1+>u*(s*<1+>u)");
		expressions.put(IDENTIFIER, "*<1+>l*(h*<1+>l)");

		final var builder = new StringBuilder();
		builder.append("+(");

		ESupplier.from(expressions.entrySet())
			.map(
				e -> String.format(
					"@<%s>(%s)",
					e.getKey(),
					e.getValue()
				)
			)
			.interleave(", ")
			.forEach(s -> builder.append(s));

		builder.append(")");

		DFA = converter.buildDFA(builder.toString());
	}
	public static final int TAB_WIDTH = 4;
	public final int lineindex;
	public final List<Token> tokens;

	CodeLine(String line, int index){
		this.lineindex = index;
		final var tokens = new ArrayList<Token>();
		this.tokens = Collections.unmodifiableList(tokens);

		DFA.tokenise(TokenA.from(line))
			.map(
				tokena -> {
					final TokenType type;
					outer:{
						var classes = tokena.classes();
						switch(classes.size()){
							case 0:{
								type = TokenType.UNHANDLED;
								break;
							}
							case 1:{
								switch(classes.iterator().next()){
									case WHITE_SPACE:{
										type = TokenType.WHITESPACE;
										break outer;
									}
									case CLASS_NAME:{
										type = TokenType.TYPE;
										break outer;
									}
									case CONSTANT:{
										type = TokenType.CONST;
										break outer;
									}
									case IDENTIFIER:{
										type = TokenType.IDENTIFIER;
										break outer;
									}
									default:{
										type = TokenType.UNHANDLED;
										break outer;
									}
								}
							}
							default:{
								type = TokenType.AMBIGUOUS;
								break;
							}
						}
					}
					return new Token(tokena.value(), type);
				}
			)
			.forEach(token -> tokens.add(token));

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
		handler:{
			if(tokens.size() == 0){
				break handler;
			}
			final var token = tokens.get(0);
			if(token.type != TokenType.WHITESPACE){
				break handler;
			}
			return token.value;
		}
		return "";
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
		for(char c: prefix().toCharArray()){
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
