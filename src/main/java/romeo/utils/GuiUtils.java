package romeo.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

/**
 * Some utility code used with the UI
 */
public class GuiUtils {
  /**
   * Color we use to indicate a field is mandatory
   */
  protected static final Color MANDATORY_COLOR = new Color(255, 255, 176);

  /**
   * Update a ProgressMonitor on the Event Dispatch Thread and return whether
   * cancel was clicked.
   * @param pm
   *          the ProgressMonitor to update
   * @param nv
   *          new value for progress, may be null in which case no action is
   *          taken and false is returned
   * @param note
   * @return cancelled
   */
  public static boolean updateProgressMonitor(final ProgressMonitor pm, final int nv, final String note) {
    if(pm == null) {
      return false;
    }
    final AtomicReference<Boolean> cancelled = new AtomicReference<>(false);
    Runnable r = new Runnable() {
      @Override
      public void run() {
        pm.setProgress(nv);
        if(note != null) {
          pm.setNote(note);
        }
        cancelled.set(pm.isCanceled());
      }
    };
    if(SwingUtilities.isEventDispatchThread()) { //If for some reason we are already on the EDT then just execute locally.
      r.run();
    } else { //If we aren't then we need to do the progress monitor changes on the EDT, and
               //we need to wait so we can get back the cancellation status.
      try {
        SwingUtilities.invokeAndWait(r);
      } catch(Exception e) {
        throw new RuntimeException("Exception updating ProgressMonitor", e);
      }
    }
    return cancelled.get();
  }

  /**
   * Do a Thread.sleep(millis) , but returns immediately if interrupted,
   * swallowing the exception
   * @param millis
   */
  public static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch(Exception e) {
    }
  }

  /**
   * Utiltity method to set the scroll increment to 16, a reasonable value we
   * use in Romeo.
   * @param scrollPane
   */
  public static void initScrollIncrement(JScrollPane scrollPane) {
    Objects.requireNonNull(scrollPane);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
  }

  /**
   * Set the scroll increments to 16 for all the JScrollPanes that are in the
   * specified container, or containers it contains (recursive).
   * @param parent
   */
  public static void initScrollIncrements(Container parent) {
    Component[] children = parent.getComponents();
    for(Component c : children) {
      if(c instanceof Container) {
        initScrollIncrements((Container) c);
      }
      if(c instanceof JScrollPane) {
        initScrollIncrement((JScrollPane) c);
      }
    }
  }

  /**
   * Get an ImageIcon given the image file path
   * @param path
   * @return imageIcon
   */
  public static ImageIcon getImageIcon(String path) {
    URL url = GuiUtils.class.getResource(path);
    Objects.requireNonNull(url, "url is null for path " + path);
    ImageIcon image = new ImageIcon(url);
    return image;
  }

  /**
   * Gets first container of the specified type from the parent chaion of the
   * specfied component
   * @param type
   * @param start
   * @return container
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static Container getAncestorOfType(Class type, Component start) {
    Container parent = start.getParent();
    if(parent == null) {
      return null;
    }
    if(type.isAssignableFrom(parent.getClass())) {
      return parent;
    }
    return getAncestorOfType(type, parent);
  }

  /**
   * Sets a GridBagLayout in target (if provided) and returns a mutable instance
   * of GridBagLayout with basic settings. (eg: no weight, 1 cell sizing,
   * horizontal fill, gridx=0, gridy=0)
   * @param target
   * @return gbc
   */
  public static GridBagConstraints prepGridBag(JComponent target) {
    if(target != null) {
      target.setLayout(new GridBagLayout());
    }
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.anchor = GridBagConstraints.CENTER;
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 1;
    gbc.gridheight = 1;
    gbc.weightx = 0d;
    gbc.weighty = 0d;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(4, 4, 2, 2);
    return gbc;
  }

  /**
   * Sets the widths in a table using the values from the widths array. For
   * negative values or columns without a corresponding element in widths a
   * default width will be used. The default width is the
   */
  public static void setColumnWidths(JTable table, int[] widths) {
    int columnCount = table.getColumnModel().getColumnCount();
    int totalWidth = table.getColumnModel().getTotalColumnWidth();
    int allocatedWidth = 0;
    int unallocatedCount = columnCount;

    for(int i = 0; i < widths.length; i++) {
      if(widths[i] >= 0) {
        allocatedWidth += widths[i];
        unallocatedCount--;
      }
    }

    //Calculate the size to use for unspecified columns
    int defaultWidth = unallocatedCount == 0 ? 0 : (totalWidth - allocatedWidth) / unallocatedCount;

    for(int c = 0; c < columnCount; c++) {
      int width = widths.length > c && widths[c] >= 0 ? widths[c] : defaultWidth;
      table.getColumnModel().getColumn(c).setPreferredWidth(width);
    }
  }

  /**
   * Returns the name of the selected tab in a tab pane
   * @return selected tab name (or null if selected index is -1)
   */
  public static String getSelectedTab(JTabbedPane tabs) {
    int index = tabs.getSelectedIndex();
    return index == -1 ? null : tabs.getTitleAt(index);
  }

  /**
   * Will attempt to set the specified tab. Returns true on success, false if
   * the named tab wasn't found (in which case the current selection remains
   * unchanged). If null is passed for title will just return false without
   * changing selection.
   * @param title
   * @return success
   */
  public static boolean setSelectedTab(JTabbedPane tabs, String title) {
    if(title == null) {
      return false;
    }
    int tabCount = tabs.getTabCount();
    for(int i = 0; i < tabCount; i++) {
      if(title.equals(tabs.getTitleAt(i))) {
        tabs.setSelectedIndex(i);
        return true;
      }
    }
    return false;
  }

}
