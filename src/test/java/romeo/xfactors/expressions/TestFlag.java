package romeo.xfactors.expressions;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import romeo.battle.impl.RoundContext;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.units.impl.UnitImpl;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IExpressionTokeniser;
import romeo.xfactors.impl.ExpressionParserImpl;

public class TestFlag {

  private RoundContext _context;
  private FleetElement _earthElementBase; //base fleet for earth (the defender)
  private FleetElement _earthElement1;
  private FleetElement _marsElement0;
  private FleetContents _venusFleet;
  
  @Before
  public void setup() {
    UnitImpl viper = new UnitImpl(null, "Viper", 2, 30, 25, 10, 120, 1, 100, 30, 25, 200, "vip", null);
    
    _context = new RoundContext(new String[] { "Mars", "Earth", "Venus" } );
    _context.setDefendingPlayer("Earth");
    _context.setRound(42);
    
    _venusFleet = new FleetContents();
    _venusFleet.addElement(new FleetElement(viper,10,0) );
    _context.setFleet("Venus", _venusFleet);
    
    FleetContents marsFleet = new FleetContents();
    _marsElement0 = new FleetElement(viper, 100, 0);
    marsFleet.addElement(_marsElement0);
    _context.setFleet("Mars", marsFleet);
    
    FleetContents earthFleet = new FleetContents();
    _earthElementBase = new FleetElement(viper, 50, 0);
    earthFleet.addElement( _earthElementBase );
    
    _earthElement1 = new FleetElement(viper, 25, 1);;
    earthFleet.addElement( _earthElement1 );
    earthFleet.addElement( new FleetElement(viper, 5, 2) );
    _context.setFleet("Earth", earthFleet);
    
    _context.setThisPlayer("Earth");
    _context.setFleetElement(_earthElement1);
    
    marsFleet.setFlag("MUTANT",true);
    marsFleet.setFlag("BIO",true);
    earthFleet.setFlag("TERRAN", true);
    earthFleet.setFlag("BIO",true);
    
    _context.setThisPlayer("Earth");
    
  }
  
  @Test
  public void testConstructor() {
    Value value = new Value(0);
    Flag flag = new Flag(Flag.ANY_PLAYER,value);
    assertEquals( Flag.ANY_PLAYER, flag.getOperand());
    assertEquals( value, flag.getFlag());
    
    try {
      new Flag(Flag.ANY_PLAYER, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Flag(888, value);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
  }
  
  @Test
  public void testParsingConstructor() {
    IExpressionParser parser = new ExpressionParserImpl();
    IExpressionTokeniser tokeniser = new ExpressionParserImpl();
    
    assertEquals( Flag.ANY_PLAYER, new Flag("ANY_PLAYER,VALUE(0)",parser, tokeniser).getOperand() );
    assertEquals( Flag.THIS_PLAYER, new Flag("THIS_PLAYER,VALUE(0)",parser, tokeniser).getOperand() );
    assertEquals( Flag.OPPOSING_PLAYERS, new Flag("OPPOSING_PLAYERS,VALUE(0)",parser, tokeniser).getOperand() );   
    
    try {
      new Flag("",parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Flag(",",parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Flag(",,,,",parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Flag("ANY_PLAYER, VALUE(0), VALUE(1)",parser, tokeniser);
      fail("Expected IllegalArgumentException");
    } catch(IllegalArgumentException expected) {}
    
    try {
      new Flag(null, parser, tokeniser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Flag("OPPOSING_PLAYERS,VALUE(1)", null, tokeniser);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
    
    try {
      new Flag("OPPOSING_PLAYERS,VALUE(1)", parser, null);
      fail("Expected NullPointerException");
    } catch(NullPointerException expected) {}
  }
  
  @Test
  public void testCheckFlags() {
    assertTrue( (Boolean)new Flag(Flag.THIS_PLAYER, new Value("TERRAN")).evaluate(_context) );
    assertTrue( (Boolean)new Flag(Flag.ANY_PLAYER, new Value("TERRAN")).evaluate(_context) );
    assertFalse( (Boolean)new Flag(Flag.OPPOSING_PLAYERS, new Value("TERRAN")).evaluate(_context) );
    
    assertTrue( (Boolean)new Flag(Flag.OPPOSING_PLAYERS, new Value("BIO")).evaluate(_context) );
    
    assertTrue( (Boolean)new Flag(Flag.ANY_PLAYER, new Value("TERRAN")).evaluate(_context) );
    assertTrue( (Boolean)new Flag(Flag.ANY_PLAYER, new Value("MUTANT")).evaluate(_context) );
    assertTrue( (Boolean)new Flag(Flag.ANY_PLAYER, new Value("BIO")).evaluate(_context) );
    assertFalse( (Boolean)new Flag(Flag.ANY_PLAYER, new Value("PUMPKIN")).evaluate(_context) );
  }
  
  @Test
  public void testFlagsCaseInsensitive() {
    //it converts the result of the flag expression to uppercase
    assertTrue( (Boolean)new Flag(Flag.THIS_PLAYER, new Value("TERRAN")).evaluate(_context) );
    assertTrue( (Boolean)new Flag(Flag.THIS_PLAYER, new Value("terran")).evaluate(_context) );
    assertTrue( (Boolean)new Flag(Flag.THIS_PLAYER, new Value("Terran")).evaluate(_context) );
    
    //but does it convert the flag to uppercase? (spoiler: it wasn't till I tested...)
    _venusFleet.setFlag("lowerCase", true);
    assertTrue( (Boolean)new Flag(Flag.ANY_PLAYER, new Value("Lowercase")).evaluate(_context) );
    assertTrue( (Boolean)new Flag(Flag.ANY_PLAYER, new Value("LOWERCASE")).evaluate(_context) );
    assertTrue( (Boolean)new Flag(Flag.ANY_PLAYER, new Value("lowerCase")).evaluate(_context) );
  }
  
  /**
   * 
   */
  @Test
  public void testExpressionToString() {
    _venusFleet.setFlag("1234.0", true); // this is what the toString of 1234d looks like
    Flag flag = new Flag(Flag.ANY_PLAYER, new Value(1234d));
    Boolean result = (Boolean)flag.evaluate(_context);
    assertTrue( result );
    
    _venusFleet.setFlag("true",true);
    assertTrue( (Boolean)new Flag(Flag.OPPOSING_PLAYERS, new Value(true)).evaluate(_context) );
  }
  
}
