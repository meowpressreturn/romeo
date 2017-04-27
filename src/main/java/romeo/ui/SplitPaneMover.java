package romeo.ui;

import javax.swing.JSplitPane;

import org.apache.commons.logging.LogFactory;

/**
 * Intended for use with SwingUtilities.invokeLater(), will set a splitpanes
 * width when its run() method is invoked.
 */
public class SplitPaneMover implements Runnable {
  protected JSplitPane _pane;
  protected int _width;

  public SplitPaneMover(JSplitPane pane, int width) {
    _pane = pane;
    _width = width;
  }

  @Override
  public void run() {
    LogFactory.getLog(this.getClass()).trace("calling setDividerLocation(" + _width + ")");
    _pane.setDividerLocation(_width);
  }
}
