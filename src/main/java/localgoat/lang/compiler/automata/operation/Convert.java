package localgoat.lang.compiler.automata.operation;

import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.structure.*;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.lang.compiler.automata.utility.NodeBuilder;
import localgoat.util.ESupplier;
import localgoat.util.ValueCache;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Convert implements Function<NFA, DFA>{
	@Override
	public DFA apply(NFA nfa){
		final var builder = new Builder(nfa.tokens());
		final Map<Node, Set<Node>> lambdaTransitable = IntStream.range(0, nfa.nodeCount())
			.mapToObj(nfa::node)
			.collect(
				Collectors.toMap(
					node -> node,
					node -> ESupplier.of(node)
						.branchingMap(
							true,
							n -> ESupplier.from(n.transitions(null))
								.map(t -> t.node())
						)
						.toStream()
						.collect(Collectors.toSet())
				)
			);

		final var nodesMap = new LinkedHashMap<Set<Node>, NodeBuilder>();
		final var reverseMap = new HashMap<NodeBuilder, Set<Node>>();
		final var nodeBuilder = new ValueCache<Set<Node>, NodeBuilder>(
			nodesMap,
			nodeset -> {
				final Set<Node> reachable = nodeset.stream()
					.flatMap(n -> lambdaTransitable.get(n).stream())
					.collect(Collectors.toSet());

				final Set<TypeState> names = ESupplier.from(reachable)
					.flatMap(n -> ESupplier.from(n.typeStates()))
					.toStream().collect(Collectors.toSet());

				final var rv = builder.addNode(names);
				nodesMap.put(nodeset, rv);
				reverseMap.put(rv, nodeset);
				return rv;
			}
		);

		final Queue<NodeBuilder> nodesQueue = new ArrayDeque<>();
		nodesQueue.add(
			nodeBuilder.get(
				lambdaTransitable.get(nfa.node(0))
			)
		);
		class Transition{
			final Token token;
			final Node destination;

			Transition(Token token, Node destination){
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
						Token token = e.getKey();
						return e.getValue().stream()
							.map(t -> t.node())
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
		return builder.buildDFA();
		//this.nodes = nodesMap.values().stream().toArray(MutableNode[]::new);
	}
}
