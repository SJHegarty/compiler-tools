package localgoat.lang.compiler.automata;

public class TypeState{
	public static final TypeState TERMINATING = new TypeState(null, State.TERMINATING);

	private final Type type;
	private final State state;
	private final int depth;
	private final boolean negated;

	public TypeState(Type type, State state){
		this(type, state, 0, false);
	}

	private TypeState(Type type, State state, int depth, boolean negated){
		this.type = type;
		this.state = state;
		this.depth = depth;
		this.negated = negated;
	}

	public TypeState drop(){
		return new TypeState(type, state, depth - 1, negated);
	}

	public TypeState negate(){
		return new TypeState(type, state, depth, !negated);
	}

	public Type type(){
		return type;
	}

	public State state(){
		return state;
	}

	public boolean isTerminating(){
		return (depth == 0) && (state == State.TERMINATING) && !negated;
	}

	public int depth(){
		return depth;
	}

	public String toString(){
		return String.format("[%s, %s, %s, %s]", type, state, depth, negated);
	}
}
