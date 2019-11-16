package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.automata.data.ReadMode;
import localgoat.lang.compiler.automata.data.Token;
import localgoat.lang.compiler.automata.data.TokenString;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.expression.Expression;
import localgoat.lang.compiler.automata.utility.Builder;
import localgoat.util.CollectionUtils;
import localgoat.util.ESupplier;
import localgoat.util.ValueCache;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DFA extends AbstractAutomaton{
	public static void main(String...args){

		final var converter = new Converter();

		converter.addClass('u', c -> 'A' <= c && c <= 'Z');
		converter.addClass('l', c -> 'a' <= c && c <= 'z');
		converter.addClass('s', c -> c == '_');
		converter.addClass('h', c -> c == '-');
		converter.addClass('q', c -> c == '\"');
		converter.addClass('e', c -> c == '\\');

		final var expressions = new HashMap<>();
		expressions.put("string", "q*+(~q, eq)q");
		expressions.put("class-name", "*<1+>(u*l)");
		expressions.put("constant", "*<1+>u*(s*<1+>u)");
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

		System.err.println(dfa.read(ReadMode.GREEDY, Token.from("ClassName")));
		System.err.println(dfa.read(ReadMode.GREEDY, Token.from("HTTP_")));
		System.err.println(dfa.read(ReadMode.GREEDY, Token.from("ENUM_CONSTANTsome-other-shit")));
		System.err.println(dfa.read(ReadMode.GREEDY, Token.from("instance-identifier")));

	}

	public static DFA lambda(){
		final var builder = new Builder(Collections.emptySet());
		builder.addNode(true);
		return builder.buildDFA();
	}

	public static DFA of(Token...tokens){
		final var tokenSet = new HashSet<>(Arrays.asList(tokens));
		final var builder = new Builder(tokenSet);
		final var n0 = builder.addNode();
		final var n1 = builder.addNode(true);
		n0.addTransitions(tokenSet, n1);
		return builder.buildDFA();
	}

	public DFA(Builder builder){
		super(builder);
		if(!isDeterministic()){
			throw new IllegalStateException();
		}
	}

	public boolean isComplete(Set<Token> alphabet){
		if(!tokens.containsAll(alphabet)){
			return false;
		}
		return null == ESupplier.from(nodes)
			.exclude(node -> node.tokens().equals(tokens))
			.get();
	}

	public boolean accepts(Token...tokens){
		var state = node(0);
		for(Token token: tokens){
			state = state.transition(token).node();
			if(state == null){
				return false;
			}
		}
		return state.isTerminating();
	}

	public TokenString read(final ReadMode mode, Token...tokens){
		return read(mode, 0, tokens);
	}

	public static class StateIndex{
		final int index;
		final Node state;

		public StateIndex(int index, Node state){
			this.index = index;
			this.state = state;
		}
	}

	public TokenString read(final ReadMode mode, final int index, final Token...tokens){
		if(index == tokens.length){
			throw new IllegalArgumentException();
		}

		var state = node(0);
		var t = state.isTerminating() ? new StateIndex(index, state) : null;
		int depth = index;
		while(depth < tokens.length && state.isTerminable()){
			final var transition = state.transition(tokens[depth++]);
			if(transition == null){
				state = null;
				break;
			}
			state = transition.node();
			if(state.isTerminating()){
				t = new StateIndex(depth, state);
				if(mode == ReadMode.EAGER){
					break;
				}
			}
		}
		if(t != null){
			final var result = new ArrayList<Token>();
			for(int i = index; i < t.index; i++){
				result.add(tokens[i]);
			}
			return new TokenString(t.state.types(), result);
		}
		if(depth == index){
			return new TokenString(Collections.emptySet(), Collections.singletonList(tokens[index]));
		}
		else{
			final var list = new ArrayList<Token>();
			for(int i = index; i < depth; i++){
				list.add(tokens[i]);
			}
			return new TokenString(Collections.emptySet(), list);
		}
	}

	public ESupplier<TokenString> tokenise(Token...input){
		if(accepts()){
			throw new UnsupportedOperationException("Cannot tokenise if the empty String is accepted.");
		}
		return new ESupplier<>(){
			int index = 0;
			@Override
			public TokenString get(){
				if(index < input.length){
					final var result = read(ReadMode.GREEDY, index, input);
					if(result == null){
						final List<Token> tokens = new ArrayList<>();
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
						index += result.tokens().size();
					}
					return result;
				}
				return null;
			}
		};
	}
}
