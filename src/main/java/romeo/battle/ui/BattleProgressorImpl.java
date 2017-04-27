/*
 * DefaultBattleProgressor.java
 * Created on Mar 1, 2006
 */
package romeo.battle.ui;

import java.awt.Component;

import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import romeo.battle.IBattleCalculator;
import romeo.battle.IBattleMetrics;
import romeo.ui.NavigatorPanel;
import romeo.utils.GuiUtils;

/**
 * Displays and updates the simulation progress using a Swing progress monitor
 * via callbacks from the battle calculator.
 */
public class BattleProgressorImpl implements IBattleCalculator.IProgressor {
  private ProgressMonitor _pm;
  private NavigatorPanel _navigator;
  private Component _parent;
  private IBattleCalculator _calculator;

  public BattleProgressorImpl(Component parent, IBattleCalculator calculator) {
    _parent = parent;
    _calculator = calculator;
    _calculator.setProgressor(this);
  }

  public void executeCalculator() {
    _pm = new ProgressMonitor(_parent, "Simulating Battles", "", 0, _calculator.getNumberOfBattles());
    _pm.setMillisToDecideToPopup(250);
    _pm.setMillisToPopup(500);
    Thread t = new Thread(_calculator);
    t.start();
  }

  @Override
  public void complete(final IBattleMetrics metrics) { //Load up the results in the navigator pane
    SwingUtilities.invokeLater(new Runnable() //changes to ui must occur in event dispatching thread
    {
      @Override
      public void run() {
        _pm.close();
        if(_navigator != null && metrics != null) {
          Report report = new Report(metrics);
          _navigator.display(report);
        }
      }
    });
    System.gc();
  }

  @Override
  public boolean setCurrentBattle(int progress) {
    return GuiUtils.updateProgressMonitor(_pm, progress, "" + progress);
  }

  public void setNavigator(NavigatorPanel panel) {
    _navigator = panel;
  }

}
