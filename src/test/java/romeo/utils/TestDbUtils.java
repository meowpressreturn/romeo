package romeo.utils;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import romeo.test.TestUtils;

public class TestDbUtils {
  
  //private final static double D = 0.000001;
  
  //nb: for these tests we create a fresh db for each one, so the initialisation of it is not static
  
  ////////////////////////////////////////////////////////////////////////////
  
  private DataSource _dataSource; 
  
  @Before
  public void setup() throws SQLException {
    _dataSource = TestUtils.inMemoryDatabase(); 
    try(Connection connection = _dataSource.getConnection()) {
      DbUtils.initDatabase(connection);
    }
    makeFruitsTable();
  }
  
  @After
  public void tearDown() {
    _dataSource = TestUtils.shutDownInMemoryDatabase(_dataSource);
  }
  
  @Test
  public void testReadQuery() throws Exception {
    //Basic read first
    try(Connection connection = _dataSource.getConnection()) {
      ResultSet rs = DbUtils.readQuery("SELECT * FROM fruits ORDER BY name ASC", null, connection);
      assertNotNull(rs);
      rs.next(); //move to apple
      rs.next(); //move to banana
      assertEquals("banana", rs.getString("name") );
      assertEquals("yellow", rs.getString("color") );
    }   
    
    //Read with parameters
    try(Connection connection = _dataSource.getConnection()) {
      Object[] params = new Object[] { "red", 4.0d }; //only apple matches 
      ResultSet rs = DbUtils.readQuery("SELECT * FROM fruits WHERE color=? AND weight > ? ORDER BY name ASC", params, connection);
      assertNotNull(rs);
      rs.next(); //move to apple
      assertEquals("apple", rs.getString("name") );
      assertFalse( rs.next() ); //no more rows
    } 
    
    //Null arguments checks
    try(Connection connection = _dataSource.getConnection()) {
      
      try{
        DbUtils.readQuery(null, new Object[] { }, connection);
        fail();
      } catch(NullPointerException expected) {}
      
      try {
        DbUtils.readQuery("SELECT * FROM fruits WHERE name=?",null, connection);
        fail();
      } catch(NullPointerException expected) {}
      
      try {
        DbUtils.readQuery("SELECT * FROM fruits WHERE name=?",new Object[] { }, null);
        fail();
      } catch(NullPointerException expected) {}
      
      //nb: null for the 'parameters' argument is fine
    }   
  }
  
  
  @Test
  public void testWriteQuery() throws SQLException {
    try(Connection connection = _dataSource.getConnection()) {
      TestUtils.assertRowCount(_dataSource, "fruits", 10);

      DbUtils.writeQuery("DELETE FROM fruits WHERE name='apple'", null, connection);

      TestUtils.assertRowCount(_dataSource, "fruits", 9);
      DbUtils.writeQuery("DELETE FROM fruits WHERE color=? AND weight<?", new Object[] { "green", 7.0 }, connection);

      TestUtils.assertRowCount(_dataSource, "fruits", 7);
      
      try {
        DbUtils.writeQuery(null, new Object[] { 0 }, connection);
        fail();
      } catch(NullPointerException expected) { }
      
      try {
        DbUtils.writeQuery("DELETE FROM fruits WHERE seeds=?", null, connection);
        fail();
      } catch(NullPointerException expected) { }
      
      try {
        DbUtils.writeQuery("DELETE FROM fruits WHERE seeds=?", new Object[] { 0 }, null);
        fail();
      } catch(NullPointerException expected) { }

    }
  }
  
  @Test
  public void testReadSingleValue() throws SQLException {
    try(Connection connection = _dataSource.getConnection()) {
      
      //null parameters are ok if no ? in the sql to be replaced
      assertEquals(
          new Integer(24),
          (Integer)DbUtils.readSingleValue("SELECT seeds FROM fruits WHERE name='apple'", null, connection) );
      
      assertEquals(
          new Integer(24),
          (Integer)DbUtils.readSingleValue("SELECT seeds FROM fruits WHERE name='apple'", new Object[] {}, connection) );
      
      assertEquals(
          new Integer(24),
          (Integer)DbUtils.readSingleValue("SELECT seeds FROM fruits WHERE name=?", new Object[] { "apple"}, connection) );
      
      try {
        DbUtils.readSingleValue(null, new Object[] { "apple"}, connection);
        fail();
      } catch(NullPointerException expected) {}
      //null parameters are not allowed when there are placeholders
      try {
        DbUtils.readSingleValue("SELECT seeds FROM fruits WHERE name=?", null, connection);
        fail();
      } catch(NullPointerException expected) {}
      try {
        DbUtils.readSingleValue("SELECT seeds FROM fruits WHERE name=?", new Object[] { "apple"}, null);
        fail();
      } catch(NullPointerException expected) {}
    
    }
  }

  @Test
  public void testMakeNotNull() throws SQLException {
    
    try (Connection connection = _dataSource.getConnection()) {
      //These are nullable in our default data (test for sanity)
      TestUtils.assertNullability(connection, "fruits", "color", true);
      TestUtils.assertNullability(connection, "fruits", "seeds", true);
      TestUtils.assertNullability(connection, "fruits", "weight", true);
      
      //Lets make 2 of them not null
      DbUtils.makeNonNull("fruits", "42", connection, "seeds","weight" );
      TestUtils.assertNullability(connection, "fruits", "color", true);
      TestUtils.assertNullability(connection, "fruits", "seeds", false);
      TestUtils.assertNullability(connection, "fruits", "weight", false);
      
      DbUtils.writeQuery("INSERT INTO fruits (name) VALUES ('Peach')",null,connection);
      assertEquals( 
          new BigDecimal( "42.00"), 
          (BigDecimal)DbUtils.readSingleValue("SELECT weight FROM fruits WHERE name='Peach'",null,connection) );
      assertEquals(
          new Integer(42), 
          (Integer)DbUtils.readSingleValue("SELECT seeds FROM fruits WHERE name='Peach'",null,connection) );
      
      //It should be safe to call it for a column that is already not null
      //in which case it will STILL change the default
      DbUtils.makeNonNull("fruits", "0", connection, "seeds" );
      DbUtils.writeQuery("INSERT INTO fruits (name) VALUES ('Ketchup')",null,connection);
      assertEquals( 
          new Integer(0), 
          (Integer)DbUtils.readSingleValue("SELECT seeds FROM fruits WHERE name='Ketchup'",null,connection) );
      
      //Defaulting to empty string will probably be the most common case
      DbUtils.makeNonNull("fruits", "''", connection, "color" );
      assertEquals("", (String)DbUtils.readSingleValue("SELECT color FROM fruits where name='cherry'",null,connection) );
      
      
      //Null checks
      try{
        DbUtils.makeNonNull(null, "0", connection, "seeds" );
        fail();
      } catch(NullPointerException expected) {}
      
      try{
        DbUtils.makeNonNull("fruits", null, connection, "seeds" );
        fail();
      } catch(NullPointerException expected) {}
      
      try{
        DbUtils.makeNonNull("fruits", "0", null, "seeds" );
        fail();
      } catch(NullPointerException expected) {}
      
      try{
        DbUtils.makeNonNull("fruits", "0", connection, (String[])null );
        fail();
      } catch(NullPointerException expected) {}
      
      //Since it takes a fragment of sql for the column default value we need to pass '' to get a default
      //of the empty string, and it throws an exception if we just pass an empty string here
      try{
        DbUtils.makeNonNull("fruits", "", connection, (String[])null );
        fail();
      } catch(IllegalArgumentException expected) {}
    }    
  }
  
  @Test
  public void testEnsureTrimmed() throws SQLException {
    makeUnpeeledTable(false); //full of untrimmed stuff
    try (Connection connection = _dataSource.getConnection()) {
      
      DbUtils.ensureTrimmed("unpeeled", "name", connection);      
      
      String[] expectedValues = new String[] { "grapefruit", "grapefruit", "GRAPEfruit", "tangerine", "lemon", "LEMON", "honeydew",
              "honeydew", "huckleberry", "clementine", "PaPaYa", "StrawBerry", "BLUEBERRY", "Plum", "plum", "KUMQUAT" };
      
      List<String> actualValues = new ArrayList<>();
      try (Statement s = connection.createStatement()) {
        ResultSet rs = s.executeQuery("SELECT name FROM unpeeled ORDER BY id");
        while(rs.next()) {
          actualValues.add( rs.getString(1) );
        }
      }
      
      assertArrayEquals(expectedValues, actualValues.toArray());
      
      try {
        DbUtils.ensureTrimmed(null, "name", connection);
        fail();
      } catch(NullPointerException expected) {}
      
      try {
        DbUtils.ensureTrimmed("UNPEELED", null, connection);
        fail();
      } catch(NullPointerException expected) {}
      
      try {
        DbUtils.ensureTrimmed("UNPEELED", "name", null);
        fail();
      } catch(NullPointerException expected) {}      
    }
    
    //TODO - test return value
  }
  
  @Test
  public void testFixDuplicates() throws SQLException {
    makeUnpeeledTable(true); //trimmed but with dupes
    try (Connection connection = _dataSource.getConnection()) {
      
      DbUtils.fixDuplicates("UNPEELED", "name", connection);  
      
      //nb: it uses casing of the first one encountered (id ordering)
      String[] expectedValues = new String[] { "grapefruit", "grapefruit #2", "GRAPEfruit #3", "tangerine", "lemon", "LEMON #2", "honeydew",
          "honeydew #2", "huckleberry", "clementine", "PaPaYa", "StrawBerry", "BLUEBERRY", "Plum", "plum #2", "KUMQUAT" };
      
      List<String> actualValues = new ArrayList<>();
      try (Statement s = connection.createStatement()) {
        ResultSet rs = s.executeQuery("SELECT name,id FROM unpeeled ORDER BY id");
        while(rs.next()) {
          String name = rs.getString(1);
          actualValues.add( name );
          //String id = rs.getString(2);
          //LogFactory.getLog(this.getClass()).info("id=" + id + ", name=" + name);
        }
      }
      
      assertArrayEquals(expectedValues, actualValues.toArray());
      
      try {
        DbUtils.fixDuplicates(null, "name", connection);
        fail();
      } catch(NullPointerException expected) {}
      
      try {
        DbUtils.fixDuplicates("UNPEELED", null, connection);
        fail();
      } catch(NullPointerException expected) {}
      
      try {
        DbUtils.fixDuplicates("UNPEELED", "name", null);
        fail();
      } catch(NullPointerException expected) {}      
      
      //TODO - test return value
    }
  }
  
  @Test
  public void testFixEmptyColumn() throws SQLException {
    makeUnpeeledTable(true);
    try(Connection connection = _dataSource.getConnection()) {
      
      //First we test without any empty values. It should return false and not change anything
      boolean updated = DbUtils.fixEmptyColumn("unpeeled", "name", "TOMATO", connection);
      assertFalse( updated );
      
      //Create some blanks to test on in place of honeydew
      connection.createStatement().execute("UPDATE unpeeled SET name='' WHERE name='honeydew'");
      

      updated = DbUtils.fixEmptyColumn("unpeeled", "name", "TOMATO", connection);
      assertTrue(updated);

//      ResultSet rs2= connection.createStatement().executeQuery("SELECT name FROM unpeeled");
//      while(rs2.next()) {
//        System.out.println( "value=" + rs2.getObject(1) );
//      }
      
      ResultSet rs =  connection.createStatement().executeQuery("SELECT COUNT(*) FROM unpeeled WHERE name='TOMATO'");
      rs.next();
      assertEquals(2, rs.getLong(1) );
      
    }
  }
  
  //////////////////////////////////////
  
  /**
   * Creates a table of test data named UNPEELED. There are 16 rows.
   * It contains the names of fruit in various cases with varying garbage whitespace and several
   * duplicates (including with differing whitespace)
   * @param trim if true will trim the values first (for tests where we dont want whitespace)
   */
  private void makeUnpeeledTable(boolean trim) {
    //worth noting that Ive already found several copy and paste errors by dint of making sure this
    //table doesnt match anything in romeos actual db!    
    try (Connection connection = _dataSource.getConnection()) {
      try (Statement stmt = connection.createStatement()) {
        stmt.execute("CREATE TABLE unpeeled (id INTEGER PRIMARY KEY, name VARCHAR)");
      }
      
      //16 entries and
      //there are 12 distinct names after trimming and de-dupe
      //Tests expect exactly whats here so don't change it!
      final String[] fruits = new String[] {
        "  grapefruit ",        //0
        "grapefruit",
        "GRAPEfruit",
        "tangerine",
        "   lemon ",
        "LEMON ",               //5
        "  honeydew ",
        "  honeydew ",
        "huckleberry\n",
        "\t\tclementine\n",     //9
        "PaPaYa",
        "  StrawBerry",
        "BLUEBERRY",
        "Plum", 
        "plum        ",
        "  KUMQUAT "            //15
        };
      
      int i=0;
      try (PreparedStatement ps = connection.prepareStatement("INSERT INTO unpeeled (id,name) VALUES (?,?)")) {
        for(String fruit : fruits) {
          if(trim) { fruit=fruit.trim(); }
          ps.setObject(1,i++);
          ps.setObject(2,fruit);
          ps.execute();
        }
      }      
    } catch(SQLException e) {
      throw new RuntimeException("Error setting up the UNPEELED table",e);
    }
  }
  
  private void makeFruitsTable() {
    try (Connection connection = _dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.execute(
          "CREATE TABLE fruits (" + "name VARCHAR NOT NULL PRIMARY KEY" + ",color VARCHAR"
          + ", seeds INTEGER, weight DECIMAL(10,2) )");
      stmt.close();
      
      final Object[][] data = new Object[][] {
        { "apple","red",24,5.0d },
        { "cucumber","white",100,10.50 },
        { "banana","yellow",0,4.5 },
        { "orange", "orange", 16, 7.5d },
        { "mango", "yellow",1, null },
        { "cherry", null, null, null },
        { "durian", null, 6, null },
        { "tomato", "red", null, 3.14 },
        { "pear", "green", null, 6.2 },
        { "kiwi", "green", 123, 6.0 },
      };
      
      PreparedStatement ps = connection.prepareStatement("INSERT INTO fruits (name, color, seeds, weight) VALUES (?,?,?,?)");
      for(Object[] record : data) {
        for(int i=1; i<=record.length; i++) {
          ps.setObject(i, record[i-1]);
        }
        ps.execute();
      }
      ps.close();
      
    } catch(SQLException e) {
      throw new RuntimeException("Error setting up the FRUITS table",e);
    }    
  }
  

  
}



















