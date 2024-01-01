package romeo.ui.forms;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;

import romeo.Romeo;
import romeo.persistence.DuplicateRecordException;
import romeo.ui.ErrorDialog;
import romeo.ui.NavigatorPanel;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;
import romeo.utils.INamed;

/**
 * A {@link JPanel} containing components created from a list of {@link FieldDef}.
 * Implements {@link INamed} for display of name when loaded into the {@link NavigatorPanel} .
 */
public class RomeoForm extends JPanel
    implements ActionListener, ItemListener, IFieldChangeListener, INamed {
  
  protected static class Components {
    Map<String,JComponent> entryFields;
    public final JComponent firstField;
    public final JButton saveButton;
    public final JButton cancelButton;
    public final JButton deleteButton;
    
    public Components(
        Map<String,JComponent> entryFields, 
        JComponent firstField, 
        JButton saveButton, 
        JButton cancelButton, 
        JButton deleteButton) {
      this.entryFields = entryFields;
      this.firstField = firstField;
      this.saveButton = saveButton;
      this.cancelButton = cancelButton;
      this.deleteButton = deleteButton;
    }
  }
  
  protected static final Color MANDATORY_COLOR = new Color(255, 255, 176);

  ////////////////////////////////////////////////////////////////////////////

  protected final Logger _log;
  
  private final List<FieldDef> _fields;
  private final Components _components;
  private final IFormLogic _formLogic;
  
  //Mutable state
  private String _name = "Untitled";
  private boolean _dirty = true;
  private boolean _new = true;
  private boolean _dataValid = true;
  private boolean _bindingInProgess = false;

  public RomeoForm(
      Logger log,
      RomeoFormInitialiser initialiser,
      List<FieldDef> fields,
      IFormLogic logic,
      boolean forceTwoColumns,
      Object record,
      boolean isNewRecord) {
    super();
    _log = Objects.requireNonNull(log, "log may not be null");
    Objects.requireNonNull(initialiser, "initialiser may not be null");
    _fields = Objects.requireNonNull(fields, "fields may not be null");
    _formLogic = Objects.requireNonNull(logic, "logic may not be null");
    
    //Build the panel based on the field definitions
    _components = initialiser.createFields(this, fields, logic, forceTwoColumns, record, isNewRecord);
    
    //Invoke any setup behaviour in the form logic
    _bindingInProgess = true;
    logic.bind(this, record);  
    afterBinding();
  }

  @Override
  public String getName() {
    return _name;
  }

  @Override
  public void setName(String string) {
    _name = string;
  }

  public Map<String, JComponent> getEntryFields() {
    if(_components.entryFields == null) {
      throw new NullPointerException("Form not initialised, entryFields therefore null");
    }
    return _components.entryFields;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    _log.trace("in actionPerformed(), actionEvent=" + e);
    try {
      if(e.getSource() == _components.saveButton) {
        _bindingInProgess = true;
        _formLogic.saveChanges();
        afterBinding();
      } else if(e.getSource() == _components.cancelButton) {
        _bindingInProgess = true;
        _formLogic.cancelChanges();
        afterBinding();
      } else if(e.getSource() == _components.deleteButton) {
        int choice = JOptionPane.showConfirmDialog(Romeo.getMainFrame(),
            "Do you really wish to delete this record from the database?", "Confirm Deletion",
            JOptionPane.YES_NO_OPTION);
        if(choice == JOptionPane.YES_OPTION) {
          _formLogic.deleteRecord();
          close();
        }
      } else {
        setDirty(true);
      }
    } catch(DuplicateRecordException dre) {
      //Thrown by services, usually because the name is not unique
      String message = Convert.wordWrap(dre.getMessage(), 80);
      JOptionPane.showMessageDialog(Romeo.getMainFrame(), message, "Duplicate Not Permitted", JOptionPane.ERROR_MESSAGE);
      _log.error("Duplicate Record Exception:" + dre.getMessage() );
    } catch(Exception ex) {
      ErrorDialog dialog = new ErrorDialog("Form action error", ex, false);
      dialog.show();
      _log.error("Form action error", ex);
    }
    updateButtons();
  }

  protected void updateButtons() {
    
    _log.trace("updateButtons()");
    
    _formLogic.inputChanged();
    updateValidity();
    _components.saveButton.setText(isNew() ? "Create" : "Save");
    _components.saveButton.setEnabled(isDirty() && isDataValid());
    _components.cancelButton.setEnabled(isDirty());
    _components.deleteButton.setEnabled(!isNew());
    _components.deleteButton.setVisible(!isNew());
  }

  protected void updateValidity() {
    _log.trace("Checking validity of fields in form at " + new Date() );
    Iterator<FieldDef> i = _fields.iterator();
    while(i.hasNext()) {
      FieldDef def = (FieldDef) i.next();
      String fieldName = def.getName();
      Object field = _components.entryFields.get(fieldName);
      if(field != null) {
        if(field instanceof IValidatingField) {
          boolean fieldValid = ((IValidatingField) field).isFieldValid();
          _log.trace("Field '" + fieldName + "' valid=" +  fieldValid);
          if(!fieldValid) {
            setDataValid(false);
            _log.trace("Form fails validation");
            return; //short circuit return if we find any that are invalid
          }
        }
        if(def.isMandatory()) {
          if(field instanceof JTextComponent) {
            String text = ((JTextComponent) field).getText();
            text = text.trim();
            _log.trace("Mandatory field '" + fieldName + "' trimmed value=\"" + text + "\""); 
            if(text.isEmpty()) {
              setDataValid(false);
              _log.trace("Form fails validation");
              return; //short circuit return if any mandatory field is empty
            }
          }
          //For now just assume a combobox is always ok as some element will always be selected
        }
      }
    }
    _log.trace("Form is valid as no invalid fields found");
    setDataValid(true);
  }

  public boolean isDirty() {
    return _dirty;
  }

  public boolean isNew() {
    return _new;
  }

  public void setDirty(boolean dirty) {
    _dirty = dirty;
  }

  public void setNew(boolean isNewRecord) {
    _new = isNewRecord;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    valueChanged(e.getSource());
  }

  @Override
  public void valueChanged(Object field) {
    _log.trace("valueChanged() _bindingInProgress=" + _bindingInProgess + ", field=" + field);
    if(!_bindingInProgess) {
      setDirty(true);
      updateButtons();
    }
  }

  public void dataChanged() {
    final RomeoForm thisForm = this;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Container parent = getParent();
        if(parent instanceof NavigatorPanel) {
          ((NavigatorPanel) parent).showPanelName();
        }
        NavigatorPanel navPanel = (NavigatorPanel) GuiUtils.getAncestorOfType(NavigatorPanel.class, thisForm);
        if(navPanel != null) {
          navPanel.showPanelName();
        }
        updateButtons();
        validate();
        repaint();
      }
    });
  }
  
  /**
   * Closes this form in the navigator panel
   */
  public void close() {
    NavigatorPanel navigatorPanel = getNavigatorPanel();
    if(navigatorPanel == null) throw new IllegalStateException("navigatorPanel should not be null here");
    navigatorPanel.close();
  }

  /**
   * Finds the ancestor NavigatorPanel
   * @return navigatorPanel
   */
  protected NavigatorPanel getNavigatorPanel() {
    NavigatorPanel navPanel = (NavigatorPanel) GuiUtils.getAncestorOfType(NavigatorPanel.class, this);
    return navPanel;
  }

  public JComponent getFocusField() { //only focus the field if creating new record. Its a nuisance otherwise!
    return isNew() ? _components.firstField : null;
  }

  /**
   * Returns a reference to the save button.
   * (NavigatorPanel can use this to set the default component.)
   */
  public JButton getSaveButton() {
    return _components.saveButton;
  }
//
//  public JButton getCancelButton() {
//    return _cancelButton;
//  }
//
//  public JButton getDeleteButton() {
//    return _deleteButton;
//  }

  public void formClosing() {
    _formLogic.dispose();
  }

  public boolean isDataValid() {
    return _dataValid;
  }

  public void setDataValid(boolean b) {
    _dataValid = b;
  }
  
  /**
   * To be called after a record is bound to clear the dirty flag and
   * clear the bindingInProgress flag.
   */
  protected void afterBinding() {
    SwingUtilities.invokeLater(new Runnable() {      
      @Override
      public void run() {
        setDirty(false);
        _bindingInProgess = false;
        dataChanged();        
      }
    });
  }

}
