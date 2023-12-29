package romeo.ui.actions;

import java.util.Objects;

import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.worlds.impl.WorldImpl;
import romeo.worlds.ui.WorldFormFactory;

public class NewWorldAction extends AbstractNewRecordAction {
  
  private final WorldFormFactory _worldFormFactory;
  
  public NewWorldAction(NavigatorPanel navigatorPanel, WorldFormFactory worldFormFactory) {
    super(navigatorPanel);
    
    _worldFormFactory = Objects.requireNonNull(worldFormFactory, "worldFormFactory may not be null");
    
    setDescription("Create a World record");
    setName("New World");
    setIcon("/images/worldNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return _worldFormFactory.newWorldForm();
  }

  @Override
  protected Object newRecord() {
    return new WorldImpl(null, "", 0, 0, null, "", 0, 0);
  }
}
