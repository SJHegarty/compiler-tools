package localgoat.lang.compiler;

public enum TokenType{
	CONST(false),
	KEYWORD(false),
	KEYSYMBOL(false),
	WHITESPACE(true),
	LINE_COMMENT(true),
	STRUCTURED_COMMENT(false),
	HANDLED_COMMENT(false),
	SYMBOL(false),
	IDENTIFIER(false),
	STATIC_IDENTIFIER(false),
	HANGING(false),
	UNHANDLED(false),
	STRING(false),
	TYPE(false),
	AMBIGUOUS(false);

	public final boolean ignored;

	TokenType(boolean ignored){
		this.ignored = ignored;
	}
}
