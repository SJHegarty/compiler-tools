package localgoat.util.functional;

import java.util.function.UnaryOperator;

public interface ThrowingUnaryOperator<T> extends ThrowingFunction<T, T>{
	@Override
	default UnaryOperator<T> orNull(){
		return ThrowingFunction.super.orNull()::apply;
	}

	default UnaryOperator<T> orValue(){
		return t -> {
			try{
				return ThrowingUnaryOperator.this.apply(t);
			}
			catch(Exception e){
				return t;
			}
		};
	}
}
