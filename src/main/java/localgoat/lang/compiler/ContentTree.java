package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.Token;
import localgoat.lang.compiler.automata.TokenString;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.util.ESupplier;
import localgoat.util.functional.CharPredicate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContentTree{

	private final List<CodeTree> trees;
	private static final LineTokeniser TOKENISER;
	public static final String LINE_COMMENT ="line-comment";
	public static final String CLASS_NAME = "class-name";
	public static final String CONSTANT = "constant";
	public static final String IDENTIFIER = "identifier";
	public static final String SYMBOL = "symbol";
	public static final String STRING = "string";
	public static final String DECIMAL = "decimal";
	public static final String HEXADECIMAL = "hexadecimal";
	public static final String KEY_WORD = "key-word";
	public static final String CONTEXT_IDENTIFIER = "context-identifier";

	public static final String CONTINUING_BRACKET = "}&";
	public static final String LINE_CONTINUATION = "::";
	public static final String OPENING_SQUARE = "[";
	public static final String CLOSING_SQUARE = "]";
	public static final String OPENING_BRACKET = "{";
	public static final String CLOSING_BRACKET = "}";
	public static final String OPENING_PARENTHESIS = "(";
	public static final String CLOSING_PARENTHESIS = ")";
	public static final String STATEMENT_TERMINATOR = ";";

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
		converter.addClass('p', c -> c == ' ');

		converter.addSubstitution('I', "*<1+>l*(u*<1+>l)");
		converter.addSubstitution('K', "*<1+>l*(h*<1+>l)");
		final var expressions = new HashMap<>();

		expressions.put(
			String.format(
				LineTokeniser.WHITE_SPACE + " --%s --%s",
				LineTokeniser.IGNORED,
				LineTokeniser.WHITE_SPACE
			),
			"*<1+>w"
		);
		expressions.put(CLASS_NAME, "*<1+>(u*l)");
		expressions.put(CONSTANT, "'@'*<1+>u*(s*<1+>u)");
		expressions.put(IDENTIFIER, "I");
		expressions.put(CONTEXT_IDENTIFIER, "'@'I");
		expressions.put(
			SYMBOL,
			String.format(
				"+(%s)",
				ESupplier.from(
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
					OPENING_SQUARE,
					CLOSING_SQUARE,
					OPENING_BRACKET,
					CLOSING_BRACKET,
					OPENING_PARENTHESIS,
					CLOSING_PARENTHESIS,
					STATEMENT_TERMINATOR,
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
					.map(s -> String.format("\'%s\'", s))
					.interleave(",")
					.toStream()
					.reduce((s0, s1) -> s0 + s1).get()
			)

		);

		expressions.put(STRING, "q*+(!q, eq)q");
		expressions.put(DECIMAL, "*<1+>d");
		expressions.put(HEXADECIMAL, "'0x'*<1+>x");
		expressions.put(
			String.format(
				LINE_COMMENT + " --%s",
				LineTokeniser.IGNORED
			),
			"'//'*."
		);
		expressions.put(KEY_WORD, "'$'+(K, '['K*<1+>(pK)']')");
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

		TOKENISER = new LineTokeniser(converter.buildDFA(builder.toString()));
	}

	public ContentTree(String text){
		final String lines[] = text.split("\r?\n", -1);

		long time = System.currentTimeMillis();

		final var queue = IntStream.range(0, lines.length)
			.parallel()
			.mapToObj(index -> TOKENISER.new CodeLine(lines[index], index))
			.collect(Collectors.toCollection(ArrayDeque::new));

		time = System.currentTimeMillis() - time;

//		System.err.println(new Exception().getStackTrace()[0] + " " + time);
		var trees = new ArrayList<CodeTree>();
		this.trees = Collections.unmodifiableList(trees);
		while(!queue.isEmpty()){
			trees.add(new CodeTree(queue));
		}
	}

	public List<CodeTree> getCode(){
		return trees;
	}

	public ESupplier<TokenString<Token<Character>>> tokens(){
		return CodeTree.tokenise(trees);
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var t: tokens()){
			builder.append(t.value());
		}
		return builder.toString();
	}

}
