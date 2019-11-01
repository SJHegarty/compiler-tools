package localgoat.lang.struct;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContentTree{

	private final List<CodeTree> trees;
	private final List<CodeLine> ignored;

	public ContentTree(String text){
		final String lines[] = text.split("\r?\n");
		final var codelines = IntStream.range(0, lines.length)
			.mapToObj(index -> new CodeLine(lines[index], index))
			.collect(Collectors.toList());

		var queue = codelines.stream()
			.filter(code -> !code.getContent().equals(""))
			.collect(
				Collectors.toCollection(ArrayDeque::new)
			);

		var trees = new ArrayList<CodeTree>();
		this.trees = Collections.unmodifiableList(trees);
		while(!queue.isEmpty()){
			trees.add(new CodeTree(null, queue));
		}

		this.ignored = codelines.stream()
			.filter(line -> line.getContent().equals(""))
			.collect(Collectors.toList());

	}

	public List<CodeTree> getCode(){
		return trees;
	}

	public String reconstruct(){
		final List<String> lines = new ArrayList<>();
		for(var code: trees){
			reconstruct(code, lines);
		}
		for(var line: ignored){
			lines.add(line.lineindex, line.reconstruct());
		}
		final var builder = new StringBuilder();
		lines.forEach(line -> builder.append("\n").append(line));
		return builder.substring(1);
	}

	private void reconstruct(CodeTree tree, List<String> lines){

		lines.add(tree.head.reconstruct());

		for(var c: tree.children){
			reconstruct(c, lines);
		}
		if(tree.closed && tree.sound){
			lines.add(tree.tail.reconstruct());
		}
	}

}
