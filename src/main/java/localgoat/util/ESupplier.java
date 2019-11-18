package localgoat.util;

import localgoat.util.functional.ThrowingFunction;
import localgoat.util.functional.ThrowingUnaryOperator;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
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

	static <T> ESupplier<T> cache(Supplier<T> source){
		return new ESupplier<T>(){
			T t;
			@Override
			public T get(){
				if(t == null){
					t = source.get();
					if(t == null){
						throw new IllegalStateException();
					}
				}
				return t;
			}
		};
	}

	static ESupplier<Character> from(String s){
		return new ESupplier<Character>(){
			int index;
			@Override
			public Character get(){
				if(index == s.length()){
					return null;
				}
				return s.charAt(index++);
			}
		};
	}
	static <T> ESupplier<T> from(T...values){
		return from(Arrays.asList(values));
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

	static <T> ESupplier<T> fromReversed(T ... values){
		return fromReversed(Arrays.asList(values));
	}

	static <T> ESupplier<T> fromReversed(List<T> source){
		return new ESupplier<T>(){
			int index = source.size();
			@Override
			public T get(){
				if(index == 0){
					return null;
				}
				return source.get(--index);
			}
		};
	}

	static <T> ESupplier<T> concat(ESupplier<? extends T>...suppliers){
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

	default String concatenate(){
		final var builder = new StringBuilder();
		for(T t: this){
			builder.append(t);
		}
		return builder.toString();
	}

	class Peekable<T> implements ESupplier<T>{
		private final ESupplier<T> wrapped;
		private final Queue<T> queue = new ArrayDeque<>();

		private Peekable(ESupplier<T> wrapped){
			this.wrapped = wrapped;
		}

		@Override
		public T get(){
			return queue.isEmpty() ? wrapped.get() : queue.poll();
		}

		public T peek(){
			if(queue.isEmpty()){
				T rv = get();
				queue.add(rv);
				return rv;
			}
			else{
				return queue.peek();
			}
		}

		@Override
		public Peekable<T> peekable(){
			throw new UnsupportedOperationException();
		}
	}

	default Peekable<T> peekable(){
		return new Peekable<>(this);
	}

	default T find(Predicate<T> predicate){
		for(;;){
			final var value = get();
			if(value == null || predicate.test(value)){
				return value;
			}
		}
	}

	default ESupplier<T> until(Predicate<T> predicate, boolean includeTerminal){
		return new ESupplier<T>(){
			private boolean terminated;
			@Override
			public T get(){
				if(terminated){
					return null;
				}
				T result = ESupplier.this.get();
				if(result == null || predicate.test(result)){
					terminated = true;
					if(!includeTerminal){
						return null;
					}
				}
				return result;
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

	default ESupplier<ESupplier<T>> split(Predicate<T> predicate){
		return split(predicate, false);
	}

	default ESupplier<ESupplier<T>> split(Predicate<T> predicate, boolean includeTerminal){
		return () -> {
			T next = ESupplier.this.get();
			if(next == null){
				return null;
			}
			return ESupplier.concat(
				ESupplier.of(next),
				ESupplier.this
			)
				.until(predicate, includeTerminal);
		};
	}

	default Stream<T> toStream(){
		return Stream.generate(this).takeWhile(t -> t != null);
	}

	default ESupplier<T> interleave(T split){
		return interleave(() -> split);
	}

	default T[] toArray(IntFunction<T[]> builder){
		return toStream().toArray(builder);
	}

	default ESupplier<T> interleave(Supplier<T> split){
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


	default ESupplier<T> mapOrValue(ThrowingUnaryOperator<T> mapper){
		return map(mapper.orValue());
	}

	default <R> ESupplier<R> mapOrNull(ThrowingFunction<T, R> mapper){
		return map(mapper.orNull());
	}

	default <R> ESupplier<R> map(Function<T, R> mapper){
		return () -> {
			for(var t: this){
				final R r = mapper.apply(t);
				if(r != null){
					return r;
				}
			}
			return null;
		};
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
			.branchBreadthFirst(false, s -> ESupplier.of(s + "a", s + "b", s + "c"))
			.limit(100)
			.exclude(s -> s.contains("abc"))
			.counting();

		for(var s: supplier){
			System.err.println(s);
			if(s.index == 30){
				break;
			}
		}
		System.err.println();
		for(var s: supplier){
			System.err.println(s);
		}
	}

	class Counting<T>{
		public final T value;
		public final int index;

		public Counting(T value, int index){
			this.value = value;
			this.index = index;
		}

		public String toString(){
			return index + ": " + value;
		}
	}

	default ESupplier<Counting<T>> counting(){
		return new ESupplier<>(){
			int index;

			@Override
			public Counting<T> get(){
				final T result = ESupplier.this.get();
				return (result == null) ? null : new Counting<>(result, index++);
			}
		};
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

	default ESupplier<T> reversed(){
		return fromReversed(toStream().collect(Collectors.toList()));
	}

	default ESupplier<T> branchDepthFirst(boolean unique, Function<T, ESupplier<? extends T>> mapper){
		final Deque<T> unmapped = new ArrayDeque<>();
		final Set<T> viewed = new HashSet<>();

		return () -> {
			if(unmapped.isEmpty()){
				for(;;){
					final T t = ESupplier.this.get();
					if(t == null){
						return null;
					}
					if(!unique || viewed.add(t)){
						unmapped.push(t);
						break;
					}
				}
			}
			final T t = unmapped.pollFirst();
			final var supplier = mapper.apply(t);
			if(supplier != null){
				for(T child: supplier.reversed()){
					unmapped.push(child);
				}
			}
			return t;
		};
	}

	default ESupplier<T> branchBreadthFirst(boolean unique, Function<T, ESupplier<? extends T>> mapper){
		return new ESupplier<>(){
			ESupplier<? extends T> supplier = ESupplier.this;
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
