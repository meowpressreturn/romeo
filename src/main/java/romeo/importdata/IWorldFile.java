package romeo.importdata;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Interface to an object that represents a parsed worlds csv file, providing
 * methods to access the information.
 */
public interface IWorldFile {
  public Iterator<Map<String, String>> iterator();

  public List<String> getNames();

  public Map<String, String> getRow(String name);

  public String[] getColumns();

  public String getNameColumn();
}
