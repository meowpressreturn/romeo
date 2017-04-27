package romeo.test;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import romeo.model.api.IServiceInitialiser;
import romeo.persistence.HsqldbSettingsInitialiser;
import romeo.persistence.QndDataSource;
import romeo.utils.DbUtils;

/**
 * Some utility methods and objects for the Romeo unit tests
 */
//TODO - refactor to reduce the use of DBUtils in the test methods
public class TestUtils {

  /**
   * Returns a datasource for an in-memory hsqldb database
   * (nb: the db has NOT been initialised in any way
   * @return
   */
  public static DataSource inMemoryDatabase() {
    QndDataSource ds = new QndDataSource();
    ds.setDriver("org.hsqldb.jdbcDriver");
    ds.setDatabase("jdbc:hsqldb:mem:scenarioTestDb");
    return ds;
  }
  
  /**
   * Sends a SHUTDOWN command to the datasource. You should also set the static reference to null.
   * This method returns you a handy null to use in case you dont have any spare.
   * @param dataSource
   * @return null
   */
  public static DataSource shutDownInMemoryDatabase(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {      
      DbUtils.writeQuery("SHUTDOWN", null, connection);
    } catch (Exception e) {
      throw new RuntimeException("afterClass failure!",e);
    } 
    return null;
  }
  
  /**
   * Applies the HsqldbSettingsInitialiser to the database
   * @param dataSource
   */
  public static void initDatabaseSettings(DataSource dataSource) {
    HsqldbSettingsInitialiser hsqldbSetup = new HsqldbSettingsInitialiser();
    applyServiceInitialiser(dataSource, hsqldbSetup);
  }
  
  public static void applyServiceInitialiser(DataSource dataSource, IServiceInitialiser initialiser) {
    try (Connection connection = dataSource.getConnection()) {      
      Set<String> tableNames = DbUtils.getTableNames(connection);
      initialiser.init(tableNames, connection);
    } catch (Exception e) {
      throw new RuntimeException("exception applying service initialiser " + initialiser,e);
    }   
  }
  
  /**
   * Sends the HSQLDB SHUTDOWN command to the database. 
   * Probably unnecessary for in-memory ones we are discarding.
   * @param dataSource
   */
  public static void shutdownDatabase(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {      
      DbUtils.writeQuery("SHUTDOWN", null, connection);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Selects the count of rows in the specified table and asserts it is as expected.
   * Warning, the table name is directly appended to query so never use untrusted values!
   * @param dataSource
   * @param table
   * @param expected
   */
  public static void assertRowCount(DataSource dataSource, String table, long expected) {
//    Objects.requireNonNull(table,"table may not be null");
//    try (Connection connection = dataSource.getConnection() ) {
//      long count = (Long)DbUtils.readSingleValue("SELECT COUNT(*) FROM " + table, null, connection);
//      assertEquals( "row count", expectedCount, count);      
//    } catch(SQLException e) {
//      throw new RuntimeException(e);
//    }
    try(Connection connection = dataSource.getConnection()) {
      assertRowCount(connection, table, expected);
    } catch(SQLException e) {
      throw new RuntimeException("Error asserting row count",e);
    }
  }
  
  /**
   * Assert that the specified table has a certain row count
   * warning: table name is used to directly concatenate sql, be sure only safe values passed
   * 
   */
  public static void assertRowCount(Connection connection, String table, long expected) throws SQLException {
    Objects.requireNonNull(table);
    Objects.requireNonNull(connection);
    String sql = "SELECT COUNT(*) FROM " + table;
    long actual;
    try (Statement s = connection.createStatement()) {
      ResultSet rs = s.executeQuery(sql);
      rs.next();
      actual = rs.getLong(1);                
    }
    assertEquals(sql,expected,actual);
  }
  
  /**
   * Tests if the specified column of the specified table is nullable or not. 
   * This actually takes table and column patterns (as per DatabaseMetaData.getColumns) so can test multiple
   * columns at once.
   * @param connection
   * @param table table pattern
   * @param column column pattern
   * @param assertNullable
   */
  public static void assertNullability(Connection connection, String table, String column, boolean assertNullable) throws SQLException {
    Objects.requireNonNull(connection, "connection may not be null");
    Objects.requireNonNull(table, "table may not be null");
    Objects.requireNonNull(column, "column may not be null");
    
    ResultSet rs = connection.getMetaData().getColumns(null, null, table, column);
    while(rs.next()) {
      String columnName = rs.getString("COLUMN_NAME");
      boolean columnNullable = "YES".equalsIgnoreCase( rs.getString("IS_NULLABLE") );
      if(assertNullable) {
        assertTrue( "Column " + columnName + " was expected to be nullable but is NOT NULL", columnNullable );
      } else {
        assertFalse( "Column " + columnName + " was expected to be NOT NULL but is nullable", columnNullable );
      }
    }
    
    
    
  }
  
}



















