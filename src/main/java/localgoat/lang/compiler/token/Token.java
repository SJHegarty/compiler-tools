package localgoat.lang.compiler.token;

import localgoat.lang.compiler.automata.structure.Type;
import localgoat.util.streaming.ESupplier;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public interface Token{
	int length();
	String value();
	Set<Type> types();

	default Token filter(TokenLayer layer){
		return filter(new FilteringContext(layer));
	}

	default Token filter(FilteringContext context){
		//TODO: look into null layer() values.
		var layer = layer();
		if(layer == null || context.filter().test(layer)){
			return this;
		}
		return null;
	}

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
		return Optional.ofNullable(type())
			.map(t -> t.layer())
			.orElse(null);
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
