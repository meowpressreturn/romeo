package romeo.scenarios.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.slf4j.Logger;

import romeo.persistence.AbstractPersistenceService;
import romeo.scenarios.api.IScenario;
import romeo.scenarios.api.IScenarioService;
import romeo.scenarios.api.ScenarioId;
import romeo.utils.Convert;
import romeo.utils.DbUtils;
import romeo.utils.IKeyGen;

public class ScenarioServiceImpl extends AbstractPersistenceService implements IScenarioService {

  private List<IScenario> _scenariosCache;
  private Map<ScenarioId, IScenario> _scenariosById;

  public ScenarioServiceImpl(
      Logger log,
      DataSource dataSource, 
      IKeyGen keyGen) {
    super(log, dataSource, keyGen);
  }

  @Override
  public synchronized List<IScenario> getScenarios() {
    if(!cacheInitialised()) {
      initCache();
    }
    return _scenariosCache;
  }

  @Override
  public synchronized IScenario getScenario(ScenarioId id) {
    Objects.requireNonNull(id, "id may not be null");
    if(!cacheInitialised()) {
      initCache();
    }
    return _scenariosById.get(id);
  }

  @Override
  public synchronized IScenario saveScenario(IScenario scenario) {
    Objects.requireNonNull(scenario, "scenario may not be null");
    String fleetsCsv = Convert.toCsv(scenario.getFleets());
    try(Connection connection = _dataSource.getConnection()) {
      if(scenario.isNew()) {
        ScenarioId id = new ScenarioId(_keyGen.createIdKey());
        String name = scenario.getName();
        final String sql = "INSERT INTO SCENARIOS (id,name,fleetsCsv) VALUES (?,?,?);";
        scenario = new ScenarioImpl(id, name, fleetsCsv);
        DbUtils.writeQuery(sql, new Object[] { id, name, fleetsCsv }, connection);
      } else {
        final String sql = "UPDATE SCENARIOS SET name=?,fleetsCsv=? WHERE id=?;";
        DbUtils.writeQuery(sql, new Object[] { scenario.getName(), fleetsCsv, scenario.getId(), }, connection);
      }
      return scenario;
    } catch(Exception e) {
      throw new RuntimeException("Error saving scenario " + scenario, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }

  @Override
  public synchronized void deleteScenario(ScenarioId id) {
    Objects.requireNonNull(id, "id may not be null");
    try(Connection connection = _dataSource.getConnection()) {
      DbUtils.writeQuery("DELETE FROM SCENARIOS WHERE id=?;", new Object[] { id }, connection);
    } catch(Exception e) {
      throw new RuntimeException("Error deleting scenario " + id, e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }

  @Override
  public synchronized void deleteAllScenarios() {
    try(Connection connection = _dataSource.getConnection()) {
      DbUtils.writeQuery("DELETE FROM SCENARIOS", null, connection);
    } catch(Exception e) {
      throw new RuntimeException("Error deleting all scenarios", e);
    } finally {
      flushCache();
      notifyDataChanged();
    }
  }
  
  private boolean cacheInitialised() {
    return _scenariosCache != null;
  }
  
  private void flushCache() {
    _log.debug("Flushing scenarios cache");
    _scenariosCache = null;
    _scenariosById = null;
  }

  private void initCache() {
    List<IScenario> results = new ArrayList<>();
    try(Connection connection = _dataSource.getConnection()) {
      final String sql = "SELECT id, name, fleetsCsv" + " FROM SCENARIOS ORDER BY UCASE(name) ASC";
      ResultSet rs = DbUtils.readQuery(sql, null, connection);
      while(rs.next()) {
        results.add(readScenario(rs));
      }
    } catch(Exception e) {
      flushCache();
      throw new RuntimeException("Error loading all scenarios ", e);
    } 
    _scenariosCache = Collections.unmodifiableList(results);
    _scenariosById = new HashMap<ScenarioId, IScenario>();
    for(IScenario scenario : _scenariosCache) {
      _scenariosById.put(scenario.getId(), scenario);
    }
  }

  /**
   * Read the data for a scenario from the current row of the supplied ResultSet
   * @param rs
   * @return scenario
   * @throws SQLException
   */
  private IScenario readScenario(ResultSet rs) throws SQLException {
    ScenarioId id = new ScenarioId( rs.getString("id") );
    String name = rs.getString("name");
    String fleetsCsv = rs.getString("fleetsCsv");
    IScenario scenario = new ScenarioImpl(id, name, fleetsCsv);
    return scenario;
  }

}
