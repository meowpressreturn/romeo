package romeo.ui;

import java.util.Objects;

import javax.swing.JSplitPane;

import org.slf4j.Logger;

/**
 * Intended for use with SwingUtilities.invokeLater(), will set a splitpanes
 * width when its run() method is invoked.
 */
public class SplitPaneMover implements Runnable {
  
  private final Logger _log;
  private final JSplitPane _pane;
  private final int _width;

  public SplitPaneMover(
      Logger log,
      JSplitPane pane, 
      int width) {
    _log = Objects.requireNonNull(log, "log may not be nul;");
    _pane = pane;
    _width = width;
  }

  @Override
  public void run() {
    _log.trace("calling setDividerLocation(" + _width + ")");
    _pane.setDividerLocation(_width);
  }
}
