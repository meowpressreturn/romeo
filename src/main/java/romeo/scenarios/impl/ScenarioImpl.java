package romeo.scenarios.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import romeo.scenarios.api.IScenario;
import romeo.scenarios.api.ScenarioId;
import romeo.utils.Convert;

public class ScenarioImpl implements IScenario {

  private ScenarioId _id;
  private String _name;
  private List<String> _fleets;

  /**
   * Constructor
   * @param id
   *          may be null (for creating new scenarios)
   * @param name
   *          may not be null or empty
   * @param fleetsCsv
   *          may not be null
   */
  public ScenarioImpl(ScenarioId id, String name, String fleetsCsv) {
    this(id, name, Convert.fromCsv(fleetsCsv));
  }

  /**
   * Constructor
   * @param id
   *          may be null (for creating new scenarios)
   * @param name
   *          may not be null or empty
   * @param fleetValues
   *          may not be null
   */
  public ScenarioImpl(ScenarioId id, String name, List<String> fleetValues) {
    _id = id;
    _name = Objects.requireNonNull(name, "name may not be null").trim();
    if(name.isEmpty()) {
      throw new IllegalArgumentException("name may not be empty");
    }
    _fleets = Collections.unmodifiableList(fleetValues);
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public ScenarioId getId() {
    return _id;
  }
  
  @Override
  public boolean isNew() {
    return _id==null;
  }

  @Override
  public List<String> getFleets() {
    return _fleets;
  }

  @Override
  public int hashCode() {
    return (_id == null) ? 0 : _id.hashCode();
  }

  /**
   * Equality is based on IDENTITY
   */
  @Override
  public boolean equals(Object obj) {
    if(this == obj)
      return true;
    if(obj == null)
      return false;
    if(getClass() != obj.getClass())
      return false;
    ScenarioImpl other = (ScenarioImpl) obj;
    if(_id == null) {
      if(other._id != null)
        return false;
    } else if(!_id.equals(other._id))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ScenarioImpl[" + _id + "," + _name + "," + _fleets.size() + " fleets]";
  }

}
