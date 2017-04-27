/*
 * RNumericField.java
 * Created on Feb 1, 2006
 */
package romeo.ui.forms;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import romeo.utils.Convert;

public class RNumericField extends JTextField implements FocusListener, DocumentListener, IValidatingField {
  private Color _normalColor;

  public RNumericField() {
    super();
    setDocument(new NumericDocument());
    addFocusListener(this);
    getDocument().addDocumentListener(this);
    _normalColor = getBackground();
    setColumns(10);
  }

  public void setConstraint(NumericFieldConstraint constraint) {
    Document doc = getDocument();
    if(doc instanceof NumericDocument) {
      ((NumericDocument) doc).setConstraint(constraint);
    } else {
      throw new UnsupportedOperationException("Document is not a NumericDocument, cannot set NumericFieldConstraint");
    }
  }

  /**
   * Set a numeric value with the specified number of decimal places.
   * @param value
   * @param decimals
   */
  public void setText(double value, int decimals) {
    String asText = Convert.toStr(value, decimals);
    setText(asText);
  }

  @Override
  public void setBackground(Color bgColor) {
    _normalColor = bgColor;
    super.setBackground(bgColor);
  }

  @Override
  public void focusGained(FocusEvent event) {
    try {
      selectAll();

      //following commented out stuff, highlights but doesnt select.
      //dont delete. This could be useful sometime.
      /*
       * String text = getText(); Highlighter h = getHighlighter();
       * Highlighter.HighlightPainter p = DefaultHighlighter.DefaultPainter;
       * h.removeAllHighlights(); h.addHighlight(0,text.length(),p);
       */
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void focusLost(FocusEvent e) {
    ;
  }

  protected void updateDisplay() {
    Document d = getDocument();
    if(d instanceof NumericDocument) {
      NumericDocument document = (NumericDocument) d;
      if(!document.isValueValid()) {
        super.setBackground(Color.RED);
      } else {
        super.setBackground(_normalColor);
      }
      repaint();
    }
  }

  @Override
  public void changedUpdate(DocumentEvent e) {
    updateDisplay();
  }

  @Override
  public void insertUpdate(DocumentEvent e) {
    updateDisplay();
  }

  @Override
  public void removeUpdate(DocumentEvent e) {
    updateDisplay();
  }

  @Override
  public boolean isFieldValid() {
    Document d = getDocument();
    if(d instanceof NumericDocument) {
      return ((NumericDocument) d).isValueValid();
    } else {
      return true;
    }
  }

}
