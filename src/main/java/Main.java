import localgoat.lang.ui.LangPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Font;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Main{
	public static void main(String... args){
		final var frame = new JFrame();
		final var pane = new LangPane();

		final var tree = new JTree();
		final var font = new Font(Font.MONOSPACED, Font.BOLD, 12);
		final var content = new Container();


		content.setLayout(new BorderLayout());
		content.add(new JScrollPane(pane), BorderLayout.CENTER);
		content.add(new JScrollPane(tree), BorderLayout.SOUTH);
		frame.getContentPane().add(content);

		{
			final int charcount = 4;
			final int tabsize = charcount * pane.getFontMetrics(font).charWidth('w');

			var tabs = new TabSet(
				IntStream.range(0, 100)
					.mapToObj(i -> new TabStop(i * tabsize))
					.toArray(TabStop[]::new)
			);
			var paraSet = StyleContext.getDefaultStyleContext()
				.addAttribute(
					SimpleAttributeSet.EMPTY,
					StyleConstants.TabSet,
					tabs
				);

			pane.setFont(font);
			pane.setParagraphAttributes(paraSet, false);
		}

		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	static class ContentTree{
		ContentTree(String text){
			//var lines =
		}
	}
}
