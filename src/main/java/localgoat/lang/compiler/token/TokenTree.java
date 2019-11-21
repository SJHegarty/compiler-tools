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

	default TokenLayer filteringLayer(){
		return TokenLayer.AESTHETIC;
	}

	default Token trim(){
		throw new UnsupportedOperationException(this.getClass().getName());
	}

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

	default TokenTree filter(TokenLayer layer){
		{
			int lordinal = layer.ordinal();
			int fordinal = filteringLayer().ordinal();
			if(lordinal > fordinal){
				throw new IllegalArgumentException("Cannot un-filter a Token.");
			}
			if(lordinal == fordinal){
				return this;
			}
		}
		if(Token.super.filter(layer) == null){
			return null;
		}

		final var head = new CachingSupplier<>(
			() -> Optional.ofNullable(head())
				.map(h -> h.filter(layer))
				.orElse(null)
		);

		final var children = new CachingSupplier<>(
			() -> ESupplier.from(children())
				.map(c -> c.filter(layer))
				.toStream()
				.collect(Collectors.toList())
		);

		final var tail = new CachingSupplier<>(
			() -> Optional.ofNullable(TokenTree.this.tail())
				.map(h -> h.filter(layer))
				.orElse(null)
		);

		return new TokenTree(){
			@Override
			public Token head(){
				return head.get();
			}

			@Override
			public List<? extends Token> children(){
				return children.get();
			}

			@Override
			public Token tail(){
				return tail.get();
			}

			@Override
			public Set<Type> types(){
				return TokenTree.this.types();
			}

			@Override
			public TokenLayer layer(){
				return TokenTree.this.layer();
			}

			@Override
			public TokenLayer filteringLayer(){
				return layer;
			}
		};
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
