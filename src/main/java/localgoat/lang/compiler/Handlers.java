package localgoat.lang.compiler;

import localgoat.lang.compiler.handlers.*;
import localgoat.util.io.CharSource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Handlers{

	static final Handler[] HANDLERS;

	static{
		HANDLERS = new Handler[]{
			IdentifierHandler.INSTANCE,
			TypeHandler.INSTANCE,
			ConstHandler.INSTANCE,
			new KeyHandler(),
			new SymbolHandler(),
			WhitespaceHandler.INSTANCE,
			new StringHandler(),
		};
		@SuppressWarnings("unchecked")
		Map.Entry<Character, Handler[]>[] collisions = (Map.Entry<Character, Handler[]>[]) IntStream.range(0, 0xff)
			.mapToObj(i -> (char)i)
			.collect(
				Collectors.toMap(
					character -> character,
					character -> {
						char c = character;
						return Stream.of(HANDLERS)
							.filter(handler -> handler.handles(c))
							.toArray(Handler[]::new);
					},
					(v0, v1) -> {
						throw new IllegalStateException();
					},
					LinkedHashMap::new
				)
			)
			.entrySet().stream()
			.filter(e -> e.getValue().length > 1)
			.toArray(Map.Entry[]::new);

		if(collisions.length != 0){
			var builder = new StringBuilder();
			for(var collision: collisions){
				builder
					.append(Handler.class.getSimpleName())
					.append(" collision for character \'")
					.append(collision.getKey())
					.append("\'.");

				builder.append("\n{");
				for(var handler: collision.getValue()){
					builder.append("\n\t").append(handler.getClass().getName());
				}
				builder.append("\n}");
			}
			throw new IllegalStateException(builder.toString());
		}
	}

}
