package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.data.TokenTree;
import localgoat.lang.compiler.automata.structure.DFA;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.data.TokenString;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.brutish.Brutish;
import localgoat.util.ESupplier;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContentTree implements TokenTree{

	private static final LineTokeniser TOKENISER;
	public static final Set<Type> CLASSES;

	static{
		final var dfa = Brutish.DFA;
		CLASSES = Collections.unmodifiableSet(dfa.types());
		TOKENISER = new LineTokeniser(dfa);
	}

	private static DFA buildTestDFA(){
		final var converter = new Converter();
		{
			final var dfa = converter.buildDFA("!(ab)");
			final boolean accepts = dfa.accepts(Token.from("abb"));
			System.err.println(accepts);
		}
		converter.addSubstitution('A', "@<child>(*<1+>+(a, b))");
		final var rv = converter.buildDFA("@<test-case>('['A*(' 'A)']')");
		return rv;
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

	@Override
	public Token head(){
		return null;
	}

	@Override
	public ESupplier<TokenString> tokens(){
		return CodeTree.tokenise(trees);
	}

	@Override
	public List<CodeTree> children(){
		return Collections.unmodifiableList(this.trees);
	}

	@Override
	public Token tail(){
		return null;
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var t: tokens()){
			builder.append(t.value());
		}
		return builder.toString();
	}

}
