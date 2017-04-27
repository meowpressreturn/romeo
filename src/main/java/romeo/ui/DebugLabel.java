package romeo.ui;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

/**
 * Used to assist with debugging layout issues mainly
 */
public class DebugLabel extends JPanel {
  public DebugLabel(String text, Color bkgr) {
    super();
    add(new JLabel(text));
    setBackground(bkgr);
    setBorder(new LineBorder(Color.BLACK));
  }
}
