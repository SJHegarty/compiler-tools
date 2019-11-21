package localgoat.util;

import java.util.function.Supplier;

public class CachingSupplier<T> implements Supplier<T>{

	private final Supplier<T> wrapped;
	private boolean cached;
	private T value;

	public CachingSupplier(Supplier<T> wrapped){
		this.wrapped = wrapped;
	}

	@Override
	public T get(){
		if(!cached){
			cached = true;
			value = wrapped.get();
		}
		return value;
	}
}
