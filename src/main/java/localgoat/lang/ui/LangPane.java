package localgoat.lang.ui;

import localgoat.lang.compiler.LineTokeniser;
import localgoat.lang.compiler.ContentTree;
import localgoat.lang.compiler.automata.Token;
import localgoat.util.ui.document.InsertRemoveListener;

import javax.swing.*;
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
import java.util.Optional;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LangPane extends JTextPane{

	private ContentTree content;

	public LangPane(){

		this.setBackground(new Color(0xff202020));
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

		final Function<Color, AttributeSet> builder = (colour) -> context.addAttribute(
			context.getEmptySet(),
			StyleConstants.Foreground,
			colour
		);

		final var missing = builder.apply(Color.ORANGE);
		final var hanging = builder.apply(Color.RED);
		//;
		final ColourMap<String> colours = new ColourMap<>(Math.PI/3);

		colours.add("line-feed");
		colours.add(ContentTree.LINE_COMMENT);
		final String AMBIGUOUS = "ambiguous";
		colours.add(AMBIGUOUS);
		colours.add(ContentTree.IDENTIFIER);
		colours.add(ContentTree.STRING);
		colours.add(ContentTree.KEY_WORD);
		colours.add(ContentTree.SYMBOL);
		colours.add(ContentTree.CLASS_NAME);
		colours.add(ContentTree.CONSTANT);
		colours.add(ContentTree.DECIMAL);
		colours.add(ContentTree.CONTEXT_IDENTIFIER);

		final Map<String, AttributeSet> atts = colours.build().entrySet().stream()
			.collect(
				Collectors.toMap(
					e -> e.getKey(),
					e -> builder.apply(e.getValue().brighter())
				)
			);

		doc.addDocumentListener(
			(InsertRemoveListener) event -> {
				LangPane.this.content = new ContentTree(getText());
				SwingUtilities.invokeLater(
					() -> {
						int index = 0;
						for(var token: content.tokens()){
							final int length = token.value().length();

							if(!token.hasClass(c -> c.hasFlag(LineTokeniser.WHITE_SPACE))){
								try{
									var extract = doc.getText(index, length);

									if(!token.value().equals(extract)){
										throw new IllegalStateException(
											String.format(
												"Extracted Token \"%s\" of name %s does not match text \"%s\" at text index %s",
												token.value(),
												token.classes(),
												extract,
												index
											)
										);
									}
								}
								catch(BadLocationException e){
									throw new IllegalStateException(e);
								}
								final AttributeSet attributes;
								final var classes = token.classes().stream()
									.map(c -> c.name())
									.collect(Collectors.toSet());

								switch(classes.size()){
									case 0:{
										attributes = hanging;
										break;
									}
									case 1:{
										attributes = Optional.ofNullable(atts.get(classes.iterator().next()))
											.orElseGet(
												() -> {
													System.err.println("Missing name handler for token - " + token);
													return missing;
												}
											);
										break;
									}
									default:{
										attributes = atts.get(AMBIGUOUS);
									}
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
