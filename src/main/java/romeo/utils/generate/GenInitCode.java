package romeo.utils.generate;

import java.util.List;

import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.utils.Convert;

/**
 * Pumps out some source code to paste back into initialisers with the
 * appropriate calls and sql to init the db. (Quick and dirty tool for use by
 * developers, not actually invoked by Romeo)
 */
public class GenInitCode {
  protected static String prepString(String value) {
    if(value == null) {
      return "NULL";
    } else {
      return "'" + Convert.replace(value, "'", "''") + "'";
    }
  }

  protected IUnitService _unitService;

  /**
   * Constructor.
   * @param unitService
   */
  public GenInitCode(IUnitService unitService) {
    if(unitService == null) {
      throw new NullPointerException("unitService is null");
    }
    _unitService = unitService;
  }

  public synchronized void run() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("//..............................................\n\n");
    // System.out.println("getting units");
    List<IUnit> units = _unitService.getUnits();
    // System.out.println("units=" + units);
    int c = units.size();
    // System.out.println("size=" + c);
    int index = 0;
    //using an iterator results in deadlock
    while(index < c) {
      //   System.out.println("get " + index);
      IUnit unit = (IUnit) units.get(index++);
      buffer.append("\t\tDbUtils.writeQuery(\"INSERT INTO UNITS ");
      buffer.append("(id,name,acronym,attacks,offense,defense,pd,");
      buffer.append("speed,carry,cost,complexity,scanner,license)");
      buffer.append("VALUES ('\"\n\t\t\t+ keyGen.createIdKey() +\"',");
      buffer.append(prepString(unit.getName()) + ",");
      buffer.append(prepString(unit.getAcronym().toString()) + ",");
      buffer.append(unit.getAttacks() + ",");
      buffer.append(unit.getOffense() + ",");
      buffer.append(unit.getDefense() + ",");
      buffer.append(unit.getPd() + ",\"\n\t\t\t+\"");
      buffer.append(unit.getSpeed() + ",");
      buffer.append(unit.getCarry() + ",\"\n\t\t\t+\"");
      buffer.append(unit.getCost() + ",");
      buffer.append(unit.getComplexity() + ",");
      buffer.append(unit.getScanner() + ",");
      buffer.append(unit.getLicense());
      buffer.append(");\",connection);");
      buffer.append("\n\n");
    }
    buffer.append("//..............................................\n");
    System.out.println(buffer.toString());
  }
}
