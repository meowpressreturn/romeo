package romeo.xfactors.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.LoggerFactory;

import romeo.ui.forms.FieldDef;
import romeo.ui.forms.RomeoFormInitialiser;
import romeo.xfactors.api.IXFactor;
import romeo.xfactors.api.IXFactorService;

public class XFactorFormFactory {
  
  private final RomeoFormInitialiser _initialiser;
  private final IXFactorService _xFactorService;
  
  public XFactorFormFactory(
      RomeoFormInitialiser initialiser, 
      IXFactorService xFactorService) {
    _initialiser = Objects.requireNonNull(initialiser, "initialiser may not be null");
    _xFactorService = Objects.requireNonNull(xFactorService, "xFactorService may not be null");
  }

  public XFactorForm newXFactorForm(IXFactor record, boolean isNewRecord) {
    return new XFactorForm(
        LoggerFactory.getLogger(XFactorForm.class),
        _initialiser, 
        fields(), 
        logic(), 
        record, 
        isNewRecord);
  }
  
  private XFactorFormLogic logic() {
    return new XFactorFormLogic(_xFactorService);
  }
  
  private List<FieldDef> fields() {
    List<FieldDef> fields = new ArrayList<FieldDef>();
    
    //name
    FieldDef name = new FieldDef("name","Name");
    name.setMandatory(true);
    fields.add(name);
    
    //description
    fields.add(new FieldDef("description","Description"));
    
    //trigger
    fields.add(new FieldDef("trigger","Trigger", FieldDef.TYPE_EXPRESSION));
    
    //xfAttacks
    fields.add(new FieldDef("xfAttacks","Attacks", FieldDef.TYPE_EXPRESSION));
    
    //xfOffence
    fields.add(new FieldDef("xfOffense","Offense", FieldDef.TYPE_EXPRESSION));
    
    //xfDefense
    fields.add(new FieldDef("xfDefense","Defense", FieldDef.TYPE_EXPRESSION));
    
    //xfPd
    fields.add(new FieldDef("xfPd","PD", FieldDef.TYPE_EXPRESSION));
    
    //xfRemove
    fields.add(new FieldDef("xfRemove","Destruct", FieldDef.TYPE_EXPRESSION));
    
    return fields;
  }
}
