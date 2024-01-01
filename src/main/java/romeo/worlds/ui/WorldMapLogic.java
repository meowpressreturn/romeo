package romeo.worlds.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collections;
import java.util.EventObject;
import java.util.Objects;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import romeo.model.api.IServiceListener;
import romeo.model.api.MapInfo;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.settings.api.ISettings;
import romeo.settings.api.ISettingsService;
import romeo.settings.impl.SettingChangedEvent;
import romeo.ui.AbstractMapLogic;
import romeo.ui.GenericMap;
import romeo.ui.GenericMap.IObjectRenderer;
import romeo.ui.GenericMap.PositionStruct;
import romeo.units.api.IUnitService;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;
import romeo.worlds.api.IHistory;
import romeo.worlds.api.IWorld;
import romeo.worlds.api.IWorldService;
import romeo.worlds.api.WorldAndHistory;
import romeo.worlds.api.WorldId;

public class WorldMapLogic extends AbstractMapLogic implements GenericMap.IObjectRenderer, IServiceListener {
  protected static final Stroke SIMPLE_LINE_STROKE = new BasicStroke(1.0f);
  protected static final Stroke DEFCON5_STROKE = new BasicStroke(0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER,
      10.0f, new float[] { 2, 3, 2 }, 0f);
  protected static final Stroke DEFCON4_STROKE = new BasicStroke(1.0f);
  protected static final Stroke DEFCON3_STROKE = new BasicStroke(2.0f);
  protected static final Stroke DEFCON2_STROKE = new BasicStroke(3.0f);
  protected static final Stroke DEFCON1_STROKE = new BasicStroke(4.0f);

  private final Logger _log;
  
  //todo - make below private, and final where possible
  protected IWorldService _worldService;
  protected IUnitService _unitService;
  protected ISettingsService _settingsService;
  //protected List<WorldAndHistory> _worldData; //Current turns world data
  protected Set<WorldAndHistory> _worldData; //Current turns world data
  protected MapInfo _mapInfo; //map dimensions in time and space

  protected JComboBox<Integer> _fleetSpeedField;
  protected JCheckBox _showOwner;
  protected JCheckBox _showScanner;
  protected JCheckBox _showRange;
  protected JCheckBox _showName;
  protected JCheckBox _showFirepower;
  protected JCheckBox _showDeltas;
  protected JCheckBox _showLabour;
  protected JCheckBox _showCapital;
  protected int _fleetSpeed = 0;
  protected int _currentTurn = -1;

  protected long _fClass;
  protected long _eClass;
  protected long _dClass;
  protected long _cClass;
  protected long _bClass;
  protected long _aClass;

  protected long _defcon5;
  protected long _defcon4;
  protected long _defcon3;
  protected long _defcon2;

  public WorldMapLogic(
    Logger log,
    IWorldService worldService,
    IUnitService unitService,
    ISettingsService settingsService,
    IPlayerService playerService) {
    _log = Objects.requireNonNull(log, "log may not be null");
    _worldService = Objects.requireNonNull(worldService, "worldService must not be null");
    _unitService = Objects.requireNonNull(unitService, "unitService must not be null");
    _settingsService = Objects.requireNonNull(settingsService, "settingsService must not be null");
    Objects.requireNonNull(playerService, "playerService must not be null");
    _worldService.addListener(this);
    _unitService.addListener(this);
    _settingsService.addListener(this);
    playerService.addListener(this);
    loadDisplayPrefs();
  }

  protected void loadDisplayPrefs() {
    _currentTurn = (int) _settingsService.getLong(ISettings.CURRENT_TURN);
    _fleetSpeed = (int) _settingsService.getLong(ISettings.MAP_SPEED);

    _fClass = _settingsService.getLong(ISettings.F_CLASS_WORLD);
    _eClass = _settingsService.getLong(ISettings.E_CLASS_WORLD);
    _dClass = _settingsService.getLong(ISettings.D_CLASS_WORLD);
    _cClass = _settingsService.getLong(ISettings.C_CLASS_WORLD);
    _bClass = _settingsService.getLong(ISettings.B_CLASS_WORLD);
    _aClass = _settingsService.getLong(ISettings.A_CLASS_WORLD);

    _defcon5 = _settingsService.getLong(ISettings.DEFCON_5);
    _defcon4 = _settingsService.getLong(ISettings.DEFCON_4);
    _defcon3 = _settingsService.getLong(ISettings.DEFCON_3);
    _defcon2 = _settingsService.getLong(ISettings.DEFCON_2);
  }

  @Override
  public JComponent supplyControls(GenericMap map) {

    ItemListener itemListener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            updateSpeed();
          }
        });
      }
    };

    //nb: this code assumes this logic instance is only used with one GenericMap
    JPanel panel = new JPanel();

    double zoom = _settingsService.getDouble(ISettings.MAP_ZOOM);
    map.setZoom(zoom);

    JLabel speedLabel = new JLabel("Speed:");
    _fleetSpeedField = new JComboBox<Integer>();
    _fleetSpeedField.addItemListener(itemListener);
    //_fleetSpeed = (int)_settingsService.getLong(ISettings.MAP_SPEED);

    _showName = new JCheckBox("Name", _settingsService.isFlagSet(ISettings.MAP_SHOW_NAME));
    _showName.addItemListener(itemListener);

    _showScanner = new JCheckBox("Scanner", _settingsService.isFlagSet(ISettings.MAP_SHOW_SCANNER));
    _showScanner.addItemListener(itemListener);

    _showRange = new JCheckBox("Range", _settingsService.isFlagSet(ISettings.MAP_SHOW_RANGE));
    _showRange.addItemListener(itemListener);

    _showOwner = new JCheckBox("Owner", _settingsService.isFlagSet(ISettings.MAP_SHOW_OWNER));
    _showOwner.addItemListener(itemListener);

    _showFirepower = new JCheckBox("FP", _settingsService.isFlagSet(ISettings.MAP_SHOW_FIREPOWER));
    _showFirepower.addItemListener(itemListener);

    _showDeltas = new JCheckBox("Deltas", _settingsService.isFlagSet(ISettings.MAP_SHOW_DELTAS));
    _showDeltas.addItemListener(itemListener);

    _showLabour = new JCheckBox("Labour", _settingsService.isFlagSet(ISettings.MAP_SHOW_LABOUR));
    _showLabour.addItemListener(itemListener);

    _showCapital = new JCheckBox("Capital", _settingsService.isFlagSet(ISettings.MAP_SHOW_CAPITAL));
    _showCapital.addItemListener(itemListener);

    TurnControls turnControls = new TurnControls(
        LoggerFactory.getLogger(TurnControls.class),
        _settingsService, 
        _worldService);

    GridBagConstraints gbc = GuiUtils.prepGridBag(panel);
    gbc.insets = new Insets(2, 2, 2, 2);

    panel.add(speedLabel, gbc);
    gbc.gridx++;
    panel.add(_fleetSpeedField, gbc);
    gbc.gridx++;
    panel.add(_showName, gbc);
    gbc.gridx++;
    panel.add(_showOwner, gbc);
    gbc.gridx++;
    panel.add(_showRange, gbc);
    gbc.gridx++;
    panel.add(_showScanner, gbc);
    gbc.gridx++;
    panel.add(turnControls.getFirstButton(), gbc);
    gbc.gridx++;
    panel.add(turnControls.getPrevButton(), gbc);
    gbc.gridx++;
    panel.add(turnControls.getNextButton(), gbc);
    gbc.gridx++;
    panel.add(turnControls.getLastButton(), gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridheight = 0;
    panel.add(new JLabel());
    gbc.gridx++;
    gbc.gridwidth = 1;
    panel.add(new JLabel(""), gbc);
    gbc.gridx++;
    panel.add(_showFirepower, gbc);
    gbc.gridx++;
    panel.add(_showLabour, gbc);
    gbc.gridx++;
    panel.add(_showCapital, gbc);
    gbc.gridx++;
    panel.add(_showDeltas, gbc);
    gbc.gridx++;
    panel.add(new JLabel(), gbc);
    gbc.gridx++;
    gbc.gridwidth = 4;
    panel.add(turnControls.getTurnLabel(), gbc);

    prepareSpeedChoices();

    panel.revalidate();
    return panel;
  }

  @Override
  public void dispose() {
    super.dispose();
    _worldService.removeListener(this);
    _unitService.removeListener(this);
  }

  @Override
  public Set<WorldAndHistory> getData() {
    if(_mapInfo == null) {
      getMapInfo();
    }

    if(_currentTurn > _mapInfo.getMaxTurn()) {
      if(_log.isTraceEnabled()) {
        _log.trace("getData() called, returning empty list for turn " + _currentTurn);
      }
      return Collections.emptySet(); //No data for this turn, user will need to import some
    }

    if(_worldData == null) {
      if(_log.isTraceEnabled()) {
        _log.trace("getData() called, and invoking worldService to load history for turn " + _currentTurn);
      }
      _worldData = _worldService.getWorldHistory(_currentTurn);
    } else {
      if(_log.isTraceEnabled()) {
        _log.trace("getData() called, and returning cached history for turn " + _currentTurn);
      }
    }
    return _worldData;
  }

  @Override
  public String getDescription(Object object) {
    return ((WorldAndHistory)object).getWorld().getName();
  }

  @Override
  public MapInfo getMapInfo() {
    if(_mapInfo == null) {
      _mapInfo = _worldService.getMapInfo();
    }
    return _mapInfo;
  }

  @Override
  public int getObjectX(Object object) {
    return ((WorldAndHistory)object).getWorld().getWorldX();
  }

  @Override
  public int getObjectY(Object object) {
    return ((WorldAndHistory)object).getWorld().getWorldY();
  }

  /**
   * Objects must be instances of {@link WorldAndHistory} and will be compared by identity
   * on the id of the world. Also returns true if both are null ... which is kinda silly because if
   * object1 is not null and object2 is null its going to cause an exception.
   */
  @Override
  public boolean isSameObject(Object object1, Object object2) {
    if(object1 == null) {
      return object2 == null;
    }
    WorldId id1 = ((WorldAndHistory)object1).getWorld().getId();
    WorldId id2 = ((WorldAndHistory)object2).getWorld().getId();
    return id1.equals(id2);
  }

  @Override
  public IObjectRenderer getRenderer(Object object) {
    return this;
  }

  /**
   * Returns the history object for the specified world for the previous turn if
   * it is available or null if it is not.
   * @param world
   *          may not be null
   * @return history
   */
  protected IHistory getPreviousTurn(IWorld world) {
    WorldId worldId = Objects.requireNonNull(world, "world must not be null").getId();
    int prevTurn = _currentTurn - 1;
    if(prevTurn < 1 || prevTurn > _mapInfo.getMaxTurn()) {
      return null;
    }
    IHistory history = _worldService.getHistory(worldId, _currentTurn - 1);
    return history;
  }

  @Override
  public void render(Graphics2D g, GenericMap map, Object object, int x, int y, double zoom) {
    WorldAndHistory wh = (WorldAndHistory)object;
    WorldAndHistory origin = (WorldAndHistory) map.getOrigin();
    WorldAndHistory highlighted = (WorldAndHistory)map.getHighlighted();

    boolean needPreviousTurn = _showDeltas.isSelected();
    IHistory previousTurn = needPreviousTurn ? getPreviousTurn(wh.getWorld()) : null;

    int labour = wh.getHistory().getLabour();
    double multiplier;
    if(labour < _fClass) {
      multiplier = 2d;
    } else if(labour < _eClass) {
      multiplier = 4d;
    } else if(labour < _dClass) {
      multiplier = 6d;
    } else if(labour < _cClass) {
      multiplier = 8d;
    } else if(labour < _bClass) {
      multiplier = 10d;
    } else if(labour < _aClass) {
      multiplier = 12d;
    } else {
      multiplier = 14d;
    } //S-Class
    double radius_dbl = (multiplier + 1) * zoom;
    if(radius_dbl < 3)
      radius_dbl = 3;
    int radius = (int) Math.ceil(radius_dbl);
    int line = 1;
    int lineHeight = 12;

    double firepower =wh.getHistory().getFirepower();
    if(firepower < _defcon5) {
      g.setStroke(DEFCON5_STROKE);
    } else if(firepower < _defcon4) {
      g.setStroke(DEFCON4_STROKE);
    } else if(firepower < _defcon3) {
      g.setStroke(DEFCON3_STROKE);
    } else if(firepower < _defcon2) {
      g.setStroke(DEFCON2_STROKE);
    } else {
      g.setStroke(DEFCON1_STROKE);
    }

    //Draw the world circle
    g.setColor(wh.getColor());
    g.drawOval(x - radius, y - radius, radius * 2, radius * 2);

    String owner = wh.getHistory().getOwner();
    String previousOwner = (previousTurn == null) ? null : previousTurn.getOwner();
    boolean changedOwner = (previousOwner != null) & !owner.equalsIgnoreCase(previousOwner);

    if(changedOwner) {
      //TODO - would be nice to use the previous owners color for the highlight
      //       but we only have the history handy
      //g.drawArc(x-radius-1, y, 1+radius*2, (radius/2), 0, -190);
      //g.drawLine(x-radius-1, y, x+radius+1, y);
      g.drawLine(x - radius - 1, y + (radius / 2), x + radius + 1, y - (radius / 2));
    }

    String nameToShow = null;
    if(_showName.isSelected()) {
      nameToShow = wh.getWorld().getName();
    }
    if(_showFirepower.isSelected()) {
      String fpStr = "[" + Convert.toStr(firepower, 2);
      if(previousTurn != null && _showDeltas.isSelected()) {
        double fpDelta = firepower - previousTurn.getFirepower();
        if(fpDelta != 0) {
          fpStr += " " + (fpDelta > 0 ? "+" : "") + Convert.toStr(fpDelta, 2);
        }
      }
      fpStr += "]";
      nameToShow = (nameToShow == null) ? fpStr : nameToShow + " " + fpStr;
    }

    if(nameToShow != null) {
      g.drawString(nameToShow, x + radius, y + radius + (line * lineHeight));
      line++;
    }

    String labCap = "";
    if(_showLabour.isSelected()) {
      labCap += labour + "\u03c1"; //rho (looks like a p)
      if(previousTurn != null && _showDeltas.isSelected()) {
        int labourDelta = labour - previousTurn.getLabour();
        if(labourDelta != 0) {
          labCap += " " + (labourDelta > 0 ? "+" : "") + labourDelta;
        }
      }
    }
    if(_showCapital.isSelected()) {
      if(!labCap.isEmpty()) {
        labCap += ", ";
      }
      int capital = wh.getHistory().getCapital();
      labCap += capital + "\u03c5"; //upsilon (looks like a u)
      if(previousTurn != null && _showDeltas.isSelected()) {
        int capitalDelta = capital - previousTurn.getCapital();
        if(capitalDelta != 0) {
          labCap += " " + (capitalDelta > 0 ? "+" : "") + capitalDelta;
        }
      }
    }
    if(!labCap.isEmpty()) {
      g.drawString(labCap, x + radius, y + radius + (line * lineHeight));
      line++;
    }

    String rangeInfo = "";
    int distance = (origin == null) ? 0
        : Convert.toDistance(origin.getWorld().getWorldX(), origin.getWorld().getWorldY(), wh.getWorld().getWorldX(),
            wh.getWorld().getWorldY());
    if(origin != null && _showRange.isSelected()) {
      rangeInfo += distance;
      if(_fleetSpeed > 0) {
        int eta = distance / _fleetSpeed + (distance % _fleetSpeed > 0 ? 1 : 0);
        rangeInfo += " (" + eta + ")";
      }
    }

    String scannerInfo = "";
    if(_showScanner.isSelected()) {
      if(origin != null) {
        if(!rangeInfo.isEmpty()) {
          scannerInfo += " ";
        }
        if(_fleetSpeed > 0) {
          int visToTarget = calcVisibility(distance, _fleetSpeed, wh.getScannerRange());
          int visToOrigin = calcVisibility(distance, _fleetSpeed, origin.getScannerRange());
          scannerInfo += " <" + visToTarget + "/" + wh.getScannerRange() + "/" + visToOrigin + ">";
        } else { //No speed to calculate visible times for
          scannerInfo += " /" + wh.getScannerRange() + "/";
        }
      } else { //No origin for distances so dont need to calc visible times
        scannerInfo += " /" + wh.getScannerRange() + "/";
      }
    }

    if(_showRange.isSelected() || _showScanner.isSelected()) {
      g.drawString(rangeInfo + scannerInfo, x + radius, y + radius + (lineHeight * line));
      line++;
    }

    if(_showOwner.isSelected()) {
      String ownerDisplay = owner;
      if(wh.getTeam() != null && !wh.getTeam().isEmpty()) {
        ownerDisplay += " (" + wh.getTeam() + ")";
      }
      if(!"".equals(ownerDisplay) && !IPlayer.NOBODY.equals(owner)) {
        g.drawString(ownerDisplay, x + radius, y + radius + (line * lineHeight));
        line++;
      }
    }

    if(object == origin) { //highlight the origin. Should pick up color from renderer if any
      g.setStroke(SIMPLE_LINE_STROKE);
      g.drawLine(x - radius, y - radius, x + radius, y + radius);
      g.drawLine(x - radius, y + radius, x + radius, y - radius);
    }

    if(object == highlighted) {
      g.setColor(Color.GRAY);
      g.setStroke(SIMPLE_LINE_STROKE);
      g.drawRoundRect(x - (radius * 2), y - (radius * 2), radius * 4, radius * 4, 8, 8);
      map.getMap().setToolTipText(wh.getWorld().getName() + " (" + wh.getHistory().getOwner() + ")");
    }
  }

  @Override
  public void dataChanged(EventObject event) {
    if(event.getSource() instanceof IWorldService || event.getSource() instanceof IPlayerService) { //Reload the map data to fetch changes
      _worldData = null;
      _mapInfo = null;
      notifyListeners();
    } else if(event.getSource() instanceof IUnitService) { //Update the selection of available fleet speeds
      prepareSpeedChoices();
      //Also need to reload the map as scanner ranges might have changed
      //...we could really use unit specific events now!
      _worldData = null;
      _mapInfo = null;
      notifyListeners();
    } else if(event instanceof SettingChangedEvent) { //Reload the map for the new turn to show history
      SettingChangedEvent sce = (SettingChangedEvent) event;
      String setting = sce.getName();
      Objects.requireNonNull(setting, "setting name must not be null");
      if(ISettings.CURRENT_TURN.equals(setting)) { //Load map data for a different turn
        _currentTurn = (int) sce.getLongValue();
        _worldData = null;
        _mapInfo = null;
        notifyListeners();
      } else if(ISettings.DEFAULT_SCANNER.equals(setting)) { //Reload map data with updated default scanner ranges
        _worldData = null;
        _mapInfo = null;
        notifyListeners();
      } else if(setting.startsWith("romeo.worlds.sizes.") || setting.startsWith("romeo.worlds.defcon.")) { //Load the new rendering preferences and redraw
        loadDisplayPrefs();
        notifyListeners();
      }
    }
  }

  protected static int calcVisibility(int distance, int speed, int scanner) {
    if(speed < 1) {
      throw new IllegalArgumentException("invalid speed:" + speed);
    }
    if(distance < 0) {
      throw new IllegalArgumentException("invalid distance:" + distance);
    }
    if(scanner < 0) {
      throw new IllegalArgumentException("invalid scanner:" + scanner);
    }

    if(speed > distance) {
      return 0;
    }
    double distanceToVisible = Math.max(0, distance - scanner);
    double turnsToVisible = distanceToVisible / (double) speed;
    double distanceWhenVisible = (double) distance - ((double) speed * Math.ceil(turnsToVisible));
    double turnsVisible = distanceWhenVisible / (double) speed;
    int v = (int) Math.ceil(turnsVisible);
    if(turnsToVisible == 0) {
      v--;
    }
    return v;
  }

  protected static int calcVisibilityOld(int distance, int speed, int scanner) {
    int v = 0;
    while((distance -= speed) > 0) {
      if(distance <= scanner && distance > 0) {
        v++;
      }
    }
    return v;
  }
  
  protected void prepareSpeedChoices() {
    int[] speeds = _unitService.getSpeeds();
    Objects.requireNonNull(_fleetSpeedField, "_fleetSpeedField must not be null");
    _fleetSpeedField.removeAllItems();
    //Temporarily drop listeners so we can add items quietly
    ItemListener[] listeners = _fleetSpeedField.getItemListeners();
    for(ItemListener l : listeners) {
      _fleetSpeedField.removeItemListener(l);
    }

    int selectedIndex = 0;
    for(int i = 0; i < speeds.length; i++) {
      _fleetSpeedField.addItem(speeds[i]);
      if(speeds[i] == _fleetSpeed) {
        selectedIndex = i;
      }
    }
    _fleetSpeedField.setSelectedIndex(selectedIndex);
    _fleetSpeed = speeds[selectedIndex]; //ensure synced 0 if it not in list
    for(ItemListener l : listeners) {
      _fleetSpeedField.addItemListener(l);
    }
    _fleetSpeedField.repaint();
    updateSpeed();
  }

  protected void updateSpeed() {
    Integer speedInt = (Integer) _fleetSpeedField.getSelectedItem();
    _fleetSpeed = speedInt == null ? 0 : speedInt.intValue();
    notifyListeners();
  }

  //@Override
  public PositionStruct getInitialCenter() {
    PositionStruct p = new PositionStruct();
    MapInfo mi = getMapInfo();
    int mapCentreX = ((mi.getRightBorder() - mi.getLeftBorder()) / 2) + mi.getLeftBorder();
    int mapCentreY = ((mi.getBottomBorder() - mi.getTopBorder()) / 2) + mi.getTopBorder();
    p.center = new Point(mapCentreX, mapCentreY);
    p.zoom = _settingsService.getDouble(ISettings.MAP_ZOOM);
    return p;
  }

  @Override
  public String getNoDataMessage() {
    return "NO DATA FOR TURN " + _currentTurn;
  }

  @Override
  public void closing(GenericMap map) {
    _log.debug("Saving world map settings");
    //From the parent GenericMap
    Point mapCentre = map.getVisibleMapCentre();
    _settingsService.setLong(ISettings.MAP_X, (int) mapCentre.getX());
    _settingsService.setLong(ISettings.MAP_Y, (int) mapCentre.getY());
    _settingsService.setDouble(ISettings.MAP_ZOOM, map.getZoom());
    _settingsService.setLong(ISettings.MAP_SPEED, _fleetSpeed);
    //From this logic's controls
    _settingsService.setFlag(ISettings.MAP_SHOW_NAME, _showName.isSelected());
    _settingsService.setFlag(ISettings.MAP_SHOW_SCANNER, _showScanner.isSelected());
    _settingsService.setFlag(ISettings.MAP_SHOW_RANGE, _showRange.isSelected());
    _settingsService.setFlag(ISettings.MAP_SHOW_FIREPOWER, _showFirepower.isSelected());
    _settingsService.setFlag(ISettings.MAP_SHOW_DELTAS, _showDeltas.isSelected());
    _settingsService.setFlag(ISettings.MAP_SHOW_LABOUR, _showLabour.isSelected());
    _settingsService.setFlag(ISettings.MAP_SHOW_CAPITAL, _showCapital.isSelected());
    _settingsService.setFlag(ISettings.MAP_SHOW_OWNER, _showOwner.isSelected());
  }
}
