package localgoat.lang.compiler;

import localgoat.lang.compiler.automata.expression.Converter;
import localgoat.lang.compiler.automata.operation.Or;
import localgoat.lang.compiler.automata.structure.Automaton;
import localgoat.lang.compiler.automata.structure.AutomatonUtils;
import localgoat.lang.compiler.automata.structure.Type;
import localgoat.lang.compiler.token.*;
import localgoat.util.CachingSupplier;
import localgoat.util.ESupplier;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IndentParser implements Parser<Symbol, TokenSeries>{

	public static final int TAB_WIDTH = 4;
	public static final String WHITE_SPACE = "white-space";
	public static final String IGNORED = "ignored";
	public static final String LINE_FEED = "line-feed";

	private static final Converter CONVERTER = new Converter();
	private static final Map<String, String> EXPRESSIONS = new TreeMap<>();
	private static final Automaton AUTOMATON;

	public static final StringToken LINE_FEED_TOKEN = new StringToken(
		Collections.singletonList(
			new Symbol('\n')
		),
		Collections.singleton(
			new Type(
				"line-feed",
				TokenLayer.SYNTACTIC,
				new HashSet<>(Arrays.asList(WHITE_SPACE, IGNORED))
			)
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
		CONVERTER.setLayer(LINE_FEED, TokenLayer.SYNTACTIC);
		CONVERTER.setLayer(WHITE_SPACE, TokenLayer.SYNTACTIC);
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

		public Line(StringToken[] values){
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
							sum += TAB_WIDTH - (sum % TAB_WIDTH);
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
		public String toString(){
			return value.toString();
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
		public Token filter(FilteringContext context){
			throw new UnsupportedOperationException();
		}

		@Override
		public TokenLayer filteringLayer(){
			throw new UnsupportedOperationException();
		}

		class FilteredTree implements TokenTree{
			private final Token head;
			private final CachingSupplier<List<Token>> children;
			private final Token tail;
			private final TokenLayer filteringLayer;

			FilteredTree(TokenTree tree, FilteringContext context){
				this.head = Optional.ofNullable(tree.head())
					.map(h -> h.filter(context))
					.orElse(null);

				this.tail = Optional.ofNullable(tree.tail())
					.map(t -> t.filter(context))
					.orElse(null);

				var childContext = context.child();
				this.children = new CachingSupplier<>(
					() -> ESupplier.from(tree.children())
						.map(c -> c.filter(childContext))
						.toStream()
						.collect(Collectors.toList())
				);

				this.filteringLayer = context.layer();
			}

			@Override
			public Token head(){
				return head;
			}

			@Override
			public List<? extends Token> children(){
				return children.get();
			}

			@Override
			public Token tail(){
				return tail;
			}

			@Override
			public Token filter(FilteringContext context){
				return new FilteredTree(this, context);
			}

			@Override
			public TokenLayer filteringLayer(){
				return filteringLayer;
			}
		}

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

				@Override
				public Token filter(FilteringContext context){
					return new FilteredTree(this, context);
				}

				@Override
				public TokenLayer filteringLayer(){
					return DepthTree.this.filteringLayer();
				}

			};
		}
	}

	@Override
	public TokenSeries parse(List<Symbol> values){
		final var lines = ESupplier.from(utils.parse(values).children())
			.map(t -> (StringToken)t)
			.split(t -> t.hasClass(LINE_FEED), true)
			.map(tokens -> new Line(tokens.toArray(StringToken[]::new)))
			.toStream()
			.collect(Collectors.toCollection(ArrayDeque::new));

		final List<TokenTree> trees = new ArrayList<>();
		while(!lines.isEmpty()){
			trees.add(new DepthTree(lines).trim());
		}
		return new TokenSeries(trees);
	}


}
