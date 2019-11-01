package localgoat.lang.struct;

import java.util.regex.Pattern;

public class CodeLine{

	private static final Pattern LINE_PATTERN;// = Pattern.compile();

	static{
		final var builder = new StringBuilder();
		builder.append("(?<tabs>\\t*)");
		builder.append("(?<prefix>\\s*)");
		builder.append("(?<content>[^\\s]*(.*[^\\s]+)*)");
		builder.append("(?<suffix>\\s*)");
		LINE_PATTERN = Pattern.compile(builder.toString());
	}

	public final int lineindex;
	public final int tabcount;
	private final String prefix;
	private final String content;
	private final String suffix;

	CodeLine(String line, int index){
		final var m = LINE_PATTERN.matcher(line);
		if(!m.matches()){
			System.err.println(line);
			System.err.println(LINE_PATTERN);
			throw new IllegalStateException();
		}
		this.lineindex = index;
		this.tabcount = m.group("tabs").length();
		this.prefix = m.group("prefix");
		this.content = m.group("content");
		this.suffix = m.group("suffix");
	}

	public String getContent(){
		return content;
	}

	public String reconstruct(){
		var builder = new StringBuilder();
		for(int i = 0; i < tabcount; i++){
			builder.append('\t');
		}
		return builder
			.append(prefix)
			.append(content)
			.append(suffix)
			.toString();
	}
}
