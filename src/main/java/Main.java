import localgoat.lang.compiler.IndentParser;
import localgoat.lang.compiler.omega.OmegaTokens;
import localgoat.lang.compiler.omega.OmegaValidators;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.compiler.token.Token;
import localgoat.lang.compiler.token.TokenString;
import localgoat.lang.compiler.token.TokenTree;
import localgoat.lang.ui.LangPane;
import localgoat.lang.ui.LangTree;
import localgoat.util.ESupplier;
import localgoat.util.ui.document.InsertRemoveListener;

import javax.swing.*;
import java.io.IOException;
import java.io.UncheckedIOException;

public class Main{
	public static void main(String...args) throws IOException{
		launchFrame();
	}

	static String readTest(){
		try(final var stream = Main.class.getResource("examples/Test.brt").openStream()){
			return new String(stream.readAllBytes());
		}
		catch(IOException e){
			throw new UncheckedIOException(e);
		}
	}
	static void printTest() throws IOException{
		final var parser = new IndentParser(
			OmegaTokens.AUTOMATON,
			OmegaValidators.TAIL_VALIDATORS
		);
		try(final var stream = Main.class.getResource("examples/Test.brt").openStream()){
			final var content = new String(stream.readAllBytes());
			final var supplier = ESupplier.from(parser.parse(Symbol.from(content)))
				.map(tree -> (Token)tree)
				.interleave(IndentParser.LINE_FEED_TOKEN)
				.branchDepthFirst(
					false,
					t -> (t instanceof TokenString) ? null : ((TokenTree)t).tokens()
				)
				.mapOrNull(t -> (TokenString)t);

			for(var token: supplier){
				System.err.print(token.value());
			}
		};
	}
	static void launchFrame(){
		final var parser = new IndentParser(
			OmegaTokens.AUTOMATON,
			OmegaValidators.TAIL_VALIDATORS
		);

		final var frame = new JFrame();
		final var pane = new LangPane();
		final var recpane = new LangPane();
		final var actualised = new LangPane();
		final var tree = new LangTree();

		recpane.setEditable(false);
		actualised.setEditable(false);
		final var tabs = new JTabbedPane();
		tabs.add("Literal", new JScrollPane(pane));
		tabs.add("Reconstruction", new JScrollPane(recpane));
		tabs.add("Effective", new JScrollPane(actualised));
		tabs.add("Tree", new JScrollPane(tree));
		//final var recpane = new LangPane();
		recpane.setEditable(false);

		pane.getDocument().addDocumentListener(
			(InsertRemoveListener)(e) -> {
				var text = pane.getText().replaceAll("\r\n", "\n");
				var parsed = parser.parse(Symbol.from(text));
				var reconstructed = ESupplier.from(parsed)
					.map(t -> t.value())
					.interleave("\n")
					.concatenate();
				recpane.setText(reconstructed);
				//actualised.setText(contentTree.effective().reconstruct());
				if(parsed.size() == 1){
					tree.setCodeTree(parsed.get(0));
				}
				else{
					tree.setCodeTrees(parsed);
				}

				//recpane.setText(reconstruction);
			}
		);

		//content.add(new JScrollPane(recpane));
		frame.getContentPane().add(tabs);

		frame.setSize(800, 600);
		frame.setVisible(true);
		SwingUtilities.invokeLater(() -> pane.setText(readTest()));
	}

}
