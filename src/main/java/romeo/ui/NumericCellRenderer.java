//2008-12-03
package romeo.ui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import romeo.utils.Convert;

/**
 * TableCellRenderer that formats Numbers with right alignment and the specified
 * number of decimal places. Implemented as a decorator that will use a
 * DefaultTableCellRenderer and then modify its label text with the formatted
 * value before returning it. It is safe to use this renderer with non-numeric
 * values too, in which case the normal DefaultTableCellRenderer behaviour will
 * apply.
 */
public class NumericCellRenderer implements TableCellRenderer {
  private DefaultTableCellRenderer _renderer = new DefaultTableCellRenderer();
  private int _decimals = 0;

  public NumericCellRenderer(int decimals) {
    _decimals = decimals;
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {
    DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) _renderer.getTableCellRendererComponent(table, value,
        isSelected, hasFocus, row, column);

    if(value instanceof Number) { //If the value is a Number we will change it to a String with specific number of decimal
                                    //places. If it isn't a number normal DefaultTableCellRenderer behaviour applies.
      double d = ((Number) value).doubleValue();
      String displayValue = Convert.toStr(d, _decimals);
      renderer.setText(displayValue);
      renderer.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
    }

    return renderer;
  }

}
