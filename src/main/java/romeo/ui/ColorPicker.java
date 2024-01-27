//2008-12-08
package romeo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import romeo.Romeo;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;

/**
 * Provides a color picker that is simpler and thus more convenient for users
 * that that provided by JColorChooser. Internally this maintains a JDialog
 * which it prepares on first use and then redisplays as necessary.
 */
public class ColorPicker {
  
  private static int ROWS = 12;

  //Colours - will be arranged in vertical columns of ROWS each
  private static final Color[] COLORS = new Color[] { new Color(255, 255, 255), new Color(255, 204, 102),
      new Color(255, 0, 0),

      new Color(204, 204, 204), new Color(153, 153, 153), new Color(102, 102, 102),

      new Color(0, 255, 255), new Color(0, 204, 204), new Color(0, 153, 153),

      new Color(153, 204, 204), new Color(153, 153, 204), new Color(153, 102, 204),

      //2
      new Color(153, 153, 102), //TEAM COLOR - grayish, kinda
      new Color(102, 102, 51), new Color(255, 0, 53), //TEAM COLOR - red, but not somebody red

      new Color(0, 255, 153), new Color(0, 153, 153), new Color(0, 102, 153),

      new Color(204, 255, 204), new Color(153, 204, 255), //TEAM COLOR - cyan with a bit less cyanide
      new Color(102, 102, 255), //TEAM COLOR - blue, but not so sweet

      new Color(255, 204, 255), new Color(255, 175, 255), new Color(255, 103, 255),

      //3

      new Color(102, 255, 102), //TEAM COLOR - a cleaner green
      new Color(153, 255, 0), new Color(204, 255, 0),

      new Color(255, 204, 0), new Color(255, 153, 51), new Color(255, 102, 0),

      new Color(102, 153, 255), new Color(0, 153, 204), new Color(0, 0, 255),

      new Color(255, 0, 255), ////
      new Color(204, 0, 204), new Color(204, 0, 153),

      //4  
      new Color(0, 255, 0), new Color(0, 153, 0), new Color(0, 102, 0),

      new Color(255, 255, 0), new Color(204, 204, 0), new Color(153, 153, 0),

      new Color(204, 153, 0), //TEAM COLOR - khakiesque
      new Color(153, 102, 51), new Color(204, 102, 51),

      new Color(255, 102, 102), new Color(255, 102, 153), //TEAM COLOUR - sort of unmanly pink. Thats vaguely magenta right?
      new Color(153, 0, 204),

  };
  
  private static class ColorPickerCancelAction extends AbstractAction {
    
    private ColorPicker _picker;
  
    public ColorPickerCancelAction(ColorPicker picker) {
      _picker = Objects.requireNonNull(picker, "picker may not be null");
    }
    
    public void actionPerformed(ActionEvent e) {
      _picker.cancel();
    }
    
  }
  
  private static class ColorPickerMovementAction extends AbstractAction {

    public enum Direction { up, down, left, right }
        
    private ColorPicker _picker;
    private Direction _direction;
    
    public ColorPickerMovementAction(ColorPicker picker, Direction direction) {
      _picker = Objects.requireNonNull(picker, "picker may not be null");
      _direction = Objects.requireNonNull(direction, "direction may not be null");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      int i = _picker.GetSelectedIndex();
      
      if(i==-1) {
        _picker.SetSelectedIndex(0);
        return;
      }
      
      int x = i / ROWS;
      int y = i % ROWS;
      switch(_direction) {        
        case up: y--; break;
        case down: y++; break; 
        case left: x--; break;          
        case right: x++; break;
        default: throw new UnsupportedOperationException(_direction.toString());
      }
      
      int width = (int) Math.ceil(COLORS.length / ROWS);
      if(x>=width) x = 0;
      if(x<0) x = width-1;
      if(y<0) y = ROWS-1;
      if(y>=ROWS) y = 0;
      
      i = y + ROWS*x;      
      _picker.SetSelectedIndex(i);
    }
    
  }
  
  /////////////////////////////////////////////////////////////////////////////

  private JDialog _dialog;
  private Color _color;
  private List<JButton> _buttons = new ArrayList<JButton>();

  /**
   * Displays the picker dialog modally until the user cancels or clicks a
   * color, at which point the dialog is closed and the color selected is
   * returned.
   * @param frame
   */
  public Color pickColor() {
    if(_dialog == null) {
      prepareDialog(Romeo.getMainFrame());
    }
    _dialog.setVisible(true);
    return _color;
  }

  public void setRelativeTo(Component c) {
    if(_dialog == null) {
      prepareDialog(Romeo.getMainFrame());
    }
    _dialog.setLocationRelativeTo(c);
  }

  /**
   * Returns the last color picked
   * @return color
   */
  public Color getLastColor() {
    return _color;
  }
  
  public void cancel() {
    _color = null;
    _dialog.setVisible(false);
  }

  private void prepareDialog(Frame frame) {
    JDialog dialog = new JDialog(frame, "Select colour", true);
    //dialog.setSize(600,285);
    dialog.setResizable(false);
    dialog.setLocationRelativeTo(frame);

    Font buttonFont = new Font("Monospaced", Font.PLAIN, 12);

    Container contentPane = dialog.getContentPane();
    JPanel panel = new JPanel();
    GridBagConstraints gbc = GuiUtils.prepGridBag(panel);
    gbc.insets = new Insets(1, 1, 1, 1);
    gbc.weightx = 2;
    //gbc.weighty = 2;

    int column = 0;
    int row = 0;
    int index = 0;
    while(index < COLORS.length) {
      final Color color = COLORS[index++];

      String label = Convert.toStr(color);
      JButton button = new JButton(label);
      button.setBackground(color);
      button.setFont(buttonFont);

      button.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          _color = color;
          _dialog.setVisible(false);
        }
      });

      gbc.gridx = column;
      gbc.gridy = row;
      panel.add(button, gbc);
      
      _buttons.add(button);

      row++;
      if(row >= ROWS) {
        row = 0;
        column++;
      }
    }

    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(new ColorPickerCancelAction(this));

    contentPane.add(panel, BorderLayout.NORTH);
    contentPane.add(cancel, BorderLayout.SOUTH);

    dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DOWN"), "DOWN");
    dialog.getRootPane().getActionMap().put("DOWN", new ColorPickerMovementAction(this, ColorPickerMovementAction.Direction.down) );
    
    dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("UP"), "UP");
    dialog.getRootPane().getActionMap().put("UP", new ColorPickerMovementAction(this, ColorPickerMovementAction.Direction.up) );
    
    dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("LEFT"), "LEFT");
    dialog.getRootPane().getActionMap().put("LEFT", new ColorPickerMovementAction(this, ColorPickerMovementAction.Direction.left) );
    
    dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("RIGHT"), "RIGHT");
    dialog.getRootPane().getActionMap().put("RIGHT", new ColorPickerMovementAction(this, ColorPickerMovementAction.Direction.right) );
    
    dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "CANCEL");
    dialog.getRootPane().getActionMap().put("CANCEL", new ColorPickerCancelAction(this) );
    
    dialog.pack();
    dialog.validate();
    
    _dialog = dialog;
  }
  
  private int GetSelectedIndex() {
    for(int i=0; i<_buttons.size(); i++) {
      if(_buttons.get(i).isFocusOwner()) return i;
    }
    return -1;
  }
  
  private void SetSelectedIndex(int i) {
    if(i < 0 || i >= _buttons.size()) throw new IndexOutOfBoundsException("Invalid button index " + i);
    _buttons.get(i).requestFocus();
  }

  //(uncomment for quick testing while developing)
//  public static void main(String[] args) {
//    JFrame frame = new JFrame("test");
//    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//    frame.setVisible(true);
//    ColorPicker p = new ColorPicker();
//    p.prepareDialog(frame);
//    p.pickColor();
//    frame.setVisible(false);
//    System.exit(0);
//  }
}
