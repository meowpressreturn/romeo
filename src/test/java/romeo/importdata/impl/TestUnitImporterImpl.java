package romeo.importdata.impl;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import romeo.importdata.IUnitImportReport;
import romeo.test.ServiceListenerChecker;
import romeo.units.api.Acronym;
import romeo.units.api.IUnit;
import romeo.units.api.UnitId;
import romeo.units.impl.MockUnitService;
import romeo.units.impl.TestUnitImpl;
import romeo.units.impl.UnitImpl;
import romeo.utils.Convert;
import romeo.xfactors.api.XFactorId;

public class TestUnitImporterImpl {

  private MockUnitService _mockUnitService;
  private ServiceListenerChecker _listener;
  private MockUnitFile _mockUnitFile;
  
  @Before
  public void setup() {
    
    _mockUnitService = new MockUnitService();
    _mockUnitFile = new MockUnitFile();
    _listener = new ServiceListenerChecker();
    _mockUnitService.addListener(_listener);
  }
  
  @Test
  public void testConstructor() {
    new UnitImporterImpl(_mockUnitService);

    try {
      new UnitImporterImpl(null);
     fail("Expected NullPointerException");
    } catch(NullPointerException expected) { }
  }
  
  @Test
  public void testImportNoAdjustments() {      
    assertEquals(0, _mockUnitService.getUnits().size());
    assertEquals(0, _listener.getDataChangedCount() );
    UnitImporterImpl unitImporter = new UnitImporterImpl(_mockUnitService);
    IUnitImportReport report = unitImporter.importData(_mockUnitFile, null, false);
    List<IUnit> units = _mockUnitService.getUnits();
    assertEquals(4, units.size());
    assertEquals(1, _listener.getDataChangedCount() ); //Should be only one notification for the entire import
    
   
    IUnit vip =  _mockUnitService.getByName("Fighter");
    assertNotNull(vip);
    //The unit imported can only do x-factors via the adjustments map, so an imported viper won't have one
    //yet (even though it was in the map its not a column in the csv).
    assertNull( vip.getXFactor() );
    //Adding in the acronym and xfactor so we can use the convenience test method in TestUnitImpl
    vip = TestUnitImpl.mutate(vip, "acronym", Acronym.fromString("vip"));
    vip = TestUnitImpl.mutate(vip, "xFactor", new XFactorId("XF1"));
    TestUnitImpl.assertVipCorrect(vip.getId(), vip); //and now we can check all the other stuff got imported.
    //nb: we can't guarantee what order the unit service got called in, so can't presume to check ids
    //and anyway, ids belong to the service so mostly out of scope for this test anyway
    
    IUnit bs = _mockUnitService.getByName("Carrier");
    bs = TestUnitImpl.mutate(bs, "acronym", Acronym.fromString("BS") );
    TestUnitImpl.assertBsCorrect(bs.getId(), bs);
    
    IUnit rap = _mockUnitService.getByName("Recon");
    rap = TestUnitImpl.mutate(rap, "acronym",Acronym.fromString("RAP") );
    TestUnitImpl.assertRapCorrect(rap.getId(), rap);
    
    //check the report
    assertEquals( 0, report.getUpdatedUnitsCount() );
    assertEquals(4, report.getImportedUnitsCount() );
  }
  
  @Test
  public void testImportNull() { 
    UnitImporterImpl unitImporter = new UnitImporterImpl(_mockUnitService);
    try {
      unitImporter.importData(null, null, false);
      fail("Expected NullPointerException");
    }catch(NullPointerException expected) {}
  }
  
  @Test
  public void testDefaultAcronym() {   
    UnitImporterImpl unitImporter = new UnitImporterImpl(_mockUnitService);
    unitImporter.importData(_mockUnitFile, null, false);
    IUnit bsr = _mockUnitService.getByName("Big Shooty Robot");
    //the default 'acronym' is as generated by the uitility method in unitImpl
    assertEquals(UnitImpl.generatePlaceholderAcronym(bsr.getName()), bsr.getAcronym());
  }
  
  @Test
  public void testWithAdjustments() {
    Map<String,Map<String,String>> adjustments = new HashMap<>();
    Map<String,String> vipAdj = new HashMap<>();
    vipAdj.put("acronym","vip");
    adjustments.put( Convert.toDigestSignature("Fighter".getBytes()), vipAdj );
    
    Map<String,String> bsAdj = new HashMap<>();
    bsAdj.put("acronym","BS");
    adjustments.put( Convert.toDigestSignature("Carrier".getBytes()), bsAdj );
    
    Map<String,String> bsrAdj = new HashMap<>();
    bsrAdj.put("acronym","BSR");
    bsrAdj.put("complexity","0");
    bsrAdj.put("scanner","1234");
    bsrAdj.put("noSuchProperty","5000"); //extra junk should be harmless
    adjustments.put( Convert.toDigestSignature("Big Shooty Robot".getBytes()), bsrAdj);
    
    Map<String,String> nsuAdj = new HashMap<>();
    nsuAdj.put("acronym","NSU");
    adjustments.put( Convert.toDigestSignature("No Such Unit".getBytes()), nsuAdj); 
    
    UnitImporterImpl unitImporter = new UnitImporterImpl(_mockUnitService);
    IUnitImportReport report = unitImporter.importData(_mockUnitFile, adjustments, true);
    
    assertEquals(1, _listener.getDataChangedCount() ); //Should be only one notification for the entire import
    
    IUnit vip = _mockUnitService.getByAcronym( Acronym.fromString("VIP") );
    assertNotNull(vip);
    assertEquals(Acronym.fromString("vip"),vip.getAcronym());
    vip = TestUnitImpl.mutate(vip, "xFactor", new XFactorId("XF1")); //needed to pass below check
    TestUnitImpl.assertVipCorrect(vip.getId(), vip);
    
    assertNotNull(_mockUnitService.getByAcronym( Acronym.fromString("BS")) );
    assertNotNull(_mockUnitService.getByAcronym( Acronym.fromString("BS")) );
    
    //we didnt provide an adjustment for rap
    assertNull( _mockUnitService.getByAcronym( Acronym.fromString("RAP") ) );
    assertNotNull( _mockUnitService.getByName("Recon") );
    
    //and having an entry for a non-existent unit wont conjure it from thin air
    assertNull( _mockUnitService.getByAcronym( Acronym.fromString("NSU") ) );
    
    //check the report
    assertEquals( 0, report.getUpdatedUnitsCount() );
    assertEquals(4, report.getImportedUnitsCount() );
  }
  
  @Test
  public void testUpdates() {

    //We preload a RAP unit in the service, and give it a speed of 300
    IUnit rap = TestUnitImpl.newRap(null);
    assertEquals( Acronym.fromString("RAP"), rap.getAcronym()); //not a test, more an assertion before we test
    rap = TestUnitImpl.mutate(rap, "speed", 300); 
    UnitId rapId = _mockUnitService.saveUnit(rap);
    
    //Now we will try importing from the mock unit file. We expect that 3 units will be imported and the RAP will
    //be updated. 
    _listener.reset();
    UnitImporterImpl unitImporter = new UnitImporterImpl(_mockUnitService); //Mock file of csv unit data (as strings)
    Map<String, Map<String, String>> adjustments = null; //No adjustments to values in the file
    boolean enableUpdate = true; //Update existing units with data from the file
    IUnitImportReport report = unitImporter.importData(_mockUnitFile, adjustments, enableUpdate);
    
    assertNull( report.getException() );
    assertEquals( 3, report.getImportedUnitsCount() );
    assertEquals( 1, report.getUpdatedUnitsCount() ); //The RAP would have been updated    
    assertEquals( 1, _listener.getDataChangedCount() ); //Should be only one notification for the entire import
    
    IUnit loadRap = _mockUnitService.getUnit(rapId);
    assertEquals(  Acronym.fromString("RAP"), loadRap.getAcronym() );
    assertEquals( 100, loadRap.getSpeed() );
    
  }
  
  @Test
  public void testReportException() {
    _mockUnitFile.setThrowAnException(true);
    UnitImporterImpl unitImporter = new UnitImporterImpl(_mockUnitService);
    IUnitImportReport report = unitImporter.importData(_mockUnitFile, null, true);
    assertNotNull( report.getException() );    
  }
  
}



















