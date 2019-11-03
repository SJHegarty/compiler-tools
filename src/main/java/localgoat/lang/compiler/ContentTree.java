package localgoat.lang.compiler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContentTree{

	private final List<CodeTree> trees;
	private final List<CodeLine> ignored;

	public ContentTree(String text){
		final String lines[] = text.split("\r?\n", -1);
		final var codelines = IntStream.range(0, lines.length)
			.mapToObj(index -> new CodeLine(lines[index], index))
			.collect(Collectors.toList());

		final Predicate<CodeLine> ignored = code -> code.first(t -> !t.ignored()) == null;
		var queue = codelines.stream()
			.filter(ignored.negate())
			.collect(
				Collectors.toCollection(ArrayDeque::new)
			);

		var trees = new ArrayList<CodeTree>();
		this.trees = Collections.unmodifiableList(trees);
		while(!queue.isEmpty()){
			trees.add(new CodeTree(queue));
		}

		this.ignored = codelines.stream()
			.filter(ignored)
			.collect(Collectors.toList());

	}

	public List<CodeTree> getCode(){
		return trees;
	}

	public String reconstruct(){
		final List<String> lines = new ArrayList<>();
		for(var code: trees){
			code.reconstruct(lines);
		}
		for(var line: ignored){
			lines.add(line.lineindex, line.reconstruct());
		}
		final var builder = new StringBuilder();
		lines.forEach(line -> builder.append("\n").append(line));
		System.err.println(new Exception().getStackTrace()[0]);
		return builder.substring(1);
	}

	public String effective(){
		final List<String> lines = new ArrayList<>();
		for(var code: trees){
			code.effective(lines);
		}
		final var builder = new StringBuilder();
		lines.forEach(line -> builder.append("\n").append(line));
		return builder.substring(1);
	}

}