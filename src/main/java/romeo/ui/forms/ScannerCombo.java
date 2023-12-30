package romeo.ui.forms;

import java.awt.Component;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.units.api.UnitId;

public class ScannerCombo extends AbstractRecordCombo implements ListCellRenderer<Object> {
  
  private int _defaultValue = 0;
  private DefaultListCellRenderer _renderer = new DefaultListCellRenderer();

  public ScannerCombo(IUnitService unitService) {
    this(unitService, 0);
  }
  
  public ScannerCombo(IUnitService unitService, int defaultValue) {
    super(unitService, null);
    _defaultValue = defaultValue;
    setRenderer(this);
  }
  
  @Override
  public String toString() {
    return "ScannerCombo@" + System.identityHashCode(this);
  }

  @Override
  protected List<IUnit> loadRecords() {
    IUnitService service = (IUnitService) getService();
    return service.getScanners();
  }

  public IUnit getScanner() {
    return (IUnit) getSelectedRecord();
  }

  public void setScanner(IUnit scanner) {
    if(scanner.getScanner() == 0) {
      throw new IllegalArgumentException("Not a scanner:" + scanner.getName());
    }
    setSelectedRecord(scanner);
  }

  public void setScannerById(UnitId unitId) {
    if(unitId == null) {
      setSelectedRecord(null);
    } else {
      IUnit scanner = ((IUnitService) getService()).getUnit(unitId);
      setScanner(scanner);
    }
  }

  /**
   * Sets the selected scanner to the first one of the specified range found or
   * to none if no unit with the specified scanner range can be found.
   * @param range
   */
  public void setScannerRange(int range) {
    setSelectedRecord(null);
    IUnitService service = (IUnitService) getService();
    List<IUnit> data = service.getUnits();
    Iterator<IUnit> i = data.iterator();
    find_scanner_loop: while(i.hasNext()) { //Iterate all the units and add to the list any that have a scanner range
      IUnit unit = i.next();
      if(unit.getScanner() == range) {
        setSelectedRecord(unit);
        break find_scanner_loop;
      }
    }
  }

  /**
   * Returns the range of the selected scanner, or if none is selected the
   * default scan range (visual only).
   * @return scannerRange
   */
  public int getScannerRange() {
    IUnit scanner = getScanner();
    return scanner == null ? _defaultValue : scanner.getScanner();
  }

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
      boolean cellHasFocus) {
    IUnit unit = (IUnit) value;
    String display;
    if(unit == null) {
      display = "(" + _defaultValue + ") Visual Only ";
    } else {
      display = "(" + unit.getScanner() + ") " + unit.getName();
    }
    return _renderer.getListCellRendererComponent(list, display, index, isSelected, cellHasFocus);
  }

}
