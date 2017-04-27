package romeo.importdata.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.importdata.IWorldFile;
import romeo.utils.Convert;

/**
 * qnd, needs refactoring
 */
public class CsvWorldFile implements IWorldFile {
//  public static final String DEFAULT_NAME_COLUMN = "name";
//  public static final String[] DEFAULT_COLUMNS = new String[] { "worldID", DEFAULT_NAME_COLUMN, "worldX", "worldY",
//      "worldEi", "worldRer", "ownerID", "owner", "ownerRace", "worldType", "labour", "capital", "firepower", "team"
//
//  };

  public class ReadException extends Exception {
    private File _file;
    
    public ReadException(File file, Exception e) {
      super("Error reading world csv: " + file,e);
      _file = file;
    }
    
    public File getFile() {
      return _file;
    }
  }
  
  private String[] _columns;
  private LinkedHashMap<String, Map<String, String>> _rows;
  private String _nameColumn;
  private List<String> _names;

  /**
   * Constructor.
   * @param file
   * @param columns
   * @param nameColumn
   */
  public CsvWorldFile(File file, String[] columns, String nameColumn) throws ReadException {
    Objects.requireNonNull(file, "file may not be null");
    Objects.requireNonNull(columns, "columns may not be null");
    Objects.requireNonNull(nameColumn,"nameColumn may not be null");
    init(columns, nameColumn);
    try {
      if(!file.exists()) {
        throw new IllegalStateException("File does not exist:" + file);
      }
      if(!file.isFile()) {
        throw new IllegalStateException("Not a file:" + file);
      }
      FileInputStream stream = new FileInputStream(file);
      parseCsv(stream);
    } catch(IOException e) {
      throw new ReadException(file, e);
    }
  }

  /**
   * Constructor. The columnNames array specifies the order of the properties
   * being read for each row, and the nameColumn identifies which of these
   * columns is the 'name' of the unit.
   * @param stream
   *          an InputStream to read the csv from
   * @param columns
   * @param nameColumn
   */
  public CsvWorldFile(InputStream stream, String[] columns, String nameColumn) {
    if(stream == null) {
      throw new NullPointerException("stream is null");
    }
    init(columns, nameColumn);
    parseCsv(stream);
  }

  /**
   * Initialise the columns and nameColumn settings.
   * @param columns
   * @param nameColumn
   */
  protected void init(String[] columns, String nameColumn) {
    if(columns == null) {
      throw new NullPointerException("columns is null");
    }
    _columns = columns;
    if(nameColumn == null || "".equals(nameColumn)) {
      throw new IllegalArgumentException("name column not specified");
    }
    _nameColumn = nameColumn;
  }

  /**
   * Reads data from the supplied stream and parses the information based on the
   * column setting and so forth. This method will close the stream when it has
   * finished. The extracted information is then available from the various
   * getters provided by this class.
   * @param stream
   */
  protected void parseCsv(InputStream stream) {
    Log log = LogFactory.getLog(this.getClass());

    String nameColumn = getNameColumn();
    String[] columns = getColumns();

    try {
      _names = new ArrayList<String>();
      LinkedHashMap<String, Map<String, String>> rowValues = new LinkedHashMap<String, Map<String, String>>();
      BufferedReader reader = null;
      try {
        InputStreamReader streamReader = new InputStreamReader(stream);
        reader = new BufferedReader(streamReader);
        String line;
        while((line = reader.readLine()) != null) {
          if(log.isDebugEnabled()) {
            log.debug("Input Line=" + line);
          }
          line = line.trim();
          if(line.startsWith("#!COLUMNS=")) {
            line = line.substring(line.indexOf('=') + 1);
            columns = Convert.toStrArray(line, ",");
            if(log.isDebugEnabled()) {
              log.debug("New columns list=" + line);
            }
          } else if(line.startsWith("#!NAMECOLUMN=")) {
            nameColumn = line.substring(line.indexOf('=') + 1);
            if(log.isDebugEnabled()) {
              log.debug("New nameColumn=" + nameColumn);
            }
          } else if("".equals(line) || line.startsWith("#")) {
            ;
          } else {
            LinkedHashMap<String, String> columnValues = new LinkedHashMap<String, String>();
            //ColumnTokeniser tokenizer = new ColumnTokeniser(line);
            List<String> values = Convert.fromCsv(line);
            values = Convert.trimStrings(values); //turns out some UC worlds have extra spaces! We want world data trimmed however
            int l = values.size();
            for(int i = 0; i < columns.length; i++) {
              String column = columns[i];
              String value = (i < l) ? values.get(i) : null;
              columnValues.put(column, value);
            }
            String name = (String) columnValues.get(nameColumn);
            if(name != null) {
              if(log.isDebugEnabled()) {
                log.debug("Parsed Record=" + columnValues);
              }
              rowValues.put(name, columnValues);
              _names.add(name);
            }
          }
        }
        _rows = rowValues;
      } finally {
        if(reader != null)
          reader.close();
      }
    } catch(Exception e) {
      throw new RuntimeException("Exception reading CSV from stream", e);
    }
  }

  /**
   * Returns an iterator over the row values. Each row is represented as a Map
   * whose keys are the column names that you passed to the constructor
   * originally.
   * @return iterator
   */
  @Override
  public Iterator<Map<String, String>> iterator() {
    return _rows.values().iterator();
  }

  /**
   * Returns a list of unit names from the CSV that was read. These are the
   * values found in the column named by the nameColumn property
   * @return names a List of String containing values from the name column
   */
  @Override
  public List<String> getNames() {
    return _names;
  }

  /**
   * Return the map for a particular row. The rows are keyed by the "name"
   * column.
   * @param name
   */
  @Override
  public Map<String, String> getRow(String name) {
    if(name == null || "".equals(name)) {
      throw new IllegalArgumentException("name not specified");
    }
    return _rows.get(name);
  }

  /**
   * Returns the columnNames array that specifies the order of the columns in
   * the csv
   * @return columnNames
   */
  @Override
  public String[] getColumns() {
    return _columns;
  }

  /**
   * Returns the name of the column used to identify the unit
   * @return nameColumn
   */
  @Override
  public String getNameColumn() {
    return _nameColumn;
  }

}
