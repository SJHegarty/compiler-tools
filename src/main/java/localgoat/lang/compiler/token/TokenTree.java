package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.structure.Type;
import localgoat.util.CachingSupplier;
import localgoat.util.ESupplier;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public interface TokenTree extends Token{
	Token head();
	List<? extends Token> children();
	Token tail();

	@Override
	Token filter(FilteringContext context);

	TokenLayer filteringLayer();/*{
		return TokenLayer.AESTHETIC;
	}*/

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

	default Set<Type> types(){
		return tokens().toStream()
			.flatMap(t -> t.types().stream())
			.collect(Collectors.toSet());
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

	@Override
	default TokenLayer layer(){
		return ESupplier.from(tokens())
			.map(t -> (Token)t)
			.branchBreadthFirst(
				true,
				t -> {
					if(t instanceof TokenTree){
						return ESupplier.from(((TokenTree)t).tokens());
					}
					return null;
				}
			)
			.toStream()
			.map(t -> t.layer())
			.sorted()
			.findFirst().orElseThrow(() -> new IllegalStateException("Token tree without terminators."));
	}
}
