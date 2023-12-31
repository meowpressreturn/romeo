package romeo.worlds.ui;

import java.util.List;

import javax.swing.JTable;

import romeo.ui.TableNavigatorMediator;
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.IFormLogic;
import romeo.ui.forms.RomeoForm;
import romeo.ui.forms.RomeoFormInitialiser;

public class WorldForm extends RomeoForm  {
  
  private JTable _historyTable;  
  private TableNavigatorMediator historyTnm;

  public WorldForm(
      RomeoFormInitialiser initialiser,
      List<FieldDef> fields,
      IFormLogic logic,
      Object record,
      boolean isNewRecord)  {
    super(initialiser, fields, logic, true, record, isNewRecord);
    setName("World");
    
    /**
     * TODO: User can edit values for the turn, but if they navigate to another
     * turn without saving their changes, the values will be lost. Need to do
     * the dirty logic for this too.
     */
  }
  
  protected JTable getHistoryTable() {
    return _historyTable;
  }
  
  protected void setHistoryTable(JTable historyTable) {
    _historyTable = historyTable;
  }
  
  protected TableNavigatorMediator getHistoryTnm() {
    return historyTnm;
  }
  
  public void setHistoryTnm(TableNavigatorMediator tableNavigatorMediator) {
    historyTnm = tableNavigatorMediator;
  }

}
