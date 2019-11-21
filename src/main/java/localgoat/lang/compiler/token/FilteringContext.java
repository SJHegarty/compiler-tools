package localgoat.lang.compiler.token;

import localgoat.util.EComparator;

import java.util.Comparator;
import java.util.function.Predicate;

public class FilteringContext{
	private final EComparator<TokenLayer> comparator;
	private final TokenLayer layer;
	private final int depth;

	public FilteringContext(TokenLayer layer){
		this(
			layer,
			TokenLayer.buildComparator(TokenLayer.values()),
			0
		);
	}

	public FilteringContext(TokenLayer layer, EComparator<TokenLayer> comparator, int depth){
		this.layer = layer;
		this.comparator = comparator;
		this.depth = depth;
	}

	public TokenLayer layer(){
		return layer;
	}

	public int depth(){
		return depth;
	}

	public Predicate<TokenLayer> filter(){
		return comparator.lessThanOrEqualTo(layer);
	}

	public FilteringContext child(){
		return new FilteringContext(layer, comparator, depth + 1);
	}
}
