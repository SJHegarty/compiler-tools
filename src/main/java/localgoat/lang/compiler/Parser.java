package localgoat.lang.compiler;

import localgoat.lang.compiler.token.Token;

import java.util.Arrays;
import java.util.List;

public interface Parser<S extends Token, D extends Token>{
	default D parse(S...values){
		return parse(Arrays.asList(values));
	}

	public D parse(List<S> values);
}
