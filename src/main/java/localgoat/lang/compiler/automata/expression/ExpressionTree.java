package localgoat.lang.compiler.automata.expression;

import java.util.List;

public interface ExpressionTree extends Expression{
	List<Expression> children();
}
