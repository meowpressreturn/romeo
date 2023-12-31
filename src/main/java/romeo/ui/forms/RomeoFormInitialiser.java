package romeo.ui.forms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import romeo.players.api.IPlayerService;
import romeo.ui.forms.RomeoForm.Components;
import romeo.units.api.IUnitService;
import romeo.utils.GuiUtils;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IXFactorService;

/**
 * Helper class that RomeoForm uses to build itself. This class knows how to create the swing components
 * for the form based on the field definitions and it wraps references to the various service dependencies 
 * those fields might have.
 */
public class RomeoFormInitialiser {

  private final IPlayerService _playerService;
  private final IXFactorService _xFactorService;
  private final IUnitService _unitService;
  private final IExpressionParser _expressionParser;

  public RomeoFormInitialiser(
      IPlayerService playerService, 
      IXFactorService xFactorService,
      IUnitService unitService,
      IExpressionParser expressionParser) {
    _playerService = Objects.requireNonNull(playerService, "playerService may not be null");
    _xFactorService = Objects.requireNonNull(xFactorService, "xFactorService may not be null");
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
    _expressionParser = Objects.requireNonNull(expressionParser, "expressionParser may not be null");
  }
  
  public Components createFields(
      RomeoForm form, 
      List<FieldDef> fields, 
      IFormLogic logic, 
      boolean forceTwoColumns, 
      Object record,
      boolean isNewRecord) {
    Objects.requireNonNull(fields, "fields may not be null");
    Objects.requireNonNull(logic, "logic may not be null");
    boolean useTwoCols = forceTwoColumns || fields.size() > 8;

    Map<String,JComponent> entryFields = new TreeMap<String, JComponent>();
    JComponent firstField = null;
    JButton saveButton;
    JButton cancelButton;
    JButton deleteButton;
    
    GridBagConstraints gbc = GuiUtils.prepGridBag(form);
    gbc.anchor = GridBagConstraints.NORTH;

    //An 'oddfield' is one that uses the second column
    boolean oddField = true; //first one at zero will be false
    int y = 0;
    for(int i = 0; i < fields.size(); i++) {
      FieldDef field = (FieldDef) fields.get(i);

      oddField = !oddField;
      y = useTwoCols ? (oddField ? y : y + 1) : y + 1; //should that be +1 not i??    

      if(field.isWide() && oddField && useTwoCols) { 
        //If this field is wide, so we cant place it in the second column
        //as we would otherwise do, so we must skip to the start of the next row
        y++;
        oddField = false;
      }

      gbc.gridx = oddField && useTwoCols ? 2 : 0;
      gbc.gridy = y;
      JLabel label = new JLabel(field.getLabel());      
      form.add(label, gbc);

      gbc.gridx = oddField && useTwoCols ? 3 : 1;

      switch (field.getType()){
        case FieldDef.TYPE_INT: {
          JTextField entryField = new RNumericField();
          if(field.isMandatory())
            entryField.setBackground(RomeoForm.MANDATORY_COLOR);
          entryField.setText((String) field.getDefaultValue());
          entryField.setColumns(10);
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);
          //entryField.getDocument().addDocumentListener(this);
          entryField.getDocument().addDocumentListener( new DocumentListeningFieldChangeNotifier(form) );
          NumericFieldConstraint constraint = (NumericFieldConstraint) field.getDetails();
          if(constraint != null) {
            ((NumericDocument) entryField.getDocument()).setConstraint(constraint);
          }
        } break;

        case FieldDef.TYPE_DOUBLE: {
          JTextField entryField = new RNumericField();
          if(field.isMandatory())
            entryField.setBackground(RomeoForm.MANDATORY_COLOR);

          entryField.setColumns(10);
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);

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
          entryField.getDocument().addDocumentListener( new DocumentListeningFieldChangeNotifier(form) );
        } break;

        case FieldDef.TYPE_TEXT: {
          JTextField entryField = new JTextField();
          if(field.isMandatory())
            entryField.setBackground(RomeoForm.MANDATORY_COLOR);
          entryField.setText((String) field.getDefaultValue());
          entryField.setColumns(10); //will give room for x+several chars. pathetic swing api
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);
          //entryField.getDocument().addDocumentListener(this);
          entryField.getDocument().addDocumentListener( new DocumentListeningFieldChangeNotifier(form) );
        } break;

        case FieldDef.TYPE_LABEL:
        case FieldDef.TYPE_FILLER: {
          JLabel entryField = new JLabel();
          entryField.setPreferredSize(new Dimension(140, 24));
          if(field.getType() == FieldDef.TYPE_LABEL) {
            entryField.setOpaque(true);
            entryField.setBackground(Color.LIGHT_GRAY);
          }
          entryField.setText((String) field.getDefaultValue());
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);
        } break;

        case FieldDef.TYPE_LONG_TEXT:
        case FieldDef.TYPE_EXPRESSION: {
          JTextArea entryField;
          boolean isExpr = false;
          if(field.getType() == FieldDef.TYPE_EXPRESSION) {
            entryField = new ExpressionField(_expressionParser);
            isExpr = true;
            int size = entryField.getFont().getSize();
            Font f = new Font("Monospaced", Font.PLAIN, size);
            entryField.setFont(f);
          } else {
            entryField = new JTextArea();
          }
          if(field.isMandatory())
            entryField.setBackground(RomeoForm.MANDATORY_COLOR);
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
          form.add(entryFieldScroll, gbc);
          entryFields.put(field.getName(), entryField);
          entryField.getDocument().addDocumentListener( new DocumentListeningFieldChangeNotifier(form) ); //todo - notify from expr validator
          gbc.gridwidth = 1;
          gbc.weightx = 1;
        } break;

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
            entryField.setBackground(RomeoForm.MANDATORY_COLOR);
          entryField.setPreferredSize(new Dimension(140, 24));
          Object defaultValue = field.getDefaultValue();
          if(defaultValue != null) {
            entryField.setSelectedItem(defaultValue);
          }
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);
          entryField.addItemListener(form);
        } break;

        case FieldDef.TYPE_XFACTOR_COMBO: {
          if(_xFactorService ==null) throw new IllegalStateException("No X-Factor service provided to form");
          XFactorCombo entryField = new XFactorCombo(_xFactorService);
          entryField.setMandatory(field.isMandatory());
          entryField.setPreferredSize(new Dimension(140, 24));
          Object defaultValue = field.getDefaultValue();
          if(defaultValue != null) {
            entryField.setSelectedItem(defaultValue);
          }
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);
          entryField.addItemListener(form);
        } break;

        case FieldDef.TYPE_COLOR: {
          ColorButton entryField = new ColorButton();
          entryField.setPreferredSize(new Dimension(128, 24));
          Object defColor = field.getDefaultValue();
          if(defColor instanceof String) {
            entryField.setColorText((String) defColor);
          } else if(defColor instanceof Color) {
            entryField.setColor((Color) defColor);
          }
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);
          entryField.addFieldChangeListener(form);
        } break;

        case FieldDef.TYPE_SCANNER_COMBO: {
          Number defaultRange = (Number) field.getDefaultValue();
          if(_unitService==null) throw new IllegalStateException("No Unit Service provided to form");
          ScannerCombo entryField 
              = (defaultRange == null) 
              ? new ScannerCombo(_unitService)
              : new ScannerCombo(_unitService, defaultRange.intValue());
          entryField.setMandatory(field.isMandatory());
          entryField.setPreferredSize(new Dimension(140, 24));
          Object defaultValue = field.getDefaultValue();
          if(defaultValue != null) {
            entryField.setSelectedItem(defaultValue);
          }
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);
          entryField.addItemListener(form);
        } break;

          
        case FieldDef.TYPE_PLAYER_COMBO: {
          if(_playerService==null) throw new IllegalStateException("No player service provided to form");
          PlayerCombo entryField = new PlayerCombo(_playerService);
          entryField.setMandatory(field.isMandatory());
          entryField.setPreferredSize(new Dimension(140, 24));
          Object defaultValue = field.getDefaultValue();
          if(defaultValue != null) {
            //entryField.setSelectedItem(defaultValue);  
          }
          form.add(entryField, gbc);
          entryFields.put(field.getName(), entryField);
          entryField.addActionListener(form);
        } break;

        case FieldDef.TYPE_CUSTOM: {
          JComponent entryField = logic.initCustom(form, field);
          if(entryField != null) {
            if(useTwoCols) {
              gbc.gridwidth = 3;
            }
            form.add(entryField, gbc);
            entryFields.put(field.getName(), entryField);
          }
        } break;

        default:
          throw new IllegalStateException("Bad field type " + field.getType() + " for " + field.getName());
      }

      if((field.getType() == FieldDef.TYPE_LONG_TEXT || field.isWide()) && !oddField && useTwoCols) { //Since the field was marked as 'wide', it also used the second so we shall consider it odd
        oddField = true;
      }

    }

    if(fields.size() > 0) {
      String firstName = ((FieldDef) fields.get(0)).getName();
      firstField = (JTextField) entryFields.get(firstName);
    }

    gbc.gridy = y + 1;
    gbc.gridx = 0;
    gbc.gridwidth = useTwoCols ? 4 : 2;
    JPanel buttonPanel = new JPanel();
    form.add(buttonPanel, gbc);

    buttonPanel.setLayout(new GridBagLayout());
    GridBagConstraints bGbc = new GridBagConstraints();
    bGbc.insets = new Insets(4, 4, 2, 2);

    bGbc.gridx = 0;
    bGbc.gridy = 0;
    saveButton = new JButton(isNewRecord ? "Create" : "Save");
    saveButton.setIcon(GuiUtils.getImageIcon("/images/tick.gif"));
    saveButton.addActionListener(form);
    buttonPanel.add(saveButton, bGbc);

    bGbc.gridx = 1;
    cancelButton = new JButton("Cancel");
    cancelButton.setIcon(GuiUtils.getImageIcon("/images/cross.gif"));
    cancelButton.addActionListener(form);
    buttonPanel.add(cancelButton, bGbc);

    bGbc.gridx = 1;
    bGbc.gridy = 1;
    deleteButton = new JButton("Delete");
    deleteButton.setIcon(GuiUtils.getImageIcon("/images/circleBar.gif"));
    deleteButton.addActionListener(form);
    buttonPanel.add(deleteButton, bGbc);

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
    form.add(fill, gbc);
    
    return new Components(entryFields, firstField, saveButton, cancelButton, deleteButton);
  }
  
}
