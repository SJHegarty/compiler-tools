package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.operation.Or;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.AutomatonUtils;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.*;
import localgoat.util.ESupplier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IndentParser implements Parser<Symbol, TokenTree>{

	public static final String WHITE_SPACE = "white-space";
	public static final String IGNORED = "ignored";
	public static final String LINE_FEED = "line-feed";

	private static final Converter CONVERTER = new Converter();
	private static final Map<String, String> EXPRESSIONS = new TreeMap<>();
	private static final Automaton AUTOMATON;

	public static final TokenString LINE_FEED_TOKEN = new TokenString(
		Collections.singleton(
			new Type(
				"line-feed",
				new HashSet<>(Arrays.asList(WHITE_SPACE, IGNORED))
			)
		),
		Collections.singletonList(
			new Symbol('\n')
		)
	);

	static{
		EXPRESSIONS.put(
			String.format(
				"%s --%s --%s",
				WHITE_SPACE,
				IGNORED,
				WHITE_SPACE
			),
			"*<1+>+('\t', ' ')"
		);
		EXPRESSIONS.put(
			String.format(
				"%s --%s --%s",
				LINE_FEED,
				IGNORED,
				WHITE_SPACE
			),
			"?'\r''\n'"
		);

		final var builder = new StringBuilder();
		builder.append("+(\n\t");

		ESupplier.from(EXPRESSIONS.entrySet())
			.map(
				e -> String.format(
					"@<%s>(%s)",
					e.getKey(),
					e.getValue()
				)
			)
			.interleave(",\n\t")
			.forEach(s -> builder.append(s));

		builder.append("\n)");

		final var pattern = builder.toString();
		AUTOMATON = CONVERTER.build(pattern);
	}

	private final AutomatonUtils utils;
	private final Function<TokenSeries, Consumer<TokenSeries>> validators;

	public IndentParser(Automaton language, Function<TokenSeries, Consumer<TokenSeries>> tailValidators){
		this.utils = new AutomatonUtils(
			new Or().apply(
				AUTOMATON,
				language
			)
		);
		this.validators = tailValidators;
	}

	private static class Line{
		private final int depth;
		private final TokenSeries value;

		public Line(TokenString[] values){
			this.value = new TokenSeries(Arrays.asList(values));
			if(values.length == 0){
				depth = Integer.MAX_VALUE;
			}
			else if(!values[0].hasClass(WHITE_SPACE)){
				depth = 0;
			}
			else{
				int sum = 0;
				for(char c : values[0].value().toCharArray()){
					switch(c){
						case '\t':{
							sum += LineTokeniser.TAB_WIDTH - (sum % LineTokeniser.TAB_WIDTH);
							break;
						}
						case ' ':{
							sum += 1;
							break;
						}
					}
				}
				depth = sum;
			}
		}
	}

	private class DepthTree implements TokenTree{
		private final TokenSeries head;
		private final List<DepthTree> children;
		private final TokenSeries tail;

		public DepthTree(Queue<Line> lines){
			final int depth;
			{
				final var line = lines.poll();
				this.head = line.value;
				if(lines.isEmpty()){
					this.children = Collections.emptyList();
					this.tail = null;
					return;
				}
				depth = line.depth;
			}
			final var children = new ArrayList<DepthTree>();
			final var validator = validators.apply(head);
			block:{
				while(!lines.isEmpty()){
					var line = lines.peek();
					if(line.depth > depth){
						children.add(new DepthTree(lines));
					}
					else{
						if(validator != null){
							if(line.depth == depth){
								tail = lines.poll().value;
								validator.accept(tail);
								break block;
							}
							throw new ValidationException(
								String.format("Tail expected for head: %s", head)
							);
						}
						break;
					}
				}
				tail = null;
			}
			this.children = Collections.unmodifiableList(children);

		}
		@Override
		public TokenTree head(){
			return head;
		}

		@Override
		public List<DepthTree> children(){
			return children;
		}

		@Override
		public TokenTree tail(){
			return tail;
		}



		@Override
		public TokenTree trim(){
			if(tail == null && children.isEmpty()){
				return head;
			}
			final var children = ESupplier.from(this.children)
				.map(c -> c.trim())
				.toStream()
				.collect(Collectors.toList());

			return new TokenTree(){
				@Override
				public Token head(){
					return head;
				}

				@Override
				public List<? extends Token> children(){
					return children;
				}

				@Override
				public Token tail(){
					return tail;
				}

				public ESupplier<? extends Token> tokens(){
					return TokenTree.super.tokens()
						.map(t -> (Token)t)
						.interleave(LINE_FEED_TOKEN);
				}
			};
		}
	}

	@Override
	public List<TokenTree> parse(List<Symbol> values){

		final var lines = ESupplier.from(utils.parse(values))
			.split(t -> t.hasClass(LINE_FEED))
			.map(tokens -> new Line(tokens.toArray(TokenString[]::new)))
			.toStream()
			.collect(Collectors.toCollection(ArrayDeque::new));

		final List<TokenTree> trees = new ArrayList<>();
		while(!lines.isEmpty()){
			trees.add(new DepthTree(lines).trim());
		}
		return Collections.unmodifiableList(trees);
	}


}
