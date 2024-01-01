package romeo.units.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.slf4j.LoggerFactory;

import romeo.importdata.impl.UnitImporterImpl;
import romeo.model.impl.AbstractService;
import romeo.units.api.Acronym;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.units.api.UnitId;
import romeo.utils.BeanComparator;
import romeo.xfactors.api.XFactorId;
import romeo.xfactors.impl.XFactorServiceImpl;

/**
 * Mock implemention of the {@link IUnitService} . 
 * Mainly for use in testing the {@link XFactorServiceImpl} and the {@link UnitImporterImpl}
 * without using a real impl. Has some very limited support for saving and retrieving units (to an internal map)
 * but not all methods are fully implemented.
 */
public class MockUnitService extends AbstractService implements IUnitService {
  
  private List<XFactorId> _unlinkCalls;
  private Map<UnitId, IUnit> _data;
  private int _nextKey = 0;
  
  public MockUnitService() {
    super(LoggerFactory.getLogger(MockUnitService.class));
    _unlinkCalls = new ArrayList<>();
    _data = new HashMap<UnitId,IUnit>();
  }
  
  @Override
  public void dataChangedExternally() {
    notifyDataChanged();    
  }
  
  /**
   * The notification method has been made public for testing purposes
   */
  @Override
  public void notifyDataChanged(java.util.EventObject event) {
    super.notifyDataChanged(event);
  }

  @Override
  public List<IUnit> getUnits() {
    BeanComparator byName = new BeanComparator("name");
    List<IUnit> units = new ArrayList<>(_data.values());
    units.sort(byName);
    return Collections.unmodifiableList(units);
  }

  @Override
  public UnitId saveUnit(IUnit unit) {
    return saveUnit(unit, true);
  }

  @Override
  public List<UnitId> saveUnits(List<IUnit> units) {
    List<UnitId> ids = new ArrayList<>();
    for(IUnit unit : units) {
      ids.add( saveUnit(unit, false) );
    }
    notifyDataChanged(new EventObject(this));
    return ids;
  }
  
  private UnitId saveUnit(IUnit unit, boolean notify) {
    UnitId id = unit.getId();
    if(id==null) {
      id = new UnitId("MOCK" + _nextKey++);
    }
    _data.put(id, new UnitImpl(id, unit) );
    if(notify) {
      notifyDataChanged(new EventObject(this));
    }
    return id;
  }

  @Override
  public IUnit getUnit(UnitId id) {
    return _data.get(id);
  }

  @Override
  public IUnit getByAcronym(Acronym acronym) {
    acronym = Objects.requireNonNull(acronym);
    for(IUnit unit : getUnits() ) { //no fancy quick lookup tables here, indeed not
      Acronym candidate = unit.getAcronym();
      if(candidate != null && candidate.equals(acronym)) {
        return unit;
      }
    }
    return null;
  }

  @Override
  public IUnit getByName(String name) {
    name = Objects.requireNonNull(name).toUpperCase(Locale.US);
    for(IUnit unit : getUnits() ) {
      String candidate = unit.getName();
      if(candidate != null && candidate.toUpperCase(Locale.US).equals(name)) {
        return unit;
      }
    }
    return null;
  }

  @Override
  public void deleteUnit(UnitId id) {
    _data.remove(id);
    notifyDataChanged(new EventObject(this));
  }

  @Override
  public int[] getSpeeds() {
    throw new UnsupportedOperationException();
  }

  @Override
  public double[] getRange(String property) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<IUnit> getScanners() {
    return Collections.emptyList();
  }

  /**
   * Records that the method was called but currently doesnt implement the actual unlinking on the test data
   */
  @Override
  public void unlinkUnitsWithXFactor(XFactorId id) {
    _unlinkCalls.add(id); 
  }
  
  /**
   * Clears the test data and record of calls to unlink
   */
  public void reset() {
    _unlinkCalls.clear();
    _data.clear();
  }
  
  public int getUnlinkCallCount() {
    return _unlinkCalls.size();
  }
  
  public List<XFactorId> getUnlinkCalls() {
    return _unlinkCalls;
  }

}



















