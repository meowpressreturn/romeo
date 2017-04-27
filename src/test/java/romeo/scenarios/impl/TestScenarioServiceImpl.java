package romeo.scenarios.impl;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import romeo.persistence.HsqldbSettingsInitialiser;
import romeo.persistence.QndDataSource;
import romeo.scenarios.api.IScenario;
import romeo.scenarios.api.ScenarioId;
import romeo.test.ServiceListenerChecker;
import romeo.utils.DbUtils;
import romeo.utils.KeyGenImpl;

/**
 * Test the {@link ScenarioServiceImpl}, and also (indirectly) the {@link ScenarioServiceInitialiser}.
 * This also makes use of methods in {@link DbUtils}.
 */
public class TestScenarioServiceImpl {
  
  private static final int N = 3; //repeat the tests this number of times to try and catch caching related bugs
  
  /**
   * DataSource for the tests. Note that JUnit recreates the test class before
   * every test method it runs, so if you want to share stuff across tests in a
   * test class, it needs to be static. And this is probably why the beforeClass
   * annotation is only allowed for a static method?
   */
  private static DataSource __dataSource;
  private static ScenarioServiceInitialiser __initialiser;
  
  private static final String HAPPYCAT_ID = "ABC121";
  private static final String LITTERBOX_ID = "ABC122";
  private static final String ERMAGERD_ID = "ABC123";
  private static final String HAPPYCAT_CSV = "\"100*VIP,5*BS\",\"200*VIP, 1*BS\"";
  private static final String LITTERBOX_CSV = "\"100*VIP,1*BS\",\"300*VIP, 2*BS\"";
  private static final String ERMAGERD_CSV = "\"50*VIP\",\"300*VIP, 4*BS\",\"100*VIP\"";
  
  /**
   * Setup an in-memory hsqldb database to run the tests against. This will use the
   * {@link HsqldbSettingsInitialiser} to prepare the db. The 
   */
  @BeforeClass
  public static void beforeClass() {
    QndDataSource ds = new QndDataSource();
    ds.setDriver("org.hsqldb.jdbcDriver");
    ds.setDatabase("jdbc:hsqldb:mem:scenarioTestDb");
    __dataSource = ds;
    
    __initialiser = new ScenarioServiceInitialiser();
    //Setup the same hsqldb settings we use in romeo (eg: no precision on varchars etc)
    HsqldbSettingsInitialiser hsqldbSetup = new HsqldbSettingsInitialiser();
    try (Connection connection = ds.getConnection()) {      
      Set<String> tableNames = DbUtils.getTableNames(connection);
      hsqldbSetup.init(tableNames, connection);
      __initialiser.init(tableNames, connection);
    } catch (Exception e) {
      throw new RuntimeException("beforeClass failure!",e);
    }
  }
  
  @AfterClass
  public static void afterClass() {
    try (Connection connection = __dataSource.getConnection()) {      
      DbUtils.writeQuery("SHUTDOWN", null, connection);
    } catch (Exception e) {
      throw new RuntimeException("afterClass failure!",e);
    }
    __dataSource = null;
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  private ScenarioServiceImpl _scenarioService;
  private ServiceListenerChecker _listener;
  
  /**
   * Repopulates the test data and creates a new {@link ScenarioServiceImpl} for each test.
   */
  @Before
  public void setup() {

    try(Connection connection = __dataSource.getConnection()) {
      //Create some test data
      Object[][] testScenarios = new Object[][] { { HAPPYCAT_ID, "happycat", HAPPYCAT_CSV },
          { LITTERBOX_ID, "litter box", LITTERBOX_CSV }, { ERMAGERD_ID, "ermagerd", ERMAGERD_CSV }, };
      for(Object[] s : testScenarios) {
        DbUtils.writeQuery("INSERT INTO scenarios (id, name, fleetsCsv) VALUES (?,?,?)", s, connection);
      }

    } catch(Exception e) {
      throw new RuntimeException("setup failure!", e);
    }

    _scenarioService = new ScenarioServiceImpl(__dataSource, new KeyGenImpl());
    _listener = new ServiceListenerChecker();
    _scenarioService.addListener(_listener);
  }
  
  /**
   * Remove all scenario data from the test db after each method
   */
  @After
  public void tearDown() {
     try (Connection connection = __dataSource.getConnection()) { 
       __initialiser.reset(connection);
    } catch (Exception e) {
      throw new RuntimeException("tearDown failure!",e);
    }
  }
  
  /////////////////////////////////
  
  @Test
  public void testConstructor() {
    
    new ScenarioServiceImpl(__dataSource, new KeyGenImpl());
    
    try {
      new ScenarioServiceImpl(null, new KeyGenImpl());
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new ScenarioServiceImpl(__dataSource,null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testLoadScenario() {
    for(int t=0; t<N; t++) {
      //Test more than once to catch caching issues
      IScenario happycat = _scenarioService.loadScenario(new ScenarioId("ABC121"));
      assertNotNull(happycat);
      assertEquals( HAPPYCAT_ID, happycat.getId().toString() );
      assertEquals( 2, happycat.getFleets().size() );     
      assertEquals( "100*VIP,5*BS", happycat.getFleets().get(0).trim() );
      assertEquals( "200*VIP, 1*BS", happycat.getFleets().get(1).trim() );
      
      IScenario ermagerd = _scenarioService.loadScenario(new ScenarioId("ABC123"));
      assertNotNull(ermagerd);
      assertEquals( ERMAGERD_ID, ermagerd.getId().toString() );
      assertEquals( 3, ermagerd.getFleets().size() );     
      assertEquals( "100*VIP", ermagerd.getFleets().get(2).trim());
    }
    
    try {
      _scenarioService.loadScenario(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    assertNull( _scenarioService.loadScenario(new ScenarioId("nosuchscenario")) );
  }

  @Test
  public void testSaveScenario() {

    for(int t = 0; t < N; t++) {
      assertRecordCount(3+t); //each loop we will create and then update a single record
      _listener.reset();
      
      //Create a new record
      IScenario cheezburger = new ScenarioImpl(null, "cheezburger" + t, Arrays.asList("5*VIP", "6*VIP"));
      assertNull(cheezburger.getId());
      assertTrue(cheezburger.isNew());
      
      cheezburger = _scenarioService.saveScenario(cheezburger);      
      assertNotNull(cheezburger.getId());
      assertFalse(cheezburger.isNew());
      assertEquals(1, _listener.getDataChangedCount() );

      try(Connection connection = __dataSource.getConnection()) {
        //Verify directly with the db that the record exists there
        ResultSet rs = DbUtils.readQuery("SELECT * FROM scenarios WHERE id=?", new Object[] { cheezburger.getId() },
            connection);
        assertTrue(rs.next());
        assertEquals("cheezburger" + t, rs.getString("name"));
        rs.close();
      } catch(SQLException sqlEx) {
        throw new RuntimeException(sqlEx);
      }

      //Also try loading it from the service to see one just saved will load (as services may do caching stuff)
      IScenario cb2 = _scenarioService.loadScenario(cheezburger.getId());
      assertNotNull(cb2);
      assertEquals("cheezburger" + t, cb2.getName());
      assertEquals(2, cb2.getFleets().size());
     
      //Update an existing record
      _listener.reset();
      IScenario editBurger = new ScenarioImpl(cb2.getId(), "editBurger", cb2.getFleets());
      _scenarioService.saveScenario(editBurger);
      assertEquals(1, _listener.getDataChangedCount());
      
      IScenario loadEditBurger = _scenarioService.loadScenario(cb2.getId());
      assertEquals( cb2.getId(), loadEditBurger.getId() );
      assertEquals( "editBurger", loadEditBurger.getName() );
    }
    
    try {
      _scenarioService.saveScenario(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}

  }

  @Test
  public void testDeleteScenario() {
    assertRecordCount(3); //sanity test before the actual test
    ScenarioId id = new ScenarioId(LITTERBOX_ID);
    _scenarioService.deleteScenario(id);
    assertRecordCount(2);
    assertNull( _scenarioService.loadScenario(id) );    
  }
  
  @Test
  public void testGetScenarios() {
    for(int t=0; t<N; t++) {
      List<IScenario> scenarios = _scenarioService.getScenarios();
      assertEquals(3, scenarios.size());
      //it should return the entries in alphabetical order
      assertEquals( "ermagerd", scenarios.get(0).getName() );
      assertEquals( "happycat", scenarios.get(1).getName() );
      assertEquals( "litter box", scenarios.get(2).getName() );
      
      try{ //The collection returned should not be modifiable
        scenarios.remove(0); 
        fail("Expected UnsupportedOperationException");
      } catch(UnsupportedOperationException expected) {}
    }
  }
  
  @Test
  public void testDeleteAllScenarios() {
    assertRecordCount(3);
    _scenarioService.deleteAllScenarios();
    assertRecordCount(0);
    assertNull( _scenarioService.loadScenario(new ScenarioId(HAPPYCAT_ID)));
    assertNull( _scenarioService.loadScenario(new ScenarioId(LITTERBOX_ID)));
    assertNull( _scenarioService.loadScenario(new ScenarioId(ERMAGERD_ID)));
    assertEquals( 0, _scenarioService.getScenarios().size() );    
    assertEquals( 1, _listener.getDataChangedCount() );
  }
  
  //////////
  
  /**
   * Asserts that the number of scenario records in the database is as specified
   * @param expectedCount number of scenarios
   */
  private void assertRecordCount(int expectedCount) {
    try (Connection connection = __dataSource.getConnection() ) {
      long count = (Long)DbUtils.readSingleValue("SELECT COUNT(*) FROM scenarios", null, connection);
      assertEquals( "expected " + expectedCount + " records but found " + count, expectedCount, count);      
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
}



















