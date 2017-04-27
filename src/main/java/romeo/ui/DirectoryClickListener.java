package romeo.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.JFileChooser;

/**
 * Listener for use with a JFileChooser when we need to select either a single
 * file or a directory. Will allow double clicks to drill down the directory
 * structure and a single click to select the directory.
 */
public class DirectoryClickListener extends MouseAdapter {
  JFileChooser _chooser;

  public DirectoryClickListener(JFileChooser chooser) {
    _chooser = Objects.requireNonNull(chooser, "chooser may not be null");
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    if(e.getClickCount() >= 2) {
      if(_chooser.getSelectedFile().isDirectory()) {
        _chooser.setCurrentDirectory(_chooser.getSelectedFile());
        _chooser.rescanCurrentDirectory();
      }
    } else {
      _chooser.approveSelection();
    }
  }
}