package localgoat.lang.compiler.automata;

public class TypeState{
	public static final TypeState TERMINATING = new TypeState(null, State.TERMINATING);

	private final Type type;
	private final State state;
	private final int depth;

	public TypeState(Type type, State state){
		this(type, state, 0);
	}

	private TypeState(Type type, State state, int depth){
		this.type = type;
		this.state = state;
		this.depth = depth;
	}

	public TypeState drop(){
		return new TypeState(type, state, depth - 1);
	}

	public Type type(){
		return type;
	}

	public State state(){
		return state;
	}

	public boolean isTerminating(){
		return (depth == 0) && (state == State.TERMINATING);
	}

	public int depth(){
		return depth;
	}
}
