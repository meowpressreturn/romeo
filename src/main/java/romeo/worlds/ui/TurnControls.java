package romeo.worlds.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.Objects;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;

import romeo.model.api.IServiceListener;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.ui.AbstractRomeoAction;
import romeo.utils.GuiUtils;
import romeo.worlds.api.IWorldService;

public class TurnControls implements IServiceListener {
  protected ISettingsService _settingsService;
  protected IWorldService _worldService;
  protected JButton _firstButton;
  protected JButton _prevButton;
  protected JButton _nextButton;
  protected JButton _lastButton;
  protected JLabel _turnLabel;
  protected long _currentTurn = -1;
  protected int _maxTurn;

  public TurnControls(Logger log, ISettingsService settingsService, IWorldService worldService) {
    Objects.requireNonNull(settingsService, "settingsService must not be null");
    Objects.requireNonNull(worldService, "worldService must not be null");
    _settingsService = settingsService;
    _worldService = worldService;

    settingsService.addListener(this);
    worldService.addListener(this);

    Dimension buttonSize = new Dimension(24, 24);

    Action firstAction = new AbstractRomeoAction(log) {
      
      @Override
      public void doActionPerformed(ActionEvent e) {
        _settingsService.setLong(ISettings.CURRENT_TURN, 1);
        updateControls();
      }
    };
    firstAction.putValue(Action.NAME, "First");
    firstAction.putValue(Action.SHORT_DESCRIPTION, "First Turn");
    firstAction.putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/first.gif"));
    _firstButton = new JButton(firstAction);
    _firstButton.setHideActionText(true);
    _firstButton.setPreferredSize(buttonSize);
    _firstButton.setMinimumSize(buttonSize);
    _firstButton.setMaximumSize(buttonSize);

    Action prevAction = new AbstractRomeoAction(log) {
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
    _prevButton = new JButton(prevAction);
    _prevButton.setHideActionText(true);
    _prevButton.setPreferredSize(buttonSize);
    _prevButton.setMinimumSize(buttonSize);
    _prevButton.setMaximumSize(buttonSize);

    Action nextAction = new AbstractRomeoAction(log) {
      @Override
      public void doActionPerformed(ActionEvent e) {
        _settingsService.setLong(ISettings.CURRENT_TURN, _currentTurn + 1);
        updateControls();
      }
    };
    nextAction.putValue(Action.NAME, "Next");
    nextAction.putValue(Action.SHORT_DESCRIPTION, "Next Turn");
    nextAction.putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/forward.gif"));
    _nextButton = new JButton(nextAction);
    _nextButton.setHideActionText(true);
    _nextButton.setPreferredSize(buttonSize);
    _nextButton.setMinimumSize(buttonSize);
    _nextButton.setMaximumSize(buttonSize);

    Action lastAction = new AbstractRomeoAction(log) {
      @Override
      public void doActionPerformed(ActionEvent e) {
        _settingsService.setLong(ISettings.CURRENT_TURN, _maxTurn);
        updateControls();
      }
    };
    lastAction.putValue(Action.NAME, "Last");
    lastAction.putValue(Action.SHORT_DESCRIPTION, "Last Turn");
    lastAction.putValue(Action.SMALL_ICON, GuiUtils.getImageIcon("/images/last.gif"));
    _lastButton = new JButton(lastAction);
    _lastButton.setHideActionText(true);
    _lastButton.setPreferredSize(buttonSize);
    _lastButton.setMinimumSize(buttonSize);
    _lastButton.setMaximumSize(buttonSize);

    _turnLabel = new JLabel("");
    _turnLabel.setFont(new Font("Dialog", Font.BOLD, 16));

    updateControls();
  }

  /**
   * Update the label text to reflect the current turn. Enable/disable buttons
   * if reached the bounds of the history. This will be done on the awt event
   * thread.
   */
  protected void updateControls() {
    _currentTurn = _settingsService.getLong(ISettings.CURRENT_TURN);
    _maxTurn = _worldService.getMapInfo().getMaxTurn();
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        _prevButton.setEnabled(_currentTurn > 1);
        _firstButton.setEnabled(_currentTurn > 1);
        _lastButton.setEnabled((_currentTurn != _maxTurn) && (_maxTurn != 0));
        _lastButton.setToolTipText((_maxTurn == 0) ? "N/A" : "Turn " + _maxTurn);
        _turnLabel.setText(String.format("Turn %02d", _currentTurn));
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
    } else if(event.getSource() instanceof IWorldService) {
      updateControls();
    }
  }

  public JComponent getFirstButton() {
    return _firstButton;
  }

  public JComponent getLastButton() {
    return _lastButton;
  }

  public JComponent getPrevButton() {
    return _prevButton;
  }

  public JComponent getNextButton() {
    return _nextButton;
  }

  public JComponent getTurnLabel() {
    return _turnLabel;
  }
}
