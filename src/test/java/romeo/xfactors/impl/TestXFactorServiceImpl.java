package romeo.xfactors.impl;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import romeo.persistence.DuplicateRecordException;
import romeo.persistence.HsqldbSettingsInitialiser;
import romeo.persistence.QndDataSource;
import romeo.test.ServiceListenerChecker;
import romeo.units.impl.MockUnitService;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;
import romeo.utils.KeyGenImpl;
import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.api.XFactorId;

public class TestXFactorServiceImpl {
  
  private static final int N = 3;

  private static DataSource __dataSource;
  private static XFactorServiceInitialiser __initialiser;
  
  @BeforeClass
  public static void beforeClass() {
    QndDataSource ds = new QndDataSource();
    ds.setDriver("org.hsqldb.jdbcDriver");
    ds.setDatabase("jdbc:hsqldb:mem:scenarioTestDb");
    __dataSource = ds;
    
    __initialiser = new XFactorServiceInitialiser(new MockUnitService(), null, new KeyGenImpl());
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
  
  private IXFactorService _xFactorService;
  private MockUnitService _mockUnitService;
  private ServiceListenerChecker _listener;
  
  @Before
  public void setup() {

    try(Connection connection = __dataSource.getConnection()) {

      final String sql 
          = "INSERT INTO XFACTORS (id,name,description,xfTrigger,xfAttacks,xfOffense,xfDefense,xfPd,xfRemove) "
          + "VALUES (?,?,?,?,?,?,?,?,?)";
      DbUtils.writeQuery(sql, new Object[] { "ABC120","First","blah","VALUE(TRUE)","VALUE(1)","VALUE(2)","VALUE(3)","VALUE(4)","VALUE(5)" } , connection);
      DbUtils.writeQuery(sql, new Object[] { "ABC121","Second","","VALUE(TRUE)","","","","","" } , connection);
      DbUtils.writeQuery(sql, new Object[] { "ABC122","Third","","VALUE(TRUE)","","","","","" } , connection);
      DbUtils.writeQuery(sql, new Object[] { "ABC123","Fourth","","VALUE(TRUE)","VALUE(4)","","","","" } , connection);

    } catch(Exception e) {
      throw new RuntimeException("setup failure!", e);
    }

    IKeyGen keyGen = new KeyGenImpl();
    _mockUnitService = new MockUnitService();
    _xFactorService = new XFactorServiceImpl(__dataSource, keyGen , _mockUnitService);
    _listener = new ServiceListenerChecker();
    _xFactorService.addListener(_listener);
  }
  
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
    
    IKeyGen keyGen = new KeyGenImpl();
    new XFactorServiceImpl(__dataSource, keyGen , _mockUnitService);
    
    try{
      new XFactorServiceImpl(null, keyGen , _mockUnitService);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try{
      new XFactorServiceImpl(__dataSource, null , _mockUnitService);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try{
      new XFactorServiceImpl(__dataSource, keyGen , null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testSaveXFactor() {   
    assertRecordCount(4);
    IXFactor xf = new XFactorImpl(null, "test", "blah", "VALUE(TRUE)", "", "", "", "", "");
    XFactorId id = _xFactorService.saveXFactor(xf);
    assertRecordCount(5);
    assertNotNull(id);    
    assertEquals(1, _listener.getDataChangedCount());
    _listener.reset();
    
    xf = new XFactorImpl(id, "update", "blah", "VALUE(TRUE)", "", "", "", "", "");
    XFactorId id2 = _xFactorService.saveXFactor(xf);
    assertRecordCount(5);
    assertEquals(id, id2);
    assertEquals(1, _listener.getDataChangedCount());
    
    //Try and load it back (several times to be sure cache is excercised)
    //(Under the impl at time of writing its always going to come from the cache anyway)
    for(int t=0; t<N; t++) {
      IXFactor reloaded = _xFactorService.getXFactor(id);
      assertNotNull(reloaded);
      assertEquals(xf.getName(), reloaded.getName());
    }
    
    try {
      _xFactorService.saveXFactor(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    
    //As of 0.6.3 we explicitly enforce unique names
    String[] names = new String[] { "First", "first"," first", "FiRsT", "first     " };
    for(String name : names) {
      IXFactor dupe = new XFactorImpl(null, name, "desc", "", "", "", "", "", "");
      try {
        _xFactorService.saveXFactor(dupe);
        fail("Expected DuplicateRecordException for " + name);
      } catch(DuplicateRecordException expected) {
      }

    }
  }
  
  @Test
  public void testGetXFactors() {    
    for(int t=0; t<N; t++) {
      List<IXFactor> records = _xFactorService.getXFactors();
      assertEquals(4, records.size());
      assertEquals("Fourth", records.get(1).getName() ); //alpha order
      
      try{ //The collection returned should not be modifiable
        records.remove(0); 
        fail("Expected UnsupportedOperationException");
      } catch(UnsupportedOperationException expected) {}
    }    
  }
  
  @Test
  public void testLoadXFactor() {
    for(int t=0; t<N; t++) {
      //id,name,description,xfTrigger,xfAttacks,xfOffense,xfDefense,xfPd,xfRemove
      //"ABC120","First","blah","VALUE(TRUE)","VALUE(1)","VALUE(2)","VALUE(3)","VALUE(4)","VALUE(5)"
      IXFactor xf = _xFactorService.getXFactor( new XFactorId("ABC120") );
      assertNotNull(xf);
      //need to verify all fields got read
      assertEquals("ABC120", xf.getId().toString());
      assertEquals("First", xf.getName() );
      assertEquals("blah", xf.getDescription());
      assertEquals("VALUE(TRUE)", xf.getTrigger());
      assertEquals("VALUE(1)", xf.getXfAttacks());
      assertEquals("VALUE(2)", xf.getXfOffense());
      assertEquals("VALUE(3)",xf.getXfDefense());
      assertEquals("VALUE(4)",xf.getXfPd());
      assertEquals("VALUE(5)",xf.getXfRemove());      
    }  
    
    assertNull( _xFactorService.getXFactor(new XFactorId("nosuchid")) );
    
    try {
      IXFactor xf = _xFactorService.getXFactor(null);
      fail("Expected NullPointerException but found " + xf);
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testLoadXFactorByName() {
    for(int t=0; t<N; t++) {
      IXFactor xf = _xFactorService.getXFactorByName("First");
      assertNotNull(xf);
      //need to verify all fields got read
      assertEquals("ABC120", xf.getId().toString());
      assertEquals("First", xf.getName() );
      assertEquals("blah", xf.getDescription());
      assertEquals("VALUE(TRUE)", xf.getTrigger());
      assertEquals("VALUE(1)", xf.getXfAttacks());
      assertEquals("VALUE(2)", xf.getXfOffense());
      assertEquals("VALUE(3)",xf.getXfDefense());
      assertEquals("VALUE(4)",xf.getXfPd());
      assertEquals("VALUE(5)",xf.getXfRemove());      
    }  
    
    assertNull( _xFactorService.getXFactorByName("no such xf") );
    
    try {
      IXFactor xf = _xFactorService.getXFactorByName(null);
      fail("Expected NullPointerException but found " + xf);
    } catch(NullPointerException expected) {}
    
    try {
      _xFactorService.getXFactorByName("");
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testDeleteXFactor() {
    
    assertRecordCount(4);
    _xFactorService.deleteXFactor( new XFactorId("ABC120") );
    assertRecordCount(3);
    _xFactorService.deleteXFactor( new XFactorId("ABC123") );
    assertRecordCount(2);
    //deleting already deleted or non-existant one is a nop
    _xFactorService.deleteXFactor( new XFactorId("ABC123") );
    _xFactorService.deleteXFactor( new XFactorId("nosuchxf") );
    assertRecordCount(2);
    
    try (Connection connection = __dataSource.getConnection()) {
      
      long c = (Long)DbUtils.readSingleValue("SELECT COUNT(*) FROM xfactors WHERE id IN ('ABC121','ABC122')", null, connection);
      assertEquals(2, c);
      
    } catch(SQLException e){
      throw new RuntimeException(e);
    }
    
    //It will try to unlink units with the xFactor using the provided unit service
    //its allowed but not required to check those xfactors exist first
    assertTrue( _mockUnitService.getUnlinkCallCount() >= 2 );
    List<XFactorId> unlinked = _mockUnitService.getUnlinkCalls();
    assertTrue( unlinked.contains(new XFactorId("ABC120")) );
    assertTrue( unlinked.contains(new XFactorId("ABC123")) );
    
  }
  
  
  //////////
  
  /**
   * Asserts that the number of scenario records in the database is as specified
   * @param expectedCount number of scenarios
   */
  private void assertRecordCount(int expectedCount) {
    try (Connection connection = __dataSource.getConnection() ) {
      long count = (Long)DbUtils.readSingleValue("SELECT COUNT(*) FROM xfactors", null, connection);
      assertEquals( "expected " + expectedCount + " records but found " + count, expectedCount, count);      
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
}



















