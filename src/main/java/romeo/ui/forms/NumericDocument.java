/*
 * NumericDocument.java
 * Created on Feb 1, 2006
 */
package romeo.ui.forms;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import romeo.utils.Convert;

public class NumericDocument extends PlainDocument {
  private NumericFieldConstraint _constraint = new NumericFieldConstraint();

  @Override
  public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
    String text = getText(0, getLength());
    char[] source = str.toCharArray();
    char[] result = new char[source.length];
    int j = 0;
    for(int i = 0; i < result.length; i++) {
      if(source[i] == '.') {
        if(_constraint.isAllowDecimal() && text.indexOf('.') == -1) {
          result[j++] = source[i];
        }
      } else if(Character.isDigit(source[i])) {
        result[j++] = source[i];
      } else if(source[i] == '-' && (offs == 0) && (i == 0) && _constraint.isNegativeAllowed()) {
        result[j++] = source[i];
      }

    }
    String resultStr = new String(result, 0, j);
    super.insertString(offs, resultStr, a);
  }

  public long getLongValue() {
    try {
      String str = getText(0, getLength());
      return Convert.toLong(str);
    } catch(Exception e) {
      return 0;
    }
  }

  public boolean isValueValid() {
    try {
      String text = getText(0, getLength());
      if(text == null || text.length() == 0 || "-".equals(text)) {
        return false;
      }
      if(text.startsWith(".") || text.endsWith(".")) {
        return false;
      }
      char[] chars = text.toCharArray();
      for(int i = 0; i < chars.length; i++) {
        if(!Character.isDigit(chars[i])) {
          if(chars[i] == '.') {
            if(false == _constraint.isAllowDecimal()) {
              return false;
            }
          } else if((i != 0) && (chars[i] != '-') || (!_constraint.isNegativeAllowed())) {
            return false;
          }
        }
      }
      return isRangeValid();
    } catch(Exception e) {
      throw new RuntimeException("Error validating document content", e);
    }
  }

  public boolean isRangeValid() {
    long v = getLongValue();
    return (v >= _constraint.getMinValue()) && (v <= _constraint.getMaxValue());
  }

  public NumericFieldConstraint getConstraint() {
    return _constraint;
  }

  public void setConstraint(NumericFieldConstraint constraint) {
    _constraint = constraint;
  }

}
