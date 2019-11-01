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
	final boolean closed;
	public final CodeTree parent;
	public final List<CodeTree> children;
	final boolean sound;

	public CodeTree(String text){
		this(
			null,
			Stream.of(text.split("\r?\n"))
				.map(line -> new CodeLine(line))
				.filter(code -> !code.content.equals(""))
				.collect(
					Collectors.toCollection(ArrayDeque::new)
				)
		);
	}

	CodeTree(CodeTree parent, Queue<CodeLine> lines){
		this.parent = parent;

		if(lines.isEmpty()){
			this.head = null;
			this.closed = false;
			this.children = Collections.emptyList();
			this.sound = false;
			return;
		}
		this.head = lines.poll();

		final int depth = head.tabcount;
		final var children = new ArrayList<CodeTree>();

		this.closed = head.content.endsWith("{");
		this.children = Collections.unmodifiableList(children);

		while(lines.size() != 0 && lines.peek().tabcount > depth){
			children.add(new CodeTree(this, lines));
		}

		if(closed){
			final var line = lines.peek();
			if(line == null || line.tabcount != depth || !line.content.equals("}")){
				sound = false;
			}
			else{
				sound = true;
				lines.poll();
			}
		}
		else{
			sound = true;
		}
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		reconstruct(0, builder);
		return builder.substring(1);
	}

	public void reconstruct(int indent, StringBuilder builder){
		builder.append("\n");
		final Runnable indenter = () -> {
			for(int i = 0; i < indent; i++){
				builder.append("\t");
			}
		};
		indenter.run();
		builder.append(head.content);
		final int indentc = indent + 1;
		for(var c: children){
			c.reconstruct(indentc, builder);
		}
		if(closed && sound){
			builder.append("\n");
			indenter.run();
			builder.append("}");
		}
	}

	public String toString(){
		return head.content;
	}
}
