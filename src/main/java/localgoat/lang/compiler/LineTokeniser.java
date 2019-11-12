package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.DFA;
import localgoat.lang.compiler.automata.StringClass;
import localgoat.lang.compiler.automata.Token;
import localgoat.lang.compiler.automata.TokenString;
import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.util.ESupplier;
import localgoat.util.functional.CharPredicate;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LineTokeniser{


	public static final String WHITE_SPACE = "white-space";
	public static final String IGNORED = "ignored";

	public static final TokenString<Token<Character>> LINE_FEED = new TokenString<>(
		Collections.singleton(
			new StringClass(
				"line-feed",
				Collections.singleton(WHITE_SPACE)
			)
		),
		Collections.singletonList(
			Token.LINE_FEED
		)
	);

	public static final int TAB_WIDTH = 4;

	private final DFA<Token<Character>> dfa;

	public LineTokeniser(DFA<Token<Character>> dfa){
		this.dfa = dfa;
	}

	public class CodeLine{
		public final int lineindex;
		public final List<TokenString<Token<Character>>> tokens;

		CodeLine(String line, int index){
			this.lineindex = index;
			this.tokens = Arrays.asList(
				dfa.tokenise(Token.from(line))
					.toStream()
					.toArray(TokenString[]::new)
			);
		}

		public List<TokenString<Token<Character>>> contentTokens(){
			var deque = new ArrayDeque<>(tokens);
			deque.pollFirst();
			for(; !deque.isEmpty() && deque.peekFirst().classes().contains("white-space"); deque.pollFirst()) ;
			for(; !deque.isEmpty() && deque.peekLast().classes().contains("white-space"); deque.pollLast()) ;
			return Collections.unmodifiableList(
				new ArrayList<>(deque)
			);
		}

		public TokenString<Token<Character>> last(Predicate<TokenString<Token<Character>>> filter){
			for(int i = tokens.size() - 1; i >= 0; i--){
				var token = tokens.get(i);
				if(filter.test(token)){
					return token;
				}
			}
			return null;
		}

		public TokenString<Token<Character>> first(Predicate<TokenString<Token<Character>>> filter){
			for(var token : tokens){
				if(filter.test(token)){
					return token;
				}
			}
			return null;
		}

		public List<TokenString<Token<Character>>> all(Predicate<TokenString<Token<Character>>> filter){
			return Collections.unmodifiableList(
				tokens.stream()
					.filter(filter)
					.collect(Collectors.toList())
			);
		}

		public String content(){
			var builder = new StringBuilder();
			for(TokenString<Token<Character>> t : contentTokens()){
				builder.append(t);
			}
			return builder.toString();
		}

		public String prefix(){
			handler:
			{
				if(tokens.size() == 0){
					break handler;
				}
				final var token = tokens.get(0);
				if(!token.hasClass(s -> s.hasFlag(IGNORED))){
					break handler;
				}
				return token.value();
			}
			return "";
		}

		public String reconstruct(){
			final var builder = new StringBuilder();
			for(var token : tokens){
				builder.append(token.value());
			}
			return builder.toString();
		}

		public int depth(){
			int sum = 0;
			for(char c : prefix().toCharArray()){
				switch(c){
					case '\t':{
						sum += TAB_WIDTH - (sum % TAB_WIDTH);
						break;
					}
					case ' ':{
						sum += 1;
						break;
					}
					default:{
						//TODO: Figure out if this happens, and if so what characters cause it
					}
				}
			}
			return sum / TAB_WIDTH;
		}

		@Override
		public String toString(){
			final var builder = new StringBuilder();
			for(var t : tokens){
				builder.append(t);
			}
			return builder.toString();
		}
	}
}
