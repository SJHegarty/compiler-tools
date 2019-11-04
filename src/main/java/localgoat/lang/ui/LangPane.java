package localgoat.lang.ui;

import localgoat.lang.compiler.ContentTree;
import localgoat.lang.compiler.TokenType;
import localgoat.util.ui.document.InsertRemoveListener;

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
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public class LangPane extends JTextPane{

	private ContentTree content;

	public LangPane(){

		this.setBackground(new Color(0xff404040));
		this.setCaretColor(new Color(0xffc0c0c0));
		setDoubleBuffered(true);
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

		final IntFunction<AttributeSet> builder = (colour) -> context.addAttribute(
			context.getEmptySet(),
			StyleConstants.Foreground,
			new Color(colour)
		);

		final var missing = builder.apply(Color.ORANGE.getRGB());
		final Map<TokenType, AttributeSet> atts = new HashMap<>();
		{
			var a = builder.apply(0xffc0c0c0);
			atts.put(TokenType.LINE_COMMENT, a);
			atts.put(TokenType.HANDLED_COMMENT, a);
			atts.put(TokenType.STRUCTURED_COMMENT, a);
		}
		atts.put(
			TokenType.STRING,
			builder.apply(0xff80ff80)
		);
		{
			var a =	builder.apply(0xff80c0ff);
			atts.put(TokenType.KEYWORD, a);
			atts.put(TokenType.SYMBOL, a);
		}
		atts.put(
			TokenType.TYPE,
			builder.apply(0xffffffff)
		);
		atts.put(
			TokenType.UNHANDLED,
			builder.apply(0xffff0000)
		);
		doc.addDocumentListener(
			(InsertRemoveListener) event -> {
				LangPane.this.content = new ContentTree(getText());
				SwingUtilities.invokeLater(
					() -> {
						int index = 0;
						for(var token: content.tokens()){
							final int length = token.value.length();
							final var type = token.type;

							if(type != TokenType.WHITESPACE){
								try{
									var extract = doc.getText(index, length);
									if(!token.value.equals(extract)){
										throw new IllegalStateException(
											String.format(
												"Extracted Token \"%s\" of type %s does not match text \"%s\" at text index %s",
												token,
												type,
												extract,
												index
											)
										);
									}
								}
								catch(BadLocationException e){
									throw new IllegalStateException(e);
								}
								var attributes = atts.get(type);
								if(attributes == null){
									System.err.println("Missing type handler: " + type + " " + token);
									attributes = missing;
								}
								doc.setCharacterAttributes(index, length, attributes, true);
							}

							index += length;
						}
					}
				);
			}
		);

	}
}
