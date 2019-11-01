package localgoat.lang.struct;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeTree{

	public final CodeLine head;
	public final CodeLine tail;
	final boolean closed;
	public final CodeTree parent;
	public final List<CodeTree> children;
	final boolean sound;


	CodeTree(CodeTree parent, Queue<CodeLine> lines){
		this.parent = parent;

		if(lines.isEmpty()){
			this.head = null;
			this.tail = null;
			this.closed = false;
			this.children = Collections.emptyList();
			this.sound = false;
			return;
		}
		this.head = lines.poll();

		final int depth = head.tabcount;
		final var children = new ArrayList<CodeTree>();

		this.closed = head.getContent().endsWith("{");
		this.children = Collections.unmodifiableList(children);

		while(lines.size() != 0 && lines.peek().tabcount > depth){
			children.add(new CodeTree(this, lines));
		}

		if(closed){
			final var line = lines.peek();
			if(line == null || line.tabcount != depth || !line.getContent().equals("}")){
				tail = null;
				sound = false;
			}
			else{
				tail = line;
				sound = true;
				lines.poll();
			}
		}
		else{
			tail = null;
			sound = true;
		}
	}

	public String toString(){
		return head.reconstruct();
	}
}
