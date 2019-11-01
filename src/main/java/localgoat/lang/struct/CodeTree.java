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
		NOTHING,
		UNCLOSED,
		UNSOUND;
	}
	final BlockType type;
	public final CodeLine head;
	public final CodeLine tail;
	public final CodeTree parent;
	public final List<CodeTree> children;



	CodeTree(CodeTree parent, Deque<CodeLine> lines){
		this.parent = parent;

		if(lines.isEmpty()){
			this.head = null;
			this.tail = null;
			this.type = BlockType.NOTHING;
			this.children = Collections.emptyList();
			return;
		}

		final var head = lines.poll();
		final var children = new ArrayList<CodeTree>();
		final int depth = head.tabcount;


		this.children = Collections.unmodifiableList(children);

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
			children.add(new CodeTree(this, lines));
			this.tail = null;
			this.type = BlockType.UNCLOSED;
		}
		else{
			this.head = head;

			while(lines.size() != 0 && lines.peek().tabcount > depth){
				children.add(new CodeTree(this, lines));
			}

			if(head.content().endsWith("{")){
				final var line = lines.peek();
				if(line == null || line.tabcount != depth || !line.content().equals("}")){
					tail = null;
					type = BlockType.UNSOUND;
				}
				else{
					tail = line;
					type = BlockType.CLOSED;
					lines.poll();
				}
			}
			else{
				tail = null;
				type = BlockType.UNCLOSED;
			}
		}
	}

	public String toString(){
		return head.reconstruct();
	}
}
