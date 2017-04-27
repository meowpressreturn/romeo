package romeo.ui.forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import romeo.ui.BeanTableModel;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.utils.Convert;

public class UnitTypesSelectorModel {
  public class Element {
    public int _start;
    public int _length;
    public IUnit _unit;

    public int getLength() {
      return _length;
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

    public void setStart(int i) {
      _start = i;
    }

    public void setUnit(IUnit unit) {
      _unit = unit;
    }
  }

  protected class UnitTypesSelectorDocument extends PlainDocument {
    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
      super.insertString(offs, str, a);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          UnitTypesSelectorModel.this.update();
        }
      });
    }

    @Override
    protected void removeUpdate(DefaultDocumentEvent chng) { //these seem to come one behind whats actually going in the document!
                                                               //using su inv later means we update on an up to date data however
      super.removeUpdate(chng);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          UnitTypesSelectorModel.this.update();
        }
      });
    }

    @Override
    protected void insertUpdate(DefaultDocumentEvent chng, AttributeSet attr) {
      super.insertUpdate(chng, attr);
      SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
          UnitTypesSelectorModel.this.update();
        }
      });
    }
  }

  //..............................................

  protected BeanTableModel _tableModel;
  protected IUnitService _unitService;
  protected List<IFieldChangeListener> _fieldChangeListeners = new ArrayList<IFieldChangeListener>(1);
  protected List<Element> _elements = new ArrayList<Element>();
  protected UnitTypesSelectorDocument _document = new UnitTypesSelectorDocument();

  public UnitTypesSelectorModel(IUnitService unitService) {
    _unitService = unitService;
    BeanTableModel.ColumnDef[] columns = new BeanTableModel.ColumnDef[] { new BeanTableModel.ColumnDef("name", "Unit"),
        new BeanTableModel.ColumnDef("attacks", "Atk"), new BeanTableModel.ColumnDef("offense", "Off"),
        new BeanTableModel.ColumnDef("defense", "Def"), new BeanTableModel.ColumnDef("firepower", "Fp"), };
    _tableModel = new BeanTableModel(columns, Collections.emptyList());
  }

  public List<Element> getElements() {
    return _elements;
  }

  public void update() {
    try {
      //some refactoring could be used here. We gen the selected list several times!
      String unitsText = _document.getText(0, _document.getLength());
      _elements = parseText(unitsText);
      _tableModel.setData(getSelectedTypes());
      notifyFieldChangeListeners();
    } catch(Exception e) {
      throw new RuntimeException("Error in update()", e);
    }
  }

  public Document getDocument() {
    return _document;
  }

  public TableModel getTableModel() {
    return _tableModel;
  }

  protected List<Element> parseText(String fleetString) {
    ArrayList<Element> elements = new ArrayList<Element>();
    int index = 0;
    int nextDelim = fleetString.indexOf(',');
    nextDelim = nextDelim == -1 ? fleetString.length() : nextDelim;
    while(nextDelim != -1 && index < fleetString.length()) {
      String token = fleetString.substring(index, nextDelim);
      Element element = new Element();
      element._start = index;
      element._length = token.length();
      token = token.trim();
      token = Convert.replace(token, "\n", "");

      try {
        IUnit unit = _unitService.getByAcronym(token);
        if(unit == null)
          throw new IllegalArgumentException("Bad acronym:" + token);
        element._unit = unit;
      } catch(Exception e) {
        //Ignore. 
      }

      elements.add(element);
      index = nextDelim + 1;
      nextDelim = index == fleetString.length() ? -1 : fleetString.indexOf(',', index + 1);
      if(nextDelim == -1)
        nextDelim = fleetString.length();
    }
    return elements;
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
    for(IFieldChangeListener listener : listeners) {
      listener.valueChanged(this);
    }
  }

  /**
   * Returns a list of the types of units that are in the field. Types listed
   * multiple times in the text field are only reported once in this list. Other
   * than that, the units in the list appear in the order they appear in the
   * elements list.
   * @return types A List of Unit instances
   */
  public List<IUnit> getSelectedTypes() {
    List<Element> elements = getElements();
    ArrayList<IUnit> types = new ArrayList<IUnit>(elements.size());
    for(Element element : elements) {
      IUnit unit = element.getUnit();
      if(unit != null && types.contains(unit) == false) {
        types.add(unit);
      }
    }
    return types;
  }

  public String makeNormalisedText() {
    List<IUnit> types = getSelectedTypes();
    Iterator<IUnit> i = types.iterator();
    StringBuffer buffer = new StringBuffer();
    while(i.hasNext()) {
      IUnit unitType = i.next();
      buffer.append(unitType.getAcronym());
      if(i.hasNext()) {
        buffer.append(", ");
      }
    }
    return buffer.toString();
  }

  public void addUnit(IUnit unit) {
    Iterator<Element> i = _elements.iterator();
    while(i.hasNext()) {
      Element element = (Element) i.next();
      if(unit == element.getUnit())
        return;
    }
    Element e = new Element();
    e.setUnit(unit);
    _elements.add(e);
  }

  public void removeUnit(IUnit unit) {
    ListIterator<Element> i = _elements.listIterator();
    while(i.hasNext()) {
      Element element = (Element) i.next();
      if(unit == element.getUnit()) {
        i.remove();
      }
    }
  }
}
