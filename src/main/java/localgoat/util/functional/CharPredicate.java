package localgoat.util.functional;

@FunctionalInterface
public interface CharPredicate{

	static CharPredicate range(char c0, char cn){
		return c -> c0 <= c && c <= cn;
	}
	
	static CharPredicate or(CharPredicate...predicates){
		return c -> {
			for(var p: predicates){
				if(p.test(c)){
					return true;
				}
			}
			return false;
		};
	}
	boolean test(char c);
}
