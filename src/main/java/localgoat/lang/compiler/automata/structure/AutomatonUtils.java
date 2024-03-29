package localgoat.lang.compiler.automata.structure;

import localgoat.lang.compiler.Parser;
import localgoat.lang.compiler.automata.ReadMode;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.expression.ExpressionParser;
import localgoat.lang.compiler.automata.operation.Convert;
import localgoat.lang.compiler.token.StringToken;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenSeries;
import localgoat.util.streaming.ESupplier;

import java.util.*;
import java.util.stream.Collectors;

public class AutomatonUtils implements Parser<Symbol, TokenSeries>{
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
		final var utils = new AutomatonUtils(converter.build(expr));

		System.err.println(utils.read(ReadMode.GREEDY, Symbol.from("ClassName")));
		System.err.println(utils.read(ReadMode.GREEDY, Symbol.from("HTTP_")));
		System.err.println(utils.read(ReadMode.GREEDY, Symbol.from("ENUM_CONSTANTsome-other-shit")));
		System.err.println(utils.read(ReadMode.GREEDY, Symbol.from("instance-identifier")));

	}

	private final Automaton a;
	public AutomatonUtils(Automaton a){
		this.a = new Convert().apply(a);
	}

	public StringToken read(final ReadMode mode, Symbol...tokens){
		return read(mode, 0, Arrays.asList(tokens));
	}

	public boolean accepts(Symbol...tokens){
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
	public TokenSeries parse(List<Symbol> values){
		return new TokenSeries(tokenise(values).toStream().collect(Collectors.toList()));
	}

	public static class StateIndex{
		final int index;
		final Node state;

		public StateIndex(int index, Node state){
			this.index = index;
			this.state = state;
		}
	}

	public StringToken read(final ReadMode mode, final int index, final List<Symbol> tokens){
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
			final var result = new ArrayList<Symbol>();
			for(int i = index; i < t.index; i++){
				result.add(tokens.get(i));
			}
			return new StringToken(result, t.state.types());
		}
		if(depth == index){
			return new StringToken(Collections.singletonList(tokens.get(index)));
		}
		else{
			final var list = new ArrayList<Symbol>();
			for(int i = index; i < depth; i++){
				list.add(tokens.get(i));
			}
			return new StringToken(list);
		}
	}

	public ESupplier<StringToken> tokenise(Symbol...symbols){
		return tokenise(Arrays.asList(symbols));
	}

	public ESupplier<StringToken> tokenise(List<Symbol> input){
		if(accepts()){
			throw new UnsupportedOperationException("Cannot tokenise if the empty String is accepted.");
		}
		return new ESupplier<>(){
			int index = 0;
			@Override
			public StringToken get(){
				if(index < input.size()){
					final var result = read(ReadMode.GREEDY, index, input);
					if(result == null){
						final List<Symbol> tokens = new ArrayList<>();
						for(int i = index; i < input.size(); i++){
							tokens.add(input.get(i));
							index = input.size();
							return new StringToken(tokens);
						}
					}
					final int size = a.tokens.size();
					if(size == 0){
						index = input.size();
					}
					else{
						index += result.length();
					}
					return result;
				}
				return null;
			}
		};
	}
}
