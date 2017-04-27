package romeo.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * For use with JFileChooser, will approve .csv files or directories
 */
public class CsvFileFilter extends FileFilter {
  @Override
  public boolean accept(File pathname) {
    if(pathname.isDirectory()) {
      return true;
    } else if(pathname.isFile()) {
      String filename = pathname.getName().toLowerCase();
      return filename.endsWith(".csv");
    }
    return false;
  }

  @Override
  public String getDescription() {
    return "CSV Files (Comma Seperated Values)";
  }
}
