package localgoat.lang.compiler.automata.data;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.util.ESupplier;

import java.util.List;

public interface TokenTree extends Token{
	Token head();
	List<? extends Token> children();
	Token tail();

	default Token child(int index){
		return children().get(index);
	}

	default ESupplier<? extends Token> tokens(){
		return ESupplier.concat(
			ESupplier.of(head()),
			ESupplier.from(children()),
			ESupplier.of(tail())
		);
	}

	@Override
	default int length(){
		int rv = 0;
		for(var token: tokens()){
			rv += token.length();
		}
		return rv;
	}

	@Override
	default String value(){
		final var builder = new StringBuilder();
		for(var token: tokens()){
			builder.append(token.value());
		}
		return builder.toString();
	}

	default String childrenString(){
		final var builder = new StringBuilder();
		for(var token: ESupplier.from(children())){
			builder.append(token);
		}
		return builder.toString();
	}
}
