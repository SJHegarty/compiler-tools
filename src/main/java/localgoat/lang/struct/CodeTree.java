package localgoat.lang.struct;

import Main;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeTree{
	public static class CodeLine{
		public final int tabcount;
		public final String content;

		CodeLine(String line){
			int tabcount = 0;
			for(; tabcount < line.length() && line.charAt(tabcount) == '\t'; tabcount++);
			this.tabcount = tabcount;
			this.content = line.substring(tabcount);
		}

		public String regenerate(){
			var builder = new StringBuilder();
			for(int i = 0; i < tabcount; i++){
				builder.append('\t');
			}
			builder.append(content);
			return builder.toString();
		}
	}

	public final CodeLine head;
	final boolean closed;
	public final CodeTree parent;
	public final List<CodeTree> children;
	final boolean sound;

	public CodeTree(String text){
		this(
			null,
			(Queue<CodeLine>) Stream.of(text.split("\r?\n"))
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

	public String toString(){
		return head.content;
	}
}
