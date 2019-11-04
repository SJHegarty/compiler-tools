package localgoat.lang.compiler;

import localgoat.lang.compiler.handlers.SymbolHandler;
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

		//final int splitIndex;
		/*block:{
			final var tokens = head.contentTokens();
			for(int i = 0; i < tokens.size(); i++){
				final var t = tokens.get(i);
				if(t.type == TokenType.SYMBOL && t.value.equals(SymbolHandler.LINE_CONTINUATION)){
					splitIndex = i;
					break block;
				}
			}
			splitIndex = -1;
		}*/
		/*if(splitIndex != -1){
			final var tokens = head.contentTokens();
			final int index = head.lineindex;
			{
				final var deque = new ArrayDeque<Token>();
				IntStream.range(0, splitIndex - 1).forEach(
					i -> deque.add(tokens.get(i))
				);
				while(!deque.isEmpty() && deque.peekLast().type.ignored){
					deque.pollLast();
				}
				var builder = new StringBuilder().append(head.prefix());
				deque.forEach(token -> builder.append(token));
				this.head = new CodeLine(builder.toString(), index);
			}
			{
				final var queue = new ArrayDeque<Token>();
				IntStream.range(splitIndex + 1, tokens.size()).forEach(
					i -> queue.add(tokens.get(i))
				);
				while(!queue.isEmpty() && queue.peekFirst().type.ignored){
					queue.pollFirst();
				}
				var builder = new StringBuilder().append(head.prefix());
				queue.forEach(token -> builder.append(token));
				lines.push(new CodeLine(builder.toString(), index));
			}
			children.add(new CodeTree(lines));
			//TODO: if children.get(0).type == CONTINUED add validation error - unsupported confusing syntax
			this.tail = null;
			this.type = BlockType.UNCLOSED;
		}*/
		//else{
			this.head = head;
			final Predicate<CodeLine> filter = line -> line.depth() > depth || line.reconstruct().length() == 0;
			while(lines.size() != 0 && filter.test(lines.peek())){
				children.add(new CodeTree(lines));
			}
			final Token headEnd = head.last(t -> !t.ignored());
			if(headEnd != null && headEnd.value.equals(SymbolHandler.OPENING_BRACKET)){
				final var line = lines.peek();
				handled:{
					unhandled:{
						if(line == null || line.depth() != depth){
							break unhandled;
						}
						final Token lineEnd = line.last(t -> !t.ignored());
						if(lineEnd == null){
							break unhandled;
						}
						switch(lineEnd.value){
							case SymbolHandler.CONTINUING_BRACKET:{
								this.type = BlockType.CONTINUED;
								break;
							}
							case SymbolHandler.CLOSING_BRACKET:{
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

	static ESupplier<Token> tokenise(Iterable<CodeTree> trees){
		return ESupplier.from(trees)
			.map(child -> child.tokens())
			.interlace(() -> ESupplier.of(Token.LINE_FEED))
			.flatMap(supplier -> supplier);
	}

	public ESupplier<Token> tokens(){
		var sources = new ArrayList<ESupplier<Token>>();
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
			.interlace(() -> ESupplier.of(Token.LINE_FEED))
			.flatMap(supplier -> supplier);
	};

	public List<CodeTree> children(){
		return Collections.unmodifiableList(children);
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var t: tokens()){
			builder.append(t);
		}
		return builder.toString();
	}

}
