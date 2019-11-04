package localgoat.lang.ui;

import localgoat.lang.compiler.CodeLine;
import localgoat.lang.compiler.ContentTree;
import localgoat.lang.compiler.Token;
import localgoat.lang.compiler.TokenType;
import localgoat.util.ESupplier;
import localgoat.util.ui.document.AllListener;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class LangPane extends JTextPane{

	private ContentTree content;

	public LangPane(){

		this.setBackground(new Color(0xff404040));
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

		final Function<Color, AttributeSet> builder = (colour) -> context.addAttribute(
			context.getEmptySet(),
			StyleConstants.Foreground,
			colour
		);

		final var missing = builder.apply(Color.ORANGE);
		final Map<TokenType, AttributeSet> atts = new HashMap<>();
		atts.put(
			TokenType.KEYWORD,
			builder.apply(new Color(0xff80c0ff))
		);

		final var colours = new HashMap<TokenType, Color>();
		doc.addDocumentListener(
			new AllListener(){
				final AtomicBoolean open = new AtomicBoolean(true);
				@Override
				public void update(DocumentEvent e){
					if(open.getAndSet(false)){
						LangPane.this.content = new ContentTree(getText());
						SwingUtilities.invokeLater(
							() -> {
								int index = 0;
								for(var token: content.tokens()){
									final int length = token.value.length();
									final var type = token.type;

									if(type != TokenType.WHITESPACE){
										var attributes = atts.get(type);
										if(attributes == null){
											System.err.println("Missing type handler: " + type);
											attributes = missing;
										}
										doc.setCharacterAttributes(index, length, attributes, false);
									}

									index += length;
								}
								open.set(true);
							}
						);
					}
				}
			}
		);

	}
}
