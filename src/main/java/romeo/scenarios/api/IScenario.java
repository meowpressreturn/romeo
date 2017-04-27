package romeo.scenarios.api;

import java.util.List;

import romeo.persistence.ICanGetId;
import romeo.utils.INamed;

public interface IScenario extends INamed, ICanGetId<ScenarioId> {

  public static final int MAX_NAME_LENGTH = 32;

  public List<String> getFleets();

}
