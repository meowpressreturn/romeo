package romeo.worlds.ui;

import java.util.Map;
import java.util.Objects;

import romeo.Romeo;
import romeo.ui.AbstractNavigatorRecordSelectionListener;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.worlds.api.WorldAndHistory;
import romeo.worlds.api.WorldId;
import romeo.worlds.impl.WorldImpl;

public class WorldNavigatorRecordSelectionListener extends AbstractNavigatorRecordSelectionListener {

  public WorldNavigatorRecordSelectionListener(NavigatorPanel navigatorPanel) {
    super(navigatorPanel);
  }

  @Override
  protected RomeoForm newForm() {
    return Romeo.CONTEXT.createWorldForm();
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
      openRecord(id);
    } else {
      throw new UnsupportedOperationException("Only World, WorldImpl or world Map are supported here . Received:" + record);
    }
  }

}
