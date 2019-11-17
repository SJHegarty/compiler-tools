package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.Parser;
import localgoat.lang.compiler.automata.ReadMode;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.expression.ExpressionParser;
import localgoat.lang.compiler.automata.operation.Convert;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenString;
import localgoat.util.ESupplier;

import java.util.*;
import java.util.stream.Collectors;

public class AutomatonUtils implements Parser<Symbol, TokenString>{
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

		final var parser = new ExpressionParser();
		final var expr = parser.parse(Symbol.from(builder.toString()));
		final var utils = new AutomatonUtils(converter.build(expr.get(0)));

		System.err.println(utils.read(ReadMode.GREEDY, Symbol.from("ClassName")));
		System.err.println(utils.read(ReadMode.GREEDY, Symbol.from("HTTP_")));
		System.err.println(utils.read(ReadMode.GREEDY, Symbol.from("ENUM_CONSTANTsome-other-shit")));
		System.err.println(utils.read(ReadMode.GREEDY, Symbol.from("instance-identifier")));

	}

	private final Automaton a;
	public AutomatonUtils(Automaton a){
		this.a = new Convert().apply(a);
	}

	public TokenString read(final ReadMode mode, Token...tokens){
		return read(mode, 0, Arrays.asList(tokens));
	}

	public boolean accepts(Token...tokens){
		var state = a.node(0);
		for(Token token: tokens){
			final var transition = state.transition(token);
			if(transition == null){
				return false;
			}
			state = transition.node();
		}
		return state.isTerminating();
	}

	@Override
	public List<TokenString> parse(List<Symbol> values){
		return tokenise(values).toStream().collect(Collectors.toList());
	}

	public static class StateIndex{
		final int index;
		final Node state;

		public StateIndex(int index, Node state){
			this.index = index;
			this.state = state;
		}
	}

	public TokenString read(final ReadMode mode, final int index, final List<? extends Token> tokens){
		if(index == tokens.size()){
			throw new IllegalArgumentException();
		}

		var state = a.node(0);
		var t = state.isTerminating() ? new StateIndex(index, state) : null;
		int depth = index;
		while(depth < tokens.size() && state.isTerminable()){
			final var transition = state.transition(tokens.get(depth++));
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
				result.add(tokens.get(i));
			}
			return new TokenString(t.state.types(), result);
		}
		if(depth == index){
			return new TokenString(Collections.emptySet(), Collections.singletonList(tokens.get(index)));
		}
		else{
			final var list = new ArrayList<Token>();
			for(int i = index; i < depth; i++){
				list.add(tokens.get(i));
			}
			return new TokenString(Collections.emptySet(), list);
		}
	}

	public ESupplier<TokenString> tokenise(Symbol...symbols){
		return tokenise(Arrays.asList(symbols));
	}

	public ESupplier<TokenString> tokenise(List<Symbol> input){
		if(accepts()){
			throw new UnsupportedOperationException("Cannot tokenise if the empty String is accepted.");
		}
		return new ESupplier<>(){
			int index = 0;
			@Override
			public TokenString get(){
				if(index < input.size()){
					final var result = read(ReadMode.GREEDY, index, input);
					if(result == null){
						final List<Token> tokens = new ArrayList<>();
						for(int i = index; i < input.size(); i++){
							tokens.add(input.get(i));
							index = input.size();
							return new TokenString(Collections.emptySet(), tokens);
						}
					}
					final int size = a.tokens.size();
					if(size == 0){
						index = input.size();
					}
					else{
						index += result.children().size();
					}
					return result;
				}
				return null;
			}
		};
	}
}
