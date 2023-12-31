package romeo.units.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import romeo.ui.forms.FieldDef;
import romeo.ui.forms.NumericFieldConstraint;
import romeo.ui.forms.RomeoForm;
import romeo.ui.forms.RomeoFormInitialiser;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.xfactors.api.IXFactorService;

public class UnitFormFactory {
  
  private final RomeoFormInitialiser _initialiser;
  private final IUnitService _unitService;
  private final IXFactorService _xFactorService;
  
  public UnitFormFactory(
      RomeoFormInitialiser initialiser,
      IUnitService unitService, 
      IXFactorService xFactorService) {
    _initialiser = Objects.requireNonNull(initialiser, "initialiser may not be null");
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
    _xFactorService = Objects.requireNonNull(xFactorService, "xFactorService may not be null");
  }
  
  public RomeoForm createUnitForm(IUnit record, boolean isNewRecord) {    
    return new UnitForm(_initialiser, fields(), logic(), record, isNewRecord);
  }
  
  private UnitFormLogic logic() {
    return new UnitFormLogic(_unitService, _xFactorService);
  }
  
  private List<FieldDef> fields() {
    List<FieldDef> fields = new ArrayList<FieldDef>();

    //name
    fields.add(new FieldDef("name","Name"));
    
    //firepower
    FieldDef firepower = new FieldDef("firepower", "Firepower", FieldDef.TYPE_LABEL);
    firepower.setDefaultValue("0");
    fields.add(firepower);
    
    //acronym
    FieldDef acronym = new FieldDef("acronym","Acronym");
    acronym.setMandatory(true);
    fields.add(acronym);
    
    //empty1
    fields.add(new FieldDef("empty1","", FieldDef.TYPE_CUSTOM));
    
    //attacks
    FieldDef attacks = new FieldDef("attacks","Attacks",FieldDef.TYPE_INT);
    NumericFieldConstraint attacksDetails = new NumericFieldConstraint();
    attacksDetails.setNegativeAllowed(false);
    attacks.setDetails(attacksDetails);
    fields.add(attacks);
    
    //offense
    FieldDef offense = new FieldDef("offense","Offense", FieldDef.TYPE_INT);
    NumericFieldConstraint offenseDetails = new NumericFieldConstraint();
    offenseDetails.setNegativeAllowed(false);
    offenseDetails.setMaxValue(100);
    offense.setDetails(offenseDetails);
    fields.add(offense);
    
    //defense
    FieldDef defense = new FieldDef("defense","Defense", FieldDef.TYPE_INT);
    NumericFieldConstraint defenseDetails = new NumericFieldConstraint();
    defenseDetails.setNegativeAllowed(false);
    defenseDetails.setMaxValue(100);
    defense.setDetails(defenseDetails);
    fields.add(defense);
    
    //pd
    FieldDef pd = new FieldDef("pd","Pop Damage", FieldDef.TYPE_INT);
    NumericFieldConstraint pdDetails = new NumericFieldConstraint();
    pdDetails.setNegativeAllowed(false);
    pd.setDetails(pdDetails);
    fields.add(pd);
    
    //speed
    FieldDef speed = new FieldDef("speed","Speed", FieldDef.TYPE_INT);
    NumericFieldConstraint speedDetails = new NumericFieldConstraint();
    speedDetails.setNegativeAllowed(false);
    speed.setDetails(speedDetails);
    fields.add(speed);
    
    //carry
    fields.add(new FieldDef("carry","Carry", FieldDef.TYPE_INT));
    
    //cost
    FieldDef cost = new FieldDef("cost","Cost", FieldDef.TYPE_INT);
    NumericFieldConstraint costDetails = new NumericFieldConstraint();
    costDetails.setNegativeAllowed(false);
    cost.setDetails(costDetails);
    fields.add(cost);
    
    //complexity
    FieldDef complexity = new FieldDef("complexity","Complexity", FieldDef.TYPE_INT);
    NumericFieldConstraint complexityDetails = new NumericFieldConstraint();
    complexityDetails.setNegativeAllowed(false);
    complexity.setDetails(complexityDetails);
    fields.add(complexity);
    
    //license
    FieldDef license = new FieldDef("license","License", FieldDef.TYPE_INT);
    NumericFieldConstraint licenseDetails = new NumericFieldConstraint();
    licenseDetails.setNegativeAllowed(false);
    license.setDetails(licenseDetails);
    fields.add(license);
    
    //scanner
    FieldDef scanner = new FieldDef("scanner","Scanner", FieldDef.TYPE_INT);
    NumericFieldConstraint scannerDetails = new NumericFieldConstraint();
    scannerDetails.setNegativeAllowed(false);
    scanner.setDetails(scannerDetails);
    fields.add(scanner);
    
    //xfactor
    fields.add( new FieldDef("xfactor","X-Factor", FieldDef.TYPE_XFACTOR_COMBO) );
    
    return fields;
  }
}
