package localgoat.lang.ui;

import localgoat.util.ui.document.AllListener;
import localgoat.util.ui.document.InsertListener;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import java.awt.Color;
import java.awt.Font;
import java.util.stream.IntStream;

public class LangPane extends JTextPane{

	public LangPane(){

		final int charcount = 4;
		final var font = new Font(Font.MONOSPACED, Font.BOLD, 12);
		final int tabsize = charcount * getFontMetrics(font).charWidth('w');

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

		setFont(font);
		setParagraphAttributes(paraSet, false);

		var doc = getStyledDocument();
		var context = StyleContext.getDefaultStyleContext();
		final var attributes = new AttributeSet[]{
			context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.GREEN),
			context.addAttribute(context.getEmptySet(), StyleConstants.Foreground, Color.RED)
		};

		doc.addDocumentListener(
			(AllListener)(e) -> {
				SwingUtilities.invokeLater(
					() -> {
						int index = getCaretPosition() - 1;
						//doc.setCharacterAttributes(index, 1, attributes[index & 1], false);
					}
				);
			}
		);

	}
}
