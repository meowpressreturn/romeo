package romeo.worlds.impl;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import romeo.model.api.InvalidTurnException;
import romeo.model.api.MapInfo;
import romeo.persistence.DuplicateRecordException;
import romeo.players.impl.MockPlayerService;
import romeo.players.impl.PlayerServiceInitialiser;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.MockSettingsService;
import romeo.test.ServiceListenerChecker;
import romeo.test.TestUtils;
import romeo.units.api.IUnit;
import romeo.units.api.UnitId;
import romeo.units.impl.MockUnitService;
import romeo.units.impl.TestUnitImpl;
import romeo.units.impl.TestUnitServiceImpl;
import romeo.units.impl.UnitServiceInitialiser;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;
import romeo.utils.KeyGenImpl;
import romeo.worlds.api.HistorySummary;
import romeo.worlds.api.IHistory;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.WorldId;

public class TestWorldServiceImpl {
  
  private static final int N = 3; //repeat the same test a bit to see if it exposes caching issues
  private static final double D = 0.00001;
  
  private static DataSource __dataSource;
  private static WorldServiceInitialiser __initialiser;
  private static IKeyGen __keyGen;
  private static ISettingsService __settingsService;
  
  @BeforeClass
  public static void beforeClass() {
    
    __settingsService = new MockSettingsService();
    __settingsService.setLong(ISettings.DEFAULT_SCANNER, 25);
    
    __dataSource = TestUtils.inMemoryDatabase();
    __keyGen = new KeyGenImpl();
    TestUtils.initDatabaseSettings(__dataSource);
    
    UnitServiceInitialiser unitInitialiser = new UnitServiceInitialiser();
    TestUtils.applyServiceInitialiser(__dataSource, unitInitialiser);
    
    try (Connection connection = __dataSource.getConnection()) {
      IUnit rap = TestUnitImpl.newRap(new UnitId("RAP"));
      TestUnitServiceImpl.writeUnit(connection, rap.getId(), rap);
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
    
    PlayerServiceInitialiser playerInitialiser = new PlayerServiceInitialiser(__keyGen);
    TestUtils.applyServiceInitialiser(__dataSource, playerInitialiser);
    
    __initialiser = new WorldServiceInitialiser(__keyGen, __settingsService);
    TestUtils.applyServiceInitialiser(__dataSource, __initialiser);
  }
  
  @AfterClass
  public static void afterClass() {
    TestUtils.shutdownDatabase(__dataSource);
    __dataSource = null;
  }

  public static void initTestHistory(Connection connection) {
  //we don't have a player record for Nobody in our test data, so can use that to see that its
    //ok to have history against a world whose owner (name) isnt in the db
    
    //Create some worlds to summarise
    //Worlds are Terra, Luna, Mercury, Ares, Jupiter, Saturn, Titan, Venus (8)
    //ids based on name
    
    //Earth's initial cluster:
    DbUtils.writeQuery("INSERT INTO worlds (id,name,worldEi,worldRer, worldX,WorldY) VALUES (?,?,?,?,?,?)", 
        new Object[] {"idTerra","Terra",50,200, 123,42 }, connection);
    DbUtils.writeQuery("INSERT INTO worlds (id,name,worldEi,worldRer, worldX,worldY) VALUES (?,?,?,?,?,?)", 
        new Object[] {"idLuna","Luna",10,100, 267,48 }, connection);
    DbUtils.writeQuery("INSERT INTO worlds (id,name,worldEi,worldRer, worldX,worldY) VALUES (?,?,?,?,?,?)", 
        new Object[] {"idMercury","Mercury",5,150, 52, 103 }, connection);
      //turn 1
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idTerra",1,"Earth",100.00,1000,500}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idLuna",1,"Nobody",0.00,10,100}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idMercury",1,"Nobody",10.43,10,10}, connection);
      //turn 2
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idTerra",2,"Earth",100.00,1000,500}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idLuna",2,"Venus",20.00,5,200}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idMercury",2,"Venus",10.43,15,8}, connection);
      //turn 3
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idTerra",3,"Earth",50.00,1000,500}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idLuna",3,"Earth",40.00,5,200}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idMercury",3,"Earth",30.00,10,10}, connection);
    
    //Mars's initial cluster
    DbUtils.writeQuery("INSERT INTO worlds (id,name,worldEi,worldRer, worldX,worldY) VALUES (?,?,?,?,?,?)", 
        new Object[] {"idAres","Ares",25,250, -67, 185 }, connection);
    DbUtils.writeQuery("INSERT INTO worlds (id,name,worldEi,worldRer, worldX,worldY) VALUES (?,?,?,?,?,?)", 
        new Object[] {"idJupiter","Jupiter",35,400, -100, 280 }, connection);
    DbUtils.writeQuery("INSERT INTO worlds (id,name,worldEi,worldRer, worldX,worldY) VALUES (?,?,?,?,?,?)", 
        new Object[] {"idSaturn","Saturn",30,350, -200, 85 }, connection);
    DbUtils.writeQuery("INSERT INTO worlds (id,name,worldEi,worldRer, worldX,worldY) VALUES (?,?,?,?,?,?)", 
        new Object[] {"idTitan","Titan",25,100, -205,88 }, connection);
      //turn 1
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idAres",1,"Mars",200.00,800,400}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idJupiter",1,"Mars",50.00,0,100}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idSaturn",1,"Nobody",0.00,0,0}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idTitan",1,"Nobody",35.50,0,0}, connection);
      //turn 2
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idAres",2,"Mars",50.00,800,400}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idJupiter",2,"Mars",10.00,10,100}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idSaturn",2,"Mars",50.00,5,25}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idTitan",2,"Mars",20.55,10,10}, connection);
      //turn 3
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idAres",3,"Mars",100.00,800,400}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idJupiter",3,"Mars",100.00,0,100}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idSaturn",3,"Mars",100.00,0,0}, connection);
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idTitan",3,"Mars",100.00,0,0}, connection);

    //Venus's initial cluster
    DbUtils.writeQuery("INSERT INTO worlds (id,name,worldEi,worldRer, worldX, worldY, scannerId) VALUES (?,?,?,?,?,?,?)", 
        new Object[] {"idVenus","Venus",5,100, 0,0, "RAP" }, connection);
      //turn 1
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idVenus",1,"Venus",50.14,500,4000}, connection);
      //turn 2
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idVenus",2,"Venus",25.00,500,4000}, connection);
      //turn 3 (venus loses all its worlds)
      DbUtils.writeQuery("INSERT INTO worlds_history (worldId, turn, owner, firepower, labour, capital) VALUES (?,?,?,?,?,?)",
          new Object[] {"idVenus",3,"Earth",150.00,250,500}, connection);
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  WorldServiceImpl _worldService;
  ServiceListenerChecker _listener;
  MockUnitService _mockUnitService;
  MockPlayerService _mockPlayerService;
  MockSettingsService _mockSettingsService;
  
  @Before
  public void setup() {
    _mockUnitService = new MockUnitService();
    _mockPlayerService = new MockPlayerService();
    _mockSettingsService = new MockSettingsService();
    _worldService = new WorldServiceImpl(__dataSource, __keyGen, _mockPlayerService, _mockUnitService, _mockSettingsService);
    _listener = new ServiceListenerChecker();
    _worldService.addListener(_listener);
    
    try (Connection connection = __dataSource.getConnection()) {
      initTestHistory(connection);
      //nb: if the reset() method isnt correct, expect integrity violation here
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
    
    new WorldServiceImpl(__dataSource, __keyGen, _mockPlayerService, _mockUnitService, _mockSettingsService);
    
    try{
      new WorldServiceImpl(null, __keyGen, _mockPlayerService, _mockUnitService, _mockSettingsService);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    try{
      new WorldServiceImpl(__dataSource, null, _mockPlayerService, _mockUnitService, _mockSettingsService);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    try{
      new WorldServiceImpl(__dataSource, __keyGen, null, _mockUnitService, _mockSettingsService);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    try{
      new WorldServiceImpl(__dataSource, __keyGen, _mockPlayerService, null, _mockSettingsService);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    try{
      new WorldServiceImpl(__dataSource, __keyGen, _mockPlayerService, _mockUnitService, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}    
  }
  
  @Test
  public void testGetMapInfo() {
    for(int t=0; t<N; t++) {
      MapInfo info = _worldService.getMapInfo();      
      assertEquals(-205, info.getLeftBorder() );
      assertEquals(0, info.getTopBorder() );
      assertEquals(267, info.getRightBorder() );
      assertEquals(280, info.getBottomBorder() );
      assertEquals(3, info.getMaxTurn() );
      assertEquals(472, info.getWidth() );
      assertEquals(280, info.getHeight());      
    }   
    assertEquals(0, _listener.getDataChangedCount()); //getting info shouldn't cause notification
  }
  
  @Test
  public void deleteWorld() {
    assertRowCount(8);
    _worldService.deleteWorld(new WorldId("idTerra")); 
    assertRowCount(7);
    assertEquals(1, _listener.getDataChangedCount()); 
    
    //Deleting it again, or a non-existent world won't cause an error, but may result in a
    //notification as services arent required to check if anything was actually deleted
    _worldService.deleteWorld(new WorldId("idTerra")); 
    _worldService.deleteWorld(new WorldId("nosuchworld")); 
    assertRowCount(7);
    
    try {
      _worldService.deleteWorld(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testLoadWorld() {
    for(int t=0; t<N; t++) {
      IWorld venus = _worldService.loadWorld( new WorldId("idVenus") );
      assertNotNull( venus );
      assertEquals(new WorldId("idVenus"), venus.getId());
      assertEquals("Venus",venus.getName());
      assertEquals(5, venus.getWorldEi());
      assertEquals(100, venus.getWorldRer());
      assertEquals(new UnitId("RAP"), venus.getScannerId() );
      
      IWorld luna = _worldService.loadWorld( new WorldId("idLuna") );
      assertNotNull( luna );
      //new Object[] {"idLuna","Luna",10,100, 267,48 }, connection);
      assertEquals( 267, luna.getWorldX() );
      assertEquals( 48, luna.getWorldY() );
      assertNull( luna.getScannerId() );
      
      assertNull( _worldService.loadWorld(new WorldId("nosuchworld")) );
    }    
    
    try {
      _worldService.loadWorld( null );
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testLoadWorldByName() {
    for(int t=0; t<N; t++) {
      IWorld venus = _worldService.loadWorldByName("Venus");
      assertNotNull( venus );
      assertEquals(new WorldId("idVenus"), venus.getId());
      assertEquals("Venus",venus.getName());
      assertEquals(5, venus.getWorldEi());
      assertEquals(100, venus.getWorldRer());
      assertEquals(new UnitId("RAP"), venus.getScannerId() );
      
      //Its case-insesitive now
      assertNotNull( _worldService.loadWorldByName("venus") );
      assertNotNull( _worldService.loadWorldByName("venUS") );
      assertNotNull( _worldService.loadWorldByName("VENUS") );
      
      IWorld luna = _worldService.loadWorldByName("Luna");
      assertNotNull( luna );
      //new Object[] {"idLuna","Luna",10,100, 267,48 }, connection);
      assertEquals( 267, luna.getWorldX() );
      assertEquals( 48, luna.getWorldY() );
      assertNull( luna.getScannerId() );
      
      assertNull( _worldService.loadWorldByName("nosuchworld") );
      
      //nb: "" is technically a valid world name so should return null here and not an IAE
      assertNull( _worldService.loadWorldByName("") );
    }    
    
    try {
      _worldService.loadWorldByName( null );
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void getWorlds() {
    for(int t=0; t<N; t++) {
      List<IWorld> worlds = _worldService.getWorlds();
      assertEquals(8, worlds.size());
      assertEquals("Ares", worlds.get(0).getName());
      assertEquals("Venus", worlds.get(7).getName());
      try { //should be immutable list
        worlds.remove(0);
        fail("Excepted UnsupportedOperationException");
      } catch(UnsupportedOperationException expected) {}
    }    
  }
  
  @Test
  public void testHaveData() {
    for(int t=0; t<N; t++) {      
      assertTrue( _worldService.haveData(1) );
      assertTrue( _worldService.haveData(2) );
      assertTrue( _worldService.haveData(3) );
      assertFalse( _worldService.haveData(4) );
      assertFalse( _worldService.haveData(888) );     
    }    
    try {
      _worldService.haveData(0);
      fail("Expected InvalidTurnException");
    } catch(InvalidTurnException expected) {}
    try {
      _worldService.haveData(-5);
      fail("Expected InvalidTurnException");
    } catch(InvalidTurnException expected) {}    
  }
  
  private void assertMercuryTurn3(IHistory turn3) {
    assertEquals( new WorldId("idMercury"), turn3.getWorldId());
    assertEquals( "Earth", turn3.getOwner() );
    assertEquals( 30d, turn3.getFirepower(), D);
    assertEquals( 10, turn3.getLabour() );
    assertEquals( 10, turn3.getCapital() );
  }
  
  private void assertMercuryTurn2(IHistory turn2) {
    assertEquals( new WorldId("idMercury"), turn2.getWorldId() );
    assertEquals( "Venus", turn2.getOwner() );
    assertEquals( 10.43d, turn2.getFirepower(), D);
    assertEquals( 15, turn2.getLabour() );
    assertEquals( 8, turn2.getCapital() );
  }
  
  @Test
  public void testLoadHistory() {
    for(int t=0; t<N;t++) {
      List<IHistory> mercuryHistory = _worldService.loadHistory(new WorldId("idMercury"));
      assertEquals(3+1, mercuryHistory.size()); //nb: element 0 is null to allow index to match turn number
      assertNull( mercuryHistory.get(0) );
      IHistory turn3 = mercuryHistory.get(3);
      assertMercuryTurn3(turn3);
      
      IHistory turn2 = mercuryHistory.get(2);
      assertMercuryTurn2(turn2);
    }  
    
    try {
      _worldService.loadHistory(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testLoadHistoryNonContiguous() {
    //Where there are missing turns in the data, there will be null elements in the returned history
    try(Connection connection = __dataSource.getConnection()) {
      DbUtils.writeQuery("DELETE FROM worlds_history WHERE turn=2", null, connection);      
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
    List<IHistory> mercuryHistory = _worldService.loadHistory(new WorldId("idMercury"));
    assertEquals(3+1, mercuryHistory.size()); //nb: element 0 is null to allow index to match turn number
    assertNull( mercuryHistory.get(2) );    
  }
  
  @Test
  public void testLoadHistoryTurn() {
    //remove turn 1 so we can test non-contiguous with firts turn
    try(Connection connection = __dataSource.getConnection()) {
      DbUtils.writeQuery("DELETE FROM worlds_history WHERE turn=1", null, connection);      
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
    
    for(int t=0; t<N;t++) {
      assertNull( _worldService.loadHistory(new WorldId("idMercury"), 1) );
      assertNull( _worldService.loadHistory(new WorldId("idMars"), 1) );
      IHistory mercury3 = _worldService.loadHistory(new WorldId("idMercury"), 3);
      assertMercuryTurn3(mercury3);
      IHistory mercury2 = _worldService.loadHistory(new WorldId("idMercury"), 2);
      assertMercuryTurn2(mercury2);
      
      //Requesting a turn outside the history gives an ITE
      for(int turn : new int[] {-10,-1,0,4,888} ) {
        try {
          _worldService.loadHistory(new WorldId("idMercury"), turn);
          fail("Expected InvalidTurnException for turn " + turn);
        } catch(InvalidTurnException expected) {}
      }
    }  
    
    try {
      _worldService.loadHistory(null,1);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}    
  }
  
  @Test
  public void testSaveWorld() {
    for(int t=0; t<N; t++) {
      assertRowCount(8+(t*1));
      _listener.reset();
      IWorld yuggoth = new WorldImpl(null, "Yuggoth" + t, 100, 200, new UnitId("RAP"), "beware the shoggoth", 300, 400);
      WorldId id = _worldService.saveWorld(yuggoth);
      assertEquals(1, _listener.getDataChangedCount() );
      IWorld loadYug = _worldService.loadWorld(id);
      assertNotNull(loadYug);
      assertEquals(id, loadYug.getId() );
      assertEquals("Yuggoth" + t, loadYug.getName());
      assertEquals(100, loadYug.getWorldX());
      assertEquals(200, loadYug.getWorldY());
      assertEquals(new UnitId("RAP"), loadYug.getScannerId() );
      assertEquals("beware the shoggoth", loadYug.getNotes());
      assertEquals(300, loadYug.getWorldEi() );
      assertEquals(400, loadYug.getWorldRer() );
      assertRowCount( 8+(t*1)+1 );
      
      IWorld updateYuggoth = new WorldImpl(id, "EditYuggoth"+t, 1000,2000, null, "or dont", 3000, 4000);
      WorldId id2 = _worldService.saveWorld(updateYuggoth);
      loadYug = _worldService.loadWorld(id);
      assertEquals(id, id2);
      assertEquals(id, loadYug.getId() );
      assertEquals("EditYuggoth" + t, loadYug.getName());
      assertEquals(1000, loadYug.getWorldX());
      assertEquals(2000, loadYug.getWorldY());
      assertNull( loadYug.getScannerId() );
      assertEquals("or dont", loadYug.getNotes());
      assertEquals(3000, loadYug.getWorldEi() );
      assertEquals(4000, loadYug.getWorldRer() );
      assertRowCount( 8+(t*1)+1 ); //same count
    }
    
    try {
      _worldService.saveWorld(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
  }
  
  @Test
  public void testSaveWorlds() {
    for(int t=0; t<N; t++) {
      _listener.reset();
      assertRowCount(8+(t*2));
      IWorld a = new WorldImpl(null, "A" + t, 100, 200, new UnitId("RAP"), "beware the shoggoth", 300, 400);
      IWorld b = new WorldImpl(null, "B"+t, 1000,2000, null, "or dont", 3000, 4000);
      
      List<IWorld> worlds = Arrays.asList(a,b);
      List<WorldId> ids = _worldService.saveWorlds(worlds);
      assertEquals(2, ids.size());
      assertNotNull( ids.get(0) );
      assertNotNull( ids.get(1) );
      assertEquals(1, _listener.getDataChangedCount());
      assertRowCount(8+(t*2)+2);
      
      //The ids in the returned list should match the order of objects passed into save
      assertEquals("beware the shoggoth", _worldService.loadWorld( ids.get(0) ).getNotes() );
      assertEquals("or dont", _worldService.loadWorld( ids.get(1) ).getNotes() );
    }
    
    try {
      _worldService.saveWorlds(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}    
  }
  
  @Test
  public void testSaveHistory() {
    
    WorldId idTerra = new WorldId("idTerra");
    IHistory terra4 = new HistoryImpl(idTerra, 4, "Earth", 123.45d, 1000, 2000);
    
    assertEquals(4, _worldService.loadHistory(idTerra).size());
    _worldService.saveHistory(terra4);
    assertEquals(1, _listener.getDataChangedCount());
    assertEquals(5, _worldService.loadHistory(idTerra).size());  
    
    terra4 = _worldService.loadHistory(idTerra,4);
    assertEquals(idTerra, terra4.getWorldId());
    assertEquals(4, terra4.getTurn());
    assertEquals("Earth", terra4.getOwner());
    assertEquals(123.45d, terra4.getFirepower(), D);
    assertEquals(1000, terra4.getLabour() );
    assertEquals(2000, terra4.getCapital() );
    
    try {
      _worldService.saveHistory(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}    
    
  }
  
  @Test
  public void saveHistories() {    
    assertEquals(4, _worldService.loadHistory(new WorldId("idTerra")).size());
    IHistory terra4 = new HistoryImpl(new WorldId("idTerra"), 4, "Earth", 123.45d, 1000, 2000);
    IHistory venus4 = new HistoryImpl(new WorldId("idVenus"), 4, "Earth", 200.0d, 1500, 3000);
    IHistory mars4 = new HistoryImpl(new WorldId("idMars"), 4, "Mars", 345.67d, 2500, 500);
    List<IHistory> histories = Arrays.asList(terra4,venus4,mars4);
    _worldService.saveHistories(histories);
    assertEquals(5, _worldService.loadHistory(new WorldId("idMars")).size());
    
    try {
      _worldService.saveHistories(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}    
  }
  
  @Test
  public void testGetSummary() {
    
    HistorySummary earth3 = _worldService.getSummary("EARTH", 3); //case insensitive
    assertEquals( "Earth", earth3.getOwner() );
    assertEquals( 4, earth3.getWorldCount() );
    assertEquals( 1210, earth3.getTotalCapital() );
    assertEquals( 1265, earth3.getTotalLabour() );
    assertEquals( 270d, earth3.getTotalFirepower(), D );
    
    //Should get 0s and empty strings back for turns lacking data
    HistorySummary earth888 = _worldService.getSummary("Earth", 888);
    assertEquals( "", earth888.getOwner() );
    assertEquals( 0, earth888.getWorldCount() );
    assertEquals( 0, earth888.getTotalCapital() );
    assertEquals( 0, earth888.getTotalLabour() );
    assertEquals( 0d, earth888.getTotalFirepower(), D );
    
    try {
      _worldService.getSummary(null, 2);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}   
    
  }
  
  /**
   * Test that saveWorld(), saveWorlds(), saveWorldAndHistory() throw a DuplicateRecordException when
   * presented a world with a duplicate name
   */
  @Test
  public void testSaveWorld_duplicateNames() {
    //duplicate name should throw a DRE
    
    IHistory history = new HistoryImpl(null, 1, "Nobody", 0, 0, 0);
    String[] badNames = new String[] { "luna","LUNA","Luna","lUNa", "Luna " };
    for(String name : badNames) {
      IWorld world = new WorldImpl(null, name, 0, 0, null, "", 0, 0);
      try {
        _worldService.saveWorld(world);
        fail("Expected DuplicateRecordException for \"" + name + "\" for saveWorld()");
      } catch(DuplicateRecordException expected) {}
      
      try {
        _worldService.saveWorldWithHistory(world, history);
        fail("Expected DuplicateRecordException for \"" + name + "\" for saveWorldAndHistory()");
      } catch(DuplicateRecordException expected) {}
      
      try {
        List<IWorld> worlds = new ArrayList<>();
        worlds.add(world);
        _worldService.saveWorlds(worlds);
        fail("Expected DuplicateRecordException for \"" + name + "\" for saveWorlds()");
      } catch(DuplicateRecordException expected) {}
    }
    
    //A unique name should work fine though
    _worldService.saveWorld( new WorldImpl(null,"Luna2",0,0,null,"",0,0));
  }
  
  /**
   * Tests that saveWorlds() can detect if there are two worlds in the list (but not the db) with the same name
   * and throw the {@link DuplicateRecordException} instead of letting them through to the db.
   */
  @Test
  public void testSaveWorlds_newDuplicates() {
    //Rationale for this extra test is that service is looking at its cache and in this case the cache wouldnt be
    //re-initialised between individual world inserts/updates from the list
    String[][] tests = new String[][] {
      { "foo","bar","foo","baz" },
      { "foo","bar","foo ", "baz" },
      { "foo","bar","baz","FOO" },
      { "foo","FOO","foO","foo " },     
    };
    for(String[] names : tests) {
      List<IWorld> worlds = new ArrayList<>();
      for(String name : names) {
        worlds.add( new WorldImpl(null, name, 0, 0, null, "", 0, 0) );        
      }
      assertRowCount(8);
      try {
        _worldService.saveWorlds(worlds);
        fail();
      } catch(DuplicateRecordException expected) {}
      assertRowCount(8); //should not have saved any in the list
    }
    
    //TODO - need to test that if the list has one thats in the db, none in the list get saved
    
    //Also need to test that it doesnt pick up updates to an existing world as a duplicate
    
  }


  //////////////////////////////////////

  private void assertRowCount(int expectedCount) {
    TestUtils.assertRowCount(__dataSource, "WORLDS", expectedCount);
  }


}
















