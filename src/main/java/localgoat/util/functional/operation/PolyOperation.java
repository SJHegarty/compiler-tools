package localgoat.util.functional.operation;

import localgoat.util.streaming.ESupplier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@FunctionalInterface
public interface PolyOperation<T>{
	default T apply(ESupplier<T> supplier){
		return apply(supplier.toStream().collect(Collectors.toList()));
	}
	default T apply(T... values){
		return apply(Arrays.asList(values));
	}

	T apply(List<T> values);
}
