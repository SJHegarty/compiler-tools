package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.structure.Type;
import localgoat.util.ESupplier;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

public interface Token{
	int length();
	String value();
	Set<Type> types();

	default Type type(){
		final var classes = types();
		switch(classes.size()){
			case 0:{
				return null;
			}
			case 1:{
				return classes.iterator().next();
			}
			default:{
				throw new UnsupportedOperationException("Cannot get type when multiple types present (" + classes + ").");
			}
		}
	}

	default TokenLayer layer(){
		return type().layer();
	}

	default Token filter(TokenLayer layer){
		if(layer.ordinal() < layer().ordinal()){
			return null;
		}
		return this;
	}

	default boolean hasFlag(String flag){
		return null != ESupplier.from(types())
			.retain(t -> t.hasFlag(flag))
			.get();
	}

	default boolean hasClass(String name){
		return hasClass(t -> Objects.equals(name, t.name()));
	}

	default boolean hasClass(Predicate<Type> predicate){
		return null != types().stream()
			.filter(predicate)
			.findFirst()
			.orElse(null);
	}




}
