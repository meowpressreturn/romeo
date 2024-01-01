package romeo.persistence;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import romeo.ApplicationException;

/**
 * Quick and dirty implementation of datasource. Does no pooling of connections.
 * Slow. (But quite adequate for the job at hand. A more involved application
 * would replace this implementation with something more robust and featureful
 * such as commons BasicDataSource for example)
 * This expects to be used with HSQLDB and hardcodes the username and password
 * to "sa","". If you wish to swap in another database you would do better to 
 * use a proper connection pool too, and configure that in the context.
 * (We don't bother using a proper connection pool in Romeo because it is a trivial
 * application and we want to keep the library count down to avoid bloating the 
 * distro even more!)
 */
public class QndDataSource implements DataSource {
  
  private final static org.slf4j.Logger LOG = LoggerFactory.getLogger(QndDataSource.class);
  
  private String _driver = null;
  private String _database = null;
  private int _loginTimeout = 0;
  
  /**
   * Returns a connection
   * @return connection
   * @throws SQLException
   */
  @Override
  public Connection getConnection() throws SQLException {
    return getConnection("sa", "");
  }

  /**
   * Returns a connection
   * @param userName
   * @param password
   * @return connection
   * @throws SQLException
   */
  @Override
  public Connection getConnection(String userName, String password) throws SQLException {
    String driver = getDriver();
    if(driver == null) {
      throw new NullPointerException("no driver specified");
    }
    String db = getDatabase();
    if(db == null) {
      throw new NullPointerException("no database specified");
    }

    if(userName == null || "".equals(userName)) {
      throw new UnsupportedOperationException("Username not specified");
    }
    if(password == null) {
      throw new NullPointerException("password is null");
    }
    try {
      Class.forName(driver);
    } catch(Exception e) {
      throw new RuntimeException("Error loading class:" + driver, e);
    }
    Connection c = null;

    try {
      c = DriverManager.getConnection(db, userName, password);
    } catch(SQLException sqlE) {
      //Caused by: java.sql.SQLException: Database lock acquisition failure: lockFile: org.hsqldb.persist.LockFile@d534dadc[file =C:\dev\romeo\database\romeo.lck, exists=true, locked=false, valid=false, ] method: checkHeartbeat read: 2013-07-28 06:29:36 heartbeat - read: -1681 ms.
      //at org.hsqldb.jdbc.Util.sqlException(Unknown Source)
      String msg = (sqlE.getMessage() == null) ? "" : sqlE.getMessage();
      if(msg.startsWith("Database lock acquisition failure")) {
        LOG.error("", sqlE);
        String txt = "Unable to open the database.\n"
            + "This may be because another instance of Romeo is already running?\n\n\n" + "Exception message:" + msg;
        throw new ApplicationException(txt, sqlE);
      }
      throw sqlE;
    } catch(Exception e) {
      throw new RuntimeException("Unable to get connection to " + db, e);
    }
    return c;
  }

  /**
   * Set the login timeout (I do not belive this is used?)
   * @param timeout
   * @throws SQLException
   */
  @Override
  public void setLoginTimeout(int timeout) throws SQLException {
    _loginTimeout = timeout;
  }

  /**
   * Returns the login timeout (I do not belive this is used?)
   * @return loginTimeout
   * @throws SQLException
   */
  @Override
  public int getLoginTimeout() throws SQLException {
    return _loginTimeout;
  }

  /**
   * Returns a new PrintWriter wrapping System.out
   * @return logWriter
   * @throws SQLException
   */
  @Override
  public PrintWriter getLogWriter() throws SQLException {
    return new PrintWriter(System.out);
  }

  /**
   * NOT SUPPORTED
   * @throws UnsupportedOperationException
   */
  @Override
  public void setLogWriter(PrintWriter arg0) throws SQLException {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Returns the url of the database
   * @return database
   */
  public String getDatabase() {
    return _database;
  }

  /**
   * Set the url of the database
   * @param dbUrl
   */
  public void setDatabase(String dbUrl) {
    _database = dbUrl;
  }

  /**
   * Returns the name of the driver class
   * @return driver
   */
  public String getDriver() {
    return _driver;
  }

  /**
   * Sets the name of the driver class
   * @param driver
   */
  public void setDriver(String string) {
    _driver = string;
  }

  /**
   * Returns null
   */
  @Override
  public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
    return null; //TODO - should it throw SQLFeatureNotSupportedException ?
  }

  @Override
  public boolean isWrapperFor(Class<?> arg0) throws SQLException {
    return false;
  }

  @Override
  public <T> T unwrap(Class<T> arg0) throws SQLException {
    return null;
  }

}
