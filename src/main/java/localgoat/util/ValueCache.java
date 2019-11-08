package localgoat.util;


import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ValueCache<K, V>{
	private final Map<K, V> map;
	private final Function<K, V> builder;

	public ValueCache(Function<K, V> builder){
		this(new HashMap<>(), builder);
	}

	public ValueCache(Map<K, V> map, Function<K, V> builder){
		this.builder = builder;
		this.map = map;
	}

	public V get(K key){
		var rv = map.get(key);
		if(rv == null){
			rv = builder.apply(key);
			map.put(key, rv);
		}
		return rv;
	}
}
