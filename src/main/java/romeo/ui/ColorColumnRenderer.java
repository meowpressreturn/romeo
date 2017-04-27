package romeo.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

/**
 * Renders a java.awt.Color as a table column value, including some text for the
 * r,g,b values.
 */
public class ColorColumnRenderer extends JLabel implements TableCellRenderer {
  public ColorColumnRenderer() {
    setForeground(Color.BLACK);
    setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {
    setText(null);
    setIcon(null);
    setIconTextGap(0);
    setHorizontalAlignment(SwingConstants.CENTER);
    setVerticalAlignment(SwingConstants.CENTER);

    if(value instanceof Color) {
      Color color = (Color) value;
      setText(color.getRed() + "," + color.getGreen() + "," + color.getBlue());
      setBackground(color);
    }

    return this;
  }

}
