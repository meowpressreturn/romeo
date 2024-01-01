package romeo.worlds.ui;

import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;

import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.IWorldService;
import romeo.worlds.api.WorldAndHistory;
import romeo.worlds.api.WorldId;
import romeo.worlds.impl.WorldImpl;

public class WorldNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  private final WorldFormFactory _worldFormFactory;
  private final IWorldService _worldService;
  
  public WorldNavigatorRecordSelectionListener(
      Logger log,
      NavigatorPanel navigatorPanel, 
      WorldFormFactory worldFormFactory,
      IWorldService worldService) {
    super(log, navigatorPanel);
    _worldFormFactory = Objects.requireNonNull(worldFormFactory, "worldFormFactory may not be null");
    _worldService = Objects.requireNonNull(worldService, "worldService may not be null");
  }

  @Override
  protected RomeoForm newForm(Object record) {
    return _worldFormFactory.newWorldForm(record, false);
  }
  
  @Override
  public void recordSelected(Object record) {
    if(record instanceof WorldImpl || record instanceof WorldAndHistory) {
      openRecord(record);
    } else if(record instanceof Map) {
      WorldId id = null;
      Object idObj = Objects.requireNonNull( ((Map<?, ?>) record).get("id"), "world id in map may not be null");
      if(idObj instanceof String) {
        id = new WorldId( (String)idObj );
      } else if (idObj instanceof WorldId) {
        id = (WorldId)idObj;
      } else {
        throw new ClassCastException("id object in world map is of type " + idObj.getClass().getName() );
      }      
      IWorld world = _worldService.getWorld(id);
      openRecord(world);
    } else {
      throw new UnsupportedOperationException("Only World, WorldImpl or world Map are supported here . Received:" + record);
    }
  }

}
