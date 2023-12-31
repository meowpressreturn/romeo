package romeo.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.persistence.AbstractRecordId;
import romeo.persistence.IdBean;

/**
 * Some database related utility methods used by initialisers, test classes, and persistence code
 */
public class DbUtils {
  /**
   * In a List of {@link IdBean} find the one with the specified id and return
   * it, or null if not found.
   * @param beans
   * @param id
   * @return bean
   */
  public static <T> IdBean<T> findIdBean(List<? extends IdBean<T>> beans, T id) {
    for(int i = 0; i < beans.size(); i++) {
      IdBean<T> bean = beans.get(i);
      if(id.equals(bean.getId())) {
        return bean;
      }
    }
    return null;
  }

  /**
   * Returns the names of tables in db. All names returned are in uppercase.
   * @param connection
   * @return tableNames
   */
  public static Set<String> getTableNames(Connection connection) {
    try {
      Set<String> tableNames = new TreeSet<String>();
      ResultSet tableRs = connection.getMetaData().getTables(null, null, "%", null);
      while(tableRs.next()) {
        tableNames.add(tableRs.getString("TABLE_NAME").toUpperCase(Locale.US));
      }
      tableRs.close();
      return tableNames;
    } catch(Exception e) {
      throw new RuntimeException("An exception was caught while retrieving table names", e);
    }
  }

  /**
   * Returns the column names in the specified table. The names will be converted
   * to uppercase.
   * @param connection
   * @param tableName
   * @return names
   */
  public static Set<String> getColumnNames(Connection connection, String tableName) {
    Objects.requireNonNull(connection, "connection may not be null");
    Objects.requireNonNull(tableName, "tableName may not be null");
    try {
      Set<String> columnNames = new TreeSet<String>();
      ResultSet columnRs = connection.getMetaData().getColumns(null, null, tableName, "%");
      while(columnRs.next()) {
        columnNames.add(columnRs.getString("COLUMN_NAME").toUpperCase(Locale.US));
      }
      return columnNames;
    } catch(Exception e) {
      throw new RuntimeException("An exception was caught while retrieving column names in " + tableName, e);
    }
  }
  
  /**
   * Returns the names (in uppercase) of caolumns in the table that are nullable.
   * @param connection
   * @param tableName
   * @return names of nullable columns
   */
  public static Set<String> getNullableColumnNames(Connection connection, String tableName) {
    Objects.requireNonNull(connection, "connection may not be null");
    Objects.requireNonNull(tableName, "tableName may not be null");
    try {
      Set<String> columnNames = new TreeSet<String>();
      ResultSet columnRs = connection.getMetaData().getColumns(null, null, tableName, "%");
      while(columnRs.next()) {
        if("YES".equalsIgnoreCase( columnRs.getString("IS_NULLABLE"))) {
          columnNames.add(columnRs.getString("COLUMN_NAME").toUpperCase(Locale.US));
        }
      }
      return columnNames;
    } catch(Exception e) {
      throw new RuntimeException("An exception was caught while retrieving names of nullable columns in " + tableName, e);
    }
  }

  /**
   * Use to run a write query such as CREATE, DELETE, UPDATE, DROP, INSERT This
   * method will create a PreparedStatement and set the parameters as values.
   * Use the ? char as a placeholder in the SQL.
   * @param sql
   * @param parameters
   *          (may be null)
   * @param connection
   */
  public static int writeQuery(String sql, Object[] parameters, Connection connection) {
    Objects.requireNonNull(sql, "sql may not be null");
    if(sql.indexOf('?')>=0) {
      Objects.requireNonNull(parameters, "parameters may not be null when sql has placeholders");
    }
    Objects.requireNonNull(connection, "connection may not be null");
    String parametersSet = null;
    try {
      try (PreparedStatement statement = connection.prepareStatement(sql)) {
        parametersSet = setParameters(statement, parameters);
        return statement.executeUpdate();
      }
    } catch(Exception e) {
      logUtilsSqlError("writeQuery", sql, parametersSet, e);
      throw new RuntimeException("Error executing write query:" + sql, e);
    }
  }

  /**
   * Use to run a read query that returns a ResultSet. This method will
   * construct a PreparedStatement and feed it the specified parameters. Use ?
   * as the placeholder. Dont forget to close the statement attached to the RS.
   * @param sql
   * @param parameters
   *          (may be null)
   * @param connection
   */
  public static ResultSet readQuery(String sql, Object[] parameters, Connection connection) {
    Objects.requireNonNull(sql, "sql may not be null");
    if(sql.indexOf('?')>=0) {
      Objects.requireNonNull(parameters, "parameters may not be null when sql has placeholders");
    }
    Objects.requireNonNull(connection, "connection may not be null");
    String parametersSet = null;
    try {
      try(PreparedStatement statement = connection.prepareStatement(sql)) {
        parametersSet = setParameters(statement, parameters);
        return statement.executeQuery();
      }
    } catch(Exception e) {
      logUtilsSqlError("ReadQuery", sql, parametersSet, e);
      throw new RuntimeException("Error executing read query:" + sql, e);
    }
  }

  /**
   * Read a single value from the database. If the query results in no rows then
   * null is returned, otherwise will return the value in the first column of
   * the first row.
   * @param sql
   * @param parameters
   * @param connection
   * @return value
   */
  public static Object readSingleValue(String sql, Object[] parameters, Connection connection) {
    Objects.requireNonNull(sql, "sql may not be null");
    if(sql.indexOf('?')>=0) {
      Objects.requireNonNull(parameters, "parameters may not be null when sql has placeholders");
    }
    Objects.requireNonNull(connection, "connection may not be null");
    ResultSet rs = null;
    try {
      try {
        rs = readQuery(sql, parameters, connection);
        return rs.next() ? rs.getObject(1) : null;
      } finally {
        if(rs != null) {
          rs.close();
        }
      }
    } catch(Exception e) {
      //nb: we don't log here as it should already have been logged in the call to readQuery we used above
      throw new RuntimeException("Error reading single value", e);
    }
  }

  /**
   * Sets the parameters into the statement. Does nothing if parameters array is
   * null. Values set are logged. Returns a string that can be logged describing
   * the parameters set.
   * Subclasses of {@link AbstractRecordId} will be converted to String automatically here.
   * @param statement
   * @param parameters
   * @return logInfo
   */
  public static String setParameters(PreparedStatement statement, Object[] parameters) throws SQLException {
    if(statement == null) {
      throw new NullPointerException("statement is null");
    }
    StringBuilder buffer = new StringBuilder();
    if(parameters != null) {
      buffer.append("parameters: ");
      for(int i = 0; i < parameters.length; i++) {
        Object value = parameters[i];
        if(value instanceof AbstractRecordId) {
          value = value.toString();
        }

        buffer.append('[');
        buffer.append(i);
        buffer.append("]=");
        buffer.append(value);
        if(i + 1 < parameters.length)
          buffer.append(',');
        statement.setObject(i + 1, value);
      }
    } else {
      buffer.append("parameters == null");
    }
    return buffer.toString();
  }

  /**
   * Writes the sql string to the application log for debugging purposes (not
   * the database tx log)
   * @param info
   */
  @Deprecated
  protected static void logSqlInfo(String info) {
    try {
      Log log = LogFactory.getLog(DbUtils.class);
      log.info(info);
    } catch(Exception e) {
      ; //IGNORE. Sorry. This goes on within error logging. We dont want more
      //minor errors cluttering it all up!
    }
  }
  
  protected static void logUtilsSqlError(String method, String sql, String parametersSet, Exception e) {
    Log log = LogFactory.getLog(DbUtils.class);
    log.error(method + " error -- query=" + sql + " -- " + parametersSet, e);
  }

  /**
   * Read the values for the specified columns from the current row into the map
   * keyed by column name (map keys are case-sensitive. Key will follow the
   * column name as specified). Column names/labels must be valid.
   * @param rs
   * @param map
   * @param columns
   */
  public static void readColumns(ResultSet rs, Map<String, Object> map, String... columns) throws SQLException {
    for(String column : columns) {
      Object value = rs.getObject(column);
      map.put(column, value);
    }
  }
  
  /**
   * Emit DDL queries to update the specified columns to be NOT NULL. Note that
   * arguments are used directly in the assemmbly of the SQL via String
   * concatenation and therefore it is your responsibility to ensure that they
   * are safe.
   * @param table
   * @param defaultSql
   *          sql statement to use for the default (eg, for an empty string pass
   *          '' in a string). 
   * @param connection
   * @param columns
   */
  public static void makeNonNull(String table, String defaultSql, Connection connection, String... columns) {
    Objects.requireNonNull(table, "table may not be null");
    Objects.requireNonNull(defaultSql, "defaultSql may not be null");
    if(defaultSql.trim().isEmpty()) {
      throw new IllegalArgumentException("defaultSql may not be empty (you should use '' for default empty string)");
    }
    Objects.requireNonNull(connection, "connection may not be null");
    Objects.requireNonNull(columns, "columns may not be null");
    try {
      for(String column : columns) {
        DbUtils.writeQuery("ALTER TABLE " + table + " ALTER COLUMN " + column + " SET DEFAULT " + defaultSql, null, connection);
        DbUtils.writeQuery("UPDATE " + table + " SET " + column + "=" + defaultSql + " WHERE " + column +" IS NULL",null, connection);
        DbUtils.writeQuery("ALTER TABLE " + table + " ALTER COLUMN " + column + " SET NOT NULL", null, connection);
      }       
    } catch(Exception e) {
      throw new RuntimeException("Error making columns not null", e);
    }
  }
  
  /**
   * Initialises the hsqldb settings for the Romeo database.
   * Sets WRITE DELAY to zero, and switches off type precision (this preference was inherited from an ancient version
   * of hsqldb when Romeo was younger)
   * @param connection
   */
  public static void initDatabase(Connection connection) {
    try {
      writeQuery("SET WRITE_DELAY 0;", null, connection);
      //DbUtils.writeQuery("SET FILES WRITE_DELAY 0;",null,connection);
      writeQuery("SET DATABASE SQL NAMES FALSE;", null, connection); //does ... ? who knows. other set seems ignored without it? voodoo?
      writeQuery("SET DATABASE SQL SIZE FALSE;", null, connection); //ignore type precision limits (makes unlengthed varachar work)
    } catch(Exception e) {
      throw new RuntimeException("Error initialising the database settings");
    }
  }
  
  /**
   * Returns true if there are any records in the specified table.
   * Warning: table name is directly concatenated into the query so take care that it is safe!
   * @param table
   * @param connection   * 
   * @return has records
   */
  public static boolean hasContent(String table, Connection connection) {
    Objects.requireNonNull(table, "table may not be null");
    return (Boolean)DbUtils.readSingleValue("SELECT COUNT(*)>0 FROM "+table, null, connection);
  }
  
  /**
   * Trims whitespace from the column values in record rows using the Java String trim() method
   * one row at a time. This is because the TRIM method in sql / hsqldb sucks. (ie: you have to be very 
   * specific about which characters to trim)
   * THIS EXPECTS THE TABLE TO HAVE AN id COLUMN.
   * @param table
   * @param column
   * @param connection
   * @return foundUntrimmed
   */
  public static boolean ensureTrimmed(String table, String column, Connection connection) {
    Objects.requireNonNull(table,"table may not be null");
    Objects.requireNonNull(column,"column may not be null");
    Objects.requireNonNull(connection,"connection may not be null");
//    final String sql = "SELECT COUNT(*)>0 FROM " +table+ " WHERE " +column+ " <> TRIM(" +column+ ")";
//    if( (Boolean)DbUtils.readSingleValue(sql,null,connection)) {
//      writeQuery("UPDATE " +table+ " SET " +column+ "=TRIM(" +column+ ")",null,connection);
//      return true;
//    } else {
//      return false;
//    }
    boolean updatedAny = false;
    try {
      try (PreparedStatement ps = connection.prepareStatement("UPDATE " +table+ " SET " +column+ "=? WHERE id=?") ) {
        try (Statement s = connection.createStatement() ) {
          ResultSet rs = s.executeQuery("SELECT id," +column+ " FROM " +table);
          while(rs.next()) {
            Object id = rs.getObject(1);
            String value = rs.getString(2);
            if(value!=null) {
              String trimmedValue = value.trim();
              if(trimmedValue!=value) {
                ps.setString(1, trimmedValue);
                ps.setObject(2, id);                
                ps.execute();
                updatedAny=true;
              }
            }
          }
        }
      }
    }catch(SQLException e) {
      throw new RuntimeException("ensureTrimmed failed. Table=" + table + ", column=" + column, e);
    }
    return updatedAny;
  }
  
  /**
   * Update existing records to change duplicate values in the specified column to non-duplicates by adding a numeric
   * suffix. Duplication is considered on a Case-insensitive (but untrimmed) basis.
   * nb: this is only intended for varchar columns and expected the record table to have an id column.
   * It makes no attempt to update any other tables that may refer to the column with duplicates as a foreign
   * key.
   * Table name and column name are used directly in concatenating the sql so be sure to pass safe values.
   * You probably want to call ensureTrimmed() on the column before calling this.
   * @param table
   * @param column
   * @param connection
   * @return true if any records were updated
   */
  public static boolean fixDuplicates(String table, String column, Connection connection) {
    Objects.requireNonNull(table,"table may not be null");
    Objects.requireNonNull(column,"column may not be null");
    Objects.requireNonNull(connection,"connection may not be null");
    Log log = LogFactory.getLog(DbUtils.class);
    //First we explicitly make sure all the existing name values in the old data are trimmed
    
    try {
      //Now we will find the duplicate names and 'fix' them by changing the names of all but the first.s
      String findDupeSql = "SELECT " +column+ " FROM (SELECT UCASE(" +column+ ") AS " +column+ " FROM " +table+ ") GROUP BY "
          +column+ " HAVING COUNT(" +column+ ")>1";
      ResultSet duplicatedRs = DbUtils.readQuery(findDupeSql, null, connection);
      boolean any = false;
      while(duplicatedRs.next()) {
        String name = duplicatedRs.getString(column);
        ResultSet instancesRs = DbUtils.readQuery(
            "SELECT id," +column+ " FROM " +table+ " WHERE UCASE(" +column+ ")=? ORDER BY id",new Object[] { name }, connection);
        instancesRs.next(); //Leave the first instance untouched
        int d=2;
        while(instancesRs.next()) {
          Object id = instancesRs.getObject("id");
          String newValue = instancesRs.getString(column) + " #" + d++;
          if(log.isDebugEnabled()) {
            log.debug("Resolving duplicate " +column+ " conflict in " +table+ " for " + id 
                + " by setting " +column+ " to " + newValue);
          }
          DbUtils.writeQuery("UPDATE " +table+ " SET " +column+ "=? WHERE id=?",new Object[] { newValue, id }, connection);
          any=true;
        }
      }    
      return any;
    } catch(Exception e) {
      throw new RuntimeException("Error fixing duplicates", e);
    }
  }
  
  /**
   * Changes any rows with an (untrimmed) empty value for the (varchar) column to the specified value.
   * @param table
   * @param column
   * @param value
   * @param connection
   */
  public static boolean fixEmptyColumn(String table, String column, String value, Connection connection) {
    Objects.requireNonNull(table,"table may not be null");
    Objects.requireNonNull(column,"column may not be null");
    Objects.requireNonNull(value,"value may not be null");
    Objects.requireNonNull(connection,"connection may not be null");
    try(PreparedStatement s = connection.prepareStatement("UPDATE " +table+ " SET " +column+ "=? WHERE " +column+ "=''" )) {
      s.setString(1,value);
      s.execute();
      int updated = s.getUpdateCount();
      if(updated>0) {
        return true;
      } else {
        LogFactory.getLog(DbUtils.class).debug("Found no empty values in column " +column+ " of " + table);
        return false;  
      }
    } catch(SQLException e) {
      throw new RuntimeException("Error replacing empty values in column " +column+ " of " +table,e);
    }
  }
  
  /**
   * Reads the values from each row for the specified column of the table and returns it as a list.
   * This is primarily intended for use during debugging, hence the lack of options.
   * nb: table and column name are directly concatenated in the query so be sure the values passed are safe
   * @param table
   * @param column
   * @param connection
   * @return values
   */
  public static List<Object> readRow(String table, String column, Connection connection) {
    Objects.requireNonNull(table, "table may not be null");
    Objects.requireNonNull(column, "column may not be null");
    Objects.requireNonNull(connection, "connection may not be null");
    try{
      try(Statement s = connection.createStatement()) {
        List<Object> results = new ArrayList<>();
        ResultSet rs = s.executeQuery("SELECT " +column+ " FROM " +table);
        while(rs.next()) {
          Object value = rs.getObject(1);
          results.add(value);
        }
        return results;
      }
    } catch(SQLException e) {
      throw new RuntimeException("Error reading " +column+ " from " +table,e);
    }
  }
  
}
