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

import org.slf4j.Logger;

import romeo.importdata.IUnitFile;
import romeo.utils.Convert;

/**
 * Class that represents the contents of an UltraCorps (tm) CSV file. It reads
 * the specified file in immediately and provides a simple API to code that
 * wants to obtain data from the file. The CSV parsing code is rather naive and
 * does not support things such as quoting of values.
 */
public class CsvUnitFile implements IUnitFile {
  
  private final Logger _log;
  private String[] _columns;
  private LinkedHashMap<String, Map<String, String>> _rows;
  private String _nameColumn;
  private List<String> _names;

  /**
   * Constructor. The columnNames array specifies the order of the properties
   * being read for each row, and the nameColumn identifies which of these
   * columns is the 'name' of the unit.
   * @param file
   * @param columns
   * @param nameColumn
   */
  public CsvUnitFile(
      Logger log,
      File file, 
      String[] columns, 
      String nameColumn) {
    _log = Objects.requireNonNull(log, "log may not be null");
    Objects.requireNonNull(file, "file may not be null");
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
      throw new RuntimeException("Exception reading from CSV file " + file, e);
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
  public CsvUnitFile(
      Logger log, 
      InputStream stream, 
      String[] columns, 
      String nameColumn) {
    _log = Objects.requireNonNull(log, "log may not be null");
    Objects.requireNonNull(stream, "stream may not be null");
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
          _log.info("Input Line=" + line);
          line = line.trim();
          if(line.startsWith("#!COLUMNS=")) {
            line = line.substring(line.indexOf('=') + 1);
            columns = Convert.toStrArray(line, ",");
            _log.debug("New columns list=" + line);
          } else if(line.startsWith("#!NAMECOLUMN=")) {
            nameColumn = line.substring(line.indexOf('=') + 1);
            _log.debug("New nameColumn=" + nameColumn);
          } else if("".equals(line) || line.startsWith("#")) {
            ;
          } else {
            LinkedHashMap<String, String> columnValues = new LinkedHashMap<String, String>();
            List<String> values = Convert.fromCsv(line);
            values = Convert.trimStrings(values); //We want all unit data trimmed
            int l = values.size();
            for(int i = 0; i < columns.length; i++) {
              String column = columns[i];
              String value = (i < l) ? values.get(i) : null;
              columnValues.put(column, value);
            }
            String name = (String) columnValues.get(nameColumn);
            if(name != null) {
              _log.info("Parsed Record=" + columnValues);
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
