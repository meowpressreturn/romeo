package romeo.fleet.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.apache.commons.logging.LogFactory;

import romeo.battle.impl.RoundContext;
import romeo.battle.ui.BattleFleetsManager;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.ui.BeanTableModel;
import romeo.ui.forms.IFieldChangeListener;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.utils.Convert;
import romeo.xfactors.api.IXFactorCompiler;

/**
 * Model for the {@link FleetField} component.
 */
public class FleetFieldModel {
  /**
   * Symbol used to prefix a flag in the fleet text
   */
  protected static final String FLAG_SYMBOL = "-";

  public class FleetModelElement {
    private int _start;
    private int _length;
    private IUnit _unit;
    private int _quantity;
    private int _sourceId;
    private String _flag;
    private boolean _valid = false;

    @Override
    public String toString() {
      return _flag == null ? _sourceId + ":" + _quantity + "*" + _unit : "!" + _flag;
    }

    public int getLength() {
      return _length;
    }

    public int getQuantity() {
      return _quantity;
    }

    public int getStart() {
      return _start;
    }

    public IUnit getUnit() {
      return _unit;
    }

    public void setLength(int i) {
      _length = i;
    }

    public void setQuantity(int i) {
      _quantity = i;
    }

    public void setStart(int i) {
      _start = i;
    }

    public void setUnit(IUnit unit) {
      _unit = unit;
    }

    public int getSourceId() {
      return _sourceId;
    }

    public void setSourceId(int i) {
      _sourceId = i;
    }

    public String getFlag() {
      return _flag;
    }

    public void setFlag(String flag) {
      _flag = flag;
    }

    public boolean isValid() {
      return _valid;
    }

    public void setValid(boolean valid) {
      _valid = valid;
    }

  }

  protected class FleetDocument extends PlainDocument {
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
      super.insertString(offs, str, a);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          FleetFieldModel.this.update(true);
        }
      });
    }

    @Override
    protected void removeUpdate(DefaultDocumentEvent chng) { //these seem to come one behind whats actually going on in the document!
      super.removeUpdate(chng);
      //We have to update our stuff after swing has finished all its stuff for
      //this event, as although we have been informed about the event now and
      //although we have called our superclass, the text in the document
      //has NOT been removed yet. 
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          FleetFieldModel.this.update(true);
        }
      });
    }

    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
      super.insertUpdate(chng, attr);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          FleetFieldModel.this.update(true);
        }
      });
    }
  }

  /////////////////////////////////////////////////////////////////////////////

  private FleetDocument _document;
  private List<IFieldChangeListener> _fieldChangeListeners;
  private IUnitService _unitService;
  private List<FleetFieldModel.FleetModelElement> _elements;
  private BeanTableModel _tableModel;
  private FleetContents _fleetContents = new FleetContents();
  private double _totalFirepower;
  private double _totalFirepowerRaw;
  private int _totalPd;
  private int _totalCarry;
  private boolean _defender = false;
  private IXFactorCompiler _compiler;
  private FleetField _field;
  private BattleFleetsManager _battleFleetsManager;

  //.........................................................

  public FleetFieldModel(FleetField field, IUnitService unitService, IXFactorCompiler compiler) {
    _field = field;
    _compiler = compiler;
    _unitService = unitService;
    _fieldChangeListeners = new ArrayList<IFieldChangeListener>(1);
    _document = new FleetDocument();
    BeanTableModel.IColumnProcessor statsProc = new BeanTableModel.IColumnProcessor() {
      @Override
      public Object processValue(Object value, int row, int col, BeanTableModel.ColumnDef colDef) {
        if(value instanceof FleetElement) {
          FleetElement element = (FleetElement) value;
          return element.getAttacks() + "*" + element.getOffense() + "/" + element.getDefense();
        } else {
          return colDef.processValue(value, row, col, colDef);
        }
      }
    };
    BeanTableModel.IColumnProcessor noFracProc = new BeanTableModel.IColumnProcessor() {
      @Override
      public Object processValue(Object value, int row, int col, BeanTableModel.ColumnDef colDef) {
        if(value instanceof Number) {
          int valueInt = ((Number) value).intValue();
          return new Integer(valueInt);
        } else {
          return colDef.processValue(value, row, col, colDef);
        }
      }
    };
    BeanTableModel.ColumnDef[] columns = new BeanTableModel.ColumnDef[] { new BeanTableModel.ColumnDef("xfActive", "X"),
        new BeanTableModel.ColumnDef("unit.name", "Unit"), new BeanTableModel.ColumnDef(null, "A*O/D", statsProc),
        new BeanTableModel.ColumnDef("unitFirepower", "uFP"), new BeanTableModel.ColumnDef("firepower", "tFP"),
        new BeanTableModel.ColumnDef("carry", "tCA", noFracProc), new BeanTableModel.ColumnDef("source", "Src"),
        new BeanTableModel.ColumnDef("quantity", "Qty", noFracProc), };
    _tableModel = new BeanTableModel(columns, Collections.emptyList());
  }

  public void addFieldChangeListener(IFieldChangeListener listener) {
    if(!_fieldChangeListeners.contains(listener)) {
      _fieldChangeListeners.add(listener);
    }
  }

  public void removeFieldChangeListener(IFieldChangeListener listener) {
    _fieldChangeListeners.remove(listener);
  }

  protected void notifyFieldChangeListeners() {
    ArrayList<IFieldChangeListener> listeners = new ArrayList<IFieldChangeListener>(_fieldChangeListeners);
    for(int i = 0; i < listeners.size(); i++) {
      ((IFieldChangeListener) listeners.get(i)).valueChanged(this);
    }
  }

  public TableModel getTableModel() {
    return _tableModel;
  }

  public Document getDocument() {
    return _document;
  }

  public List<FleetFieldModel.FleetModelElement> getElements() {
    if(_elements == null) {
      List<FleetFieldModel.FleetModelElement> l = Collections.emptyList();
      return l;
    }
    return _elements;
  }

  public FleetContents getFleetContents() {
    return _fleetContents;
  }

  protected String getFieldText() {
    try {
      return _document.getText(0, _document.getLength());
    } catch(Exception e) {
      throw new RuntimeException("Error getting fleet text", e);
    }
  }

  /**
   * Update the field contents by parsing the text and evaluating xFactors
   * @param cascade
   *          cascade the update to other fields in the
   *          {@link BattleFleetsManager}
   */
  public synchronized void update(boolean cascade) {
    try {
      BattleFleetsManager bfm = getBattleFleetsManager();

      String fleetText = getFieldText();
      _elements = parseFleet(fleetText);
      updateFleetContents();

      String playerName = _field.getName();
      String[] playerNames = bfm == null ? new String[] { playerName } : bfm.getNames();

      //Calculate fp

      RoundContext rc = new RoundContext(playerNames);
      if(bfm == null) {
        rc.setFleet(playerName, _fleetContents);
      } else { //If we have a BFM then set refs to all the opposing fleets too
        for(int p = playerNames.length - 1; p >= 0; p--) {
          String thatName = playerNames[p];
          FleetField f = (FleetField) bfm.getFleetField(thatName);
          if(cascade && !playerName.equals(thatName)) { //Also invoke update of foreign fields
            f.getModel().update(false);
          }
          FleetContents fc = f.getModel().getFleetContents();
          rc.setFleet(thatName, fc);
        }
      }

      rc.setThisPlayer(playerName);
      if(isDefender()) {
        rc.setDefendingPlayer(playerName);
      } else {
        rc.setDefendingPlayer(bfm == null ? null : bfm.getDefenderName());
      }

      if(_compiler != null) {
        _fleetContents.compileXFactors(_compiler);
        _fleetContents.evaluateXFactors(rc);
        _totalFirepower = _fleetContents.getFirepower();
        _totalFirepowerRaw = _fleetContents.getFirepower(true);
        _totalPd = _fleetContents.getFleetPd();
        _totalCarry = (int) _fleetContents.getCarry();
      } else {
        LogFactory.getLog(this.getClass()).error("No xfactor compiler");
      }
      //....

      _tableModel.setData(new ArrayList<FleetElement>(_fleetContents.getElements()));

      notifyFieldChangeListeners();
    } catch(Exception e) {
      throw new RuntimeException("Error updating model", e);
    }
  }

  public double getTotalFirepower(boolean raw) {
    return raw ? _totalFirepowerRaw : _totalFirepower;
  }

  public double getTotalFirepower() {
    return getTotalFirepower(false);
  }

  public double getTotalCarry() {
    return _totalCarry;
  }

  protected void updateFleetContents() {
    List<FleetModelElement> elements = getElements();
    Iterator<FleetModelElement> i = elements.iterator();
    FleetContents contents = new FleetContents();
    while(i.hasNext()) {
      FleetModelElement fmElement = (FleetModelElement) i.next();
      IUnit unit = fmElement.getUnit();
      int qty = fmElement.getQuantity();
      if(unit != null && qty > 0) {
        FleetElement fcElement = new FleetElement(unit, qty, fmElement.getSourceId());
        contents.addElement(fcElement);
      }
      String flag = fmElement.getFlag();
      if(flag != null) {
        contents.setFlag(flag, true);
      }
    }
    _fleetContents = contents;
  }

  protected String makeNormalisedText() {
    boolean needComma = false;
    _fleetContents.sort();
    StringBuffer buffer = new StringBuffer();
    Iterator<String> flagIterator = _fleetContents.getFlags();
    Iterator<FleetElement> elementIterator = _fleetContents.iterator();
    while(flagIterator.hasNext()) {
      if(needComma) {
        buffer.append(", ");
      }
      buffer.append(FLAG_SYMBOL);
      buffer.append(flagIterator.next());
      needComma = true;
    }
    while(elementIterator.hasNext()) {
      FleetElement element = elementIterator.next();
      IUnit unit = element.getUnit();
      int qty = (int) element.getQuantity();
      if(qty > 0) {
        if(needComma) {
          buffer.append(", ");
        }
        int sourceId = element.getSource();
        if(sourceId != 0) {
          buffer.append(sourceId);
          buffer.append(':');
        }
        buffer.append(qty);
        buffer.append(" * ");
        buffer.append(unit.getAcronym());
        needComma = true;
      }
    }
    return buffer.toString();
  }

  protected List<FleetModelElement> parseFleet(String fleetString) {
    ArrayList<FleetModelElement> elements = new ArrayList<FleetModelElement>();
    int index = 0;
    int nextDelim = fleetString.indexOf(',');
    nextDelim = nextDelim == -1 ? fleetString.length() : nextDelim;
    while(nextDelim != -1 && index < fleetString.length()) {
      String token = fleetString.substring(index, nextDelim);
      FleetModelElement element = new FleetModelElement();
      element.setStart(index);
      element.setLength(token.length());
      token = token.trim();
      token = Convert.replace(token, "\n", "");

      try {
        if(token.startsWith(FLAG_SYMBOL)) { //Flag token
          if(token.length() == 1)
            throw new IllegalArgumentException("Flag text not specified");
          String flag = token.substring(1).toUpperCase(Locale.US);
          element.setFlag(flag);
          element.setValid(true);
        } else { //Normal qty * acronym token

          //if(token.length()==0) break; //skip ,,
          int star = token.indexOf('*');
          if(star == -1)
            throw new IllegalArgumentException("Missing '*' in token:" + token);
          String acronym = token.substring(star + 1).trim(); // on the right
          IUnit unit = _unitService.getByAcronym(acronym);
          if(unit == null)
            throw new IllegalArgumentException("Bad acronym:" + acronym);
          String qtyStr = token.substring(0, star).trim(); //on the left

          //Here to parse sourceId
          int sourceId = 0;
          String sourceStr = null;
          int colonIndex = qtyStr.indexOf(':');
          if(colonIndex != -1) {
            sourceStr = qtyStr.substring(0, colonIndex);
            if(qtyStr.length() - 1 == colonIndex) {
              throw new RuntimeException("Missing quantity in " + token);
            }
            qtyStr = qtyStr.substring(colonIndex + 1, qtyStr.length());
            try {
              sourceId = Integer.parseInt(sourceStr);
            } catch(Exception e) {
              throw new NumberFormatException("Bad sourceId:" + sourceStr);
            }
          }

          int qtyInt = 0;
          try {
            qtyInt = Integer.parseInt(qtyStr);
          } catch(Exception e) {
            throw new NumberFormatException("Cannot convert quantity to int:" + qtyStr);
          }
          element.setUnit(unit);
          element.setQuantity(qtyInt);
          element.setSourceId(sourceId);
          element.setValid(true);
        }
      } catch(Exception e) { //Mark the element as invalid. We dont try to report the message.
        element.setValid(false);
      }

      elements.add(element);
      index = nextDelim + 1;
      nextDelim = index == fleetString.length() ? -1 : fleetString.indexOf(',', index + 1);
      if(nextDelim == -1)
        nextDelim = fleetString.length();
    }
    return elements;
  }

  public boolean isDefender() {
    return _defender;
  }

  public void setDefender(boolean b) {
    _defender = b;
  }

  public int getTotalPd() {
    return _totalPd;
  }

  public BattleFleetsManager getBattleFleetsManager() {
    return _battleFleetsManager;
  }

  /**
   * Optionally provide a reference to a BattleFleetsManager. This allows the
   * XFactors to be evaluated taking opposing fleets into account when updating
   * the display.
   * @param battleFleetsManager
   */
  public void setBattleFleetsManager(BattleFleetsManager battleFleetsManager) {
    _battleFleetsManager = battleFleetsManager;
  }

}
