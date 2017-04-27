package romeo.scenarios.api;

import java.util.List;

import romeo.model.api.IService;

public interface IScenarioService extends IService {

  public List<IScenario> getScenarios();

  public IScenario loadScenario(ScenarioId id);

  public IScenario saveScenario(IScenario scenario);

  public void deleteScenario(ScenarioId id);

  public void deleteAllScenarios();
}
