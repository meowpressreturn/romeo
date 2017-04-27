package romeo.ui.actions;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.StringUtils;

import romeo.Romeo;
import romeo.ui.AbstractRomeoAction;
import romeo.ui.GenericMap;
import romeo.ui.IRecordSelectionListener;
import romeo.ui.MainFrame;
import romeo.ui.NavigatorPanel;
import romeo.utils.GuiUtils;
import romeo.worlds.api.WorldAndHistory;
import romeo.worlds.ui.WorldMapLogic;
import romeo.worlds.ui.WorldNavigatorRecordSelectionListener;

/**
 * Find a World on the map. Will include owner name in the search to facilitate
 * finding a player's empire too. When a match is found that world will be set
 * as the origin and opened in the navigator panel. It will also attempt to
 * center the map on the world. The search logic will iterate over the
 * WorldHistoryStruct in the WorldsMap, so it is only searching the currently
 * displayed turn.
 */
public class FindWorldAction extends AbstractRomeoAction {
  IRecordSelectionListener _recordSelector;

  /**
   * Constructor
   * @param navigatorPanel
   */
  public FindWorldAction(NavigatorPanel navigatorPanel) {
    super();
    Objects.requireNonNull(navigatorPanel, "navigatorPanel must not be null");
    _recordSelector = new WorldNavigatorRecordSelectionListener(navigatorPanel);
    putValue(Action.NAME, "Find on Map");
    putValue(Action.LONG_DESCRIPTION, "Locate a World or Player on the map");
    putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/magnifier.gif"));
  }

  @Override
  protected void doActionPerformed(ActionEvent e) {
    //    PreferencesControls prefsCtrl = (PreferencesControls)Romeo.CONTEXT.getBean(
    //        "preferencesControls",PreferencesControls.class);
    //    JPanel prefsPanel = prefsCtrl.getPanel();
    //    _navigatorPanel.display(prefsPanel);\
    MainFrame mainFrame = Romeo.getMainFrame();
    String name = JOptionPane.showInputDialog(mainFrame, "Enter a world and/or player name", "Find on Map",
        JOptionPane.QUESTION_MESSAGE);
    if(name == null) {
      return;
    }
    name = name.trim().toUpperCase(Locale.US);
    if(name.isEmpty()) {
      return;
    }

    GenericMap map = mainFrame.getWorldsMap();
    WorldMapLogic ml = (WorldMapLogic) map.getLogic();
    Set<WorldAndHistory> data = ml.getData();
    WorldAndHistory bestMatch = null;
    int bestScore = 1;
    for(WorldAndHistory wh : data) {
      String worldName = wh.getWorld().getName() + " " + wh.getHistory().getOwner();
      int score = StringUtils.getFuzzyDistance(worldName, name, Locale.ENGLISH);
      if(score > bestScore) {
        bestScore = score;
        bestMatch = wh;
      }
    }
    if(bestMatch == null) {
      showNotFound(name);
    } else {
      showWorld(bestMatch);
    }
  }

  private void showNotFound(String name) {
    JOptionPane.showMessageDialog(Romeo.getMainFrame(), "World not found: " + name, "Find World",
        JOptionPane.ERROR_MESSAGE);
  }

  private void showWorld(WorldAndHistory whs) {
    MainFrame mainFrame = Romeo.getMainFrame();
    GenericMap map = mainFrame.getWorldsMap();
    map.setOrigin(whs);
    _recordSelector.recordSelected(whs);
    int mapX = map.getLogic().getObjectX(whs);
    int mapY = map.getLogic().getObjectY(whs);
    Point visibleCentre = new Point(mapX, mapY);
    map.setVisibleMapCentre(visibleCentre);
    mainFrame.setSelectedTab(MainFrame.TAB_NAME_MAP);
  }
}