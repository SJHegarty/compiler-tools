package localgoat.lang.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface StrippedListener extends DocumentListener{
	default void insertUpdate(DocumentEvent e){
		update();
	}

	default void removeUpdate(DocumentEvent e){
		update();
	}

	default void changedUpdate(DocumentEvent e){
		update();
	}

	void update();
}
