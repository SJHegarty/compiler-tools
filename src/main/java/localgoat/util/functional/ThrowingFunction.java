package localgoat.util.functional;

import java.util.function.Function;

public interface ThrowingFunction<T, R>{
	R apply(T t) throws Exception;

	default Function<T, R> orNull(){
		return (t) -> {
			try{
				return ThrowingFunction.this.apply(t);
			}
			catch(Exception e){
				return null;
			}
		};
	}
}
