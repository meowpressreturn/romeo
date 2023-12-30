/*
 * AboutAction.java
 * Created on Feb 7, 2006
 */
package romeo.ui.actions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import romeo.utils.Convert;
import romeo.utils.GuiUtils;

/**
 * Shows a dialog frame containing text from a specified file. The text will be
 * word wrapped.
 */
public class AboutAction extends AbstractAction {
  private String _aboutText = null;
  private JFrame _frame;
  private String _name;
  private String _file;

  private static final int COLS = 90;

  public AboutAction(JFrame frame, String name, String file) {
    _frame = frame;
    _name = name;
    _file = file;
    putValue(Action.NAME, name);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Color background = Color.WHITE;
    Color textColour = Color.BLACK;

    if(_aboutText == null) {
      _aboutText = "\n" + loadText(_file);
    }
    JDialog dialog = new JDialog(_frame, _name, true);
    dialog.getContentPane().setBackground(background);
    dialog.setSize(690, 580);
    dialog.setResizable(false);
    JTextArea textArea = new JTextArea();
    textArea.setTabSize(2);
    textArea.setText(_aboutText);
    textArea.setEditable(false);
    //textArea.setWrapStyleWord(true); //ignored apparently (eh?? how can?)
    Font font = new Font("Monospaced", Font.PLAIN, 12);
    textArea.setFont(font);
    textArea.setForeground(textColour);
    textArea.setBackground(dialog.getContentPane().getBackground());

    JPanel panel = new JPanel();
    panel.setBackground(dialog.getContentPane().getBackground());
    GridBagConstraints gbc = GuiUtils.prepGridBag(panel);
    gbc.insets = new Insets(0, 16, 0, 16);
    gbc.weightx = 2;
    gbc.weighty = 2;

    //Fail, the scrollpane is only a few pixel high (width ok)
    //JScrollPane scrollPane = new JScrollPane(textArea);
    //panel.add(scrollPane, gbc);
    //dialog.getContentPane().add(panel, BorderLayout.CENTER);

    //Original method - functional but no insets
    //JScrollPane scrollPane = new JScrollPane(textArea);
    //dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);

    //Insets are scrolled as part of the content
    panel.add(textArea, gbc);
    JScrollPane textScroll = new JScrollPane(panel);
    textScroll.getVerticalScrollBar().setUnitIncrement(16);
    dialog.getContentPane().add(textScroll, BorderLayout.CENTER);

    dialog.setLocationRelativeTo(_frame);
    dialog.setModal(false);
    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    dialog.validate();
    dialog.setVisible(true);

    final JTextArea myTextArea = textArea;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        myTextArea.scrollRectToVisible(new Rectangle(0, 0, 1, 1));
      }
    });
  }

  private String loadText(String file) {
    try {
      InputStream stream = getClass().getClassLoader().getResourceAsStream(file);
      if(stream==null) {
        throw new IllegalStateException("No about text resource:" + _file);
      }
      InputStreamReader reader = new InputStreamReader(stream);
      BufferedReader br = new BufferedReader(reader);
      StringBuffer buffer = new StringBuffer();
      String line = null;
      do {
        line = br.readLine();
        if(line != null) {
          buffer.append(line);
        }
        buffer.append("\n");
      } while(line != null);
      br.close();
      return Convert.wordWrap(buffer.toString(), COLS);
    } catch(Exception e) {
      e.printStackTrace();
      return "Failed to load file:" + file;
    }
  }
}