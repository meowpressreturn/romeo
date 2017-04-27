package romeo.ui.forms;

import java.awt.Color;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.apache.commons.logging.LogFactory;

import romeo.xfactors.api.IExpressionParser;

public class ExpressionField extends JTextArea implements IValidatingField {
  protected class ExpressionDocument extends PlainDocument {
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
      super.insertString(offs, str, a);
      ExpressionField.this.checkExpression();
    }

    @Override
    protected void removeUpdate(DefaultDocumentEvent chng) { //these seem to come one behind whats actually going in the document!
      super.removeUpdate(chng);
      ExpressionField.this.checkExpression();
    }

    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
      super.insertUpdate(chng, attr);
      ExpressionField.this.checkExpression();
    }

  }

  protected boolean _valid = true;
  protected IExpressionParser _parser;
  protected Color _normalBg;
  protected String _initStuffYet = "Yes";

  public ExpressionField(IExpressionParser parser) {
    _normalBg = getBackground();
    _parser = parser;
    ExpressionDocument document = new ExpressionDocument();
    setDocument(document);
  }

  @Override
  public boolean isFieldValid() {
    return _valid;
  }

  protected void checkExpression() {
    //We need to update the validity a bit later as swing hasnt yet finished updating
    //the Document with the changes in the field that caused this listener to get called
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        String text = ExpressionField.this.getText();
        text = text.trim();
        LogFactory.getLog(this.getClass()).trace("checkExpression() \"" + text + "\"");
        if(text.isEmpty()) {
          ExpressionField.this._valid = true;
        } else {
          IExpressionParser parser = ExpressionField.this.getParser();
          try {
            parser.getExpression(text);
            ExpressionField.this._valid = true;
          } catch(Exception e) {
            ExpressionField.this._valid = false;
          }
        }
        ExpressionField.super.setBackground(_valid ? _normalBg : Color.RED);
      }
    });
  }

  @Override
  public void setBackground(Color bg) {
    /**
     * This method is first called (presumably by the superclass) when our
     * variables havent been initialised yet, so regardless of what we set as
     * their initial values they wont have them yet. Objects will be null, and
     * booleans false.
     */

    if(_valid && _initStuffYet != null) {
      _normalBg = bg;
      super.setBackground(bg);
    } else {
      super.setBackground(bg);
      //System.out.println("bob=" + bob + ", bg=" + bg);
    }
  }

  public IExpressionParser getParser() {
    return _parser;
  }

  public void setParser(IExpressionParser parser) {
    _parser = parser;
  }

}
