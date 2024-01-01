package romeo.ui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Objects;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

/**
 * Context menu with common text operations, e.g. cut, copy, paste, select all. 
 */
public class CommonTextActionsMenu extends JPopupMenu implements ActionListener {
  
  private static final String CUT = "Cut";
  private static final String COPY= "Copy";
  private static final String PASTE = "Paste";
  private static final String SELECT_ALL = "Select All";
  
  //TODO - for a FleetField it would be good to have a "Select Element" option to 
  //       select the fleet element (and its trailing comma) which the cursor is inside
  //       can do this as a subclass for FleetFields
  
  private final JTextComponent _field;
   
  /**
   * Constructor. Requires reference to the field whose text it will act upon but does not
   * automatically add the menu to that field as you might wish to attach it to some other 
   * component or defer adding it until later.
   * 
   * @param field the text field which the menu will act upon
   */
  public CommonTextActionsMenu(JTextComponent field) {
    _field = Objects.requireNonNull(field, "field may not be null");    
    initItems();
  }
  
  protected void initItems() {
    createItem(CUT, KeyEvent.VK_X);
    createItem(COPY, KeyEvent.VK_C);
    createItem(PASTE, KeyEvent.VK_V);
    createItem(SELECT_ALL, KeyEvent.VK_A);
  }
  
  protected JMenuItem createItem(String label, int key) {
    JMenuItem item = new JMenuItem(label);
    if(key > 0) {
      item.setAccelerator(KeyStroke.getKeyStroke(key, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    }
    item.addActionListener(this);
    add(item);
    return item;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if(source instanceof JMenuItem) {
      String label = ((JMenuItem)source).getText();
      doAction(label);
    }
  }
  
  protected void doAction(String label) {
    switch (label) {
      case CUT:
        _field.cut();
        break;
        
      case COPY:
        _field.copy();
        break;
        
      case PASTE:
        _field.paste();
        break;
        
      case SELECT_ALL:
        _field.requestFocusInWindow(); //needs focus for selectAll to work
        _field.selectAll();
        break;
    }
  }
  
}
