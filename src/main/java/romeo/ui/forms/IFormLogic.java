package romeo.ui.forms;

import javax.swing.JComponent;

/**
 * Interface for logic of forms displayed in the so-called 'Navigator Panel'.
 * Note that instances of this are typically stateful and should not be shared between multiple form instances.
 */
public interface IFormLogic {

  /**
   * Called to tell the form logic to load a record's details into the UI. The
   * record is provided.
   * @param form
   * @param record
   */
  public void bind(RomeoForm form, Object record);

  /**
   * Called when the user clicks the save button in the UI
   */
  public void saveChanges();

  /**
   * Called when the user clicks the cancel button in the UI
   */
  public void cancelChanges();

  /**
   * Called when the user clicks the delete button in the UI (and confirms the
   * delete dialog)
   */
  public void deleteRecord();

  /**
   * Called when the form logic needs to do cleanup TODO - do we need this?
   */
  public void dispose();

  /**
   * Called when stuff might have changed in the form UI
   */
  public void inputChanged();

  /**
   * Called during form initialisation to init custom fields, note that this now occurs
   * during the RomeoForm constructor so work done in a subclass constructor wont have
   * occured yet.
   * form.
   */
  public JComponent initCustom(RomeoForm form, FieldDef field);
}
