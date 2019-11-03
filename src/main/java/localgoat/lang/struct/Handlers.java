package localgoat.lang.struct;

import localgoat.lang.struct.handlers.Handler;
import localgoat.lang.struct.handlers.IdentifierHandler;
import localgoat.lang.struct.handlers.KeyHandler;
import localgoat.lang.struct.handlers.StringHandler;
import localgoat.lang.struct.handlers.SymbolHandler;
import localgoat.lang.struct.handlers.WhitespaceHandler;
import localgoat.util.functional.CharPredicate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class Handlers{

	static final Handler WHITESPACE_HANDLER;
	static final Handler[] HANDLERS;

	static{
		WHITESPACE_HANDLER = new WhitespaceHandler();
		HANDLERS = new Handler[]{
			IdentifierHandler.INSTANCE,
			new KeyHandler(),
			new SymbolHandler(),
			WHITESPACE_HANDLER,
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
