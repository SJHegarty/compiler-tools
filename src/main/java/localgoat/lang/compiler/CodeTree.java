package localgoat.lang.compiler;

import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenString;
import localgoat.lang.compiler.token.TokenTree;
import localgoat.lang.compiler.omega.Omega;
import localgoat.util.ESupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class  CodeTree implements TokenTree{


	static ESupplier<TokenString> tokenise(Iterable<CodeTree> trees){
		return ESupplier.from(trees)
			.map(child -> child.tokens())
			.interleave(() -> ESupplier.of(IndentParser.LINE_FEED_TOKEN))
			.flatMap(supplier -> supplier);
	}

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
	public final CodeLine head;
	private final List<CodeTree> children;
	public final CodeLine tail;

	private CodeTree(){
		this.type = BlockType.CONTINUATION;
		this.head = null;
		this.tail = null;
		this.children = new ArrayList<>();
	}

	public CodeTree(Deque<CodeLine> lines){

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
			final Predicate<CodeLine> filter = line -> line.depth() > depth || line.reconstruct().length() == 0;
			while(lines.size() != 0 && filter.test(lines.peek())){
				children.add(new CodeTree(lines));
			}
			final Predicate<TokenString> stringPredicate = t -> !t.hasClass(sc -> sc.hasFlag(IndentParser.IGNORED));
			final TokenString headEnd = head.last(stringPredicate);
			if(headEnd != null && headEnd.value().equals(Omega.OPENING_BRACKET)){
				final var line = lines.peek();
				handled:{
					unhandled:{
						if(line == null || line.depth() != depth){
							break unhandled;
						}
						final TokenString lineEnd = line.last(stringPredicate);
						if(lineEnd == null){
							break unhandled;
						}
						switch(lineEnd.value()){
							case Omega.CONTINUING_BRACKET:{
								this.type = BlockType.CONTINUED;
								break;
							}
							case Omega.CLOSING_BRACKET:{
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

	private CodeTree(BlockType type, CodeLine head, List<CodeTree> children, CodeLine tail){
		this.type = type;
		this.head = head;
		this.children = children;
		this.tail = tail;
	}

	public ESupplier<TokenString> tokens(){
		var sources = new ArrayList<ESupplier<TokenString>>();
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
			.interleave(() -> ESupplier.of(IndentParser.LINE_FEED_TOKEN))
			.flatMap(supplier -> supplier);
	};

	@Override
	public Token head(){
		return head;
	}

	public List<CodeTree> children(){
		return Collections.unmodifiableList(children);
	}

	@Override
	public Token tail(){
		return tail;
	}

	@Override
	public Token trim(){
		throw new UnsupportedOperationException();
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var t: tokens()){
			builder.append(t.value());
		}
		return builder.toString();
	}

	public CodeTree effective(){
		final var result = new CodeTree(
			type,
			(head == null) ? null : head.effective(),
			ESupplier.from(children)
				.map(c -> c.effective())
				.toStream()
				.collect(Collectors.toList()),
			(tail == null) ? null : tail.effective()
		);
		if(result.head == null && result.children.isEmpty() && result.tail == null){
			return null;
		}
		return result;
	}
}
