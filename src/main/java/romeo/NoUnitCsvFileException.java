package romeo;

import romeo.units.impl.UnitServiceInitialiser;

public class NoUnitCsvFileException extends ApplicationException {

  public static final String TXT_WHERE_GOT_HELP = 
      "At the time of writing, UltraCorps provides a 'csv' download link"
      +" at the foot of the Units Help Page when you open the help page from within a game. "
      + " By default it names the file 'unit.csv'.";
  
  ////////////////////////////////////////////////////////////////////////////
  
  private boolean _showErrorDialog;
  
  public NoUnitCsvFileException(boolean showErrorDialog) {
    super("Required file " + UnitServiceInitialiser.UNITS_FILE_RESOURCE_PATH
        + " is missing.\nPlease download the unit.csv file for your game from the UltraCorps site and add it to the resources folder "
        + " or select it when prompted at Romeo startup. (" + TXT_WHERE_GOT_HELP + ")");
    _showErrorDialog = showErrorDialog;
  }
  
  public boolean isShowErrorDialog() {
    return _showErrorDialog;
  }

}



















