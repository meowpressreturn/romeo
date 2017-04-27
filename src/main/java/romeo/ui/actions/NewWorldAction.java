package romeo.ui.actions;

import romeo.Romeo;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;
import romeo.worlds.impl.WorldImpl;

public class NewWorldAction extends AbstractNewRecordAction {
  public NewWorldAction(NavigatorPanel navigatorPanel) {
    super(navigatorPanel);
    setDescription("Create a World record");
    setName("New World");
    setIcon("/images/worldNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return Romeo.CONTEXT.createWorldForm();
  }

  @Override
  protected Object newRecord() {
    return new WorldImpl(null, "", 0, 0, null, "", 0, 0);
  }
}
