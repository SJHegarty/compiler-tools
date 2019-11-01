import localgoat.lang.struct.CodeTree;
import localgoat.lang.ui.LangPane;
import localgoat.lang.ui.LangTree;
import localgoat.lang.ui.StrippedListener;

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
			(StrippedListener)() -> {
				var code = new CodeTree(pane.getText());
				tree.setCodeTree(code);
				recpane.setText(code.reconstruct());
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

	static class ContentTree{
		ContentTree(String text){
			//var lines =
		}
	}
}
