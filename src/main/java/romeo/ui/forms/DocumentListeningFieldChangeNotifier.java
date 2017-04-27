package romeo.ui.forms;

import java.util.Objects;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * Adpater that will call a {@link IFieldChangeListener} in an invokeLater() upon receiving
 * a DocumentEvent. Note that it passes a reference to the Document in its call to valueChanged()
 * and not the field itself (as that is unavailable from the event)
 */
public class DocumentListeningFieldChangeNotifier implements DocumentListener {
  
  private final IFieldChangeListener _fieldChangeListener;
  
  public DocumentListeningFieldChangeNotifier(IFieldChangeListener listener) {
    _fieldChangeListener = Objects.requireNonNull(listener, "listener may not be null");
  }
  
  /**
   * Will call the field change listener later with the document.
   * It passes a reference to the Document from the event.
   * @param e
   */
  private void notifyListener(DocumentEvent e) {
    final Document document = e.getDocument();
    SwingUtilities.invokeLater(new Runnable() {      
      @Override
      public void run() {
        _fieldChangeListener.valueChanged(document);
      }
    } );
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    notifyListener(e);    
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    notifyListener(e);       
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    notifyListener(e);       
  }

}



















