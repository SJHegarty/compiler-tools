package localgoat.lang.struct;

public class CodeLine{

	public final int lineindex;
	public final int tabcount;
	public final String content;

	CodeLine(String line, int index){
		int tabcount = 0;
		for(; tabcount < line.length() && line.charAt(tabcount) == '\t'; tabcount++);

		this.lineindex = index;
		this.tabcount = tabcount;
		this.content = line.substring(tabcount);
	}

	public String reconstruct(){
		var builder = new StringBuilder();
		for(int i = 0; i < tabcount; i++){
			builder.append('\t');
		}
		builder.append(content);
		return builder.toString();
	}
}
