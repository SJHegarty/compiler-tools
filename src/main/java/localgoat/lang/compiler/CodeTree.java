package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.Token;
import localgoat.lang.compiler.automata.TokenString;
import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

public class CodeTree{

	public static final String CONTRACTION_DELIMITOR = " :: ";

	enum BlockType{
		CLOSED,
		CONTINUATION,
		CONTINUED,
		NOTHING,
		BLANK,
		UNCLOSED,
		UNSOUND;
	}
	final BlockType type;
	public final LineTokeniser.CodeLine head;
	private final List<CodeTree> children;
	public final LineTokeniser.CodeLine tail;

	private CodeTree(){
		this.type = BlockType.CONTINUATION;
		this.head = null;
		this.tail = null;
		this.children = new ArrayList<>();
	}

	public CodeTree(Deque<LineTokeniser.CodeLine> lines){

		if(lines.isEmpty()){
			this.head = null;
			this.tail = null;
			this.type = BlockType.NOTHING;
			this.children = Collections.emptyList();
			return;
		}

		final var head = lines.poll();
		if(head.reconstruct().length() == 0){
			this.head = head;
			this.tail = null;
			this.type = BlockType.BLANK;
			this.children = Collections.emptyList();
			return;
		}

		this.children = new ArrayList<>();
		final int depth = head.depth();

			this.head = head;
			final Predicate<LineTokeniser.CodeLine> filter = line -> line.depth() > depth || line.reconstruct().length() == 0;
			while(lines.size() != 0 && filter.test(lines.peek())){
				children.add(new CodeTree(lines));
			}
			final TokenString<Token<Character>> headEnd = head.last(t -> !t.classes().contains(ContentTree.WHITE_SPACE));
			final String OPENING_BRACKET = "{";
			final String CLOSING_BRACKET = "}";
			final String CONTINUING_BRACKET = "}&";
			if(headEnd != null && headEnd.value().equals(OPENING_BRACKET)){
				final var line = lines.peek();
				handled:{
					unhandled:{
						if(line == null || line.depth() != depth){
							break unhandled;
						}
						final TokenString<Token<Character>> lineEnd = line.last(t -> !t.classes().contains(ContentTree.WHITE_SPACE));
						if(lineEnd == null){
							break unhandled;
						}
						switch(lineEnd.value()){
							case CONTINUING_BRACKET:{
								this.type = BlockType.CONTINUED;
								break;
							}
							case CLOSING_BRACKET:{
								this.type = BlockType.CLOSED;
								break;
							}
							default:{
								break unhandled;
							}
						}
						lines.poll();
						this.tail = line;
						break handled;
					}
					this.tail = null;
					this.type = BlockType.UNSOUND;
				}
			}
			else{
				this.tail = null;
				this.type = BlockType.UNCLOSED;
			}

			for(int i = 0; i < children.size(); i++){
				var child = children.get(i);
				if(child.type == BlockType.CONTINUED){
					final var continuation = new ArrayList<CodeTree>();
					continuation.add(child);
					for(final int next = i + 1; next < children.size();){
						child = children.remove(next);
						continuation.add(child);
						if(child.type != BlockType.CONTINUED){
							break;
						}
					}
					final var collapsed = new CodeTree();
					collapsed.children.addAll(continuation);
					children.set(i, collapsed);
				}
			}
		//}
	}

	static ESupplier<TokenString<Token<Character>>> tokenise(Iterable<CodeTree> trees){
		return ESupplier.from(trees)
			.map(child -> child.tokens())
			.interleave(() -> ESupplier.of(LineTokeniser.LINE_FEED))
			.flatMap(supplier -> supplier);
	}

	public ESupplier<TokenString<Token<Character>>> tokens(){
		var sources = new ArrayList<ESupplier<TokenString<Token<Character>>>>();
		if(head != null){
			sources.add(ESupplier.from(head.tokens));
		}
		if(!children.isEmpty()){
			sources.add(tokenise(children));
		}
		if(tail != null){
			sources.add(ESupplier.from(tail.tokens));
		}
		return ESupplier.from(sources)
			.interleave(() -> ESupplier.of(LineTokeniser.LINE_FEED))
			.flatMap(supplier -> supplier);
	};

	public List<CodeTree> children(){
		return Collections.unmodifiableList(children);
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var t: tokens()){
			builder.append(t.value());
		}
		return builder.toString();
	}

}
