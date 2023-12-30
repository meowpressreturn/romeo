package romeo.xfactors.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import romeo.ui.forms.FieldDef;
import romeo.ui.forms.RomeoForm;
import romeo.xfactors.api.IExpressionParser;
import romeo.xfactors.api.IXFactorService;

public class XFactorForm extends RomeoForm {
  
  public XFactorForm(IXFactorService xFactorService, IExpressionParser expressionParser) {
    Objects.requireNonNull(xFactorService, "xFactorService may not be null");
    Objects.requireNonNull(expressionParser, "expressionParser may not be null");
    setExpressionParser(expressionParser);
    
    setName("X-Factor");
    setFormLogic(new XFactorFormLogic(xFactorService));
    List<FieldDef> fields = new ArrayList<FieldDef>();
    
    //name
    FieldDef name = new FieldDef("name","Name");
    name.setMandatory(true);
    fields.add(name);
    setFields(fields);
    
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
  }
}
