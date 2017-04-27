/*
 * XFactorFormLogic.java
 * Created on Mar 14, 2006
 */
package romeo.xfactors.ui;

import java.util.EventObject;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import romeo.model.api.IServiceListener;
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.IFormLogic;
import romeo.ui.forms.RomeoForm;
import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.api.XFactorId;
import romeo.xfactors.impl.XFactorImpl;

/**
 * Stateful class that manages the logic of an X-Factor form for {@link RomeoForm}
 */
public class XFactorFormLogic implements IFormLogic, IServiceListener {
  private RomeoForm _form;
  private IXFactor _xfactor;
  
  private final IXFactorService _xfactorService;
  
  public XFactorFormLogic(IXFactorService xFactorService) {
    _xfactorService = Objects.requireNonNull(xFactorService, "xFactorService may not be null");
  }

  @Override
  public void inputChanged() {
    ;
  }

  @Override
  public void bind(RomeoForm form, Object record) {
    Objects.requireNonNull(_xfactorService, "_xFactorService must not be null");
    _form = form;
    loadWith((IXFactor) record);
    _xfactorService.addListener(this);
  }

  @Override
  public void cancelChanges() {
    if(_xfactor.isNew()) {
      _form.close();
    } else {
      loadWith(_xfactorService.loadXFactor(_xfactor.getId()));
    }
  }

  @Override
  public void deleteRecord() {
    if(!_xfactor.isNew()) {
      _xfactorService.deleteXFactor(_xfactor.getId());
    } else {
      cancelChanges();
    }
  }

  /**
   * Sets the _xfactor reference to the specified record (may not be null) and updates the fields in the UI
   * with the values from the record, sets the form title appropriately, clears the dirty flag, and notifies the
   * form handler that data has changed.
   * @param xf
   */
  private void loadWith(IXFactor xf) {
    _xfactor = xf;
    if(_form != null) {
      String name = xf.getName();
      ((JTextField) _form.getEntryFields().get("name")).setText(name);
      ((JTextField) _form.getEntryFields().get("description")).setText(xf.getDescription());

      ((JTextArea) _form.getEntryFields().get("trigger")).setText(xf.getTrigger());
      ((JTextArea) _form.getEntryFields().get("xfAttacks")).setText(xf.getXfAttacks());
      ((JTextArea) _form.getEntryFields().get("xfOffense")).setText(xf.getXfOffense());
      ((JTextArea) _form.getEntryFields().get("xfDefense")).setText(xf.getXfDefense());
      ((JTextArea) _form.getEntryFields().get("xfPd")).setText(xf.getXfPd());
      ((JTextArea) _form.getEntryFields().get("xfRemove")).setText(xf.getXfRemove());

      _form.setDirty(false);
      _form.setNew(_xfactor.isNew());

      if(name.isEmpty()) {
        _form.setName("Untitled X-Factor");
      } else {
        _form.setName(_xfactor.isNew() ? "Create New X-Factor" : "X-Factor - " + name);
      }

      _form.dataChanged(); //notify form handler of form data changes
    }
  }

  @Override
  public void saveChanges() {
    String name = ((JTextField) _form.getEntryFields().get("name")).getText();
    String description = ((JTextField) _form.getEntryFields().get("description")).getText();
    String trigger = ((JTextArea) _form.getEntryFields().get("trigger")).getText();
    String xfAttacks = ((JTextArea) _form.getEntryFields().get("xfAttacks")).getText();
    String xfOffense = ((JTextArea) _form.getEntryFields().get("xfOffense")).getText();
    String xfDefense = ((JTextArea) _form.getEntryFields().get("xfDefense")).getText();
    String xfPd = ((JTextArea) _form.getEntryFields().get("xfPd")).getText();
    String xfRemove = ((JTextArea) _form.getEntryFields().get("xfRemove")).getText();

    IXFactor record = new XFactorImpl(_xfactor.getId(), name, description, trigger, xfAttacks, xfOffense, xfDefense,
        xfPd, xfRemove);
    XFactorId id = _xfactorService.saveXFactor(record);
    if(!id.equals(_xfactor.getId())) { //Create new object if a new id was allocated (ie: it was a new record)
      record = new XFactorImpl(id, name, description, trigger, xfAttacks, xfOffense, xfDefense,
          xfPd, xfRemove);
    }
    loadWith(record);
  }

  /**
   * Called by the service when an XFactor changed somewhere, maybe. 
   * Will try to refresh the xfactor record that is open in the ui (if its not already halfway through an edit by the user)
   * @param event (unused)
   */
  @Override
  public void dataChanged(EventObject event) {
    if( !_xfactor.isNew() && !_form.isDirty()) { //Only load fresh data if user hadnt started an edit operation
      //Try to reload the same xFactor record we had open in the UI
      IXFactor reloaded = _xfactorService.loadXFactor(_xfactor.getId());
      if(reloaded == null) { //It was deleted (turn this into a new record form in case they want it back)
        loadWith( new XFactorImpl(null, _xfactor) );
        _form.setDirty(true);
      } else { //Load the (potentially) fresh data for this xfactor into the form
        loadWith(reloaded);
      }
    }
  }

  @Override
  public void dispose() {
    _xfactorService.removeListener(this);
  }

  /**
   * This form does not define any custom fields.
   * @return null
   */
  @Override
  public JComponent initCustom(FieldDef field) {
    return null;
  }

}
