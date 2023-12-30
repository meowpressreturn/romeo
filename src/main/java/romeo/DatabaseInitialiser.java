package romeo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.model.api.IServiceInitialiser;
import romeo.utils.Convert;
import romeo.utils.DbUtils;

public class DatabaseInitialiser {
  private final DataSource _dataSource;
  private final List<IServiceInitialiser> _initialisers;
  
  public DatabaseInitialiser(DataSource dataSource, List<IServiceInitialiser> initialisers) {
    _dataSource = Objects.requireNonNull(dataSource, "dataSource may not be null");
    _initialisers = Objects.requireNonNull(initialisers, "initialisers may not be null");
  }
  
  /**
   * Runs the IServiceInitialisers defined in the initialisers property (which
   * we set using spring DI). The initialisers are responsible for examining the
   * state of the database and initialising it or updating schemas inherited
   * from older versions of Romeo.
   */
  public void runInitialisers() {
    Log log = LogFactory.getLog(this.getClass());
    try {
      Romeo.incrementSplashProgress("Start database");
      Connection connection = null;
      try {
        connection = _dataSource.getConnection();
      } catch(SQLException connEx) {
        String msg = connEx.getMessage();
        if("error in script file line: 4 unexpected token: TRIGGER".equalsIgnoreCase(msg)
            || msg.toLowerCase().contains("unexpected token: trigger")) { //I can fix it!
          log.info("Fixing invalid use of TRIGGER keyword in database created with an older Romeo and HSQLDB version");
          updateTriggerField();
          connection = _dataSource.getConnection(); //retry after fix
        } else { //Ralph wrecked it
          throw connEx;
        }
      }

      try {
        long startTime = System.currentTimeMillis();
        Set<String> tableNames = DbUtils.getTableNames(connection);
        log.info("Executing service initialisers");
        for(IServiceInitialiser initialiser : _initialisers) {
          Romeo.incrementSplashProgress("Run " + initialiser.getClass().getName());
          log.info("Executing service initialiser:" + initialiser);
          initialiser.init(tableNames, connection);
        }
        long endTime = System.currentTimeMillis();
        log.info("Executed service initialisers in " + (endTime-startTime) + " ms");
      } finally {
        connection.close();
      }
    } catch(ApplicationException ae) {
      throw ae;
    } catch(Exception e) {
      throw new RuntimeException("Exception caught running initialisers", e);
    }
  }
  
  /**
   * The older version of hsqldb used by Romeo 0.5.x allowed us to use the name
   * 'trigger' as a column name, however the current version does not and won't
   * even let us open the old databases. To fix this we need to go and tweak the
   * .script file itself! See: deployment-chapt.html#dec_script_manual_change in
   * the hsqldb documentation.
   */
  protected void updateTriggerField() {
    try {
      BufferedReader reader = null;
      BufferedWriter writer = null;
      try {
        File romeoScript = new File("database/romeo.script");
        File romeoScriptOld = new File("database/romeo.script.old");

        //First rename the old script file
        boolean ok = romeoScript.renameTo(romeoScriptOld);
        if(!ok) {
          throw new RuntimeException("Failed to rename old .script file");
        }

        //Open the old script file for reading line by line
        FileInputStream inStream = new FileInputStream(romeoScriptOld);
        InputStreamReader streamReader = new InputStreamReader(inStream);
        reader = new BufferedReader(streamReader);

        //Open the new script file to copy to line by line
        FileOutputStream outStream = new FileOutputStream(romeoScript);
        OutputStreamWriter streamWriter = new OutputStreamWriter(outStream);
        writer = new BufferedWriter(streamWriter);

        String line = null;
        while((line = reader.readLine()) != null) { //Iterate all the lines in the old script file and write them to the
                                                      //new one, fixing any occurences of the invalid "trigger" and "TRIGGER" in the
                                                    //line, to the new valid names "TRIGGER" and "XFTRIGGER" before writing it.
          String newLine = line;
          newLine = Convert.replace(newLine, "trigger", "xfTrigger");
          newLine = Convert.replace(newLine, "TRIGGER", "XFTRIGGER");
          writer.write(newLine);
          writer.write("\n");
        }
      } finally {
        if(reader != null) {
          reader.close();
        }
        if(writer != null) {
          writer.close();
        }
      }
    } catch(IOException iox) {
      throw new RuntimeException("Error trying to fix trigger column in database", iox);
    }
  }
}
