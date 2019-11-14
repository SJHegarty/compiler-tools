package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.data.TokenString;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.util.ESupplier;
import localgoat.util.functional.CharPredicate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContentTree{

	private static final LineTokeniser TOKENISER;
	public static final Set<Type> CLASSES;

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
	public static final String LINE_CONTINUATION = "line-continuation";

	public static final String CONTINUING_BRACKET = "}&";
	public static final String OPENING_SQUARE = "[";
	public static final String CLOSING_SQUARE = "]";
	public static final String OPENING_BRACKET = "{";
	public static final String CLOSING_BRACKET = "}";
	public static final String OPENING_PARENTHESIS = "(";
	public static final String CLOSING_PARENTHESIS = ")";
	public static final String STATEMENT_TERMINATOR = ";";

	static{
		final var dfa = buildTestDFA();
		CLASSES = Collections.unmodifiableSet(dfa.types());
		TOKENISER = new LineTokeniser(dfa);
	}

	private static DFA<Token<Character>> buildTestDFA(){
		final var converter = new Converter();
		converter.addSubstitution('A', "@<child>(*<1+>+(a, b))");
		final var rv = converter.buildDFA("@<test-case>('['A*(' 'A)']')");
		rv.nodes().forEach(
			n -> {
				var states = n.typeStates();
				if(!states.isEmpty()){
					System.err.println(n.index());
					for(var s: states){
						System.err.println("\t" + s);
					}
					System.err.println();
				}
			}
		);
		for(var v: rv.tokenise(Token.from("[abba aba bbaba][ababb abb]"))){
			System.err.println(v);
		}
		return rv;
	}

	private static DFA<Token<Character>> buildDFA(){
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
		converter.addSubstitution('K', "*<1+>l*('-'*<1+>l)");
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
		expressions.put(CONSTANT, "'@'u*<1+>+(u, d)*('_'*<1+>+(u, d))");
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
		expressions.put(LINE_CONTINUATION, "'::'");
		expressions.put(STRING, "q*+(~q, eq)q");
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

		return converter.buildDFA(builder.toString());
	}

	private final List<CodeTree> trees;

	public ContentTree(String text){
		final String lines[] = text.split("\r?\n", -1);

		long time = System.currentTimeMillis();

		final var queue = IntStream.range(0, lines.length)
			.parallel()
			.mapToObj(index -> TOKENISER.tokenise(lines[index], index))
			.collect(Collectors.toCollection(ArrayDeque::new));

		time = System.currentTimeMillis() - time;

//		System.err.println(new Exception().getStackTrace()[0] + " " + time);
		var trees = new ArrayList<CodeTree>();
		this.trees = Collections.unmodifiableList(trees);
		while(!queue.isEmpty()){
			trees.add(new CodeTree(queue));
		}
	}

	private ContentTree(List<CodeTree> trees){
		this.trees = trees;
	}

	public ContentTree effective(){
		return new ContentTree(
			ESupplier.from(trees)
				.map(t -> t.effective())
				.toStream()
				.collect(Collectors.toList())
		);
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
