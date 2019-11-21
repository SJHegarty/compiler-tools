package localgoat.lang.compiler.omega;

import localgoat.lang.compiler.IndentParser;
import localgoat.lang.compiler.ValidationException;
import localgoat.lang.compiler.token.StringToken;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenSeries;
import localgoat.util.ESupplier;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;

public class OmegaValidators{

	public static final Function<TokenSeries, Consumer<TokenSeries>> TAIL_VALIDATORS = head -> {
		final Function<ESupplier<Token>, StringToken> seeker = supplier -> {
			return supplier
				.mapOrNull(t -> (StringToken)t)
				.find(t -> !t.hasFlag(IndentParser.IGNORED));
		};

		final var token = seeker.apply(
			ESupplier.fromReversed(head.children())
		);

		if(token != null){
			final String value = token.value();
			class Matcher implements Consumer<TokenSeries>{
				private final Set<String> expected;

				Matcher(String... expected){
					this.expected = new TreeSet<>(Arrays.asList(expected));
				}

				@Override
				public void accept(TokenSeries tail){
					final var token = seeker.apply(ESupplier.from(tail.children()));
					if(token == null || !expected.contains(token.value())){
						final String headColoured = "\u001B[36m" + head + "\u001B[0m";
						final String tailColoured = "\u001B[36m" + tail + "\u001B[0m";
						/*throw new ValidationException(
							String.format(
								"Head and tail do not match, value from %s expected.\n\t\tHead: %s\n\t\tTail: %s",
								expected,
								headColoured,
								tailColoured
							)
						);*/
					}
				}
			}
			switch(value){
				case "[": return new Matcher("]");
				case "(": return new Matcher(")");
				case "{": return new Matcher("}", "}&");
			}
		}
		return null;
	};

}
