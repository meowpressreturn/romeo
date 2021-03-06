package romeo.ui;

import java.awt.LayoutManager;

import javax.swing.JPanel;

import romeo.utils.INamed;

public class NamedPanel extends JPanel implements INamed {
  protected String _name;

  public NamedPanel() {
    super();
  }

  public NamedPanel(String name) {
    super();
    setName(name);
  }

  public NamedPanel(boolean isDoubleBuffered) {
    super(isDoubleBuffered);
  }

  public NamedPanel(LayoutManager layout, boolean isDoubleBuffered) {
    super(layout, isDoubleBuffered);
  }

  public NamedPanel(LayoutManager layout) {
    super(layout);
  }

  @Override
  public void setName(String name) {
    _name = name;
  }

  @Override
  public String getName() {
    return _name;
  }

}
