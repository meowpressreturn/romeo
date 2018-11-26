package romeo.units.impl;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import romeo.persistence.DuplicateRecordException;
import romeo.test.ServiceListenerChecker;
import romeo.test.TestUtils;
import romeo.units.api.Acronym;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.units.api.UnitId;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;
import romeo.utils.KeyGenImpl;
import romeo.xfactors.api.XFactorId;

public class TestUnitServiceImpl {

  private static final int N = 3;
  private static final double D = 0.0001;
  private static final UnitId VIP_ID = new UnitId("ABC120");
  private static final UnitId BS_ID = new UnitId("ABC121");
  
  
  private static DataSource __dataSource;
  private static UnitServiceInitialiser __initialiser;
  private static IKeyGen __keyGen;
  
  @BeforeClass
  public static void beforeClass() {
    __dataSource = TestUtils.inMemoryDatabase();
    TestUtils.initDatabaseSettings(__dataSource);
    
    __keyGen = new KeyGenImpl();
    
    __initialiser = new UnitServiceInitialiser();
    TestUtils.applyServiceInitialiser(__dataSource, __initialiser);
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
  
  public static void writeUnit(Connection connection, UnitId id, IUnit unit) {
    DbUtils.writeQuery("INSERT INTO units (id, name, attacks, offense, defense, pd, speed, carry, cost,"
        +" complexity, scanner, license, acronym, xFactor) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
        , new Object[] {
            id,
            unit.getName(), 
            unit.getAttacks(),
            unit.getOffense(),
            unit.getDefense(), 
            unit.getPd(), 
            unit.getSpeed(), 
            unit.getCarry(), 
            unit.getCost(), 
            unit.getComplexity(), 
            unit.getScanner(), 
            unit.getLicense(),
            unit.getAcronym().toString(),
            unit.getXFactor()==null ? "" : unit.getXFactor().toString(),
            } , connection);
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  private IUnitService _unitService;
  private ServiceListenerChecker _listener;
  
  @Before
  public void setup() {
    _unitService = new UnitServiceImpl(__dataSource, __keyGen);
    _listener = new ServiceListenerChecker();
    _unitService.addListener(_listener);
    
    try (Connection connection = __dataSource.getConnection()) {      
      IUnit vip = TestUnitImpl.newVip();
      writeUnit(connection, VIP_ID, vip);
      
      IUnit bs = TestUnitImpl.newBStar();
      writeUnit(connection, BS_ID, bs);
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  @After
  public void tearDown() {
    try (Connection connection = __dataSource.getConnection()) { 
      __initialiser.reset(connection);
    } catch (Exception e) {
      throw new RuntimeException("tearDown failure!",e);
    }
  }
  
  ///////////////////////////////
  
  @Test
  public void testConstructor() {
    
    new UnitServiceImpl(__dataSource, __keyGen);
    
    try {
      new UnitServiceImpl(null, __keyGen);
      fail("Expected NullPointerExpception");
    } catch(NullPointerException expected) {}
    
    try {
      new UnitServiceImpl(__dataSource, null);
      fail("Expected NullPointerExpception");
    } catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testLoadUnit() {
    
    for(int t=0; t<N; t++) {

      IUnit vip = _unitService.getUnit(new UnitId("ABC120"));
      TestUnitImpl.assertVipCorrect(VIP_ID, vip);      
      IUnit bs = _unitService.getUnit(new UnitId("ABC121"));
      TestUnitImpl.assertBsCorrect(BS_ID, bs);      
      assertNull( _unitService.getUnit( new UnitId("nosuchunit")) );
    }
    
    try {
      _unitService.getUnit(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testGetByAcronym() {
    
    for(int t=0; t<N; t++) {

      IUnit vip = _unitService.getByAcronym(Acronym.fromString("VIP"));
      TestUnitImpl.assertVipCorrect(VIP_ID, vip);      
      IUnit bs = _unitService.getByAcronym(Acronym.fromString("BS"));
      TestUnitImpl.assertBsCorrect(BS_ID, bs);
      assertNull( _unitService.getByAcronym(Acronym.fromString("NSU")) );
    }
    
    try {
      _unitService.getByAcronym(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {} 
  }
  
  @Test
  public void testGetByName() {    
    for(int t=0; t<N; t++) {
      IUnit vip = _unitService.getByName("Fighter");
      TestUnitImpl.assertVipCorrect(VIP_ID,vip);
      IUnit bs = _unitService.getByName("Carrier");
      TestUnitImpl.assertBsCorrect(BS_ID, bs);      
      assertNull( _unitService.getByName("No Such Unit") );
    }
    
    try {
      _unitService.getByName(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testGetUnits() {
    for(int t=0; t<N; t++) {
      List<IUnit> units = _unitService.getUnits();
      assertEquals(2, units.size());
      IUnit bs = units.get(0);
      TestUnitImpl.assertBsCorrect(BS_ID,bs);
      IUnit vip = units.get(1);
      TestUnitImpl.assertVipCorrect(VIP_ID,vip);
      try {
        units.remove(0);
        fail("Excepted UnsupportedOperationException");
      } catch(UnsupportedOperationException expected) {}
    }       
  }
  
  @Test
  public void testSaveUnit1() {
    _listener.reset();
    assertRecordCount(2);
    IUnit rap = TestUnitImpl.newRap(null);
    UnitId id = _unitService.saveUnit(rap);
    assertEquals(1, _listener.getDataChangedCount());
    assertNotNull(id);
    assertRecordCount(3);  
    
    IUnit loadRap = _unitService.getUnit(id);
    TestUnitImpl.assertRapCorrect(id, loadRap);
    _listener.reset();
    try {
      _unitService.saveUnit(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    assertEquals(0, _listener.getDataChangedCount());
  }
  
  @Test
  public void testSaveUnit2() {
    for(int t=0; t<N; t++) {
      _listener.reset();
      //Test overwriting the BS details with those of a RAP to ensure update also
      //sets columns correctly 
      _unitService.saveUnit( new UnitImpl(BS_ID, TestUnitImpl.newRap(null) ) );
      assertEquals(1, _listener.getDataChangedCount());
      IUnit bsRap = _unitService.getUnit(BS_ID);
      TestUnitImpl.assertRapCorrect(BS_ID, bsRap);
    }
  }
  
  @Test
  public void testSaveUnits() {
    int unitNumber = 0;
    for(int t=0; t<N; t++) {
      _listener.reset();
      List<IUnit> units = new ArrayList<>();
      for(int i=0;i<5;i++) {
        unitNumber++;
        Map<String,Object> u = UnitImpl.asMap(TestUnitImpl.newRap(null));
        u.put("name","testRap"+unitNumber);
        u.put("acronym", Acronym.fromString("TR"+unitNumber) );
        IUnit unit = UnitImpl.createFromMap(u);         
        units.add(unit);
      }
      assertRecordCount( (t*5) + 2 );
      List<UnitId> ids = _unitService.saveUnits(units);
      assertEquals(5, ids.size());
      assertRecordCount( (t*5) + 2 + 5 );
      assertEquals(1, _listener.getDataChangedCount());
    }
    _listener.reset();
    List<UnitId> ids = _unitService.saveUnits(new ArrayList<IUnit>());
    assertEquals(0, _listener.getDataChangedCount()); //no entries shouldnt trigger notification
    assertEquals(0, ids.size() );
    
    try {
      _unitService.saveUnits(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testSaveUnits_duplicates() {
    
    { //check that it spots a dupe name
      List<IUnit> list = new ArrayList<>();
      list.add( new UnitImpl(null, "A", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("A"), null) );
      list.add( new UnitImpl(null, "B", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("B"), null) );
      list.add( new UnitImpl(null, "C", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("C"), null) );
      list.add( new UnitImpl(null, "D", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("D"), null) );
      list.add( new UnitImpl(null, "A", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("E"), null) ); //dupe name
      
      try {
        _unitService.saveUnits(list);
        fail();
      } catch(DuplicateRecordException expected) {}
      
      //It  should also spot one that already exists in the db
      list.clear();
      list.add( new UnitImpl(null, "fighter", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("A"), null) );
      
      try {
        _unitService.saveUnits(list);
        fail();
      } catch(DuplicateRecordException expected) {}
    }
    
    { //check that it spots a dupe acronym
      List<IUnit> list = new ArrayList<>();
      list.add( new UnitImpl(null, "A", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("A"), null) );
      list.add( new UnitImpl(null, "B", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("B"), null) );
      list.add( new UnitImpl(null, "C", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("C"), null) );
      list.add( new UnitImpl(null, "D", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("A"), null) ); //duped acronym
      
      try {
        _unitService.saveUnits(list);
        fail();
      } catch(DuplicateRecordException expected) {}
      
      //It  should also spot one that already exists in the db (regardless of case)
      list.clear();
      list.add( new UnitImpl(null, "A", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, Acronym.fromString("ViP"), null) );
      
      try {
        _unitService.saveUnits(list);
        fail();
      } catch(DuplicateRecordException expected) {}
    }
  }
  
  @Test
  public void testDelete() {
    assertRecordCount(2);
    assertNotNull( _unitService.getUnit(VIP_ID) );
    _unitService.deleteUnit(VIP_ID);
    assertRecordCount(1);
    assertEquals(1, _listener.getDataChangedCount() );
    assertNull( _unitService.getUnit(VIP_ID) );
    
    _listener.reset();
    _unitService.deleteUnit(new UnitId("nosuchid"));
    assertEquals(1, _listener.getDataChangedCount() );  //always notifies since it doesnt know if it existed!
    
    try {
      _unitService.deleteUnit(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testGetSpeeds() {    
    for(int t=0; t<N; t++) {
      assertArrayEquals( new int[] {0,80,120}, _unitService.getSpeeds() );
    }
    deleteAllUnits();    
    //Explicitly use a clean service for this, created after the table was wiped
    UnitServiceImpl cleanUnitService = new UnitServiceImpl(__dataSource, __keyGen);
    for(int t=0;t<N;t++) {
      assertArrayEquals(new int[] {0}, cleanUnitService.getSpeeds());
    }
  }
  
  @Test
  public void testGetRanges() {
    for(int t=0; t<N; t++) {
      assertArrayEquals( new double[] {1d,20d}, _unitService.getRange("attacks"), D );
      assertArrayEquals( new double[] {20d,90d}, _unitService.getRange("offense"), D );
      assertArrayEquals( new double[] {25d,98d}, _unitService.getRange("defense"), D );
      assertArrayEquals( new double[] {2d,10d}, _unitService.getRange("pd"), D );
      assertArrayEquals( new double[] {80d,120d}, _unitService.getRange("speed"), D );
      assertArrayEquals( new double[] {1d,500d}, _unitService.getRange("carry"), D );
      assertArrayEquals( new double[] {100d,2000d}, _unitService.getRange("cost"), D );
      assertArrayEquals( new double[] {30d,1800d}, _unitService.getRange("complexity"), D );
      assertArrayEquals( new double[] {25d,100d}, _unitService.getRange("scanner"), D );
      assertArrayEquals( new double[] {200,2000}, _unitService.getRange("license"), D );      
      
      //TODO "multipliedOffense" and "logisticsFactor"
    }
    
    try {
      _unitService.getRange(null);
      fail("expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      _unitService.getRange("");
      fail("expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      _unitService.getRange("noSuchProperty");
      fail("expected exception");
    } catch(Exception expected) {}
  }
  
  @Test
  public void testGetScanners() {    
    //Add something without a scanner to be sure
    IUnit foo = new UnitImpl(null, "Foo", 0, 0, 0, 0, 50, 100, 25, 14, 0, 50, Acronym.fromString("FOO"), null);
    _unitService.saveUnit(foo);    
    for(int t=0; t<N; t++) {      
      List<IUnit> scanners = _unitService.getScanners();
      assertEquals(2, scanners.size() );
      assertEquals( Acronym.fromString("VIP"), scanners.get(0).getAcronym() );
      assertEquals( Acronym.fromString("BS"), scanners.get(1).getAcronym() );
    }    
    //Test if no units it returns an empty list and not null
    deleteAllUnits();
    _unitService = new UnitServiceImpl(__dataSource, __keyGen);
    for(int t=0; t<N; t++) {      
      List<IUnit> scanners = _unitService.getScanners();
      assertNotNull(scanners);
      assertEquals(0, scanners.size() );
    }        
  }
  
  @Test
  public void testUnlinkXFactors() {    
    assertNotNull( _unitService.getByAcronym(Acronym.fromString("VIP")).getXFactor() );
    _unitService.unlinkUnitsWithXFactor(new XFactorId("XF1"));
    assertEquals(1, _listener.getDataChangedCount());
    assertNull( _unitService.getByAcronym(Acronym.fromString("VIP")).getXFactor() ); //VIP xf should be cleared
    
    //It should be safe to call for xFactors that dont exist
    _unitService.unlinkUnitsWithXFactor(new XFactorId("nosuchxf"));
    
    try {
      _unitService.unlinkUnitsWithXFactor(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}    
  }
  
  @Test
  public void notifiesOnEdc() {
    _unitService.dataChangedExternally();
    assertEquals(1, _listener.getDataChangedCount());
  }
  
  @Test
  public void testSaveUnit_duplicates() {
    //duplicate name or acronym should throw a DRE
    
    
    
    
    {
    //db already has a VIP
      Map<String,Object> vipMap = UnitImpl.asMap( TestUnitImpl.newVip() );
      int i=0;
      String[] badNames = new String[] { "fighter","FIGHTER","Fighter","fIgHtEr", " fighter    ", "Fighter " };
      for(String name : badNames) {
        vipMap.put("name", name);
        vipMap.put("acronym",Acronym.fromString("ACRONYM"+i++));
        IUnit unit = UnitImpl.createFromMap(vipMap);
        try {
          _unitService.saveUnit(unit);
          fail("Expected DuplicateRecordException for \"" + name + "\"");
        } catch(DuplicateRecordException expected) {}
      }
    }
    
    {
      //db already has a VIP
      Map<String,Object> vipMap = UnitImpl.asMap( TestUnitImpl.newVip() );
      //A unique name & acronym should work fine though
      vipMap.put("name", "Fighter2");
      vipMap.put("acronym", Acronym.fromString("VIP2"));
      IUnit unit = UnitImpl.createFromMap(vipMap);
      _unitService.saveUnit(unit);
    }
    
    //Acronym is also checked, and again it needs to be trimmed
    {
    //db already has a VIP
      Map<String,Object> vipMap = UnitImpl.asMap( TestUnitImpl.newVip() );
      int i=0;
      String[] badAcronyms = new String[] { "VIP"," vip","Vip   ","vIp", " vip    ", "VIP " };
      for(String acronym : badAcronyms) {
        vipMap.put("name", "Fighter"+i++);
        vipMap.put("acronym",Acronym.fromString(acronym));
        IUnit unit = UnitImpl.createFromMap(vipMap);
        try {
          _unitService.saveUnit(unit);
          fail("Expected DuplicateRecordException for \"" + acronym + "\"");
        } catch(DuplicateRecordException expected) {}
      }
    }
  }
  
  //////////////////////
  
  /**
   * Asserts that the number of scenario records in the database is as specified
   * @param expectedCount number of scenarios
   */
  private void assertRecordCount(int expectedCount) {
    TestUtils.assertRowCount(__dataSource, "units", expectedCount);
  }
  
  private void deleteAllUnits() {
    try (Connection connection = __dataSource.getConnection()) {
      DbUtils.writeQuery("DELETE FROM units", null, connection);
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
}



















