/*
 * ColorButton.java
 * Created on Feb 5, 2006
 */
package romeo.ui.forms;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;

import romeo.ui.ColorPicker;
import romeo.utils.Convert;

/**
 * Button that holds a colour value and pops up a {@link ColorPicker} to set it
 * when clicked.
 */
public class ColorButton extends JButton implements ActionListener {
  private Color _color = Color.YELLOW;
  private List<IFieldChangeListener> _fieldChangeListeners = new ArrayList<IFieldChangeListener>(1);
  private ColorPicker _picker;
  private boolean _positioned = false;

  /**
   * No-args constructor
   */
  public ColorButton() {
    setColor(_color);
    addActionListener(this);
  }

  /**
   * Set a ColorPicker to use (instead of creating one by default)
   * @param picker
   *          (may be null)
   */
  public void setColorPicker(ColorPicker picker) {
    _picker = picker;
  }

  /**
   * Returns the {@link ColorPicker} used by this button. Note that this may be
   * null if none was set and the button hasnt been used yet.
   * @return picker
   */
  public ColorPicker getColorPicker() {
    return _picker;
  }

  /**
   * Returns true if the initial positioning of the color picker has been done.
   * If this is false then picker will be positioned relative to button when its
   * clicked (at which point flag will be set true)
   * @return positioned
   */
  public boolean isPositioned() {
    return _positioned;
  }

  /**
   * Set flag indicating if the initial positioning of the color picker has been
   * done. If this is false then picker will be positioned relative to button
   * when its clicked (at which point flag will be set true)
   * @param positioned
   */
  public void setPositioned(boolean positioned) {
    _positioned = positioned;
  }

  /**
   * Callback listener for when the button is clicked
   * @param e
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if(_picker == null) {
      _picker = new ColorPicker();
      _picker.setRelativeTo(this);
    }

    if(!isPositioned()) {
      _picker.setRelativeTo(this);
      setPositioned(true);
    }

    Color newColor = _picker.pickColor();
    if(newColor == null) {
      return;
    }
    if(newColor != _color) {
      notifyFieldChangeListeners();
    }
    setColor(newColor);
  }

  /**
   * Set the color displayed by the button
   * @param color
   */
  public void setColor(Color color) {
    if(color == null) {
      color = Color.BLACK;
    }
    _color = color;
    setBackground(color);
    setText(getColorText());
    repaint();
  }

  /**
   * Returns the text describing the buttons color in format r,g,b
   * @return colorText
   */
  public String getColorText() {
    return Convert.toStr(_color);
  }

  /**
   * Set the color using text in the r,g,b decimal format
   * @param rgb
   */
  public void setColorText(String rgb) {
    Color color = Convert.toColor(rgb);
    setColor(color);
  }

  /**
   * Returns the currently selected color
   * @return color
   */
  public Color getColor() {
    return _color;
  }

  /**
   * Ad an {@link IFieldChangeListener} to get notified when the button value
   * changes
   * @param listener
   */
  public void addFieldChangeListener(IFieldChangeListener listener) {
    if(!_fieldChangeListeners.contains(listener)) {
      _fieldChangeListeners.add(listener);
    }
  }

  /**
   * Removes the specfied {@link IFieldChangeListener}
   * @param listener
   */
  public void removeFieldChangeListener(IFieldChangeListener listener) {
    _fieldChangeListeners.remove(listener);
  }

  /**
   * Notifies the listeners that the value changed. This is not invoked by the
   * setColor method but rather when the user picks a new color, so if you want
   * to invoke listeners following a programmatic call to setColor you need to
   * call this.
   */
  public void notifyFieldChangeListeners() {
    ArrayList<IFieldChangeListener> listeners = new ArrayList<IFieldChangeListener>(_fieldChangeListeners);
    for(IFieldChangeListener listener : listeners) {
      listener.valueChanged(this);
    }
  }

}
