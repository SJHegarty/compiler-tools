package localgoat.util.functional.operation;

import java.util.Arrays;
import java.util.List;

@FunctionalInterface
public interface PolyOperation<T>{
	default T apply(T... values){
		return apply(Arrays.asList(values));
	}

	T apply(List<T> values);
}
