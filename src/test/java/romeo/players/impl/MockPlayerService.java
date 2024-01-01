package romeo.players.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import romeo.model.impl.AbstractService;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.api.PlayerId;

public class MockPlayerService extends AbstractService implements IPlayerService {

  public MockPlayerService() {
    super(LoggerFactory.getLogger(MockPlayerService.class));
  }
  
  /**
   * Notification method made public for testing purposes
   */
  @Override
  public void notifyDataChanged(java.util.EventObject event) {
    super.notifyDataChanged(event);
  }
  
  @Override
  public List<IPlayer> getPlayers() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Map<String, Object>> getPlayersSummary(int turn) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public PlayerId savePlayer(IPlayer player) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void savePlayers(Collection<IPlayer> players) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public IPlayer loadPlayer(PlayerId id) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IPlayer loadPlayerByName(String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void deletePlayer(PlayerId id) {
    // TODO Auto-generated method stub
    
  }

}



















