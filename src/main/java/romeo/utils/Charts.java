package romeo.utils;

import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JScrollPane;

import org.apache.commons.beanutils.PropertyUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import romeo.units.api.IUnit;

/**
 * Some utility methods used when preparing the charts
 */
public class Charts {
  protected static final Number ZERO = new Integer(0);

  /**
   * Group Numbers into categories based on their int value. Given a SORTED
   * collection of Number instances will create an instance of KeyedValues that
   * contains the number of occurances of a category keyed by the category name,
   * a string generated based on the size of the category and what category it
   * is. Stuff that falls above maxCategory goes into a final category "Above "
   * n. If categorySize or maxCategory are <1 they are changed to 1.
   * @param data
   *          A collection of Numbers whose intValue is used
   * @param categorySize
   * @param maxCategory
   * @return frequencies
   */
  public static KeyedValues getFrequencies(Collection<? extends Number> data, int categorySize, int maxCategory) {
    if(categorySize < 1) {
      categorySize = 1;
    }
    if(maxCategory < 1) {
      maxCategory = 1;
    }
    DefaultKeyedValues keyedValues = new DefaultKeyedValues();
    Iterator<? extends Number> i = data.iterator();
    int currentCategory = categorySize; //top value for this category
    int currentCategoryValue = 0;
    int max = Integer.MIN_VALUE;
    String key = "0";
    while(i.hasNext()) {
      int thisValue = ((Number) i.next()).intValue();
      //Assert that this is the highest or equal highest value we have encountered
      if(max > thisValue)
        throw new IllegalStateException("data not sorted correctly");
      max = thisValue; //Update the max value encountered
      //Now add categories to the KV until we find and add this value
      //to its appropriate category
      boolean foundCategory = false;
      while(!foundCategory) {
        //Determine key for current category
        if(currentCategory > maxCategory) {
          currentCategory = Integer.MAX_VALUE;
          key = categorySize == 1 ? ">" + maxCategory : "Above " + maxCategory;
        } else {
          key = categorySize == 1 ? (currentCategory - 1) + ""
              : (currentCategory - categorySize) + " to " + (currentCategory - 1);
        }
        //..        

        if(thisValue < currentCategory) {
          currentCategoryValue++;
          foundCategory = true;
        } else {
          //We have entered a new category. Save the old one to the DS
          if(categorySize == 1 && currentCategory - 1 == 0 && currentCategoryValue == 0) {
            ; //Skip adding an empty size 1 zero category
          } else {
            keyedValues.addValue(key, new Integer(currentCategoryValue));
          }
          currentCategoryValue = 0;
          currentCategory += categorySize;
        }
      }
    }
    //save last category if it has nonzero value
    if(currentCategoryValue > 0) {
      keyedValues.addValue(key, new Integer(currentCategoryValue));
    }
    return keyedValues;
  }

  /**
   * Adds values to a DefaultCategoryDataset from an instance of KeyedValues
   * using the key in the KeyedValues as the column key, and with the specified
   * rowKey. This method can also create the dataset for you if you pass in null
   * for dcd. Or you could pass in a new instance of a different subclass and
   * then get it back in the return value saving you a whole line of code.
   * @param dcd
   *          The DefaultCategoryDataset to modify (null to create it)
   * @param keyedValues
   *          The values to copy into the dataset
   * @param rowKey
   *          The rowKey to use when adding (may not be null)
   * @return dcd The DefaultCategoryDataset instance
   */
  public static DefaultCategoryDataset addColumns(DefaultCategoryDataset dcd, KeyedValues keyedValues,
      Comparable<? extends Object> rowKey) {
    if(dcd == null) {
      dcd = new DefaultCategoryDataset();
    }
    int n = keyedValues.getItemCount();
    for(int col = 0; col < n; col++) {
      @SuppressWarnings("unchecked")
      Comparable<? extends Object> colKey = keyedValues.getKey(col);
      Number value = keyedValues.getValue(col);
      dcd.addValue(value, rowKey, colKey);
    }
    return dcd;
  }

  /**
   * Returns the last of a list of Number as an int
   * @param data
   *          a List of Number
   * @return lastInt
   */
  public static int getLastInt(List<? extends Number> data) {
    int max = ((Number) data.get(data.size() - 1)).intValue();
    return max;
  }

  public static JScrollPane prepareUnitScatterGraph(XYSeriesCollection dataset, String title, String xLabel,
      String yLabel) {
    XYObjectTooltipGenerator ttGen = new XYObjectTooltipGenerator() {
      @Override
      public String getTooltipText(Object object, Number x, Number y) {
        if(object instanceof IUnit) {
          IUnit unit = (IUnit) object;
          return unit.getName() + " " + unit.getAttacks() + "*" + unit.getOffense() + "/" + unit.getDefense() + ", cpx="
              + unit.getComplexity() + ", license=" + unit.getLicense() + ", cost=" + unit.getCost() + ", fp="
              + Convert.toStr(unit.getFirepower(), 2);
        } else {
          return super.getTooltipText(object, x, y);
        }
      }
    };
    JFreeChart chart = ChartFactory.createScatterPlot(title, xLabel, yLabel, dataset, PlotOrientation.HORIZONTAL, false,
        true, false);
    XYPlot xyp = (XYPlot) chart.getPlot();
    xyp.getRenderer().setBaseToolTipGenerator(ttGen);
    ChartPanel panel = new ChartPanel(chart);
    final Dimension CHART_DIMENSION = new Dimension(360, 400);
    panel.setPreferredSize(CHART_DIMENSION);
    panel.setPopupMenu(null);
    panel.setInitialDelay(0);
    JScrollPane scrollPane = new JScrollPane(panel);
    scrollPane.setPreferredSize(CHART_DIMENSION);
    return scrollPane;
  }

  /**
   * Prepare an XYDataset from a List of beans by extracting the specified
   * properties for use as x and y values.The properties must be numeric.
   * @param dataset
   * @param data
   *          a list of beans
   * @param propertyX
   *          name of property providing the x value
   * @param propertyY
   *          name of the property to supply the Y value
   */
  public static void initialiseXYBeanDataSet(XYSeriesCollection dataset, List<? extends Object> data, String propertyX,
      String propertyY) {
    XYSeries series = new XYSeries(propertyX + " vs " + propertyY, true, true);
    dataset.removeAllSeries();
    Iterator<? extends Object> i = data.iterator();
    while(i.hasNext()) {
      Object bean = i.next();
      try {
        double x = ((Number) PropertyUtils.getProperty(bean, propertyX)).doubleValue();
        double y = ((Number) PropertyUtils.getProperty(bean, propertyY)).doubleValue();
        XYObjectDataItem item = new XYObjectDataItem(x, y, bean);
        series.add(item);
      } catch(Exception e) {
        e.printStackTrace(); //print trace,but otherwise ignore
      }
    }
    dataset.addSeries(series);
  }
}
