package romeo.persistence;

import java.sql.Connection;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;

import romeo.model.api.IServiceInitialiser;
import romeo.utils.DbUtils;

/**
 * The HSQLDB settings initialiser will prepare Romeo's hsqldb database, setting up its configuration and such like
 * Sets some db-wide settings (such as WRITE_DELAY)
 */
public class HsqldbSettingsInitialiser implements IServiceInitialiser {
  
  private final Logger _log;
  
  public HsqldbSettingsInitialiser(Logger log) {
    _log = Objects.requireNonNull(log, "log may not be null");
  }
  
  /**
   * If the WORLDS table hasnt been created assume the db is being freshly set
   * up and set the WRITE_DELAY to 0.
   * @param tableNames
   * @param connection
   */
  @Override
  public void init(Set<String> tableNames, Connection connection) {
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
      _log.info("Initialising HSQLDB database settings");
      DbUtils.initDatabase(connection);
    } else {
      _log.debug("Skipping Hsqldb settings initialisation");
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
