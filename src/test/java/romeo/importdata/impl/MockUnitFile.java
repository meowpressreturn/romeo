package romeo.importdata.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import romeo.importdata.IUnitFile;
import romeo.units.impl.TestUnitImpl;
import romeo.units.impl.UnitImpl;
import romeo.utils.Convert;

/**
 * A mock instance of the {@link IUnitFile} interface with some hardcoded unit data for testing with.
 * Note that  tests rely on the specifc value and number of records in here, so if you cnage it you will
 * have to update all the affected tests. 
 */
public class MockUnitFile implements IUnitFile {

  /**
   * Use this to convert a unit map (from UnitImpl) into a unit map with String values like what a real
   * unit file would have.
   * @param data
   * @return
   */
  public static Map<String,String> toStringValues(Map<String,?> data) {
    Map<String,String> map = new HashMap<String,String>();
    for(String key : data.keySet()) {
      
      Object value = data.get(key);
      if(value instanceof Double || value instanceof Float) {
        value = Convert.toStr((Double)value, 2); //2 digit floats thanks
      } else {
        value = "" + value; //Stringify even the dreaded null
      }
      map.put(key,(String)value);      
    }    
    return map;
  }
  
  private static final String[] COLUMNS = new String[] {
      "name","attacks","offense","defense","pd","carry","complexity","cost","license","scanner","foo","bar","speed"
  } ;
  
  private static final String NAME_COLUMN = "name";

  ////////////////////////////////////////////////////////////////////////////
  
  private Map<String,Map<String,String>> _data;
  private List<String> _names;  
  
  private boolean _throwAnException = false;
  
  public MockUnitFile() {
    
    _data = new LinkedHashMap<>();
    Set<String> columns = new HashSet<>(Arrays.asList(COLUMNS));
    
    Map<String,String> vipMap = toStringValues( UnitImpl.asMap( TestUnitImpl.newVip() ) );
    vipMap.put("foo","my dog is inside the piano");
    vipMap.put("bar","xyz");
    Convert.constrainToKeys(vipMap, columns); //we dont want the acronym etc in this map
    _data.put( vipMap.get(NAME_COLUMN), vipMap );
    
    Map<String,String> bsMap = toStringValues( UnitImpl.asMap( TestUnitImpl.newBStar() ) );
    bsMap.put("foo","my dog is inside the piano");
    bsMap.put("bar","xyz");
    Convert.constrainToKeys(bsMap, columns);
    _data.put( bsMap.get(NAME_COLUMN), bsMap );
    
    Map<String,String> rapMap = toStringValues( UnitImpl.asMap( TestUnitImpl.newRap(null) ) );
    rapMap.put("foo","my dog is inside the piano");
    rapMap.put("bar","xyz");
    Convert.constrainToKeys(rapMap, columns);
    _data.put( rapMap.get(NAME_COLUMN), rapMap );
    
    Map<String,String> bsrMap = toStringValues( UnitImpl.asMap( TestUnitImpl.newBsr(null) ) ) ;
    bsrMap.put("foo","my dog is inside the piano");
    bsrMap.put("bar","xyz");
    Convert.constrainToKeys(bsrMap, columns);
    _data.put( bsrMap.get(NAME_COLUMN), bsrMap );
    
    _names = Collections.unmodifiableList( new ArrayList<>(_data.keySet()));
  }
  
  @Override
  public Iterator<Map<String, String>> iterator() {
    if(_throwAnException) {
      throw new RuntimeException("This is a test exception");
    }
    return _data.values().iterator();
  }

  @Override
  public List<String> getNames() {
    return _names;
  }

  @Override
  public Map<String, String> getRow(String name) {
    return _data.get(name);
  }

  @Override
  public String[] getColumns() {
    return COLUMNS;
  }

  @Override
  public String getNameColumn() {
    return NAME_COLUMN;
  }

  public boolean isThrowAnException() {
    return _throwAnException;
  }

  public void setThrowAnException(boolean _throwAnException) {
    this._throwAnException = _throwAnException;
  }
  
  

}



















