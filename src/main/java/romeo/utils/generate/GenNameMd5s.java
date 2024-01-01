package romeo.utils.generate;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;

import romeo.importdata.impl.CsvUnitFile;
import romeo.utils.Convert;

/**
 * Scratch code used by developer. Generates md5 hash strings of unit names
 * Expects unit file in c:/dev/romeo/unit.csv
 */
public class GenNameMd5s {
  public static final String DEFAULT_NAME_COLUMN = "name";
  public static final String[] DEFAULT_COLUMNS = new String[] { DEFAULT_NAME_COLUMN, "firepower", "maximum", "offense",
      "defense", "attacks", "pd", "carry", "speed", "complexity", "basePrice", "cost", "license", "unitID",
      "turnAvailable", "stealth", "scanner" };

  public static void main(String[] args) {
    try {
      File file = new File("c:/dev/romeo/resources/unit.csv");
      CsvUnitFile csvFile = new CsvUnitFile(LoggerFactory.getLogger(CsvUnitFile.class), file, DEFAULT_COLUMNS, DEFAULT_NAME_COLUMN);
      Iterator<String> i = csvFile.getNames().iterator();
      while(i.hasNext()) {
        String name = (String) i.next();
        Map<String, String> row = csvFile.getRow(name);
        String unitName = row.get("name");
        String md5 = Convert.toDigestSignature(unitName.getBytes());
        System.out.println("<adjustment signature=\"" + md5 + "\" acronym=\"" + unitName + "\"/>");
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
}
