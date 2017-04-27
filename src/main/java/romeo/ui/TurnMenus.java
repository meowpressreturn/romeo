package romeo.ui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import romeo.model.api.IServiceListener;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.utils.GuiUtils;
import romeo.worlds.api.IWorldService;

/**
 * Manages the items and actions for the Turn menu TODO - extract common impl
 * details between this and TurnControls
 */
public class TurnMenus implements IServiceListener {
  protected int _currentTurn = -1;
  protected int _maxTurn = -1;
  protected ISettingsService _settingsService;
  protected IWorldService _worldService;
  protected JMenu _menu;
  protected JMenuItem _firstItem;
  protected JMenuItem _prevItem;
  protected JMenuItem _nextItem;
  protected JMenuItem _lastItem;

  public TurnMenus(ISettingsService settingsService, IWorldService worldService) {
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
    _worldService = Objects.requireNonNull(worldService, "worldService must not be null");
    _settingsService.addListener(this);

    _menu = new JMenu();
    _menu.setText("Turn");
    _menu.setMnemonic('t');

    Action firstAction = new AbstractRomeoAction() {
      @Override
      public void doActionPerformed(ActionEvent e) {
        _settingsService.setLong(ISettings.CURRENT_TURN, 1);
        updateControls();
      }
    };
    firstAction.putValue(Action.NAME, "First");
    firstAction.putValue(Action.SHORT_DESCRIPTION, "First Turn");
    firstAction.putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/first.gif"));
    _firstItem = _menu.add(firstAction);
    _firstItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));
    _firstItem.setText("First (1)");

    Action prevAction = new AbstractRomeoAction() {
      @Override
      public void doActionPerformed(ActionEvent e) {
        if(_currentTurn > 1) {
          _settingsService.setLong(ISettings.CURRENT_TURN, _currentTurn - 1);
        }
        updateControls();
      }
    };
    prevAction.putValue(Action.NAME, "Previous");
    prevAction.putValue(Action.SHORT_DESCRIPTION, "Previous Turn");
    prevAction.putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/back.gif"));
    _prevItem = _menu.add(prevAction);
    _prevItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, ActionEvent.CTRL_MASK));

    Action nextAction = new AbstractRomeoAction() {
      @Override
      public void doActionPerformed(ActionEvent e) {
        _settingsService.setLong(ISettings.CURRENT_TURN, _currentTurn + 1);
        updateControls();
      }
    };
    nextAction.putValue(Action.NAME, "Next");
    nextAction.putValue(Action.SHORT_DESCRIPTION, "Next Turn");
    nextAction.putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/forward.gif"));
    _nextItem = _menu.add(nextAction);
    _nextItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD, ActionEvent.CTRL_MASK));

    Action lastAction = new AbstractRomeoAction() {
      @Override
      public void doActionPerformed(ActionEvent e) {
        _settingsService.setLong(ISettings.CURRENT_TURN, _maxTurn);
        updateControls();
      }
    };
    lastAction.putValue(Action.NAME, "Last");
    lastAction.putValue(Action.SHORT_DESCRIPTION, "Last Turn");
    lastAction.putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/last.gif"));
    _lastItem = _menu.add(lastAction);
    _lastItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));

    updateControls();
  }

  public void dispose() {
    _settingsService.removeListener(this);
  }

  public JMenu getMenu() {
    return _menu;
  }

  protected void updateControls() {
    _currentTurn = (int) _settingsService.getLong(ISettings.CURRENT_TURN);
    _maxTurn = _worldService.getMapInfo().getMaxTurn();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        _prevItem.setEnabled(_currentTurn > 1);
        _prevItem.setText((_currentTurn <= 1) ? "Previous (n/a)" : "Previous (" + (_currentTurn - 1) + ")");
        _nextItem.setText("Next (" + (_currentTurn + 1) + ")");
        _firstItem.setEnabled(_currentTurn > 1);
        _lastItem.setEnabled((_currentTurn != _maxTurn) && (_maxTurn != 0));
        _lastItem.setText((_maxTurn == 0) ? "Last (n/a)" : "Last (" + _maxTurn + ")");
        _menu.setText("Turn (" + _currentTurn + ")");
      }
    });
  }

  @Override
  public void dataChanged(EventObject event) {
    if(event instanceof SettingChangedEvent) {
      SettingChangedEvent sce = (SettingChangedEvent) event;
      if(ISettings.CURRENT_TURN.equals(sce.getName())) {
        updateControls();
      }
    }

  }
}
