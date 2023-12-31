package romeo.ui;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import romeo.model.api.MapInfo;
import romeo.utils.Convert;
import romeo.utils.events.IEventHub;
import romeo.utils.events.IEventListener;
import romeo.utils.events.ShutdownEvent;

/**
 * 2D map display of things, with zooming and scrolling, and clicking. 
 * In practice the only things we use this for displaying are worlds 
 * so the only instance of this class is for the worlds map. 
 */
public class GenericMap implements IEventListener {
  
  public static interface IMapLogic {
    public void addListener(GenericMap map);

    public JComponent supplyControls(GenericMap map);

    public Set<? extends Object> getData();

    public MapInfo getMapInfo();

    public boolean isSameObject(Object object1, Object object2);

    public int getObjectX(Object object); //in world coordinates

    public int getObjectY(Object object); //in world coordinates

    public IObjectRenderer getRenderer(Object object);

    public String getNoDataMessage();

    public void closing(GenericMap map);

    @Deprecated
    public String getDescription(Object object);
  }

  public static interface IObjectRenderer {
    public void render(Graphics2D g, GenericMap map, Object object, int x, int y, double zoom);
  }

  public static class PositionStruct {
    public Point center;
    public double zoom;
  }

  protected static final int SCROLL_INCREMENT = 25;
  protected static final int SLIDER_RATIO = 100;
  private static final int EDGE_PAD = 64;

  /////////////////////////////////////////////////////////////////////////////

  private JPanel _panel;
  private JPanel _controls;
  private JPanel _map;
  private double _zoom = 1.5d;
  private JSlider _zoomSlider;
  private JScrollPane _scrollPane;
  private IMapLogic _logic;
  private Object _originObject;
  private Object _highlightedObject;
  private Color _backgroundColor = Color.BLACK;
  private Font _font = null;
  private List<IRecordSelectionListener> _selectionListeners = new ArrayList<IRecordSelectionListener>(1);

  /**
   * Constructor.
   * Will add itself as a weak listener to the shutdownNotifier, and will add the (optional) record selection
   * listener as a listener to itself.
   * @param logic
   * @param listener may be null
   * @param shutdownNotifier required
   */
  public GenericMap(IMapLogic logic, IRecordSelectionListener listener, IEventHub shutdownNotifier) {
    _logic = Objects.requireNonNull(logic, "logic may not be null");
    
    logic.addListener(this);    
    Objects.requireNonNull(shutdownNotifier,"shutdownNotifier may not be null").addWeakListener(this);
    
    if(listener != null) {
      addRecordSelectionListener(listener);
    }

    _map = new JPanel() {
      @Override
      public void paint(Graphics graphics) {
        GenericMap.this.paintMap((Graphics2D) graphics);
      }
    };

    _map.addMouseListener(new MouseAdapter() { //TODO - consuming events when appropriate
      @Override
      public void mousePressed(MouseEvent e) {
        mouseClicked(e);
      }

      @Override
      public void mouseClicked(MouseEvent e) {
        if(e.isConsumed()) {
          return;
        }
        if(e.isShiftDown()) {
          if(_highlightedObject != null) { //Shift-click to open world
            notifyRecordSelectionListeners(_highlightedObject);
          }
        } else {
          if(e.getButton() == MouseEvent.BUTTON1 && _highlightedObject != null) { //left click changes origin thing
            _originObject = _highlightedObject;
            _map.repaint();
            if(e.getClickCount() == 2) { //double-click will inform listeners it  was selected
              if(_highlightedObject != null) {
                notifyRecordSelectionListeners(_highlightedObject);
              }
            }
          }

          if(e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() != 2) { //Right click to recenter map on mouse

            int mapX = toWorldX(e.getX());
            int mapY = toWorldY(e.getY());

            if(!e.isControlDown() && _highlightedObject != null) { //Snap to highlighted object if ctrl not down
              mapX = _logic.getObjectX(_highlightedObject);
              mapY = _logic.getObjectY(_highlightedObject);
            }

            Point visibleCentre = new Point(mapX, mapY);
            setVisibleMapCentre(visibleCentre);

            if(e.isControlDown() == false) { //Also move the mouse cursor to provide a visual ref (unless ctrl down)
              try {
                int sx = toScreenX(mapX) - _scrollPane.getHorizontalScrollBar().getValue();
                int sy = toScreenY(mapY) - _scrollPane.getVerticalScrollBar().getValue();
                Point p = _scrollPane.getLocationOnScreen();
                sx += p.getX();
                sy += p.getY();
                //System.out.println("set mouse " + sx + "," + sy);
                new Robot().mouseMove(sx, sy);
              } catch(AWTException ae) {
                //ae.printStackTrace();
              }
            }
          }
        }

      }

    });

    _map.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        int wx = toWorldX(e.getX());
        int wy = toWorldY(e.getY());
        Set<? extends Object> data = _logic.getData();
        Object closest = null;
        int minDist = Integer.MAX_VALUE;
        for(Object object : data) {
          int distance = Convert.toDistance(_logic.getObjectX(object), _logic.getObjectY(object), wx, wy);
          if(distance < minDist) {
            closest = object;
            minDist = distance;
          }
        }
        _highlightedObject = closest;
        _map.repaint();
      }
    });

    _panel = new JPanel();
    _panel.setLayout(new BorderLayout());
    _scrollPane = new JScrollPane(_map);
    _scrollPane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
    _scrollPane.getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
    _panel.add(_scrollPane, BorderLayout.CENTER);

    _controls = new JPanel();
    _controls.setLayout(new BoxLayout(_controls, BoxLayout.Y_AXIS));
    _zoomSlider = new JSlider(JSlider.HORIZONTAL, 1, 2 * SLIDER_RATIO, (int) (_zoom * SLIDER_RATIO));
    _zoomSlider.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent event) {
        Log log = LogFactory.getLog(this.getClass());
        Point centre = getVisibleMapCentre();
        _zoom = _zoomSlider.getValue() / (double) SLIDER_RATIO;
        if(log.isTraceEnabled()) {
          log.trace("State changed, centre=" + centre.getX() + "," + centre.getY() + " _zoom=" + _zoom);
        }
        updateSize(centre);
        _zoomSlider.setToolTipText("" + _zoom);
        _map.repaint();
      }
    });
    _zoomSlider.setMajorTickSpacing(SLIDER_RATIO / 4);
    _zoomSlider.setMinorTickSpacing(SLIDER_RATIO / 10);
    _zoomSlider.setPaintTicks(true);

    _controls.add(_zoomSlider);

    JComponent extraControls = _logic.supplyControls(this);
    if(extraControls != null) {
      _controls.add(extraControls);
    }

    _panel.add(_controls, BorderLayout.SOUTH);

    _panel.revalidate();
  }

  /**
   * Returns the map coordinate at the centre of the visible part of the map.
   * @return visibleMapCentre
   */
  public Point getVisibleMapCentre() {
    int scrollX = _scrollPane.getHorizontalScrollBar().getValue();
    int centreX = (_scrollPane.getWidth() / 2) + scrollX;

    int scrollY = _scrollPane.getVerticalScrollBar().getValue();
    int centreY = (_scrollPane.getHeight() / 2) + scrollY;

    return new Point(toWorldX(centreX), toWorldY(centreY));
  }

  /**
   * Set the map scroll to be centered at the specified coordinates (if
   * possible) nb: this doesnt call invalidate on the _map JPanel. That is left
   * to caller.
   * @param visibleCentre
   */
  public void setVisibleMapCentre(Point visibleCentre) {
    Log log = LogFactory.getLog(this.getClass());
    if(log.isTraceEnabled()) {
      log.trace("setVisibleMapCentre(" + visibleCentre + ") called on thread " + Thread.currentThread().getName());
    }

    int spWidth = _scrollPane.getWidth();
    int spHeight = _scrollPane.getHeight();

    if(spWidth == 0 || spHeight == 0) { 
      //Hack to make Swing actually initialise the width and height properly
      //however this no longer seems to work (if it ever did). We still get 0 width and height afterwards!
      if(log.isTraceEnabled()) {
        log.trace("before hack scrollPane width=" + _scrollPane.getWidth() + ", height=" + _scrollPane.getHeight()
            + " : setting scrollbar values to 1");
      }
      _scrollPane.getHorizontalScrollBar().setValue(1);
      _scrollPane.getVerticalScrollBar().setValue(1);
      //Now lets try that again shall we...
      if(log.isTraceEnabled()) {
        log.trace("after hack scrollPane width=" + _scrollPane.getWidth() + ", height=" + _scrollPane.getHeight());
      }
      spWidth = _scrollPane.getWidth();
      spHeight = _scrollPane.getHeight();
    } else {
      log.trace("spWidth=" + spWidth + ", spHeight=" + spHeight + " so not applying hack");
    }

    int centreX = toScreenX((int) visibleCentre.getX());
    int centreY = toScreenY((int) visibleCentre.getY());

    int scrollX = centreX - (spWidth / 2);
    int scrollY = centreY - (spHeight / 2);

    //Scrolls to this spot provided that it is not already visible.
    //This works when we are trying to set the initial location (assuming its not visible)
    //unlike the next method we apply below...
    if(log.isTraceEnabled()) {
      log.trace("Scrolling to " + scrollX + "," + scrollY);
    }
    _map.scrollRectToVisible(new Rectangle(scrollX, scrollY, 0, 0));

    //Will scroll even if the spot is already somewhere on screen, but this only
    //works for the Y scroll when we are trying to set the initial position (unless
    //in debugger with a breakpoint!) and the X will be ignored.
    _scrollPane.getHorizontalScrollBar().setValue(scrollX);
    _scrollPane.getVerticalScrollBar().setValue(scrollY);

    if(log.isTraceEnabled()) {
      log.trace("setVisibleMapCentre(" + visibleCentre.getX() + "," + visibleCentre.getY() + ") : scrolled to scrollX=" + scrollX
          + ", scrollY=" + scrollY + ", ScrollPane spWidth=" + spWidth + ", spHeight=" + spHeight + ", leftBorder="
          + _logic.getMapInfo().getLeftBorder() + ", zoom=" + _zoom + ", Visible screen centreX=" + centreX + ", centreY=" + centreY
          + ", actual horizontalScrollBar.value=" + _scrollPane.getHorizontalScrollBar().getValue()
          + ", actual verticalScrollBar.value=" + _scrollPane.getVerticalScrollBar().getValue() );
    }
  }

  public void addRecordSelectionListener(IRecordSelectionListener l) {
    if(!_selectionListeners.contains(l)) {
      _selectionListeners.add(l);
    }
  }

  public void removeRecordSelectionListener(IRecordSelectionListener l) {
    _selectionListeners.remove(l);
  }

  protected void notifyRecordSelectionListeners(Object record) {
    List<IRecordSelectionListener> listeners = new ArrayList<IRecordSelectionListener>(_selectionListeners);
    for(IRecordSelectionListener listener : listeners) {
      listener.recordSelected(record);
    }
  }

  public void refresh() {
    _highlightedObject = null;
    if(_originObject != null) {
      _originObject = findObject(_originObject);
    }
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        updateSize(null);
        _panel.revalidate();
        _map.repaint();
      }
    });
    _map.repaint();
  }

  protected void updateSize(Point mapCentre) {
    MapInfo mapInfo = _logic.getMapInfo();
    int pad = (int) (EDGE_PAD * _zoom);
    //how does this work without using left and top?
    //because toScreenX(leftBorder) would always convert to zero for us
    Dimension size = new Dimension(toScreenX(mapInfo.getRightBorder()) + pad,
        toScreenY(mapInfo.getBottomBorder()) + pad);
    _map.setMinimumSize(size);
    _map.setMaximumSize(size);
    _map.setPreferredSize(size);
    //_map.validate();
    //_map.repaint();
    if(mapCentre != null)
      setVisibleMapCentre(mapCentre);
    _map.invalidate(); //need like this to get scrollbars to update ok. Stupid Swing.
    _scrollPane.revalidate();
  }

  public int toScreenX(int worldX) {
    int minX = _logic.getMapInfo().getLeftBorder();
    return (int) (((worldX - minX) * _zoom) + (EDGE_PAD * _zoom));
  }

  public int toScreenY(int worldY) {
    int minY = _logic.getMapInfo().getTopBorder();
    return (int) (((worldY - minY) * _zoom) + (EDGE_PAD * _zoom));
  }

  public int toWorldX(int screenX) {
    int minX = _logic.getMapInfo().getLeftBorder();
    return (int) (((screenX - (EDGE_PAD * _zoom)) / _zoom) + minX);
  }

  public int toWorldY(int screenY) {
    int minY = _logic.getMapInfo().getTopBorder();
    return (int) (((screenY - (EDGE_PAD * _zoom)) / _zoom) + minY);
  }

  public JComponent getComponent() {
    return _panel;
  }

  public JPanel getControls() {
    return _controls;
  }

  protected void paintMap(Graphics2D g) {
    //LogFactory.getLog(this.getClass()).trace("paintMap called");
    g.setColor(_backgroundColor);
    g.fillRect(0, 0, _map.getWidth(), _map.getHeight());
    if(_font != null) {
      g.setFont(_font);
    }

    Set<? extends Object> data = _logic.getData();
    int size = data == null ? 0 : data.size();

    if(size == 0) { //No data to display
      Point vis = getVisibleMapCentre();
      int x = toScreenX(vis.x);
      int y = toScreenY(vis.y);
      g.setColor(Color.WHITE);
      String noDataMsg = _logic.getNoDataMessage();
      Rectangle2D msgSize = g.getFontMetrics().getStringBounds(noDataMsg, g);
      x -= msgSize.getWidth() / 2;
      y -= msgSize.getHeight() / 2;
      g.drawString(noDataMsg, x, y);
      return;
    }

    //following radius only used if no renderer supplied
    int radius = (int) (4 * _zoom);
    if(radius < 2) { //Minimum visual size for our worlds
      radius = 2;
    }
    //....

    MapInfo mapInfo = _logic.getMapInfo();
    int mapLeft = toScreenX(mapInfo.getLeftBorder());
    int mapRight = toScreenX(mapInfo.getRightBorder());
    int mapTop = toScreenY(mapInfo.getTopBorder());
    int mapBottom = toScreenY(mapInfo.getBottomBorder());

    g.setColor(Color.GRAY);
    g.drawLine(mapLeft, mapTop, mapRight, mapTop);
    g.drawLine(mapRight, mapTop, mapRight, mapBottom);
    g.drawLine(mapRight, mapBottom, mapLeft, mapBottom);
    g.drawLine(mapLeft, mapBottom, mapLeft, mapTop);

    Rectangle clip = getScreenClipRectangle();

    nextObject: for(Object object : data) {
      int x = toScreenX(_logic.getObjectX(object));
      int y = toScreenY(_logic.getObjectY(object));
      if(clip != null && clip.contains(x, y) == false) {
        continue nextObject;
      }

      IObjectRenderer renderer = _logic.getRenderer(object);
      if(renderer != null) {
        renderer.render(g, this, object, x, y, _zoom);
      } else { //Default rendering
        g.setColor(Color.WHITE);
        g.drawOval(x - radius, y - radius, radius * 2, radius * 2);
        g.drawString(_logic.getDescription(object), x + radius, y + radius + 12);

        if(object == _originObject) { //highlight the origin. Should pick up color from renderer if any
          g.drawLine(x - radius, y - radius, x + radius, y + radius);
          g.drawLine(x - radius, y + radius, x + radius, y - radius);
        }

        if(object == getHighlighted()) {
          g.setColor(Color.GRAY);
          g.drawRoundRect(x - (radius * 2), y - (radius * 2), radius * 4, radius * 4, 8, 8);
          _map.setToolTipText(_logic.getDescription(object));
        }

      }

    }
  }

  /**
   * Determine what part of the map is visible so we can avoid wasting time
   * trying to draw objects that are offscreen. We add some extra space to allow
   * for objects that are just offscreen and that may have bits showing on
   * screen.
   * @return clip
   */
  protected Rectangle getScreenClipRectangle() {
    final int extra = 128;
    Rectangle clip = new Rectangle(_scrollPane.getHorizontalScrollBar().getValue() - extra,
        _scrollPane.getVerticalScrollBar().getValue() - extra, _scrollPane.getWidth() + extra + (extra / 2),
        _scrollPane.getHeight() + extra + (extra / 2)); //note BR less extra than TL
    return clip;
  }

  public double getZoom() {
    return _zoom;
  }

  public void setZoom(double zoom) {
    _zoom = zoom;
    _zoomSlider.setValue((int) (zoom * (double) SLIDER_RATIO));
    _zoomSlider.setToolTipText("" + _zoom);
    refresh();
  }

  public Object getOrigin() {
    return _originObject;
  }

  public void setOrigin(Object origin) {
    LogFactory.getLog(this.getClass()).debug("SetOrigin called with " + origin);
    _originObject = origin;
    refresh();
  }

  public Object getHighlighted() {
    return _highlightedObject;
  }

  protected Object findObject(Object object) {
    Set<? extends Object> data = _logic.getData();
    for(Object candidate : data) {
      if(_logic.isSameObject(object,candidate)) {
        return candidate;
      }
    }
    return null;
  }

  public IMapLogic getLogic() {
    return _logic;
  }

  public JPanel getMap() {
    return _map;
  }

  @Override
  public void onEvent(EventObject event) {
    if(event instanceof ShutdownEvent) {
      _logic.closing(this);
    }
  }

  public void setBackgroundColor(Color color) {
    _backgroundColor = Objects.requireNonNull(color, "color may not be null");
  }

  public Color getBackgroundColor() {
    return _backgroundColor;
  }

  public Font getFont() {
    return _font;
  }

  /**
   * Set a default font. This will be set before the call to the logic's
   * render() method. (nb: the render method can of course set its own fonts if
   * it pleases). You may also pass null in which case a setFont call will not
   * be made.
   * @param font
   */
  public void setFont(Font font) {
    _font = font;
  }

}
