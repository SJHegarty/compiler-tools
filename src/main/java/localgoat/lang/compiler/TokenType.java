package localgoat.lang.compiler;

public enum TokenType{
	KEYWORD(false),
	KEYSYMBOL(false),
	WHITESPACE(true),
	COMMENT(true),
	FORMATTED_COMMENT(true),
	SYMBOL(false),
	IDENTIFIER(false),
	STATIC_IDENTIFIER(false),
	HANGING(false),
	UNHANDLED(false),
	STRING(false),
	TYPE(false);

	public final boolean ignored;

	TokenType(boolean ignored){
		this.ignored = ignored;
	}
}
