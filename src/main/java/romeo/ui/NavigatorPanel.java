package romeo.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.LogFactory;

import romeo.Romeo;
import romeo.ui.forms.RomeoForm;
import romeo.utils.GuiUtils;
import romeo.utils.INamed;

/**
 * This is the panel on the right where forms get shown. (The 'navigator'
 * nomenclature is historical. In older versions multiple forms could be shown
 * here and there were back/forward/close buttons to navigate between them. Only
 * the close button remains now, and only one form at a time will be shown).
 */
public class NavigatorPanel extends JPanel {
  protected JLabel _nameLabel;
  protected JButton _closebutton;
  protected JScrollPane _formScrollPane;
  protected JPanel _currentPanel;

  protected JButton _closeButton;

  public NavigatorPanel() {
    super();
    setLayout(new BorderLayout());

    final NavigatorPanel navigatorPanel = this;

    Action closeAction = new AbstractRomeoAction() {
      @Override
      public void doActionPerformed(ActionEvent e) {
        navigatorPanel.close();
      }
    };
    closeAction.putValue(Action.NAME, "Dismiss");
    closeAction.putValue(Action.SHORT_DESCRIPTION, "Dismiss Form");
    closeAction.putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/close.gif"));
    _closeButton = new JButton(closeAction);
    _closeButton.setHideActionText(true);

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.setLayout(new BorderLayout());

    _nameLabel = new JLabel("");
    Font nameFont = new Font("Dialog", Font.BOLD, 16);
    _nameLabel.setFont(nameFont);
    toolBar.add(_nameLabel, BorderLayout.WEST);
    toolBar.add(_closeButton, BorderLayout.EAST);

    add(toolBar, BorderLayout.NORTH);

    _formScrollPane = new JScrollPane();
    _formScrollPane.getVerticalScrollBar().setUnitIncrement(16);
    _formScrollPane.getHorizontalScrollBar().setUnitIncrement(16);
    add(_formScrollPane, BorderLayout.CENTER);

    //nb: Java has yet to support horizontal scrolling via the touchpad

    updatePanel(null); //also calls validate & repaint
  }

  /**
   * Show a new panel and add it to the history list and the panels list. Can be
   * called from any thread.
   * @param panel
   *          Panel to display
   */
  public synchronized void display(JPanel panel) {
    Objects.requireNonNull(panel, "panel must not be null");
    if(panel != _currentPanel) {
      forget(_currentPanel);
      updatePanelLater(panel);
    }
  }

  public void close() {
    if(_currentPanel != null) {
      JPanel closePanel = _currentPanel;
      forget(closePanel);
      if(_currentPanel == closePanel) {
        updatePanel(null);
      }
      validate();
      repaint();
    }
  }

  protected void updatePanel(JPanel panel) {
    LogFactory.getLog(this.getClass()).debug("updatePanel invoked. Current Panel=" + getLbl(_currentPanel));
    if(panel == null) {
      panel = new JPanel();
      panel.add(mkRomeoLabel());
      _closeButton.setEnabled(false);
    } else {
      _closeButton.setEnabled(true);
    }

    _formScrollPane.setViewportView(panel);
    _currentPanel = panel;

    showPanelName();

    if(panel instanceof RomeoForm) {
      JComponent focusField = ((RomeoForm) panel).getFocusField();
      if(focusField != null) {
        focusField.requestFocus();
      }
      JButton saveButton = ((RomeoForm) panel).getSaveButton();
      if(saveButton != null) {
        getRootPane().setDefaultButton(saveButton);
      }
    }

    validate(); //if you dont call this then it wont be displayed!
    repaint(); //Also need to do this or only it displays sometimes    
  }

  public void showPanelName() {
    if(_currentPanel instanceof INamed) {
      _nameLabel.setText(" " + ((INamed) _currentPanel).getName());

    } else {
      _nameLabel.setText("");
    }
  }

  protected void forget(JPanel panel) {
    if(panel instanceof RomeoForm) {
      ((RomeoForm) panel).formClosing();
    }
  }

  protected void updatePanelLater(final JPanel myPanel) {
    final NavigatorPanel navigatorPanel = this;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        navigatorPanel.updatePanel(myPanel);
      }
    });
  }

  private static String getLbl(JPanel panel) {
    if(panel == null) {
      return "null";
    }
    Component[] comp = panel.getComponents();
    for(int i = 0; i < comp.length; i++) {
      Component c = comp[i];
      if(c instanceof JLabel) {
        return ((JLabel) c).getText();
      }
    }
    return "Unknown. Component count=" + panel.getComponentCount();
  }

  public JPanel getCurrentPanel() {
    return _currentPanel;
  }

  private JLabel mkRomeoLabel() {
    JLabel label = new JLabel(Romeo.LICENSE);
    label.setFont(new Font("sansserif", Font.PLAIN, 10));
    return label;
  }
}
