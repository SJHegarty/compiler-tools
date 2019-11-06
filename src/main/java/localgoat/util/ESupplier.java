package localgoat.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@FunctionalInterface
public interface ESupplier<T> extends Supplier<T>, Iterable<T>{

	static <T> ESupplier<T> convert(Supplier<T> supplier){
		return () -> supplier.get();
	}

	static <T> ESupplier<T> empty(){
		return () -> null;
	}

	static <T> ESupplier<T> of(T...elements){
		return new ESupplier<>(){
			int index = 0;

			@Override
			public T get(){
				while(index < elements.length){
					T result = elements[index++];
					if(result != null){
						return result;
					}
				}
				return null;
			}
		};
	}

	static <T> ESupplier<T> from(Iterable<T> source){
		final var iterator = source.iterator();
		return () -> {
			T rv = null;
			while(rv == null && iterator.hasNext()){
				//TODO - Log a warning.
				rv = iterator.next();
			}
			return rv;
		};
	}

	static <T> ESupplier<T> concat(ESupplier<T>...suppliers){
		if(suppliers.length == 0){
			return empty();
		}
		for(var s: suppliers){
			if(s == null){
				throw new IllegalArgumentException();
			}
		}
		return new ESupplier<T>(){
			int index = 0;
			@Override
			public T get(){
				while(index < suppliers.length){
					T result = suppliers[index].get();
					if(result == null){
						index += 1;
					}
					else{
						return result;
					}
				}
				return null;
			}
		};
	}

	@Override
	default Iterator<T> iterator(){
		return new Iterator<>(){
			boolean retrieved;
			T value;

			@Override
			public boolean hasNext(){
				if(!retrieved){
					retrieved = true;
					value = get();
				}
				return value != null;
			}

			@Override
			public T next(){
				if(!hasNext()){
					throw new NoSuchElementException();
				}
				retrieved = false;
				return value;
			}
		};
	}

	default Stream<T> toStream(){
		return Stream.generate(this).takeWhile(t -> t != null);
	}

	default ESupplier<T> interlace(T split){
		return interlace(() -> split);
	}

	default ESupplier<T> interlace(Supplier<T> split){
		final ESupplier<T> wrapped = this;
		return new ESupplier<T>(){

			T next;
			boolean fromSource = true;

			@Override
			public T get(){
				if(fromSource){
					fromSource = false;
					var last = (next == null) ? wrapped.get() : next;
					next = wrapped.get();
					return last;
				}
				fromSource = true;
				return (next == null) ? null : split.get();
			}
		};
	}

	default <R> ESupplier<R> map(Function<T, R> mapper){
		return () -> Optional.ofNullable(get())
			.map(mapper)
			.orElse(null);
	}

	default <R> ESupplier<R> flatMap(Function<T, ESupplier<R>> mapper){
		final ESupplier<T> wrapped = this;
		return new ESupplier<R>(){
			ESupplier<R> supplier;

			@Override
			public R get(){
				for(;;){
					while(supplier == null){
						T t = wrapped.get();
						if(t == null){
							return null;
						}
						supplier = mapper.apply(t);
					}
					R result = supplier.get();
					if(result == null){
						supplier = null;
					}
					else{
						return result;
					}
				}
			}
		};
	}

	default ESupplier<T> retain(Predicate<T> predicate){
		return () -> {
			for(;;){
				final T result = ESupplier.this.get();
				if(result == null || predicate.test(result)){
					return result;
				}
			}
		};
	}

	default ESupplier<T> exclude(Predicate<T> predicate){
		return retain(predicate.negate());
	}

	default ESupplier<T> limit(final int maxSize){
		return new ESupplier<>(){
			int count = 0;
			@Override
			public T get(){
				if(count == maxSize){
					return null;
				}
				count += 1;
				return ESupplier.this.get();
			}
		};
	}

	public static void main(String...args){
		var supplier = ESupplier.of("a")
			.branchingMap(false, s -> ESupplier.of(s + "a", s + "b", s + "c"))
			.limit(100)
			.exclude(s -> s.contains("abc"));

		int index = 0;
		for(var s: supplier){
			System.err.println(index++ + ": " + s);
		}
	}

	default ESupplier<T> unique(){
		final Set<T> viewed = new HashSet<>();
		return () -> {
			for(;;){
				final var result = ESupplier.this.get();
				if(result == null || viewed.add(result)){
					return result;
				}
			}
		};
	}

	default ESupplier<T> branchingMap(boolean unique, Function<T, ESupplier<T>> mapper){
		return new ESupplier<>(){
			ESupplier<T> supplier = ESupplier.this;
			final Queue<T> unmapped = new ArrayDeque<>();
			final Set<T> viewed = new HashSet<>();

			@Override
			public T get(){
				if(supplier == null){
					return null;
				}
				for(;;){
					final T result = supplier.get();
					if(result == null){
						if(unmapped.isEmpty()){
							supplier = null;
							return null;
						}
						supplier = mapper.apply(unmapped.poll());
						if(supplier == null){
							supplier = empty();
						}
						continue;
					}
					if(!unique || viewed.add(result)){
						unmapped.add(result);
						return result;
					}
				}
			}
		};
	}
}