package romeo.utils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import org.slf4j.LoggerFactory;

import romeo.importdata.impl.CsvUnitFile;

/**
 * Utility for getting signatures of units (so you can edit the adjustments file). Intended to be run from the
 * command line or IDE.
 */
public class GetMd5Dump {
  
  private static final String NAME = "name";
  
  /**
   * Program to get a quick and dirty tool to get a dump of the md5 signatures of unit names.
   * Requires unit.csv in the classpath (eg: in resources)
   * You can override the default column names by passing columns as args. Othewise will use the
   * column list that was current as of 0.6.3.
   * (Its not neccessary to specify all columns, just up to the name column, which at present is the
   * first column)
   */
  public static void main(String[] args) {
    
    //If name isn't the first column in unit.csv then need to override by passing args to spec columns up to name
    String[] columns = (args==null || args.length==0) ? new String[] { NAME } : args;
    GetMd5Dump instance = new GetMd5Dump("unit.csv", columns);
    String dump = instance.readFile();
    System.out.println(dump);    
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  private String[] _columns;
  private String _resource;
  
  public GetMd5Dump(String resource, String[] columns) {
    _resource = Objects.requireNonNull(resource, "resource may not be null");
    _columns = Objects.requireNonNull(columns, "columns may not be null");
    if(columns.length==0) {
      throw new IllegalArgumentException("no columns specified");
    }
  }
  
  public String readFile() {
    InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(_resource);
    CsvUnitFile uf = new CsvUnitFile(LoggerFactory.getLogger(CsvUnitFile.class), stream, _columns, NAME);
    Iterator<Map<String,String>> i = uf.iterator();
    StringBuilder builder = new StringBuilder();
    ArrayList<String> values = new ArrayList<>(2);
    values.add("");
    values.add("");
    while(i.hasNext()) {
      String name = i.next().get(NAME);      
      values.set(0,  Convert.toDigestSignature(name.getBytes()) );
      values.set(1, name );
      String dumpLine = Convert.toCsv(values);
      builder.append(dumpLine);
      builder.append('\n');
    }    
    return builder.toString();
  }

}



















