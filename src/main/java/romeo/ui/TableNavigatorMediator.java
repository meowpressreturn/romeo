package romeo.ui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

/**
 * When the table is double clicked will open the selected record in the
 * {@link NavigatorPanel}. Note: This class adds a listener to the specified
 * table.
 */
public class TableNavigatorMediator {
  private JTable _table;
  private MouseListener _mouseListener;
  private KeyListener _keyListener;
  private IRecordSelectionListener _recordSelectionListener;

  /**
   * Constructor
   * @param table
   * @param listener
   *          used to open the form when a record is selected
   */
  public TableNavigatorMediator(JTable table, AbstractNavigatorRecordSelectionListener listener) {
    Objects.requireNonNull(table, "table must not be null");
    Objects.requireNonNull(listener, "listener must not be null");

    _table = table;
    //Nav.RSL class already has form opening logic so we use it to manage that for us
    _recordSelectionListener = listener;

    //Listen for mouse clicks on the table
    _mouseListener = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() > 1) { //If more than one click (ie: doubleclick)
          openSelectedRecord();
        }
      }
    };
    _table.addMouseListener(_mouseListener);

    //Listen for keys typed on the table
    _keyListener = new KeyAdapter() {
      @Override
      public void keyTyped(KeyEvent e) {
        if(e.getKeyChar() == ' ') { //If its space then open the selected record
          openSelectedRecord();
        }
      }
    };
    _table.addKeyListener(_keyListener);
  }

  /**
   * Stop listening for mouse events from the table.
   */
  public void stopListening() {
    _table.removeMouseListener(_mouseListener);
    _table.removeKeyListener(_keyListener);
  }

  /**
   * Open the tables currently selected record in the navigtor panel
   */
  private void openSelectedRecord() {
    Object record = getSelectedRecord();
    if(record != null) {
      _recordSelectionListener.recordSelected(record);
    }
  }

  /**
   * Get the currently selected record if there is one. Note that if the value
   * is still adjusting it will return null.
   * @return record
   */
  private Object getSelectedRecord() {
    ListSelectionModel lsm = (ListSelectionModel) _table.getSelectionModel();
    if(!lsm.isSelectionEmpty()) {
      int rowIndex = lsm.getMinSelectionIndex();
      TableModel model = _table.getModel();
      //      if(model instanceof ServiceTableModel && !lsm.getValueIsAdjusting() )
      //      {
      //        Object record = ((ServiceTableModel)model).getRowBean(rowIndex);
      //        return record;
      //      }
      if(model instanceof BeanTableModel && !lsm.getValueIsAdjusting()) {
        Object record = ((BeanTableModel) model).getRowBean(rowIndex);
        return record;
      }
    }
    return null;
  }

}
