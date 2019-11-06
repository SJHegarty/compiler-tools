package localgoat.util;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CollectionUtils{

	public static <T> Set<T> union(Set<T>...sets){
		return Stream.of(sets)
			.flatMap(set -> set.stream())
			.collect(Collectors.toSet());
	}

	public static <T> Set<T> exclusion(Set<T> all, Set<T> exclusions){
		return all.stream()
			.filter(t -> !exclusions.contains(t))
			.collect(Collectors.toSet());
	}
}
