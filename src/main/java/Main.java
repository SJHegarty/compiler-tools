import localgoat.lang.struct.ContentTree;
import localgoat.lang.ui.LangPane;
import localgoat.lang.ui.LangTree;
import localgoat.util.ui.document.InsertListener;

import javax.swing.*;
import java.awt.Container;

public class Main{
	public static void main(String... args){
		final var frame = new JFrame();
		final var pane = new LangPane();
		final var recpane = new LangPane();
		recpane.setEditable(false);
		final var tree = new LangTree();
		final var content = new Container();

		pane.getDocument().addDocumentListener(
			(InsertListener)(e) -> {
				var text = pane.getText().replaceAll("\r\n", "\n");
				var contentTree = new ContentTree(text);
				var reconstruction = contentTree.reconstruct();
				if(!text.equals(reconstruction)){
					throw new IllegalStateException();
				}
				var code = contentTree.getCode();
				if(code.size() == 1){
					tree.setCodeTree(code.get(0));
				}
				else{
					tree.setCodeTrees(code);
				}

				recpane.setText(reconstruction);
			}
		);

		content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
		content.add(new JScrollPane(pane));
		content.add(new JScrollPane(recpane));
		content.add(new JScrollPane(tree));
		frame.getContentPane().add(content);

		frame.setSize(800, 600);
		frame.setVisible(true);
	}

}
