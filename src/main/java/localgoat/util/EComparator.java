package localgoat.util;

import java.util.Comparator;
import java.util.function.Predicate;

public interface EComparator<T> extends Comparator<T>{

	static <T> EComparator<T> convert(Comparator<T> comparator){
		if(comparator instanceof EComparator){
			return (EComparator)comparator;
		}
		return (t0, t1) -> comparator.compare(t0, t1);
	}

	default Predicate<T> lessThan(T t){
		return t0 -> compare(t0, t) < 0;
	}

	default Predicate<T> lessThanOrEqualTo(T t){
		return t0 -> compare(t0, t) <= 0;
	}

	default Predicate<T> greaterThan(T t){
		return t0 -> compare(t0, t) > 0;
	}

	default Predicate<T> greaterThanOrEqualTo(T t){
		return t0 -> compare(t0, t) >= 0;
	}

}
