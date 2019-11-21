package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.Token;

import java.util.Collections;
import java.util.Set;

public class IgnoredToken implements Token{
	private static final Type IGNORED = new Type(
		"ignored",
		TokenLayer.AESTHETIC,
		Collections.singleton("ignored")
	);

	private final String value;

	public IgnoredToken(String value){
		this.value = value;
	}

	@Override
	public int length(){
		return value.length();
	}

	@Override
	public String value(){
		return value;
	}

	@Override
	public Set<Type> types(){
		return Collections.singleton(IGNORED);
	}

	@Override
	public TokenLayer layer(){
		return TokenLayer.SYNTACTIC;
	}
}
