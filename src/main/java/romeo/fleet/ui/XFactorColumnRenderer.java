//2008-12-07
package romeo.fleet.ui;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import romeo.utils.GuiUtils;

/**
 * Renders the xFactor icon for the x column in a {@link FleetField} when that
 * elements xFactors are active.
 */
public class XFactorColumnRenderer extends JLabel implements TableCellRenderer {
  public static final ImageIcon XFACTOR_ICON = GuiUtils.getImageIcon("/images/xFactorSmall.gif");

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
      int row, int column) {
    setText(null);
    setIcon(null);
    setIconTextGap(0);
    setHorizontalAlignment(SwingConstants.CENTER);
    setVerticalAlignment(SwingConstants.CENTER);

    if(value instanceof Boolean) {
      if(((Boolean) value).booleanValue() == true) {
        setIcon(XFACTOR_ICON);
      }
    }

    return this;
  }

}
