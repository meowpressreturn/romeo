package romeo.ui.forms;

import java.awt.Color;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;

import javax.swing.JComboBox;

import romeo.model.api.IService;
import romeo.model.api.IServiceListener;
import romeo.persistence.AbstractRecordId;
import romeo.persistence.ICanGetId;

public abstract class AbstractRecordCombo extends JComboBox<Object> implements IValidatingField, IServiceListener {
  //Note, the generic type must be Object as in addition to IdBean we use a String for the
  //'none' option

  protected boolean _fieldValid = true;
  protected Color _normalBg;
  protected String _initYet = "Yes";
  protected String _noneOption = "None";
  protected boolean _mandatory = false;
  protected IService _service;

  public AbstractRecordCombo(IService service, String noneOption) {
    setNoneOption(noneOption);
    init(service);
    prepareOptions();
  }

  public AbstractRecordCombo(IService service) {
    Objects.requireNonNull(service,"service may not be null");
    init(service);
    prepareOptions();
  }

  private void init(IService service) {
    service.addListener(this);
    _service = service;
    addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        updateValidity();
      }
    });
  }

  @Override
  public void dataChanged(EventObject event) {
    ICanGetId<?> selectedBean = getSelectedRecord();
    prepareOptions();
    setSelectedRecord(selectedBean);
  }

  protected void prepareOptions() {
    List<? extends ICanGetId<?>> data = loadRecords();
    if(data == null)
      data = Collections.emptyList();
    removeAllItems();
    addItem(_noneOption);
    for(ICanGetId<?> bean : data) {
      addItem(bean);
    }
  }

  /**
   * Subclass must implement to return the records to populate the selector.
   * @return records
   */
  protected abstract List<? extends ICanGetId<?>> loadRecords();

  /**
   * Returns the selected record, or if the None record is selected it returns
   * null.
   * @return record
   */
  public ICanGetId<?> getSelectedRecord() {
    Object sel = getSelectedItem();
    if(sel == null || sel == _noneOption) {
      return null;
    }
    ICanGetId<?> idBean = (ICanGetId<?>) sel;
    return idBean;
  }

  /**
   * Set the selected record. Passing null selects the None option.
   * @param record
   *          or null for None option
   */
  public void setSelectedRecord(ICanGetId<?> record) {
    AbstractRecordId id = (record == null) ? null : (AbstractRecordId)record.getId();
    if(id == null) {
      setSelectedItem(_noneOption);
    } else {
      setSelectedItem(record);
    }
    updateValidity();
  }

  protected void updateValidity() {
    if(isMandatory()) {
      setFieldValid(getSelectedRecord() != null);
    } else {
      setFieldValid(true);
    }
    setBackground(isFieldValid() ? _normalBg : Color.RED);
  }

  @Override
  public void setBackground(Color bg) {
    if(isFieldValid() && _initYet != null) {
      _normalBg = bg;
      super.setBackground(bg);
    } else {
      super.setBackground(bg);
    }
  }

  @Override
  public boolean isFieldValid() {
    return _fieldValid;
  }

  public void setFieldValid(boolean b) {
    _fieldValid = b;
  }

  public String getNoneOption() {
    return _noneOption;
  }

  public void setNoneOption(String string) {
    _noneOption = string;
  }

  public boolean isMandatory() {
    return _mandatory;
  }

  public void setMandatory(boolean b) {
    _mandatory = b;
  }

  public IService getService() {
    return _service;
  }

}
