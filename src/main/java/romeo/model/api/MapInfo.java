package romeo.model.api;

/**
 * Bean for representing the coordinates bounds of the map.
 * As of 0.6.3 this class is now an immutable value object.
 */
public class MapInfo {
  
  private int _leftBorder;
  private int _rightBorder;
  private int _topBorder;
  private int _bottomBorder;
  private int _maxTurn;
  
  public MapInfo(int leftBorder, int topBorder, int rightBorder, int bottomBorder, int maxTurn) {
    _leftBorder = leftBorder;
    _topBorder = topBorder;
    _rightBorder = rightBorder;
    _bottomBorder = bottomBorder;
    _maxTurn = maxTurn;
  }

  public int getMaxTurn() {
    return _maxTurn;
  }

  /**
   * Return the distance between the furthest west and furthest east world
   * @return width
   */
  public int getWidth() {
    return _rightBorder - _leftBorder;
  }

  /**
   * Returns the distance between the most northerlyand the most southerly
   * worlds
   * @return hight
   */
  public int getHeight() {
    return _bottomBorder - _topBorder;
  }

  /**
   * Returns the y coordinate of the most southern world
   * @return bottomBorder
   */
  public int getBottomBorder() {
    return _bottomBorder;
  }

  /**
   * Returns the x coordinate of the most westerly world
   * @return leftBorder
   */
  public int getLeftBorder() {
    return _leftBorder;
  }

  /**
   * Returns the x coordinate of the most easterly world
   * @return rightBorder
   */
  public int getRightBorder() {
    return _rightBorder;
  }

  /**
   * Returns the y coordinate of the most northely world
   * @return topBorder
   */
  public int getTopBorder() {
    return _topBorder;
  }
}
