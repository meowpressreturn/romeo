/*
 * UnitFormLogic.java
 * Created on Feb 6, 2006
 */
package romeo.units.ui;

import java.util.EventObject;
import java.util.Objects;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import romeo.model.api.IServiceListener;
import romeo.ui.forms.FieldDef;
import romeo.ui.forms.IFormLogic;
import romeo.ui.forms.RomeoForm;
import romeo.ui.forms.XFactorCombo;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.units.api.UnitId;
import romeo.units.impl.UnitImpl;
import romeo.utils.Convert;
import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.IXFactorService;
import romeo.xfactors.api.XFactorId;

public class UnitFormLogic implements IFormLogic, IServiceListener {
  private RomeoForm _form;
  private IUnit _unit;
  private IUnitService _unitService;
  private IXFactorService _xfactorService;
  
  public UnitFormLogic(IUnitService unitService, IXFactorService xFactorService) {
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
    _xfactorService = Objects.requireNonNull(xFactorService, "xFactorService may not be null");
  }

  @Override
  public void bind(RomeoForm form, Object record) {
    Objects.requireNonNull(_unitService, "_unitService must not be null");
    _form = form;
    loadWith((IUnit) record);
    _unitService.addListener(this);
  }

  @Override
  public void cancelChanges() {
    if(_unit.isNew()) {
      _form.close();
    } else {
      loadWith(_unitService.loadUnit(_unit.getId()));
    }
  }

  @Override
  public void deleteRecord() {
    if(!_unit.isNew()) {
      _unitService.deleteUnit(_unit.getId());
    } else {
      cancelChanges();
    }
  }

  public void loadWith(IUnit unit) {
    _unit = unit;
    if(_form != null) {
      String name = unit.getName();
      ((JTextField) _form.getEntryFields().get("name")).setText(name);
      ((JTextField) _form.getEntryFields().get("attacks")).setText(unit.getAttacks() + "");
      ((JTextField) _form.getEntryFields().get("offense")).setText(unit.getOffense() + "");
      ((JTextField) _form.getEntryFields().get("defense")).setText(unit.getDefense() + "");
      ((JTextField) _form.getEntryFields().get("pd")).setText(unit.getPd() + "");
      ((JTextField) _form.getEntryFields().get("speed")).setText(unit.getSpeed() + "");
      ((JTextField) _form.getEntryFields().get("carry")).setText(unit.getCarry() + "");
      ((JTextField) _form.getEntryFields().get("cost")).setText(unit.getCost() + "");
      ((JTextField) _form.getEntryFields().get("complexity")).setText(unit.getComplexity() + "");
      ((JTextField) _form.getEntryFields().get("scanner")).setText(unit.getScanner() + "");
      ((JTextField) _form.getEntryFields().get("license")).setText(unit.getLicense() + "");
      ((JTextField) _form.getEntryFields().get("acronym")).setText(unit.getAcronym());
      ((XFactorCombo) _form.getEntryFields().get("xfactor")).setXFactor(unit.getXFactor());

      //_form.setDirty(false);
      _form.setNew(_unit.getId() == null);

      if("".equals(name) || name == null) {
        _form.setName("Untitled Unit");
      } else {
        _form.setName(_unit.getId() == null ? "Create New Unit" : "Unit - " + name);
      }

      updateFirepower();

      //_form.dataChanged();
    }
  }

  @Override
  public void inputChanged() {
    updateFirepower();
  }

  protected void updateFirepower() {
    String attacksStr = ((JTextField) _form.getEntryFields().get("attacks")).getText();
    String offenseStr = ((JTextField) _form.getEntryFields().get("offense")).getText();
    String defenseStr = ((JTextField) _form.getEntryFields().get("defense")).getText();
    double firepower = UnitImpl.calcFirepower(Convert.toInt(attacksStr), Convert.toInt(offenseStr),
        Convert.toInt(defenseStr));

    JLabel firepowerLabel = (JLabel) _form.getEntryFields().get("firepower");
    firepowerLabel.setText(Convert.toStr(firepower, 2));
    firepowerLabel.repaint();
  }

  @Override
  public void saveChanges() {
    String name = ((JTextField) _form.getEntryFields().get("name")).getText();
    String attacksStr = ((JTextField) _form.getEntryFields().get("attacks")).getText();
    String offenseStr = ((JTextField) _form.getEntryFields().get("offense")).getText();
    String defenseStr = ((JTextField) _form.getEntryFields().get("defense")).getText();
    String pdStr = ((JTextField) _form.getEntryFields().get("pd")).getText();
    String speedStr = ((JTextField) _form.getEntryFields().get("speed")).getText();
    String carryStr = ((JTextField) _form.getEntryFields().get("carry")).getText();
    String costStr = ((JTextField) _form.getEntryFields().get("cost")).getText();
    String complexityStr = ((JTextField) _form.getEntryFields().get("complexity")).getText();
    String scannerStr = ((JTextField) _form.getEntryFields().get("scanner")).getText();
    String licenseStr = ((JTextField) _form.getEntryFields().get("license")).getText();
    String acronym = ((JTextField) _form.getEntryFields().get("acronym")).getText();
    IXFactor xf = (IXFactor) ((XFactorCombo) _form.getEntryFields().get("xfactor")).getXFactor();
    XFactorId xfactor = (xf == null) ? null : xf.getId();

    UnitImpl unit = new UnitImpl(
        _unit.getId(),  //would be null for a new record
        name, 
        Convert.toInt(attacksStr),
        Convert.toInt(offenseStr), 
        Convert.toInt(defenseStr), 
        Convert.toInt(pdStr), 
        Convert.toInt(speedStr), 
        Convert.toInt(carryStr),
        Convert.toInt(costStr), 
        Convert.toInt(complexityStr),
        Convert.toInt(scannerStr),
        Convert.toInt(licenseStr),
        acronym, 
        xfactor);
    UnitId id = _unitService.saveUnit(unit);
    loadWith( new UnitImpl(id, unit) );
  }

  public void setUnitService(IUnitService service) {
    _unitService = service;
  }

  @Override
  public void dataChanged(EventObject event) {
    if(_unit.getId() != null && !_form.isDirty()) { //Only load fresh data if user hadnt started an edit operation
      IUnit reloaded = _unitService.loadUnit(_unit.getId());
      if(reloaded == null) { //It was deleted 
        loadWith(new UnitImpl(null, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "", null));
        _form.setDirty(true);
      } else { //Load fresh data into the form
        loadWith(reloaded);
      }
    }
  }

  @Override
  public void dispose() {
    _unitService.removeListener(this);
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
