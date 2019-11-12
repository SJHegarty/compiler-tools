package localgoat.lang.compiler.automata;

import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.expression.Expression;
import localgoat.util.CollectionUtils;
import localgoat.util.ESupplier;
import localgoat.util.ValueCache;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DFA<T extends TokenA> implements Automaton<T>{
	public static void main(String...args){

		final var converter = new Converter();

		converter.addClass('U', c -> 'A' <= c && c <= 'Z');
		converter.addClass('l', c -> 'a' <= c && c <= 'z');
		converter.addClass('s', c -> c == '_');
		converter.addClass('h', c -> c == '-');
		converter.addClass('q', c -> c == '\"');
		converter.addClass('e', c -> c == '\\');

		final var expressions = new HashMap<>();
		expressions.put("string", "q*+(~q, eq)q");
		expressions.put("class-name", "*<1+>(U*l)");
		expressions.put("constant", "*<1+>U*(s*<1+>U)");
		expressions.put("identifier", "*<1+>l*(h*<1+>l)");

		final var builder = new StringBuilder();
		builder.append("+(");

		ESupplier.from(expressions.entrySet())
			.map(
				e -> String.format(
					"@<%s>(%s)",
					e.getKey(),
					e.getValue()
				)
			)
			.interleave(",")
			.forEach(s -> builder.append(s));

		builder.append(")");

		final var expr = Expression.parse(builder.toString());
		final var dfa = converter.buildDFA(expr);

		System.err.println(dfa.read(ReadMode.GREEDY, TokenA.from("ClassName")));
		System.err.println(dfa.read(ReadMode.GREEDY, TokenA.from("HTTP_")));
		System.err.println(dfa.read(ReadMode.GREEDY, TokenA.from("ENUM_CONSTANTsome-other-shit")));
		System.err.println(dfa.read(ReadMode.GREEDY, TokenA.from("instance-identifier")));

	}

	private final MutableNode<T>[] nodes;
	private final Set<T> tokens;

	DFA(Builder<T> builder){
		this.tokens = new HashSet<>(builder.tokens);
		this.nodes = builder.nodes.stream()
			.map(nbuilder -> nbuilder.initialise(this))
			.toArray(MutableNode[]::new);

		builder.nodes.stream()
			.forEach(nbuilder -> nbuilder.finalise());

	}

	public DFA(NFA<T> nfa){
		this.tokens = new HashSet<>(nfa.tokens());
		final Map<Node<T>, Set<Node<T>>> lambdaTransitable = IntStream.range(0, nfa.nodeCount())
			.mapToObj(nfa::node)
			.collect(
				Collectors.toMap(
					node -> node,
					node -> ESupplier.of(node)
						.branchingMap(true, n -> ESupplier.from(n.transitions(null)))
						.toStream()
						.collect(Collectors.toSet())
				)
			);

		final var nodesMap = new LinkedHashMap<Set<Node<T>>, MutableNode<T>>();
		final var reverseMap = new HashMap<MutableNode<T>, Set<Node<T>>>();
		final var nodeBuilder = new ValueCache<>(
			nodesMap,
			nodeset -> {
				final Set<Node<T>> reachable = nodeset.stream()
					.flatMap(n -> lambdaTransitable.get(n).stream())
					.collect(Collectors.toSet());

				final Set<String> names = ESupplier.from(reachable)
					.flatMap(n -> ESupplier.from(n.classes()))
					.toStream().collect(Collectors.toSet());

				final var rv = new MutableNode<T>(this, nodesMap.size(), names);
				nodesMap.put(nodeset, rv);
				reverseMap.put(rv, nodeset);
				return rv;
			}
		);

		final Queue<MutableNode<T>> nodesQueue = new ArrayDeque<>();
		nodesQueue.add(
			nodeBuilder.get(
				lambdaTransitable.get(nfa.node(0))
			)
		);
		class Transition{
			final T token;
			final Node<T> destination;

			Transition(T token, Node<T> destination){
				this.token = token;
				this.destination = destination;
			}
		}
		while(!nodesQueue.isEmpty()){
			final var node = nodesQueue.poll();
			final var srcNodes = reverseMap.get(node);
			final var transitions = srcNodes.stream()
				.flatMap(n -> lambdaTransitable.get(n).stream())
				.flatMap(n -> n.transitions().entrySet().stream())
				.flatMap(
					e -> {
						T token = e.getKey();
						return e.getValue().stream()
							.map(n -> new Transition(token, n));
					}
				)
				.filter(transition -> transition.token != null)
				.collect(
					Collectors.groupingBy(
						transition -> transition.token,
						Collectors.mapping(
							transition -> transition.destination,
							Collectors.toSet()
						)
					)
				);

			transitions.forEach(
				(token, srcDests) -> {
					var dest = nodesMap.get(srcDests);
					if(dest == null){
						dest = nodeBuilder.get(srcDests);
						nodesQueue.add(dest);
					}
					node.addTransition(token, dest);
				}
			);
		}
		this.nodes = nodesMap.values().stream().toArray(MutableNode[]::new);
	}

	public DFA(T...tokens){
		if(tokens.length == 0){
			this.tokens = Collections.emptySet();
			this.nodes = new MutableNode[]{
				new MutableNode(this, 0, true)
			};
			return;
		}
		this.tokens = new HashSet<>(Arrays.asList(tokens));
		this.nodes = new MutableNode[]{
			new MutableNode<>(this, 0, false),
			new MutableNode<>(this, 1, true)
		};
		nodes[0].addTransitions(this.tokens, nodes[1]);
	}

	public DFA(Set<T> alphabet, DFA<T> source){
		this.tokens = CollectionUtils.union(alphabet, source.tokens);
		final boolean complete = source.isComplete(this.tokens);
		this.nodes = IntStream.range(0, source.nodes.length + (complete ? 0 : 1))
			.mapToObj(i -> new MutableNode<>(this, i, i < source.nodes.length && source.nodes[i].isTerminating()))
			.toArray(MutableNode[]::new);

		for(int i = 0; i < source.nodes.length; i++){
			final var node = nodes[i];
			source.nodes[node.index()].transitions().forEach(
				(token, srcdests) -> {
					if(srcdests.size() != 1){
						throw new IllegalStateException();
					}
					srcdests.forEach(
						srcdest -> node.addTransition(token, nodes[srcdest.index()])
					);
				}
			);
		}

		if(!complete){
			var sink = nodes[source.nodes.length];
			for(var node: nodes){
				node.addTransitions(
					CollectionUtils.exclusion(tokens, node.tokens()),
					sink
				);
			}
		}

	}

	public boolean isComplete(Set<T> alphabet){
		if(!tokens.containsAll(alphabet)){
			return false;
		}
		return null == ESupplier.from(nodes)
			.exclude(node -> node.tokens().equals(tokens))
			.get();
	}

	@Override
	public int nodeCount(){
		return nodes.length;
	}

	@Override
	public Node<T> node(int index){
		return nodes[index];
	}

	@Override
	public Set<T> tokens(){
		return Collections.unmodifiableSet(tokens);
	}

	@Override
	public boolean isDeterministic(){
		return true;
	}

	public boolean accepts(T...tokens){
		var state = node(0);
		for(T token: tokens){
			state = state.transition(token);
			if(state == null){
				return false;
			}
		}
		return state.isTerminating();
	}

	public class TokenString{
		private final List<T> tokens;
		private final Set<String> classes;

		private TokenString(Set<String> classes, List<T> tokens){
			this.tokens = Collections.unmodifiableList(tokens);
			this.classes = Collections.unmodifiableSet(classes);
		}

		public String toString(){
			return classes + ": " + value();
		}

		public String value(){
			final var builder = new StringBuilder();
			for(var t: tokens){
				builder.append(t);
			}
			return builder.toString();
		}

		public Set<String> classes(){
			return classes;
		}

		public List<T> tokens(){
			return tokens;
		}
	}

	public TokenString read(final ReadMode mode, T...tokens){
		return read(mode, 0, tokens);
	}

	public TokenString read(final ReadMode mode, final int index, final T...tokens){
		class Terminating{
			final int index;
			final Node<T> state;

			public Terminating(int index, Node<T> state){
				this.index = index;
				this.state = state;
			}
		}

		var state = node(0);
		var t = state.isTerminating() ? new Terminating(index, state) : null;
		int depth = index;
		while(depth < tokens.length && state.isTerminable()){
			state = state.transition(tokens[depth++]);
			if(state == null){
				break;
			}
			if(state.isTerminating()){
				t = new Terminating(depth, state);
				if(mode == ReadMode.EAGER){
					break;
				}
			}
		}
		if(t != null){
			final var result = new ArrayList<T>();
			for(int i = index; i < t.index; i++){
				result.add(tokens[i]);
			}
			return new TokenString(t.state.classes(), result);
		}
		return null;
	}

	public ESupplier<TokenString> tokenise(T...input){
		return new ESupplier<>(){
			int index = 0;
			@Override
			public TokenString get(){
				if(index < input.length){
					final var result = read(ReadMode.GREEDY, index, input);
					if(result == null){
						final List<T> tokens = new ArrayList<>();
						for(int i = index; i < input.length; i++){
							tokens.add(input[i]);
							index = input.length;
							return new TokenString(Collections.emptySet(), tokens);
						}
					}
					final int size = tokens.size();
					if(size == 0){
						index = input.length;
					}
					else{
						index += result.tokens.size();
					}
					return result;
				}
				return null;
			}
		};
	}
}
