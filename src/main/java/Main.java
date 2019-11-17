import localgoat.lang.compiler.ContentTree;
import localgoat.lang.compiler.IndentParser;
import localgoat.lang.compiler.omega.Omega;
import localgoat.lang.compiler.omega.OmegaValidators;
import localgoat.lang.compiler.token.Symbol;
import localgoat.lang.ui.LangPane;
import localgoat.lang.ui.LangTree;
import localgoat.util.ESupplier;
import localgoat.util.ui.document.InsertRemoveListener;

import javax.swing.*;
import java.io.IOException;

public class Main{
	public static void main(String...args) throws IOException{
		final var parser = new IndentParser(
			Omega.AUTOMATON,
			OmegaValidators.TAIL_VALIDATORS
		);
		try(final var stream = Main.class.getResource("examples/Test.brt").openStream()){
			final var content = new String(stream.readAllBytes());
			final var supplier = ESupplier.from(parser.parse(Symbol.from(content)));
			for(var token: supplier){
				System.err.println(token.value());
			}
			/*for(var line: supplier.split(t -> t.hasClass(IndentParser.LINE_FEED), false)){
				System.err.print("\u001B[37m[\u001B[0m");
				for(var t: line){
					System.err.print(t.value());
				}
				System.err.println("\u001B[37m]\u001B[0m");
			}*/
		};
	}
	static void launchFrame(){
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
				var contentTree = new ContentTree(text);
				recpane.setText(contentTree.reconstruct());
				actualised.setText(contentTree.effective().reconstruct());
				var code = contentTree.getCode();
				if(code.size() == 1){
					tree.setCodeTree(code.get(0));
				}
				else{
					tree.setCodeTrees(code);
				}

				//recpane.setText(reconstruction);
			}
		);

		//content.add(new JScrollPane(recpane));
		frame.getContentPane().add(tabs);

		frame.setSize(800, 600);
		frame.setVisible(true);
	}

}
