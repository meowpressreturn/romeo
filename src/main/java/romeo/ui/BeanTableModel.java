package romeo.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.apache.commons.beanutils.PropertyUtils;

import romeo.utils.BeanComparator;

/**
 * Simple table model where each row's values come from a bean or a Map. An
 * inner class ColumnDef is used to define columns, specifying the title of the
 * column the bean property or map key whence its value is obtained, and
 * optionally, an object that can process (eg: transform) those values for
 * display purposes. (Note that {@link ServiceTableModel} extends this class
 * with a listener that will reload data when informed by the service that there
 * something changed)
 */
public class BeanTableModel extends AbstractTableModel {
  /**
   * Object that transforms a soucre value into the value displayed in the
   * table. This can be used for formatting or calculations.
   */
  public interface IColumnProcessor {
    public Object processValue(Object value, int row, int col, ColumnDef colDef);
  }

  /**
   * Defines a column
   */
  public static class ColumnDef implements IColumnProcessor {
    protected String _property;
    protected String _label;
    protected IColumnProcessor _processor = this;

    public ColumnDef() {
    }

    public ColumnDef(String name, String label) {
      _property = name;
      _label = label;
    }

    public ColumnDef(String name, String label, IColumnProcessor processor) {
      _property = name;
      _label = label;
      _processor = processor;
    }

    @Override
    public Object processValue(Object value, int row, int col, ColumnDef colDef) {
      return value;
    }
  }

  ////////////////////////////////////////////////////////////////////////////

  protected ColumnDef[] _columns;
  protected List<? extends Object> _data;
  protected int _sortColumn = 0;
  protected boolean _descending;

  public BeanTableModel(ColumnDef[] columns, List<? extends Object> data) {
    _columns = columns;
    _data = data;
    sortRows();
  }

  public void setSortColumn(int column) {
    if(column >= _columns.length) {
      throw new IllegalArgumentException("Bad sort column " + column);
    }
    _sortColumn = column;
    sortRows();
    fireTableDataChanged();
  }

  public void setData(List<? extends Object> data) {
    _data = data;
    fireTableDataChanged();
  }

  protected void sortRows() {
    final boolean caseSensitive = false;
    String property = _columns[_sortColumn]._property;
    Collections.sort(_data, new BeanComparator(property, _descending, caseSensitive));
  }

  @Override
  public int getColumnCount() {
    return _columns.length;
  }

  /**
   * Returns the label for the specified column. This method implements the
   * getColumnName() method defined in AbstractTableModel.
   * @param columnIndex
   * @return label
   */
  @Override
  public String getColumnName(int columnIndex) {
    return _columns[columnIndex]._label;
  }

  @Override
  public int getRowCount() {
    return _data.size();
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    Object row = getRowBean(rowIndex);
    if(row == null) {
      return null;
    }
    ColumnDef column = _columns[columnIndex];
    try {
      Object value = null;
      if(column._property != null) {
        if(value instanceof Map) {
          value = ((Map<?, ?>) row).get(column._property);
        } else {
          value = PropertyUtils.getProperty(row, column._property);
        }
      } else {
        value = row;
      }
      IColumnProcessor processor = column._processor;
      if(processor != null) {
        value = processor.processValue(value, rowIndex, columnIndex, column);
      }
      return value;
    } catch(Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public Object getRowBean(int rowIndex) {
    return _data.get(rowIndex);
  }

  @Override
  public Class<? extends Object> getColumnClass(int columnIndex) {
    if(_data.isEmpty()) {
      return super.getColumnClass(columnIndex);
    } else {
      Object value = getValueAt(0, columnIndex);
      if(value == null) {
        return super.getColumnClass(columnIndex);
      }

      return value.getClass();
    }
  }

  public void initColumnClickListener(JTable table) {
    final JTable myTable = table;
    final BeanTableModel myModel = this;
    MouseAdapter ma = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() > 1) { //Double clicks will cause the clicked column to become the sort column
          TableColumnModel tcm = myTable.getColumnModel();
          int clickedColumn = tcm.getColumnIndexAtX(e.getX());
          int sortColumn = myTable.convertColumnIndexToModel(clickedColumn);
          if(sortColumn > -1) { //Only change to sort column if the new one is a valid column index
            myModel.setSortColumn(sortColumn);
          }
        }
      }
    };
    JTableHeader tableHeader = myTable.getTableHeader();
    tableHeader.addMouseListener(ma);
  }

  /**
   * Returns the sort direction. True for descending and false if ascending.
   * @return descending
   */
  public boolean isSortDescending() {
    return _descending;
  }

  /**
   * Set whether sorting should be descending (default is false)
   * @param desc
   */
  public void setSortDescending(boolean desc) {
    if(desc != _descending) {
      _descending = desc;
      sortRows();
      fireTableDataChanged();
    }
  }

  public int getSortColumn() {
    return _sortColumn;
  }

}
