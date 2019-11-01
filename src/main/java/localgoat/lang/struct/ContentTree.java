package localgoat.lang.struct;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ContentTree{

	private final List<CodeTree> trees;
	private final List<CodeLine> ignored;

	public ContentTree(String text){
		final String lines[] = text.split("\r?\n");
		final var codelines = IntStream.range(0, lines.length)
			.mapToObj(index -> new CodeLine(lines[index], index))
			.collect(Collectors.toList());

		var queue = codelines.stream()
			.filter(code -> !code.content.equals(""))
			.collect(
				Collectors.toCollection(ArrayDeque::new)
			);

		var trees = new ArrayList<CodeTree>();
		this.trees = Collections.unmodifiableList(trees);
		while(!queue.isEmpty()){
			trees.add(new CodeTree(null, queue));
		}

		this.ignored = codelines.stream()
			.filter(line -> line.content.equals(""))
			.collect(Collectors.toList());

	}

	public List<CodeTree> getCode(){
		return trees;
	}

	public String reconstruct(){
		final List<String> lines = new ArrayList<>();
		for(var code: trees){
			reconstruct(code, 0, lines);
		}
		for(var line: ignored){
			lines.add(line.lineindex, line.reconstruct());
		}
		final var builder = new StringBuilder();
		lines.forEach(line -> builder.append("\n").append(line));
		return builder.substring(1);
	}

	private void reconstruct(CodeTree tree, int indent, List<String> lines){
		final Consumer<String> indenter = (suffix) -> {
			var builder = new StringBuilder();
			for(int i = 0; i < indent; i++){
				builder.append("\t");
			}
			builder.append(suffix);
			lines.add(builder.toString());
		};

		indenter.accept(tree.head.content);

		final int indentc = indent + 1;
		for(var c: tree.children){
			reconstruct(c, indentc, lines);
		}
		if(tree.closed && tree.sound){
			indenter.accept("}");
		}
	}

}
