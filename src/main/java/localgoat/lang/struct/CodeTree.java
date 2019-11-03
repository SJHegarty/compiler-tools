package localgoat.lang.struct;

import localgoat.lang.struct.handlers.SymbolHandler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.IntStream;

public class CodeTree{

	public static final String CONTRACTION_DELIMITOR = " :: ";

	enum BlockType{
		CLOSED,
		CONTINUATION,
		CONTINUED,
		NOTHING,
		UNCLOSED,
		UNSOUND;
	}
	final BlockType type;
	public final CodeLine head;
	public final CodeLine tail;
	private final List<CodeTree> children;

	private CodeTree(CodeLine head){
		this.type = BlockType.CONTINUATION;
		this.head = head;
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

		this.children = new ArrayList<>();
		final var head = lines.poll();
		final int depth = head.depth();

		final int splitIndex;
		block:{
			final var tokens = head.contentTokens();
			for(int i = 0; i < tokens.size(); i++){
				final var t = tokens.get(i);
				if(t.type == TokenType.SYMBOL && t.value.equals(SymbolHandler.LINE_CONTINUATION)){
					splitIndex = i;
					break block;
				}
			}
			splitIndex = -1;
		}
		if(splitIndex != -1){
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
		}
		else{
			this.head = head;

			while(lines.size() != 0 && lines.peek().depth() > depth){
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
					final var collapsed = new CodeTree(continuation.get(0).head);
					collapsed.children.addAll(continuation);
					children.set(i, collapsed);
				}
			}
		}
	}

	public List<CodeTree> children(){
		return Collections.unmodifiableList(children);
	}

	public String toString(){
		return head.reconstruct();
	}

	public String reconstruct(){
		final var lines = new ArrayList<String>();
		reconstruct(lines);
		final var builder = new StringBuilder();
		if(lines.size() != 0){
			builder.append(lines.get(0));
			IntStream.range(1, lines.size()).forEach(
				i -> builder.append(lines.get(i))
			);
		}
		return builder.toString();
	}

	public void effective(List<String> lines){
		effective(lines, head.depth());
	}

	private void effective(List<String> lines, int depth){
		switch(type){
			case CONTINUATION:{
				children.forEach(child -> child.effective(lines, depth));
				break;
			}
			case NOTHING:{
				break;
			}
			default:{
				var indent = new StringBuilder();

				for(int i = 0; i < depth; i++){
					indent.append('\t');
				}

				final boolean continuation = children.size() == 1 && children.get(0).head.lineindex == head.lineindex;
				lines.add(indent + head.content() + (continuation? "{":""));
				final int childDepth = depth + 1;
				for(var c: children){
					c.effective(lines, childDepth);
				}
				if(type == BlockType.CLOSED || type == BlockType.CONTINUED){
					lines.add(indent + tail.content());
				}
				else if(continuation){
					lines.add(indent + "}");
				}
			}
		}
	}

	public void reconstruct(List<String> lines){
		switch(type){
			case CONTINUATION:{
				children().forEach(child -> child.reconstruct(lines));
				break;
			}
			case NOTHING:{
				break;
			}
			default:{
				if(children.size() == 1){
					var child = children.get(0);
					var headc = child.head;
					if(head.lineindex == headc.lineindex){
						final int line = lines.size();
						child.reconstruct(lines);
						lines.set(line, head.reconstruct() + CodeTree.CONTRACTION_DELIMITOR + lines.get(line).substring(headc.depth()));
						return;
					}
				}

				lines.add(head.reconstruct());

				for(var c : children){
					c.reconstruct(lines);
				}
				if(type == BlockType.CLOSED || type == BlockType.CONTINUED){
					lines.add(tail.reconstruct());
				}
			}
		}
	}
}
