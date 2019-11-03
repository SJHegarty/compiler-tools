package localgoat.lang.struct.handlers;

import localgoat.lang.struct.Token;
import localgoat.lang.struct.TokenType;
import localgoat.util.functional.CharPredicate;
import localgoat.util.io.CharSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SymbolHandler implements Handler{

	public static final String CONTINUING_BRACKET = "}&";
	public static final String LINE_CONTINUATION = "::";
	public static final String OPENING_BRACKET = "{";
	public static final String CLOSING_BRACKET = "}";

	private static final Set<String> SYMBOLS;
	private static final Set<String> SYMBOL_PREFIXES;
	private static final boolean SYMBOL_CHARS[];
	private static final int MAX_SYMBOL_LENGTH;
	static final CharPredicate SYMBOLIC;

	static final Handler INSTANCE;

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
				OPENING_BRACKET,
				CLOSING_BRACKET,
				CONTINUING_BRACKET,
				LINE_CONTINUATION,
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

		SYMBOLIC = c -> (c < SYMBOL_CHARS.length) && SYMBOL_CHARS[c];
		INSTANCE = new SymbolHandler();
	}
	@Override
	public boolean handles(char head){
		return SYMBOLIC.test(head);
	}

	@Override
	public Token extract(CharSource source){
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
}
