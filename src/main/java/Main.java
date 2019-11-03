import localgoat.lang.compiler.ContentTree;
import localgoat.lang.ui.LangPane;
import localgoat.lang.ui.LangTree;
import localgoat.util.ui.document.InsertListener;

import javax.swing.*;

public class Main{
	public static void main(String... args){
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
		//recpane.setEditable(false);

		pane.getDocument().addDocumentListener(
			(InsertListener)(e) -> {
				var text = pane.getText().replaceAll("\r\n", "\n");
				var contentTree = new ContentTree(text);
				recpane.setText(contentTree.reconstruct());
				actualised.setText(contentTree.effective());
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
