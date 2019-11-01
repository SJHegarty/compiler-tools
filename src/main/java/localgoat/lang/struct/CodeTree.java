package localgoat.lang.struct;

import localgoat.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

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

/*	public static CodeTree build(Deque<CodeLine> lines){
		var result = new CodeTree(lines);
		if(result.type == BlockType.CONTINUED){
			final var continuation = new ArrayList<CodeTree>();
			do{
				continuation.add(result);
				result = new CodeTree(lines);
			}
			while(result.type == BlockType.CONTINUED);
			continuation.add(result);
			final var rv = new CodeTree(continuation.get(0).head);
			for(var child: continuation){
				rv.children.add(child);
			}
			return rv;
		}
		else{
			return result;
		}
	}
*/
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
		final int depth = head.tabcount;

		var split = head.content().split(" :: ", 2);
		if(split.length == 2){

			final var tabs = StringUtils.repeating('\t', head.tabcount);
			final int index = head.lineindex;
			{
				var line = tabs + head.prefix() + split[0];
				this.head = new CodeLine(line, index);
			}
			{
				var line = tabs + split[1] + head.suffix();
				lines.push(new CodeLine(line, index));
			}
			children.add(new CodeTree(lines));
			//TODO: if children.get(0).type == CONTINUED add validation error - unsupported confusing syntax
			this.tail = null;
			this.type = BlockType.UNCLOSED;
		}
		else{
			this.head = head;

			while(lines.size() != 0 && lines.peek().tabcount > depth){
				children.add(new CodeTree(lines));
			}

			if(head.content().endsWith("{")){
				final var line = lines.peek();
				handled:{
					unhandled:{
						if(line == null || line.tabcount != depth){
							break unhandled;
						}
						switch(line.content()){
							case "}&":{
								this.type = BlockType.CONTINUED;
								break;
							}
							case "}":{
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
				/*
						if(result.type == BlockType.CONTINUED){
							final var continuation = new ArrayList<CodeTree>();
							do{
								continuation.add(result);
								result = new CodeTree(lines);
							}
							while(result.type == BlockType.CONTINUED);
							continuation.add(result);
							final var rv = new CodeTree(continuation.get(0).head);
							for(var child: continuation){
								rv.children.add(child);
							}
							return rv;
						}
				 */
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
}
