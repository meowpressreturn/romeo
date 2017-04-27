/*
 * XYObjectTooltipGenerator.java
 * Created on Mar 6, 2006
 */
package romeo.utils;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Tooltip generator that knows how to read the object from an XYObjectDataItem
 * and will display its toString() as the text
 */
public class XYObjectTooltipGenerator implements XYToolTipGenerator {

  /**
   * Callback defined by the XYToolTipGenerator interface. Will lookup the data
   * item and if it is an {@link XYObjectDataItem} will delegate to
   * getTooltipText() to return the tooltip text.
   * @param dataset
   * @param series
   *          series index in dataset
   * @param item
   *          item index in series
   * @return tooltip
   */
  @Override
  public String generateToolTip(XYDataset dataset, int series, int item) {
    if(dataset instanceof XYSeriesCollection) {
      XYSeriesCollection sc = (XYSeriesCollection) dataset;
      Object object = sc.getSeries(series).getItems().get(item);
      if(object instanceof XYObjectDataItem) {
        XYObjectDataItem di = (XYObjectDataItem) object;
        return getTooltipText(di.getObject(), di.getX(), di.getY());
      }
    }
    return null;
  }

  /**
   * Returns the objects toString(). (Subclass may override for more complex
   * behaviour)
   * @param object
   * @param x
   * @param y
   * @return tooltipText
   */
  public String getTooltipText(Object object, Number x, Number y) {
    return object == null ? null : object.toString();
  }

}
