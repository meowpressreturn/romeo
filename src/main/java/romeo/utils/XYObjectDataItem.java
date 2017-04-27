/*
 * XYObjectDataItem.java
 * Created on Mar 6, 2006
 */
package romeo.utils;

import org.jfree.data.xy.XYDataItem;

/**
 * Simple implementation of the XYDataItem index that holds a reference to the
 * original object that provided the data (useful for tooltip generators)
 */
public class XYObjectDataItem extends XYDataItem {
  protected Object _object;

  public XYObjectDataItem(double x, double y, Object object) {
    super(x, y);
    _object = object;
  }

  public XYObjectDataItem(Number x, Number y, Object object) {
    super(x, y);
    _object = object;
  }

  public Object getObject() {
    return _object;
  }

  public void setObject(Object object) {
    _object = object;
  }

}
