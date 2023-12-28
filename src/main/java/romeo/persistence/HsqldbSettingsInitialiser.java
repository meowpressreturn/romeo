package romeo.persistence;

import java.sql.Connection;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.model.api.IServiceInitialiser;
import romeo.utils.DbUtils;

/**
 * The HSQLDB settings initialiser will prepare Romeo's hsqldb database, setting up its configuration and such like
 * Sets some db-wide settings (such as WRITE_DELAY)
 */
public class HsqldbSettingsInitialiser implements IServiceInitialiser {
  /**
   * If the WORLDS table hasnt been created assume the db is being freshly set
   * up and set the WRITE_DELAY to 0.
   * @param tableNames
   * @param connection
   */
  @Override
  public void init(Set<String> tableNames, Connection connection) {
    Log log = LogFactory.getLog(this.getClass());
    if(!tableNames.contains("WORLDS")) {

//      log.info("Initialising HSQLDB database settings");
//      DbUtils.writeQuery("SET WRITE_DELAY 0;", null, connection);
//      //DbUtils.writeQuery("SET FILES WRITE_DELAY 0;",null,connection);
//      DbUtils.writeQuery("SET DATABASE SQL NAMES FALSE;", null, connection); //does nothing!
//      DbUtils.writeQuery("SET DATABASE SQL SIZE FALSE;", null, connection); //ignore type precision limits (makes unlengthed varachar work)
      
      //We use the existence of the WORLDS table as a way of determining if this is the
      //first time we are running. If it is we need to change the write delay to avoid
      //losing data on application shutdown. (HSQLDB by default wouldnt write to the db
      //immediately for performance reasons).
      log.info("Initialising HSQLDB database settings");
      DbUtils.initDatabase(connection);
    } else {
      log.debug("Skipping Hsqldb settings initialisation");
    }
  }

  /**
   * No-op
   * @param connection
   *          unused
   */
  @Override
  public void reset(Connection connection) {
    ; //noop
  }

}
