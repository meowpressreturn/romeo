//2008-12-09
package romeo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.ApplicationException;
import romeo.Romeo;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;

/**
 * Class that creates and displays an error message in a modal dialog box. Pass
 * the exception to the constructor. Call show() to show the dialog.
 */
public class ErrorDialog {
  private static final int COLUMNS = 90;

  private JDialog _dialog;
  private JTextArea _textArea;
  private boolean _exitOnClose;
  private JLabel _titleLabel;

  /**
   * Constructor. If an exception is provided and it is an
   * {@link ApplicationException} then its message will be rendered. For other
   * types of exception, the message, the romeo version, and the stacktrace will
   * all be rendered. If the exitOnClose flag is true then when the dialog is
   * closed the application will also be exited. nb: you will need to call
   * show() to make the dialog appear
   * @param title
   * @param ex
   *          optional exception to log the stacktrace for
   * @param exitOnClose
   *          if true Romeo will exit when the dialog is closed
   */
  public ErrorDialog(String title, Exception ex, boolean exitOnClose) {
    setExitOnClose(exitOnClose);
    Frame frame = getFrame();
    String errorText = formatMessage(title, ex);
    _dialog = new JDialog(frame, title, exitOnClose);
    _dialog.getContentPane().setBackground(Color.BLUE);
    _dialog.setResizable(false);
    _dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    _dialog.addWindowListener(new WindowAdapter() { //Call close when the dialog windows x button is clicked
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });

    _textArea = new JTextArea();
    _textArea.setMargin(new Insets(8, 8, 8, 8));
    _textArea.setTabSize(2);
    _textArea.setText(errorText);
    _textArea.setEditable(false);
    Font font = new Font("Lucida Console", Font.BOLD, 12);
    _textArea.setFont(font);
    _textArea.setForeground(Color.WHITE);
    _textArea.setBackground(_dialog.getContentPane().getBackground());
    JScrollPane textScroll = new JScrollPane(_textArea);
    textScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    textScroll.getVerticalScrollBar().setUnitIncrement(16);
    Dimension size = new Dimension(800, 400);
    textScroll.setMaximumSize(size);
    textScroll.setPreferredSize(size);

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        close();
      };
    });
    closeButton.setIcon(GuiUtils.getImageIcon("/images/cross.gif"));

    _titleLabel = new JLabel("Error Details:");
    _titleLabel.setFont(new Font("Dialog", Font.BOLD, 16));
    _titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
    _titleLabel.setOpaque(true);
    _titleLabel.setBackground(Color.RED);

    JPanel panel = new JPanel();
    GridBagConstraints gbc = GuiUtils.prepGridBag(panel);
    gbc.insets = new Insets(1, 1, 1, 1);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 2;
    gbc.gridy = -1;

    gbc.gridy++;
    panel.add(_titleLabel, gbc);

    gbc.gridy++;
    panel.add(textScroll, gbc);

    gbc.gridy++;
    panel.add(closeButton, gbc);

    _dialog.getContentPane().add(panel, BorderLayout.CENTER);

    _dialog.pack();
    _dialog.setLocationRelativeTo(frame);
  }

  /**
   * Set the text for the title label in the dialog. By default this is the
   * incredibly useful description "An error has occured".
   * @param title
   */
  public void setTitle(String title) {
    _titleLabel.setText(title);
  }

  public String getTitle() {
    return _titleLabel.getText();
  }

  /**
   * Display the dialog. Clicking the dialogs close buttons will cause the
   * close() method to be called. (This may also be called programatically). It
   * is safe to call this from other threads than the AWT event thread.
   */
  public void show() {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        _dialog.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            _textArea.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
          }
        });
      }
    });
  }

  /**
   * Hide and dispose the dialog. If the exitOnClose flag is set then this will
   * also cause the application to exit immediately with a -1 code and a message
   * about abnormal exit would be written to the log if one is configured.
   */
  public void close() {
    _dialog.setVisible(false);
    _dialog.dispose();

    if(_exitOnClose) {
      Log log = LogFactory.getLog(Romeo.class);
      log.info("Romeo application is now exiting abnormally.");
      System.exit(-1);
    }
  }

  /**
   * Formats the stacktrace and other information for display
   * @param title
   * @param e
   */
  private String formatMessage(String title, Exception e) {
    if(e == null) {
      return "Unknown error";
    } else if(e instanceof ApplicationException) { //For ApplicationException just show the message
      String msg = e.getMessage();
      msg = Convert.wordWrap(msg, COLUMNS);
      return msg;
    } else { //Extract the stack trace as a string if its not an ApplicationException
      String msg = e.getMessage();
      msg = Convert.wordWrap(msg, COLUMNS) + "\n\n";
      Throwable root = Convert.rootCause(e);
      if(root != null) {
        msg += Convert.wordWrap("Root Cause:" + root.getMessage(), COLUMNS) + "\n";
      }
      msg += "Romeo version=" + Romeo.ROMEO_VERSION + "\n\n";
      StringWriter writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      msg += writer.getBuffer().toString();
      return msg;
    }
  }

  /**
   * Returns the reference to the mainframe if it has been set. Null if not.
   * @return frame
   */
  private Frame getFrame() {
    try {
      return Romeo.getMainFrame();
    } catch(IllegalStateException e) { //Ignore exception and return null
      return null;
    }
  }

  /**
   * If true the close() method will call System.exit(-1) and make a note about
   * abnormal exit in the log.
   * @return exitOnClose
   */
  public boolean isExitOnClose() {
    return _exitOnClose;
  }

  /**
   * Set whether or not to exit the application when close is called()
   * @param exitOnClose
   */
  public void setExitOnClose(boolean exitOnClose) {
    _exitOnClose = exitOnClose;
  }
}
