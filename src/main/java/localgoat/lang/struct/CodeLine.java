package localgoat.lang.struct;

import localgoat.util.functional.CharPredicate;
import localgoat.util.io.CharSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CodeLine{

	/*private static final Pattern LINE_PATTERN;// = Pattern.compile();

	static{
		final var builder = new StringBuilder();
		builder.append("(?<tabs>\\t*)");
		builder.append("(?<prefix>\\s*)");
		builder.append("(?<content>[^\\s]*(.*[^\\s]+)*)");
		builder.append("(?<suffix>\\s*)");
		LINE_PATTERN = Pattern.compile(builder.toString());
	}*/

	public static final int TAB_WIDTH = 4;

	private static final char KEY_HEAD = '$';
	private static final String KEY_HEAD_STRING = KEY_HEAD + "";
	private static final Token HANGING_KEY_HEAD = new Token(KEY_HEAD_STRING, TokenType.HANGING);

	private static final Set<String> SYMBOLS;
	private static final Set<String> SYMBOL_PREFIXES;
	private static final boolean SYMBOL_CHARS[];
	private static final int MAX_SYMBOL_LENGTH;

	static{
		SYMBOLS = new HashSet<>();
		SYMBOLS.addAll(
			Arrays.asList(
				"^",
				"~",
				"!",
				"-",
				"+",
				":",
				"$",
				"{",
				"}",
				"::",
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
	}
	private static final CharPredicate LOWER = c -> 'a' <= c && c <= 'z';
	private static final CharPredicate UPPER = c -> 'A' <= c && c <= 'Z';
	private static final CharPredicate NUMERICAL = c -> '0' <= c && c <= '9';
	private static final CharPredicate SYMBOLIC = c -> (c < SYMBOL_CHARS.length) && SYMBOL_CHARS[c];

	private static Token extractIndent(CharSource source){
		return new Token(extractWhitespace(source).value, TokenType.INDENT);
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
		if(source.read() != KEY_HEAD){
			exceptInvalid();
		}
		return new Token(KEY_HEAD_STRING + extractSymbol(source), TokenType.SYMBOL);
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

	private static final BiFunction<CharSource, StringBuilder, Token> GARBAGE_EXTRACTOR = (source, builder) -> {
		for(;;){
			final char value = source.peek();
			if(value == CharSource.STREAM_END || Character.isWhitespace(value)){
				break;
			}
			builder.append(value);
			source.read();
		}
		return new Token(builder.toString(), TokenType.HANGING);
	};



	/*private static final Function<CharSource, Token> KEY_EXTRACTOR = (source) -> {

	}*/






	private static final BiConsumer<CharSource, StringBuilder> KEYWORD_EXTRACTOR = (source, builder) -> {
		if(source.read() != KEY_HEAD){
			throw new IllegalArgumentException();
		}
		builder.append(KEY_HEAD);
		//IDENTIFIER_EXTRACTOR.accept(source, builder);
	};

	private static String extract(CharSource source, BiConsumer<CharSource, StringBuilder> extractor){
		return extract(source, extractor, "");
	}

	private static String extract(CharSource source, BiConsumer<CharSource, StringBuilder> extractor, String prefix){
		final var builder = new StringBuilder().append(prefix);
		extractor.accept(source, builder);
		return builder.toString();
	}

	public final int lineindex;
	public final List<Token> tokens;
	/*public final int tabcount;
	private final String prefix;
	private final String content;
	private final String suffix;*/

	CodeLine(String line, int index){
		this.lineindex = index;
		final var tokens = new ArrayList<Token>();
		this.tokens = Collections.unmodifiableList(tokens);

		try(var source = new CharSource(line)){
			tokens.add(extractIndent(source));

			for(;;){
				final char[] chars = source.peek(2);
				if(chars.length == 0){
					break;
				}
				if(chars[0] == KEY_HEAD){
					if(chars.length == 2){
						if(LOWER.test(chars[1])){
							tokens.add(extractKeyword(source));
							continue;
						}
						if(SYMBOLIC.test(chars[1])){
							tokens.add(extractKeysymbol(source));
						}
					}
					source.read();
					tokens.add(HANGING_KEY_HEAD);
					continue;
				}
				if(LOWER.test(chars[0])){
					tokens.add(extractIdentifier(source));
					continue;
				}
				if(SYMBOLIC.test(chars[0])){
					tokens.add(extractSymbol(source));
					continue;
				}
				if(Character.isWhitespace(chars[0])){
					tokens.add(extractWhitespace(source));
					continue;
				}
				tokens.add(
					new Token(
						new String(source.read(1)),
						TokenType.HANGING
					)
				);
			}
		}
	}

	public String content(){
		var deque = new ArrayDeque<>(tokens);
		deque.pollFirst();
		for(;!deque.isEmpty() && deque.peekFirst().type.ignored; deque.pollFirst());
		for(;!deque.isEmpty() && deque.peekLast().type.ignored; deque.pollLast());

		var builder = new StringBuilder();
		for(Token t: deque){
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

	public int tabcount(){
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
