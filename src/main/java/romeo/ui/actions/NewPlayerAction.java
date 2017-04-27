package romeo.ui.actions;

import romeo.Romeo;
import romeo.players.impl.PlayerImpl;
import romeo.ui.NavigatorPanel;
import romeo.ui.forms.RomeoForm;

/**
 * Action for opening a form to create a new player record in the navigator
 * panel
 */
public class NewPlayerAction extends AbstractNewRecordAction {
  /**
   * Constructor
   * @param navigatorPanel
   */
  public NewPlayerAction(NavigatorPanel navigatorPanel) {
    super(navigatorPanel);
    setDescription("Create a Player record");
    setName("New Player");
    setIcon("/images/playerNew.gif");
  }

  @Override
  protected RomeoForm newForm() {
    return Romeo.CONTEXT.createPlayerForm();
  }

  @Override
  protected Object newRecord() {
    return new PlayerImpl();
  }
}
