package romeo.model.api;

import java.sql.Connection;
import java.util.Set;

/**
 * Interface for classes called used to initialise the database (etc...) on
 * first use. ie: create tables, import initial units and perform other such
 * initialisation activities.
 */
public interface IServiceInitialiser {
  /**
   * Called to initialise the database for the related services tables if not
   * already done. A list of tablenames in the database is provided to allow
   * this check to be made.
   * @param tableNames
   *          the names of all the existing database tables
   * @param connection
   *          a connection to the database
   */
  public void init(Set<String> tableNames, Connection connection);

  /**
   * Called to cleanup any data related to the appropriate service in the
   * database. This could be called when we want to clean out old data in
   * anticipation of a new game or during unit testing.
   * @param connection
   *          a connection to the database
   */
  public void reset(Connection connection);
}
