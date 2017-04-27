package romeo.ui.forms;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.Romeo;
import romeo.persistence.DuplicateRecordException;
import romeo.ui.ErrorDialog;
import romeo.ui.NavigatorPanel;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;
import romeo.utils.INamed;

public class RomeoForm extends JPanel
    implements ActionListener, ItemListener, IFieldChangeListener, INamed {
  
  protected static final Color MANDATORY_COLOR = new Color(255, 255, 176);

  ////////////////////////////////////////////////////////////////////////////

  private List<FieldDef> _fields = Collections.emptyList();
  private Map<String, JComponent> _entryFields;
  private String _name = "Untitled";
  private boolean _dirty = true;
  private boolean _new = true;
  private JButton _saveButton;
  private JButton _cancelButton;
  private JButton _deleteButton;
  private IFormLogic _formLogic;
  private JComponent _firstField;
  private boolean _dataValid = true;
  private boolean _forceTwoColumns = false;
  private boolean _bindingInProgess = false;

  public RomeoForm() {
    super();
  }

  public void setFields(List<FieldDef> fields) {
    if(fields == null) {
      fields = Collections.emptyList();
    }
    _fields = fields;
  }

  public List<FieldDef> getFields() {
    return _fields;
  }

  /**
   * Returns whether or not to lay the form out using two columns instead of
   * one. If the forceTwoColumns flag is set this will always return true
   * otherwise it depends on the number of fields.
   * @return useTwoColumns
   */
  protected boolean isUseTwoColumns() {
    return isForceTwoColumns() || _fields.size() > 8;
  }

  public boolean isForceTwoColumns() {
    return _forceTwoColumns;
  }

  public void setForceTwoColumns(boolean forceTwoColumns) {
    _forceTwoColumns = forceTwoColumns;
  }

  public void initialise(Object record) {
    Objects.requireNonNull(_formLogic, "_formLogic must not be null");

    _entryFields = new TreeMap<String, JComponent>();
    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.anchor = GridBagConstraints.NORTH;

    boolean useTwoCols = isUseTwoColumns();

    //An 'oddfield' is one that uses the second column
    boolean oddField = true; //first one at zero will be false
    int y = 0;
    for(int i = 0; i < _fields.size(); i++) {
      FieldDef field = (FieldDef) _fields.get(i);

      oddField = !oddField;
      y = useTwoCols ? (oddField ? y : y + 1) : y + 1; //should that be +1 not i??    

      if(field.isWide() && oddField && useTwoCols) { //If this field is wide, so we cant place it in the second column
                                                       //as we would otherwise do, so we must skip to the start of the next row
        y++;
        oddField = false;
      }

      gbc.gridx = oddField && useTwoCols ? 2 : 0;
      gbc.gridy = y;
      JLabel label = new JLabel(field.getLabel());      
      add(label, gbc);

      gbc.gridx = oddField && useTwoCols ? 3 : 1;

      switch (field.getType()){
        case FieldDef.TYPE_INT: {
          JTextField entryField = new RNumericField();
          if(field.isMandatory())
            entryField.setBackground(MANDATORY_COLOR);
          entryField.setText((String) field.getDefaultValue());
          entryField.setColumns(10);
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);
          //entryField.getDocument().addDocumentListener(this);
          entryField.getDocument().addDocumentListener( new DocumentListeningFieldChangeNotifier(this) );
          NumericFieldConstraint constraint = (NumericFieldConstraint) field.getDetails();
          if(constraint != null) {
            ((NumericDocument) entryField.getDocument()).setConstraint(constraint);
          }
        }
          break;

        case FieldDef.TYPE_DOUBLE: {
          JTextField entryField = new RNumericField();
          if(field.isMandatory())
            entryField.setBackground(MANDATORY_COLOR);

          entryField.setColumns(10);
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);

          NumericFieldConstraint constraint = (NumericFieldConstraint) field.getDetails();
          if(constraint != null) {
            //nb: if supplying own constraint must manually set allowDecimal
            ((NumericDocument) entryField.getDocument()).setConstraint(constraint);
          } else {
            constraint = new NumericFieldConstraint();
            constraint.setAllowDecimal(true);
            ((NumericDocument) entryField.getDocument()).setConstraint(constraint);
          }
          //Do this last
          entryField.setText((String) field.getDefaultValue());
          //entryField.getDocument().addDocumentListener(this);
          entryField.getDocument().addDocumentListener( new DocumentListeningFieldChangeNotifier(this) );
        }
          break;

        case FieldDef.TYPE_TEXT: {
          JTextField entryField = new JTextField();
          if(field.isMandatory())
            entryField.setBackground(MANDATORY_COLOR);
          entryField.setText((String) field.getDefaultValue());
          entryField.setColumns(10); //will give room for x+several chars. pathetic swing api
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);
          //entryField.getDocument().addDocumentListener(this);
          entryField.getDocument().addDocumentListener( new DocumentListeningFieldChangeNotifier(this) );
        }
          break;

        case FieldDef.TYPE_LABEL:
        case FieldDef.TYPE_FILLER: {
          JLabel entryField = new JLabel();
          entryField.setPreferredSize(new Dimension(140, 24));
          if(field.getType() == FieldDef.TYPE_LABEL) {
            entryField.setOpaque(true);
            entryField.setBackground(Color.LIGHT_GRAY);
          }
          entryField.setText((String) field.getDefaultValue());
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);
        }
          break;

        case FieldDef.TYPE_LONG_TEXT:
        case FieldDef.TYPE_EXPRESSION: {
          JTextArea entryField;
          boolean isExpr = false;
          if(field.getType() == FieldDef.TYPE_EXPRESSION) {
            entryField = Romeo.CONTEXT.createExpressionField();
            isExpr = true;
            int size = entryField.getFont().getSize();
            Font f = new Font("Monospaced", Font.PLAIN, size);
            entryField.setFont(f);
          } else {
            entryField = new JTextArea();
          }
          if(field.isMandatory())
            entryField.setBackground(MANDATORY_COLOR);
          entryField.setTabSize(2);
          entryField.setText((String) field.getDefaultValue());
          entryField.setLineWrap(!isExpr);
          entryField.setWrapStyleWord(false);
          if(useTwoCols) {
            gbc.gridwidth = 3;
          }
          gbc.weightx = 2;
          JScrollPane entryFieldScroll = new JScrollPane(entryField);
          entryFieldScroll.setVerticalScrollBarPolicy(
              isExpr ? JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED : JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
          entryFieldScroll.setPreferredSize(new Dimension(useTwoCols ? 240 : 275, isExpr ? 128 : 64));
          add(entryFieldScroll, gbc);
          _entryFields.put(field.getName(), entryField);
          //entryField.getDocument().addDocumentListener(this); //TODO - replace with own listener triggered from the validation listener
          entryField.getDocument().addDocumentListener( new DocumentListeningFieldChangeNotifier(this) ); //todo - notify from expr validator
          gbc.gridwidth = 1;
          gbc.weightx = 1;
        }
          break;

        case FieldDef.TYPE_COMBO: {
          Object details = field.getDetails();
          List<?> options = Collections.emptyList();
          if(details instanceof IOptionSource) {
            options = ((IOptionSource) details).getOptions(field);
          } else if(details instanceof List) {
            options = (List<?>) details;
          }
          JComboBox<?> entryField = new JComboBox<Object>(options.toArray());
          if(field.isMandatory())
            entryField.setBackground(MANDATORY_COLOR);
          entryField.setPreferredSize(new Dimension(140, 24));
          Object defaultValue = field.getDefaultValue();
          if(defaultValue != null) {
            entryField.setSelectedItem(defaultValue);
          }
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);
          entryField.addItemListener(this);
        }
          break;

        case FieldDef.TYPE_XFACTOR_COMBO: {
          XFactorCombo entryField = new XFactorCombo();
          entryField.setMandatory(field.isMandatory());
          entryField.setPreferredSize(new Dimension(140, 24));
          Object defaultValue = field.getDefaultValue();
          if(defaultValue != null) {
            entryField.setSelectedItem(defaultValue);
          }
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);
          entryField.addItemListener(this);
        }
          break;

        case FieldDef.TYPE_COLOR: {
          ColorButton entryField = new ColorButton();
          entryField.setPreferredSize(new Dimension(128, 24));
          Object defColor = field.getDefaultValue();
          if(defColor instanceof String) {
            entryField.setColorText((String) defColor);
          } else if(defColor instanceof Color) {
            entryField.setColor((Color) defColor);
          }
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);
          entryField.addFieldChangeListener(this);
        }
          break;

        case FieldDef.TYPE_SCANNER_COMBO: {
          Number defaultRange = (Number) field.getDefaultValue();
          ScannerCombo entryField = (defaultRange == null) ? new ScannerCombo()
              : new ScannerCombo(defaultRange.intValue());
          entryField.setMandatory(field.isMandatory());
          entryField.setPreferredSize(new Dimension(140, 24));
          Object defaultValue = field.getDefaultValue();
          if(defaultValue != null) {
            entryField.setSelectedItem(defaultValue);
          }
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);
          entryField.addItemListener(this);
        }
          break;

        case FieldDef.TYPE_PLAYER_COMBO: {
          PlayerCombo entryField = new PlayerCombo();
          entryField.setMandatory(field.isMandatory());
          entryField.setPreferredSize(new Dimension(140, 24));
          Object defaultValue = field.getDefaultValue();
          if(defaultValue != null) {
            //entryField.setSelectedItem(defaultValue);  
          }
          add(entryField, gbc);
          _entryFields.put(field.getName(), entryField);
          entryField.addActionListener(this);
        }
          break;

        case FieldDef.TYPE_CUSTOM: {
          JComponent entryField = _formLogic.initCustom(field);
          if(entryField != null) {
            if(useTwoCols) {
              gbc.gridwidth = 3;
            }
            add(entryField, gbc);
            _entryFields.put(field.getName(), entryField);
          }
        }
          break;

        default:
          throw new IllegalStateException("Bad field type " + field.getType() + " for " + field.getName());
      }

      //      if(field.getType() == FieldDef.TYPE_LONG_TEXT && !oddField && useTwoCols)
      //      { 
      //        oddField = true;
      //      }
      if((field.getType() == FieldDef.TYPE_LONG_TEXT || field.isWide()) && !oddField && useTwoCols) { //Since the field was marked as 'wide', it also used the second so we shall consider it odd
        oddField = true;
      }

    }

    if(_fields.size() > 0) {
      String firstName = ((FieldDef) _fields.get(0)).getName();
      _firstField = (JTextField) _entryFields.get(firstName);
    }

    gbc.gridy = y + 1;
    gbc.gridx = 0;
    gbc.gridwidth = useTwoCols ? 4 : 2;
    JPanel buttonPanel = new JPanel();
    add(buttonPanel, gbc);

    buttonPanel.setLayout(new GridBagLayout());
    GridBagConstraints bGbc = new GridBagConstraints();
    bGbc.insets = new Insets(4, 4, 2, 2);

    bGbc.gridx = 0;
    bGbc.gridy = 0;
    _saveButton = new JButton(_new ? "Create" : "Save");
    _saveButton.setIcon(GuiUtils.getImageIcon("/images/tick.gif"));
    _saveButton.addActionListener(this);
    buttonPanel.add(_saveButton, bGbc);

    bGbc.gridx = 1;
    _cancelButton = new JButton("Cancel");
    _cancelButton.setIcon(GuiUtils.getImageIcon("/images/cross.gif"));
    _cancelButton.addActionListener(this);
    buttonPanel.add(_cancelButton, bGbc);

    bGbc.gridx = 1;
    bGbc.gridy = 1;
    _deleteButton = new JButton("Delete");
    _deleteButton.setIcon(GuiUtils.getImageIcon("/images/circleBar.gif"));
    _deleteButton.addActionListener(this);
    buttonPanel.add(_deleteButton, bGbc);

    JLabel bGlue = new JLabel("");
    bGbc.gridx = 2;
    bGbc.weightx = 2;
    buttonPanel.add(bGlue, bGbc);

    JLabel fill = new JLabel("");
    //glue.setText("fill"); glue.setOpaque(true); glue.setBackground(Color.CYAN);
    gbc.gridx = useTwoCols ? 4 : 2;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridy++;
    gbc.weightx = 1;
    gbc.weighty = 2;
    add(fill, gbc);

    _bindingInProgess = true;
    _formLogic.bind(this, record);  
    afterBinding();
  }
  
  /**
   * To be called after a record is bound to clear the dirty flag and
   * clear the bindingInProgress flag.
   */
  private void afterBinding() {
    SwingUtilities.invokeLater(new Runnable() {      
      @Override
      public void run() {
        setDirty(false);
        _bindingInProgess = false;
        dataChanged();        
      }
    });
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
    if(_entryFields == null) {
      throw new NullPointerException("Form not initialised, entryFields therefore null");
    }
    return _entryFields;
  }

  public void setFormLogic(IFormLogic logic) {
    _formLogic = logic;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Log log = LogFactory.getLog(this.getClass());
    log.trace("in actionPerformed(), actionEvent=" + e);
    try {
      if(e.getSource() == _saveButton) {
        _bindingInProgess = true;
        _formLogic.saveChanges();
        afterBinding();
      } else if(e.getSource() == _cancelButton) {
        _bindingInProgess = true;
        _formLogic.cancelChanges();
        afterBinding();
      } else if(e.getSource() == _deleteButton) {
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
      log.error("Duplicate Record Exception:" + dre.getMessage() );
    } catch(Exception ex) {
      ErrorDialog dialog = new ErrorDialog("Form action error", ex, false);
      dialog.show();
      log.error("Form action error", ex);
    }
    updateButtons();
  }

  protected void updateButtons() {
    
    LogFactory.getLog(this.getClass()).trace("updateButtons()");
    
    _formLogic.inputChanged();
    updateValidity();
    _saveButton.setText(isNew() ? "Create" : "Save");
    _saveButton.setEnabled(isDirty() && isDataValid());
    _cancelButton.setEnabled(isDirty());
    _deleteButton.setEnabled(!isNew());
    _deleteButton.setVisible(!isNew());
  }

  protected void updateValidity() {
    Log log = LogFactory.getLog(this.getClass());
    log.trace("Checking validity of fields in form at " + new Date() );
    Iterator<FieldDef> i = _fields.iterator();
    while(i.hasNext()) {
      FieldDef def = (FieldDef) i.next();
      String fieldName = def.getName();
      Object field = _entryFields.get(fieldName);
      if(field != null) {
        if(field instanceof IValidatingField) {
          boolean fieldValid = ((IValidatingField) field).isFieldValid();
          log.trace("Field '" + fieldName + "' valid=" +  fieldValid);
          if(!fieldValid) {
            setDataValid(false);
            log.trace("Form fails validation");
            return; //short circuit return if we find any that are invalid
          }
        }
        if(def.isMandatory()) {
          if(field instanceof JTextComponent) {
            String text = ((JTextComponent) field).getText();
            text = text.trim();
            log.trace("Mandatory field '" + fieldName + "' trimmed value=\"" + text + "\""); 
            if(text.isEmpty()) {
              setDataValid(false);
              log.trace("Form fails validation");
              return; //short circuit return if any mandatory field is empty
            }
          }
          //For now just assume a combobox is always ok as some element will always be selected
        }
      }
    }
    log.trace("Form is valid as no invalid fields found");
    setDataValid(true);
  }

  public boolean isDirty() {
    return _dirty;
  }

  public boolean isNew() {
    return _new;
  }

  public void setDirty(boolean b) {
    _dirty = b;
  }

  public void setNew(boolean b) {
    _new = b;
  }

  @Override
  public void itemStateChanged(ItemEvent e) {
    valueChanged(e.getSource());
  }

  @Override
  public void valueChanged(Object field) {
    LogFactory.getLog(this.getClass()).trace("valueChanged() _bindingInProgress=" + _bindingInProgess + ", field=" + field);
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
    Objects.requireNonNull(navigatorPanel, "navigatorPanel should not be null here");
    navigatorPanel.close();
  }

  protected NavigatorPanel getNavigatorPanel() {
    NavigatorPanel navPanel = (NavigatorPanel) GuiUtils.getAncestorOfType(NavigatorPanel.class, this);
    return navPanel;
  }

  public JComponent getFocusField() { //only focus the field if creating new record. Its a nuisance otherwise!
    return isNew() ? _firstField : null;
  }

  public JButton getSaveButton() {
    return _saveButton;
  }

  public JButton getCancelButton() {
    return _cancelButton;
  }

  public JButton getDeleteButton() {
    return _deleteButton;
  }

  public void formClosing() {
    _formLogic.dispose();
  }

  public boolean isDataValid() {
    return _dataValid;
  }

  public void setDataValid(boolean b) {
    _dataValid = b;
  }

}
