package romeo.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import romeo.utils.GuiUtils;

/**
 * Renders the table header cells, providing a sort icon when using {@link BeanTableModel}
 * and tooltip text/
 */
public class BeanTableHeaderRenderer implements TableCellRenderer {
  private static final ImageIcon SORTED_ICON = GuiUtils.getImageIcon("/images/sort.gif");

  private DefaultTableCellRenderer _renderer;

  /**
   * No-args constructor.
   */
  public BeanTableHeaderRenderer() {
    _renderer = new DefaultTableCellRenderer();
  }

  /**
   * Use the specified DefaultTableCellRenderer as the renderer, its icon will
   * be modifed as necessary. (Decorator pattern)
   * @param wrap
   */
  public BeanTableHeaderRenderer(DefaultTableCellRenderer wrap) {
    _renderer = wrap;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {
    _renderer.setIcon(null);
    _renderer.setToolTipText(null);
    DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) _renderer.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

    TableModel model = table.getModel();
    if(model instanceof BeanTableModel) {
      int sortColumn = ((BeanTableModel) model).getSortColumn();
      if(sortColumn == column) {
        renderer.setIcon(SORTED_ICON);
      }
    }

    renderer.setToolTipText(renderer.getText());

    return renderer;
  }
}
