package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.DFA;
import localgoat.lang.compiler.automata.Token;
import localgoat.lang.compiler.automata.TokenString;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.util.ESupplier;
import localgoat.util.functional.CharPredicate;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CodeLine{

	static final DFA<Token<Character>> DFA;
	public static final TokenString<Token<Character>> LINE_FEED = new TokenString<>(Collections.singleton("line-feed"), Collections.singletonList(Token.LINE_FEED));
	public static final String LINE_COMMENT = "line-comment";
	public static final String WHITE_SPACE = "white-space";
	public static final String CLASS_NAME = "class-name";
	public static final String CONSTANT = "constant";
	public static final String IDENTIFIER = "identifier";
	public static final String SYMBOL = "symbol";
	public static final String STRING = "string";
	public static final String DECIMAL = "decimal";
	public static final String HEXADECIMAL = "hexadecimal";
	public static final String KEY_WORD = "key-word";

	static{
		final var converter = new Converter();
		class CharRange implements CharPredicate{

			final char char0;
			final char charN;

			CharRange(char char0, char charN){
				this.char0 = char0;
				this.charN = charN;
			}
			@Override
			public boolean test(char c){
				return char0 <= c && c <= charN;
			}
		}

		converter.addClass('u', new CharRange('A', 'Z'));
		converter.addClass('l', new CharRange('a', 'z'));
		converter.addClass('w', c -> c == ' ' || c == '\t');
		converter.addClass('h', c -> c == '-');
		converter.addClass('s', c -> c == '_');
		converter.addClass(
			't',
			CharPredicate.or(
				c -> (c == '!'),
				new CharRange('#', '\''),
				new CharRange('*', '/'),
				new CharRange(':', '@'),
				c -> "\\^_`|~".indexOf(c) != -1
			)
		);
		converter.addClass('d', new CharRange('0', '9'));
		converter.addClass(
			'x',
			CharPredicate.or(
				new CharRange('0', '9'),
				new CharRange('a', 'f'),
				new CharRange('A', 'F')
			)
		);
		converter.addClass('q', c -> c == '\"');
		converter.addClass('e', c -> c == '\\');
		final var expressions = new HashMap<>();

		expressions.put(WHITE_SPACE, "*<1+>w");
		expressions.put(CLASS_NAME, "*<1+>(u*l)");
		expressions.put(CONSTANT, "*<1+>u*(s*<1+>u)");
		expressions.put(IDENTIFIER, "*<1+>l*(u*<1+>l)");
		expressions.put(SYMBOL, "*<1+>t");
		expressions.put(STRING, "q*+(!q, eq)q");
		expressions.put(DECIMAL, "*<1+>d");
		expressions.put(HEXADECIMAL, "'0x'*<1+>x");
		expressions.put(LINE_COMMENT, "'//'*.");
		expressions.put(KEY_WORD, "'$'*<1+>l*(h*<1+>l)");
	//	expressions.put()


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
	public final List<TokenString<Token<Character>>> tokens;

	CodeLine(String line, int index){
		this.lineindex = index;
		final var tokens = new ArrayList<Token<Character>>();
		//this.tokens = Collections.unmodifiableList(tokens);

		this.tokens = Arrays.asList(DFA.tokenise(Token.from(line)).toStream().toArray(TokenString[]::new));

	}

	public List<TokenString<Token<Character>>> contentTokens(){
		var deque = new ArrayDeque<>(tokens);
		deque.pollFirst();
		for(;!deque.isEmpty() && deque.peekFirst().classes().contains(WHITE_SPACE); deque.pollFirst());
		for(;!deque.isEmpty() && deque.peekLast().classes().contains(WHITE_SPACE); deque.pollLast());
		return Collections.unmodifiableList(
			new ArrayList<>(deque)
		);
	}

	public TokenString<Token<Character>> last(Predicate<TokenString<Token<Character>>> filter){
		for(int i = tokens.size() - 1; i >= 0; i--){
			var token = tokens.get(i);
			if(filter.test(token)){
				return token;
			}
		}
		return null;
	}

	public TokenString<Token<Character>> first(Predicate<TokenString<Token<Character>>> filter){
		for(var token: tokens){
			if(filter.test(token)){
				return token;
			}
		}
		return null;
	}

	public List<TokenString<Token<Character>>> all(Predicate<TokenString<Token<Character>>> filter){
		return Collections.unmodifiableList(
			tokens.stream()
				.filter(filter)
				.collect(Collectors.toList())
		);
	}

	public String content(){
		var builder = new StringBuilder();
		for(TokenString<Token<Character>> t: contentTokens()){
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
			if(!token.classes().contains(WHITE_SPACE)){
				break handler;
			}
			return token.value();
		}
		return "";
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var token: tokens){
			builder.append(token.value());
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
