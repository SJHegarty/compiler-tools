package localgoat.lang.compiler.brutish;

import localgoat.lang.compiler.LineTokeniser;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.TokenString;
import localgoat.util.ESupplier;
import localgoat.util.functional.CharPredicate;

import java.util.*;

public class Brutish{

	public static final String WHITE_SPACE = "white-space";
	public static final String IGNORED = "ignored";

	public static final TokenString LINE_FEED_TOKEN = new TokenString(
		Collections.singleton(
			new Type(
				"line-feed",
				new HashSet<>(Arrays.asList(WHITE_SPACE, IGNORED))
			)
		),
		Collections.singletonList(
			new Symbol('\n')
		)
	);
	public static final String COMMENT = "comment";
	public static final String LINE_FEED = "line-feed";
	public static final String LINE_COMMENT ="line-comment";
	public static final String CLASS_NAME = "class-name";
	public static final String CONSTANT = "constant";
	public static final String FUNCTION_IDENTIFIER = "function-identifier";
	public static final String IDENTIFIER = "identifier";
	public static final String SYMBOL = "symbol";
	public static final String STRING = "string";
	public static final String DECIMAL = "decimal";
	public static final String HEXADECIMAL = "hexadecimal";
	public static final String KEY_WORD = "key-word";
	public static final String CONTEXT_IDENTIFIER = "context-identifier";
	public static final String LINE_CONTINUATION = "line-continuation";
	public static final String MATCHED = "matched";
	public static final String STATEMENT_TERMINATOR = "statement-terminator";
	public static final String CONTINUING_BRACKET = "}&";
	public static final String OPENING_SQUARE = "[";
	public static final String CLOSING_SQUARE = "]";
	public static final String OPENING_BRACKET = "{";
	public static final String CLOSING_BRACKET = "}";
	public static final String OPENING_PARENTHESIS = "(";
	public static final String CLOSING_PARENTHESIS = ")";

	private static final Converter CONVERTER = new Converter();
	private static final Map<String, String> EXPRESSIONS = new TreeMap<>();
	public static final Automaton AUTOMATON;

	static{
		configureClasses();
		configureSubstitutions();
		configureExpressions();

		final var builder = new StringBuilder();
		builder.append("+(\n\t");

		ESupplier.from(EXPRESSIONS.entrySet())
			.map(
				e -> String.format(
					"@<%s>(%s)",
					e.getKey(),
					e.getValue()
				)
			)
			.interleave(",\n\t")
			.forEach(s -> builder.append(s));

		builder.append("\n)");
		AUTOMATON = CONVERTER.build(builder.toString());
	}

	private static void configureClasses(){
		CONVERTER.addClass('u', CharPredicate.range('A', 'Z'));
		CONVERTER.addClass('l', CharPredicate.range('a', 'z'));
		CONVERTER.addClass('w', c -> c == ' ' || c == '\t');
		CONVERTER.addClass('d', CharPredicate.range('0', '9'));
		CONVERTER.addClass(
			'x',
			CharPredicate.or(
				CharPredicate.range('0', '9'),
				CharPredicate.range('a', 'f'),
				CharPredicate.range('A', 'F')
			)
		);
		CONVERTER.addClass('q', c -> c == '\"');
		CONVERTER.addClass('e', c -> c == '\\');
		CONVERTER.addClass('p', c -> c == ' ');
	}

	private static void configureSubstitutions(){
		CONVERTER.addSubstitution('K', "*<1+>l*('-'*<1+>l)");
		CONVERTER.addSubstitution('F', "*<1+>l*(u*<1+>l)");
		CONVERTER.addSubstitution('I', "K");
	}

	private static void configureExpressions(){
		configureSymbols();
		configureMatched();
		EXPRESSIONS.put(
			String.format(
				WHITE_SPACE + " --%s --%s",
				IGNORED,
				WHITE_SPACE
			),
			"*<1+>w"
		);
		EXPRESSIONS.put(STATEMENT_TERMINATOR, "';'");
		EXPRESSIONS.put(CLASS_NAME, "*<1+>(u*l)");
		EXPRESSIONS.put(CONSTANT, "'@'u*<1+>+(u, d)*('_'*<1+>+(u, d))");
		EXPRESSIONS.put(IDENTIFIER, "+(I, F)");
		EXPRESSIONS.put(CONTEXT_IDENTIFIER, "'@'I");
		EXPRESSIONS.put(LINE_CONTINUATION, "'::'");
		EXPRESSIONS.put(STRING, "q*+(~q, eq)q");
		EXPRESSIONS.put(DECIMAL, "*<1+>d");
		EXPRESSIONS.put(HEXADECIMAL, "'0x'*<1+>x");
		EXPRESSIONS.put(
			String.format(
				"%s --%s",
				LINE_COMMENT,
				COMMENT
			),
			"'//'*~('\r', '\n')"
		);
		EXPRESSIONS.put(
			LINE_FEED + " --" + IGNORED,
			"?'\r''\n'"
		);
		EXPRESSIONS.put(KEY_WORD, "'$'+(K, '['K*<1+>(pK)']')");
	}

	private static void configureMatched(){
		configureOr(
			MATCHED,
			new String[]{
				OPENING_SQUARE,
				CLOSING_SQUARE,
				OPENING_BRACKET,
				CLOSING_BRACKET,
				OPENING_PARENTHESIS,
				CLOSING_PARENTHESIS,
				CONTINUING_BRACKET
			}
		);
	}

	private static void configureSymbols(){
		configureOr(
			SYMBOL,
			new String[]{
				".",
				"..",
				"...",
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
			}
		);
	}

	private static void configureOr(String key, String[] values){
		EXPRESSIONS.put(
			key,
			String.format(
				"+(%s)",
				ESupplier.from(values)
					.map(s -> String.format("\'%s\'", s))
					.interleave(",")
					.toStream()
					.reduce((s0, s1) -> s0 + s1).get()
			)
		);
	}
}
