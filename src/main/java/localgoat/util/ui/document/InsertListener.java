package localgoat.util.ui.document;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface InsertListener extends DocumentListener{

	default void removeUpdate(DocumentEvent e){

	}

	default void changedUpdate(DocumentEvent e){

	}
}
