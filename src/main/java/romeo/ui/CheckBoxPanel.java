package romeo.ui;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CheckBoxPanel extends JPanel implements ItemListener {
  protected List<JCheckBox> _boxes = new ArrayList<JCheckBox>();
  protected Map<String, JCheckBox> _boxLookup = new TreeMap<String, JCheckBox>();
  protected ItemListener _itemListener;

  public CheckBoxPanel() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  /**
   * Removes the existing checkboxes. ItemListener is not informed.
   */
  public void clearOptions() {
    for(JCheckBox checkbox : _boxes) {
      checkbox.removeItemListener(this);
      this.remove(checkbox);
    }
    _boxes.clear();
    _boxLookup.clear();
  }

  public void addCheckBox(String text, boolean selected) {
    if(_boxLookup.containsKey(text)) {
      throw new IllegalArgumentException("label already added:" + text);
    }
    JCheckBox cb = new JCheckBox(text, selected);
    this.add(cb);
    _boxes.add(cb);
    _boxLookup.put(text, cb);
    cb.addItemListener(this);
  }

  public void setItemListener(ItemListener listener) {
    _itemListener = listener;
  }

  /**
   * Implemented to pass on the event to the panels ItemListener
   * @param e
   */
  @Override
  public void itemStateChanged(ItemEvent e) {
    if(_itemListener != null) {
      _itemListener.itemStateChanged(e);
    }

  }

  public Set<String> getCheckedLabels() {
    Set<String> selections = new HashSet<String>();
    for(JCheckBox box : _boxes) {
      if(box.isSelected()) {
        selections.add(box.getText());
      }
    }
    return selections;
  }

  /**
   * Will set the selected state of the specified checkbox if it exists. This
   * will not cause an event to be sent to item listeners. If the named box
   * doesnt exist then no action is taken.
   * @param label
   * @param selected
   */
  public void setSelected(String label, boolean selected) {
    JCheckBox box = _boxLookup.get(label);
    if(box != null) {
      box.removeItemListener(this);
      box.setSelected(selected);
      box.addItemListener(this);
    }
  }

  /**
   * If the named checkbox exists then this will return true if it is selected.
   * Otherwise false is returned (including for non-existent checkboxes)
   * @param label
   * @return selected
   */
  public boolean isSelected(String label) {
    JCheckBox box = _boxLookup.get(label);
    return (box == null) ? false : box.isSelected();
  }

  public void setAll(boolean selected) {
    for(JCheckBox box : _boxes) {
      box.removeItemListener(this);
      box.setSelected(selected);
      box.addItemListener(this);
    }
  }
}
