package romeo.players.impl;

import static org.junit.Assert.*;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import romeo.model.api.InvalidTurnException;
import romeo.persistence.DuplicateRecordException;
import romeo.players.api.IPlayer;
import romeo.players.api.PlayerId;
import romeo.settings.api.ISettings;
import romeo.settings.impl.MockSettingsService;
import romeo.test.ServiceListenerChecker;
import romeo.test.TestUtils;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;
import romeo.utils.KeyGenImpl;
import romeo.worlds.impl.TestWorldServiceImpl;
import romeo.worlds.impl.WorldServiceInitialiser;

public class TestPlayerServiceImpl {
  
  private static final int N = 3; //repeat the same test a bit to see if it exposes caching issues
  private static final double D = 0.00001;
  
  private static DataSource __dataSource;
  private static PlayerServiceInitialiser __initialiser;
  private static IKeyGen __keyGen;
  
  public static void initPlayerData(Connection connection) {
    Objects.requireNonNull(connection, "connection may not be null");
    //COLOR, ID, NAME, NOTES, STATUS
    DbUtils.writeQuery("INSERT INTO players (id, name, status, notes, color, team) VALUES (?,?,?,?,?,?)"
        , new Object[]{"ABC120","Earth","blue","earth notes","0,0,255","Evil"}, connection);
    
    DbUtils.writeQuery("INSERT INTO players (id, name, status, notes, color, team) VALUES (?,?,?,?,?,?)"
        , new Object[]{"ABC121","Mars","red","aries","255,0,0","Evil"}, connection);
    
    DbUtils.writeQuery("INSERT INTO players (id, name, status, notes, color, team) VALUES (?,?,?,?,?,?)"
        , new Object[]{"ABC122","Venus","green","swampy","0,255,0","Good"}, connection);
  }
  
  @BeforeClass
  public static void beforeClass() {
    __dataSource = TestUtils.inMemoryDatabase();
    __keyGen = new KeyGenImpl();
    TestUtils.initDatabaseSettings(__dataSource);
    
    __initialiser = new PlayerServiceInitialiser(__keyGen);
    TestUtils.applyServiceInitialiser(__dataSource, __initialiser);
  }
  
  @AfterClass
  public static void afterClass() {
    TestUtils.shutdownDatabase(__dataSource);
    __dataSource = null;
  }
  
  ////////////////////////////////////////////////////////////////////////////
  
  PlayerServiceImpl _playerService;
  ServiceListenerChecker _listener;
  MockSettingsService _settingsService;
  
  @Before
  public void setup() {
    _settingsService = new MockSettingsService();
    _settingsService.setLong(ISettings.DEFAULT_SCANNER, 25);
    _playerService = new PlayerServiceImpl(__dataSource, __keyGen);
    _listener = new ServiceListenerChecker();
    _playerService.addListener(_listener);
    try (Connection connection = __dataSource.getConnection()) {
      initPlayerData(connection);
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
    
    new PlayerServiceImpl(__dataSource, __keyGen);
    
    try{
      new PlayerServiceImpl(null, __keyGen);
      fail("expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try{
      new PlayerServiceImpl(__dataSource, null);
      fail("expected NullPointerException");
    } catch(NullPointerException expected) {}    
  }
  
  @Test
  public void testLoadPlayer() {    
    for(int t=0; t<N; t++) {
      assertEarthCorrect( _playerService.loadPlayer(new PlayerId("ABC120")) );
      assertMarsCorrect( _playerService.loadPlayer(new PlayerId("ABC121")) );
      assertVenusCorrect( _playerService.loadPlayer(new PlayerId("ABC122")) );
      assertNull( _playerService.loadPlayer(new PlayerId("nosuchplayer")) );
    }
    
    try {
      _playerService.loadPlayer(null);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    assertEquals(0, _listener.getDataChangedCount()); //loading shouldnt cause any notifications
  }
  
  @Test
  public void testLoadPlayerByName() {    
    for(int t=0; t<N; t++) {
      assertEarthCorrect( _playerService.loadPlayerByName("Earth") );
      assertMarsCorrect( _playerService.loadPlayerByName("MaRs") );
      assertVenusCorrect( _playerService.loadPlayerByName("VENUS") ); //it is now case-insensitive (0.6.3)
      assertNull( _playerService.loadPlayerByName("nosuchplayer") );
    }
    
    try {
      _playerService.loadPlayerByName(null);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
    
    assertEquals(0, _listener.getDataChangedCount()); //loading shouldnt cause any notifications
  }
  
  @Test
  public void testGetPlayers() {
    
    for(int t=0; t<N; t++) {
      List<IPlayer> players = _playerService.getPlayers();
      assertNotNull( players );
      assertEquals(3, players.size());
      
      //Should be in alpha order
      assertEarthCorrect( players.get(0) );
      assertMarsCorrect( players.get(1) );
      assertVenusCorrect( players.get(2) );
      
      //The list returned is immutable
      try {
        players.remove(0);
        fail("Expected UnsupportedOperationException");
      } catch(UnsupportedOperationException expected) {}
    }  
    
    assertEquals(0, _listener.getDataChangedCount()); //loading shouldnt cause any notifications
    
    //Test getting when the table is empty (should return empty list)
    deleteAllPlayers();
    PlayerServiceImpl cleanService = new PlayerServiceImpl(__dataSource, __keyGen);
    for(int t = 0; t < N; t++) {
      List<IPlayer> players = cleanService.getPlayers();
      assertNotNull(players);
      assertEquals(0, players.size());
    }
  }
  
  @Test
  public void testSavePlayer() {
    int plutoNumber = 0; //need to keep the names unique
    for(int t=0; t<N; t++) {
      plutoNumber++;
      assertRecordCount(3 + t);
      _listener.reset();
      IPlayer pluto = new PlayerImpl(null, "Pluto"+plutoNumber, "not a planet", "cold", new Color(255, 255,255), "Good");
      PlayerId id = _playerService.savePlayer(pluto);
      assertEquals(1, _listener.getDataChangedCount());
      assertNotNull(id);
      assertRecordCount(3 + t + 1);
      
      IPlayer loadPluto = _playerService.loadPlayer(id);
      assertNotNull(loadPluto);
      assertEquals(id, loadPluto.getId());
      assertEquals("Pluto"+plutoNumber, loadPluto.getName());
      assertEquals("not a planet", loadPluto.getStatus());
      assertEquals("cold", loadPluto.getNotes());
      assertEquals(new Color(255,255,255), loadPluto.getColor());
      assertEquals("Good", loadPluto.getTeam() );
      
      
      //Test that we cant save a duplicate name
      String[] names = new String[] { "Earth","earth","eARTH","  earth   " };
      for(String name : names) {
        IPlayer earth2 = new PlayerImpl(null, name, "", "", Color.RED, "");
        try{
          _playerService.savePlayer(earth2);
          fail("expected a DuplicateRecordException for \"" + name + "\"");
        }catch(DuplicateRecordException expected) {}       
      }
      
    }
    
    try {
      _playerService.savePlayer(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testSavePlayers() {
    assertRecordCount(3);
    int playerNumber = 0;
    for(int t=0; t<N; t++) {
      
      _listener.reset();      
      List<IPlayer> players = new ArrayList<>();
      for(int i=0; i<5; i++) {
        playerNumber++;
        String name = "Player" + playerNumber;
        players.add( new PlayerImpl(null, name, "testing multisave", "notes field", new Color(255, 255,255), "Home") );
      }
      assertRecordCount(3 + (t*5));
      _playerService.savePlayers(players);
      assertRecordCount(3 + (t*5)+5);
      assertEquals(1, _listener.getDataChangedCount()); //should be one notification for the whole batch
    }
    
    try{
      _playerService.savePlayers(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    _listener.reset();
    assertRecordCount(3 + ((N-1)*5)+5);
    _playerService.savePlayers( new ArrayList<IPlayer>() );
    assertEquals(0, _listener.getDataChangedCount()); //No notification if the list was empty 
    assertRecordCount(3 + ((N-1)*5)+5);
  }
  
  @Test
  public void testSavePlayers_duplicates() {
      
      IPlayer mars = _playerService.loadPlayerByName("mars");
      assertNotNull(mars); //sanity check
    
      String[][] tests = new String[][] {
        { "foo","bar","baz","baz" },
        { "foo","bar","baz","FOO" },
        { "foo"," foo","bar" },
        { "foo","bar","baz","bar     " },
        { "foo","bar","baz","eaRth " },
        { "foo","bar","mars","foo" }, //should fail on foo, not mars!
      };
      
      for(String[] test : tests) {
        List<IPlayer> players = new LinkedList<>();
        for(String name : test) {
          players.add( new PlayerImpl(null, name, "", "", Color.BLUE, "") );
        }
        players.add(mars); //add a pre-existing record to the save list too
        try{
          _playerService.savePlayers(players);
          fail("Expected DuplicateRecordException for " + players);
        } catch(DuplicateRecordException expected) {}
      }
      
      //One that exists but is being updated should be fine
      List<IPlayer> players = new LinkedList<>();
      players.add(mars);
      _playerService.savePlayers(players);
      //nb: currently savePlayers() does not return ids or updated records
      
      
      
  }
  
  
  @Test
  public void testDeletePlayer() {
    PlayerId id = new PlayerId("ABC120");
    assertRecordCount(3);
    assertNotNull( _playerService.loadPlayer(id) );
    assertEquals(0, _listener.getDataChangedCount());
    _playerService.deletePlayer(id);
    assertRecordCount(2);
    assertNull( _playerService.loadPlayer(id) );
    assertEquals(1, _listener.getDataChangedCount());
    
    try {
      _playerService.deletePlayer(null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testGetPlayersSummary() {
    
    //Special setup for the summary test
    //It also requires the worlds tables to be setup    
    
    WorldServiceInitialiser worldServiceInitialiser = new WorldServiceInitialiser(__keyGen, _settingsService);
    try(Connection connection = __dataSource.getConnection()) {
      Set<String> tables = DbUtils.getTableNames(connection);
      worldServiceInitialiser.init(tables, connection);
      
      TestWorldServiceImpl.initTestHistory(connection);
      
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
    
    //end setup
    
    for(int t=0; t<N; t++) {
      //Each element in the summary list is a map containing summary columns for a specific player
      //Elements are alpha ordered on player name
      List<Map<String,Object>> data3 = _playerService.getPlayersSummary(3);
      assertNotNull(data3);
      assertEquals(3, data3.size());      
      Map<String,Object> venus3 = data3.get(2);
      assertEquals(new PlayerId("ABC122"),venus3.get("id")); 
      assertEquals("Venus",venus3.get("name"));
      assertEquals("green",venus3.get("status"));
      assertEquals("swampy",venus3.get("notes"));
      assertEquals(new Color(0,255,0), venus3.get("color"));
      assertEquals("Good",venus3.get("team"));      
      assertEquals(0l, venus3.get("worlds"));
      assertEquals(0.0d, (Double)venus3.get("visibleFp"),D);
      assertEquals(0l, venus3.get("labour"));
      assertEquals(0l, venus3.get("capital")); 
      
      List<Map<String,Object>> data2 = _playerService.getPlayersSummary(2);
      assertNotNull(data2);
      assertEquals(3, data2.size());
      Map<String,Object> venus2 = data2.get(2);
      assertEquals("Venus",venus2.get("name"));assertEquals(3l, venus2.get("worlds"));
      assertEquals(520l, venus2.get("labour"));
      assertEquals(55.43d, venus2.get("visibleFp"));
      
      Map<String, Object> mars2 = data2.get(1);
      assertEquals("Mars", mars2.get("name"));
      assertEquals(new PlayerId("ABC121"),mars2.get("id"));
      assertEquals(4l, mars2.get("worlds"));
      assertEquals(130.55d, mars2.get("visibleFp"));
      assertEquals(825l, mars2.get("labour"));
      assertEquals(535l, mars2.get("capital"));
    }
    
    try {
      _playerService.getPlayersSummary(0); //0 not allowed
      fail("Expected InvalidTurnException");
    } catch(InvalidTurnException expected) {}
    
    try {
      _playerService.getPlayersSummary(-1); //negative not allowed
      fail("Expected InvalidTurnException");
    } catch(InvalidTurnException expected) {}
    
    _playerService.getPlayersSummary(888); //turns for which data unavailable are ok
    
  }
  
  //////////////////////////////
  
  
  private void assertRecordCount(int expectedCount) {
    TestUtils.assertRowCount(__dataSource, "players", expectedCount);
  }
  
  private void assertEarthCorrect(IPlayer earth) {
    assertNotNull(earth);
    assertEquals(new PlayerId("ABC120"), earth.getId());
    assertEquals("Earth", earth.getName());
    assertEquals("blue", earth.getStatus());
    assertEquals("earth notes", earth.getNotes());
    assertEquals(new Color(0,0,255), earth.getColor());
    assertEquals("Evil", earth.getTeam());
  }
  
  private void assertMarsCorrect(IPlayer mars) {
    assertNotNull(mars);
    assertEquals(new PlayerId("ABC121"), mars.getId());
    assertEquals("Mars", mars.getName());
    assertEquals("red", mars.getStatus());
    assertEquals("aries", mars.getNotes());
    assertEquals(new Color(255,0,0), mars.getColor());
    assertEquals("Evil", mars.getTeam());
  }
  
  private void assertVenusCorrect(IPlayer venus) {
    assertNotNull(venus);
    assertEquals(new PlayerId("ABC122"), venus.getId());
    assertEquals("Venus", venus.getName());
    assertEquals("green", venus.getStatus());
    assertEquals("swampy", venus.getNotes());
    assertEquals(new Color(0,255,0), venus.getColor());
    assertEquals("Good", venus.getTeam());
  }
  
  private void deleteAllPlayers() {
    try (Connection connection = __dataSource.getConnection()) {
      DbUtils.writeQuery("DELETE FROM players", null, connection);
    } catch(SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
}



















