package localgoat.lang.struct;

import localgoat.util.functional.CharPredicate;
import localgoat.util.io.CharSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CodeLine{

	public static final int TAB_WIDTH = 4;

	private static final CharPredicate LOWER;
	private static final CharPredicate UPPER;
	private static final CharPredicate NUMERICAL;
	private static final CharPredicate SYMBOLIC;

	private static final char STRING_HEAD = '\"';
	private static final char KEY_HEAD = '$';
	private static final String KEY_HEAD_STRING = KEY_HEAD + "";
	private static final Token HANGING_KEY_HEAD = new Token(KEY_HEAD_STRING, TokenType.HANGING);

	private static final Handler[] HANDLERS;
	private static final Set<String> SYMBOLS;
	private static final Set<String> SYMBOL_PREFIXES;
	private static final boolean SYMBOL_CHARS[];
	private static final int MAX_SYMBOL_LENGTH;

	public static final String CONTINUATION_SYMBOL = "::";
	static{
		SYMBOLS = new HashSet<>();
		SYMBOLS.addAll(
			Arrays.asList(
				"^",
				"~",
				"!",
				"&",
				"|",
				"-",
				"+",
				"/",
				"*",
				":",
				"{",
				"}",
				"}&",
				CONTINUATION_SYMBOL,
				"->",
				"!=",
				"?=",
				"<=",
				">=",
				">",
				"<",
				"--",
				"++",
				"-=",
				"+="
			)
		);

		MAX_SYMBOL_LENGTH = SYMBOLS.stream()
			.mapToInt(s -> s.length())
			.max().getAsInt();

		SYMBOL_PREFIXES = new HashSet<>();
		SYMBOL_CHARS = new boolean[0xff];
		for(String symbol: SYMBOLS){
			for(int i = 0; i < symbol.length(); i++){
				SYMBOL_CHARS[symbol.charAt(i)] = true;
				SYMBOL_PREFIXES.add(symbol.substring(0, i + 1));
			}
		}

		LOWER = c -> 'a' <= c && c <= 'z';
		UPPER = c -> 'A' <= c && c <= 'Z';
		NUMERICAL = c -> '0' <= c && c <= '9';
		SYMBOLIC = c -> (c < SYMBOL_CHARS.length) && SYMBOL_CHARS[c];

		HANDLERS = new Handler[]{
			new KeyHandler(),
			new IdentifierHandler(),
			new SymbolHandler(),
			new WhitespaceHandler(),
			new StringHandler(),
		};
		@SuppressWarnings("unchecked")
		Map.Entry<Character, Handler[]>[] collisions = (Map.Entry<Character, Handler[]>[])IntStream.range(0, 0xff)
			.mapToObj(i -> (char)i)
			.collect(
				Collectors.toMap(
					character -> character,
					character -> {
						char c = character;
						return Stream.of(HANDLERS)
							.filter(handler -> handler.handles(c))
							.toArray(Handler[]::new);
					},
					(v0, v1) -> {
						throw new IllegalStateException();
					},
					LinkedHashMap::new
				)
			)
			.entrySet().stream()
			.filter(e -> e.getValue().length > 1)
			.toArray(Map.Entry[]::new);

		if(collisions.length != 0){
			var builder = new StringBuilder();
			for(var collision: collisions){
				builder
					.append(Handler.class.getSimpleName())
					.append(" collision for character \'")
					.append(collision.getKey())
					.append("\'.");

				builder.append("\n{");
				for(var handler: collision.getValue()){
					builder.append("\n\t").append(handler.getClass().getName());
				}
				builder.append("\n}");
			}
			throw new IllegalStateException(builder.toString());
		}
	}

	private static Token extractWhitespace(CharSource source){
		final var builder = new StringBuilder();
		for(;;){
			final char c = source.peek();
			if(c == CharSource.STREAM_END){
				break;
			}
			else if(Character.isWhitespace(c)){
				builder.append(c);
				source.read();
			}
			else break;
		}
		return new Token(builder.toString(), TokenType.WHITESPACE);
	}

	private static void exceptInvalid(){
		throw new IllegalStateException("Should never be called with an invalid first character.");
	}

	private static Token extractKeysymbol(CharSource source){
		if(source.read() != '$'){
			exceptInvalid();
		}
		return new Token("$"  + extractSymbol(source), TokenType.KEYSYMBOL);
	}

	private static Token extractSymbol(CharSource source){
		final char[] chars = source.peek(MAX_SYMBOL_LENGTH);
		if(!SYMBOLIC.test(chars[0])){
			exceptInvalid();
		}
		int depth = 0;
		while(depth < chars.length){
			if(SYMBOL_PREFIXES.contains(new String(chars, 0, depth + 1))){
				depth++;
				continue;
			}
			break;
		}
		while(depth > 0 && !SYMBOLS.contains(new String(chars, 0, depth))){
			depth--;
		}
		if(depth == 0){
			return new Token(
				new String(source.read(1)),
				TokenType.HANGING
			);
		}
		source.read(depth);
		return new Token(
			new String(chars, 0, depth),
			TokenType.SYMBOL
		);
	}

	private static Token extractKeyword(CharSource source){
		if(source.read() != KEY_HEAD){
			exceptInvalid();
		}
		return new Token(KEY_HEAD_STRING + extractIdentifier(source), TokenType.KEYWORD);
	}

	private static Token extractIdentifier(CharSource source){
		return extractCompound(
			LOWER,
			c -> LOWER.test(c) || NUMERICAL.test(c),
			c -> c == '-',
			source,
			TokenType.IDENTIFIER
		);
	}

	private static Token extractType(CharSource source){
		return extractCompound(
			UPPER,
			c -> LOWER.test(c) || NUMERICAL.test(c),
			UPPER,
			source,
			TokenType.TYPE
		);
	}

	private static Token extractCompound(
		CharPredicate headChar,
		CharPredicate baseChar,
		CharPredicate combiner,
		CharSource source,
		TokenType type
	){
		final var builder = new StringBuilder();
		{
			final char c = source.read();
			builder.append(c);
			if(!headChar.test(c)){
				exceptInvalid();
			}
		}
		loop:for(;;){
			final char[] chars = source.peek(2);
			switch(chars.length){
				case 0:{
					break loop;
				}
				case 2:{
					if(combiner.test(chars[0])){
						if(baseChar.test(chars[1])){
							builder.append(chars);
							source.read(2);
							continue loop;
						}
						break loop;
					}
				}
				case 1:{
					if(baseChar.test(chars[0])){
						builder.append(chars[0]);
						source.read();
						continue loop;
					}
					break loop;
				}
			}
		}
		return new Token(builder.toString(), type);
	};

	public final int lineindex;
	public final List<Token> tokens;

	private interface Handler{
		boolean handles(char head);
		Token extract(CharSource source);
	}


	CodeLine(String line, int index){
		this.lineindex = index;
		final var tokens = new ArrayList<Token>();
		this.tokens = Collections.unmodifiableList(tokens);

		try(var source = new CharSource(line)){
			tokens.add(extractWhitespace(source));

			outer: for(;;){
				final char c = source.peek();
				if(c == CharSource.STREAM_END){
					break;
				}
				for(var handler: HANDLERS){
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

	private static class KeyHandler implements Handler{
		@Override
		public boolean handles(char head){
			return head == KEY_HEAD;
		}

		@Override
		public Token extract(CharSource source){
			final var chars = source.peek(2);
			if(chars.length == 2){
				if(LOWER.test(chars[1])){
					return extractKeyword(source);
				}
				if(SYMBOLIC.test(chars[1])){
					return extractKeysymbol(source);
				}
			}
			source.read();
			return HANGING_KEY_HEAD;
		}
	}

	private static class IdentifierHandler implements Handler{
		@Override
		public boolean handles(char head){
			return LOWER.test(head);
		}

		@Override
		public Token extract(CharSource source){
			return extractIdentifier(source);
		}
	}

	private static class SymbolHandler implements Handler{
		@Override
		public boolean handles(char head){
			return SYMBOLIC.test(head);
		}

		@Override
		public Token extract(CharSource source){
			return extractSymbol(source);
		}
	}

	private static class WhitespaceHandler implements Handler{
		@Override
		public boolean handles(char head){
			return Character.isWhitespace(head);
		}

		@Override
		public Token extract(CharSource source){
			return extractWhitespace(source);
		}
	}

	private static class StringHandler implements Handler{

		@Override
		public boolean handles(char head){
			return head == STRING_HEAD;
		}

		@Override
		public Token extract(CharSource source){
			if(source.read() != STRING_HEAD){
				exceptInvalid();
			}
			final var builder = new StringBuilder().append('\"');
			for(;;){
				char c = source.read();
				hanging:{
					switch(c){
						case CharSource.STREAM_END:{
							break;
						}
						case '\"':{
							return new Token(builder.append('\"').toString(), TokenType.STRING);
						}
						case '\\':{
							builder.append(c);
							final char escaped = source.read();
							if(escaped == CharSource.STREAM_END){
								break;
							}
							c = escaped;
						}
						default:{
							builder.append(c);
							break hanging;
						}
					}
					//TODO: make hanging a property of the Token, rather than a token type - this should be a hanging STRING token.
					return new Token(builder.toString(), TokenType.HANGING);
				}
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
					sum += TAB_WIDTH;
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
