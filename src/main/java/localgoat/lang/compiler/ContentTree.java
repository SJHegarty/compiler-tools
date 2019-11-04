package localgoat.lang.compiler;

import localgoat.util.ESupplier;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContentTree{

	private final List<CodeTree> trees;

	public ContentTree(String text){
		final String lines[] = text.split("\r?\n", -1);
		final var queue = IntStream.range(0, lines.length)
			.mapToObj(index -> new CodeLine(lines[index], index))
			.collect(Collectors.toCollection(ArrayDeque::new));

		var trees = new ArrayList<CodeTree>();
		this.trees = Collections.unmodifiableList(trees);
		while(!queue.isEmpty()){
			trees.add(new CodeTree(queue));
		}
	}

	public List<CodeTree> getCode(){
		return trees;
	}

	public ESupplier<Token> tokens(){
		return CodeTree.tokenise(trees);
	}

	public String reconstruct(){
		final var builder = new StringBuilder();
		for(var t: tokens()){
			builder.append(t);
		}
		return builder.toString();
	}

}
