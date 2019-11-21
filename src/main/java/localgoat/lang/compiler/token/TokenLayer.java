package localgoat.lang.compiler.token;

import localgoat.util.EComparator;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

public enum TokenLayer{
	SEMANTIC,
	SYNTACTIC,
	SCHOLASTIC,
	AESTHETIC;

	public static void main(String...args){
		final var c = buildComparator(SEMANTIC, SCHOLASTIC, SYNTACTIC, AESTHETIC);
		final var p = c.lessThanOrEqualTo(TokenLayer.SYNTACTIC);
		Stream.of(TokenLayer.values())
			.sorted(c)
			.forEach(t -> System.err.println(t + " " + p.test(t)));

	}

	static EComparator<TokenLayer> buildComparator(TokenLayer...layers){
		final var input = new TreeSet<>(Arrays.asList(layers));
		if(layers.length != input.size()){
			throw new IllegalArgumentException(
				String.format(
					"%s contains repeated values.",
					Arrays.asList(layers)
				)
			);
		}
		final var all = new TreeSet<>(Arrays.asList(TokenLayer.values()));
		if(!all.equals(input)){
			throw new IllegalArgumentException(
				String.format("%s != %s", input, all)
			);
		}
		if(layers[0] != SEMANTIC || layers[layers.length - 1] != AESTHETIC){
			throw new IllegalArgumentException(
				String.format("Illegal ordering: %s", Arrays.asList(layers))
			);
		}
		final int indices[] = new int[layers.length];
		for(int i = 0; i < indices.length; i++){
			indices[layers[i].ordinal()] = i;
		}
		return (t0, t1) -> indices[t0.ordinal()] - indices[t1.ordinal()];
	}
}
