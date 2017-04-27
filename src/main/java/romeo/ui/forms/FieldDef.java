/*
 * FieldDef.java
 * Created on Jan 31, 2006
 */
package romeo.ui.forms;

/**
 * Defines a field on a form in our experimental form definition mechanism.
 */
public class FieldDef {
  public static final int TYPE_TEXT = 0;
  public static final int TYPE_INT = 1;
  public static final int TYPE_LONG_TEXT = 2;
  public static final int TYPE_COMBO = 3;
  public static final int TYPE_COLOR = 4;
  public static final int TYPE_LABEL = 5;
  public static final int TYPE_EXPRESSION = 6;
  public static final int TYPE_XFACTOR_COMBO = 7;
  public static final int TYPE_SCANNER_COMBO = 8;
  public static final int TYPE_PLAYER_COMBO = 9;
  public static final int TYPE_DOUBLE = 10;
  public static final int TYPE_CUSTOM = 11;
  public static final int TYPE_FILLER = 12;

  protected String _name;
  protected int _type;
  protected String _label;
  protected Object _defaultValue;
  protected Object _details;
  protected boolean _mandatory;
  protected boolean _wide;

  public FieldDef() {
    ;
  }

  public FieldDef(String name, String label) {
    _name = name;
    _type = TYPE_TEXT;
    _label = label;
  }

  public FieldDef(String name, String label, int type) {
    _name = name;
    _type = type;
    _label = label;
    if(type == TYPE_LONG_TEXT || type == TYPE_CUSTOM) {
      _wide = true;
    }
  }

  @Override
  public String toString() {
    return "FieldDef[" + _name + "," + _type + "," + _label + "]";
  }

  public String getLabel() {
    return _label;
  }

  public String getName() {
    return _name;
  }

  public int getType() {
    return _type;
  }

  public void setLabel(String string) {
    _label = string;
  }

  public void setName(String string) {
    _name = string;
  }

  public void setType(int i) {
    _type = i;
  }

  public Object getDefaultValue() {
    if(_defaultValue == null) {
      if(_type == TYPE_INT) {
        return "0";
      } else if(_type == TYPE_DOUBLE) {
        return "0.0";
      } else if(_type == TYPE_COMBO || _type == TYPE_SCANNER_COMBO) {
        return null;
      } else if(_type == TYPE_CUSTOM) {
        return null;
      } else {
        return "";
      }
    } else {
      return _defaultValue;
    }
  }

  public void setDefaultValue(Object dv) {
    _defaultValue = dv;
  }

  public Object getDetails() {
    return _details;
  }

  public void setDetails(Object object) {
    _details = object;
  }

  public boolean isMandatory() {
    return _mandatory;
  }

  public void setMandatory(boolean b) {
    _mandatory = b;
  }

  public boolean isWide() {
    return _wide;
  }

  public void setWide(boolean wide) {
    _wide = wide;
  }

}
