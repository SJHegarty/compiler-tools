package localgoat.util.functional.operation;

import java.util.List;

@FunctionalInterface
public interface AssociativeOperation<T> extends PolyOperation<T>{

	default T apply(List<T> values){
		return apply(values, 0, values.size());
	}

	default T apply(List<T> values, int index, int length){
		final T t0;
		final T t1;
		switch(length){
			case 0: return null;
			case 1: return values.get(index);
			case 2:{
				t0 = values.get(index);
				t1 = values.get(index + 1);
				break;
			}
			default:{
				final int hlength0 = length >> 1;
				t0 = apply(values, index, hlength0);
				t1 = apply(values, index + hlength0, length - hlength0);
				break;
			}
		}
		return apply(t0, t1);
	}

	T apply(T t0, T t1);

}
