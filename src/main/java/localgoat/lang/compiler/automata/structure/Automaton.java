package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.util.ESupplier;

import java.util.*;
import java.util.stream.Collectors;

public class Automaton{

	public static Automaton lambda(){
		final var builder = new Builder(Collections.emptySet());
		builder.addNode(true);
		return new Automaton(builder);
	}

	public static Automaton of(Token...tokens){
		final var tokenSet = new HashSet<>(Arrays.asList(tokens));
		final var builder = new Builder(tokenSet);
		final var n0 = builder.addNode();
		final var n1 = builder.addNode(true);
		n0.addTransitions(tokenSet, n1);
		return new Automaton(builder);
	}

	protected final MutableNode[] nodes;
	protected final Set<Token> tokens;

	public Automaton(Builder builder){
		this.tokens = new HashSet<>(builder.tokens());
		this.nodes = builder.nodes().stream()
			.map(nbuilder -> nbuilder.initialise(this))
			.toArray(MutableNode[]::new);

		builder.nodes().stream()
			.forEach(nbuilder -> nbuilder.finalise());

	}

	public List<Node> nodes(){
		return new AbstractList<>(){

			@Override
			public int size(){
				return nodeCount();
			}

			@Override
			public Node get(int index){
				return node(index);
			}
		};
	}

	public Set<Type> types(){
		return nodes().stream()
			.flatMap(n -> n.types().stream())
			.collect(
				Collectors.toCollection(
					() -> new TreeSet<>(Comparator.comparing(t -> String.valueOf(t)))
				)
			);
	}

	public boolean isComplete(Set<Token> alphabet){
		if(!tokens.containsAll(alphabet)){
			return false;
		}
		return null == ESupplier.from(nodes)
			.exclude(node -> node.tokens().equals(tokens))
			.get();
	}

	public boolean isDeterministic(){
		for(var n: nodes()){
			final var transitions = n.transitions();
			if(transitions.containsKey(null)){
				return false;
			}
			for(var e: transitions.entrySet()){
				if(e.getValue().size() != 1){
					return false;
				}
			}
		}
		return true;
	}

	public final Node node(int index){
		return nodes[index];
	}

	public final int nodeCount(){
		return nodes.length;
	}

	public final Set<Token> tokens(){
		return Collections.unmodifiableSet(tokens);
	}
}
