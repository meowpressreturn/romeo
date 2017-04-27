package romeo.units.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import romeo.Romeo;
import romeo.model.api.IServiceListener;
import romeo.ui.forms.IFieldChangeListener;
import romeo.ui.forms.UnitTypesSelector;
import romeo.units.api.IUnit;
import romeo.units.api.IUnitService;
import romeo.units.api.UnitUtils;
import romeo.utils.BeanComparator;
import romeo.utils.Charts;
import romeo.utils.GuiUtils;
import romeo.utils.XYObjectDataItem;

public class UnitGraphsPanel extends JTabbedPane implements IServiceListener {
  private IUnitService _unitService;
  private DefaultCategoryDataset _priceDataset;
  private XYSeriesCollection _licenseVsCpxDataset;
  private XYSeriesCollection _costVsCpxDataset;
  private XYSeriesCollection _amoVsDefDataset;
  private XYSeriesCollection _cpxVsFpDataset;
  private XYSeriesCollection _costVsFpDataset;
  private XYSeriesCollection _licenseVsFpDataset;
  private DefaultCategoryDataset _compareDs;
  private UnitTypesSelector _compareTypesSelector;

  private static final Dimension CHART_DIMENSION = new Dimension(360, 460);

  /**
   * Constructor
   * @param unitService
   */
  public UnitGraphsPanel(IUnitService unitService) {
    _unitService = Objects.requireNonNull(unitService, "unitService may not be null");
    _unitService.addListener(this);

    Romeo.incrementSplashProgress("Comparison Chart");
    JComponent compare = prepareCompareChart();
    this.add(compare, "Compare");

    Romeo.incrementSplashProgress("Base Costs Graph");
    JComponent priceGraph = preparePriceGraph();
    this.add(priceGraph, "Cost");

    Romeo.incrementSplashProgress("Lic vs Cpx Graph");
    JComponent licVsCpx = prepareLicenseVsCpxGraph();
    this.add(licVsCpx, "LicVsCpx");

    Romeo.incrementSplashProgress("Cost vs Cpx Graph");
    JComponent costVsCpx = prepareCostVsCpxGraph();
    this.add(costVsCpx, "CostVsCpx");

    Romeo.incrementSplashProgress("Fp vs Cpx Graph");
    JComponent cpxVsFp = prepareCpxVsFpGraph();
    this.add(cpxVsFp, "FpVsCpx");

    Romeo.incrementSplashProgress("Fp vs Cost Graph");
    JComponent costVsFp = prepareCostVsFpGraph();
    this.add(costVsFp, "FpVsCost");

    Romeo.incrementSplashProgress("Fp vs Lic Graph");
    JComponent licenseVsFp = prepareLicenseVsFpGraph();
    this.add(licenseVsFp, "FpVsLic");

    Romeo.incrementSplashProgress("Amo vs Def Graph");
    JComponent amoVsDef = prepareAmoVsDefGraph();
    this.add(amoVsDef, "AmoVsDef");
  }
  
  @Override
  public String toString() {
    return "UnitGraphsPanel@" + System.identityHashCode(this);
  }

  @Override
  public void dataChanged(EventObject event) {
    preparePriceDataset(false);
    prepareLicenseVsCpxDataset();
    prepareCostVsCpxDataset();
    prepareAmoVsDefDataset();
    prepareCompareDataset();
  }

  protected void preparePriceDataset(boolean init) {
    List<IUnit> units = _unitService.getUnits();
    if(init) {
      units = new ArrayList<IUnit>(units);
      Comparator<Object> comparator = new BeanComparator("cost");
      Collections.sort(units, comparator);
      _priceDataset.clear();
    }
    for(IUnit unit : units) {
      if(init) {
        _priceDataset.addValue(new Integer(unit.getCost()), "Base Cost", unit.getName());
      } else {
        _priceDataset.setValue(new Integer(unit.getCost()), "Base Cost", unit.getName());
      }
    }
  }

  protected JComponent preparePriceGraph() {
    _priceDataset = new DefaultCategoryDataset();
    preparePriceDataset(true);
    JFreeChart chart = ChartFactory.createBarChart("", "", "", _priceDataset, PlotOrientation.HORIZONTAL, false, true,
        false);

    //chart.getPlot().setBackgroundPaint(Color.WHITE);
    //chart.getPlot().get

    ChartPanel panel = new ChartPanel(chart);

    //panel.setBackground( Color.RED ); //doesnt do anything

    int numUnits = _unitService.getUnits().size();
    panel.setPreferredSize(new Dimension(360, (numUnits * 13) + 16));
    panel.setPopupMenu(null);
    panel.setInitialDelay(0);

    JPanel fullPanel = new JPanel();
    GridBagConstraints gbc = GuiUtils.prepGridBag(fullPanel);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    JButton sortButton = new JButton("Sort by base cost");
    sortButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        preparePriceDataset(true);
      }
    });
    fullPanel.add(sortButton, gbc);
    gbc.gridy++;
    fullPanel.add(panel, gbc);
    JScrollPane scrollPane = new JScrollPane(fullPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    return scrollPane;
  }

  protected void prepareLicenseVsCpxDataset() {
    if(_licenseVsCpxDataset == null) {
      _licenseVsCpxDataset = new XYSeriesCollection();
    }
    Charts.initialiseXYBeanDataSet(_licenseVsCpxDataset, _unitService.getUnits(), "complexity", "license");
  }

  protected JComponent prepareLicenseVsCpxGraph() {
    _licenseVsCpxDataset = new XYSeriesCollection();
    prepareLicenseVsCpxDataset();
    return Charts.prepareUnitScatterGraph(_licenseVsCpxDataset, "", "Complexity", "License");
  }

  protected void prepareCostVsCpxDataset() {
    if(_costVsCpxDataset == null) {
      _costVsCpxDataset = new XYSeriesCollection();
    }
    Charts.initialiseXYBeanDataSet(_costVsCpxDataset, _unitService.getUnits(), "complexity", "cost");
  }

  protected JComponent prepareCostVsCpxGraph() {
    _costVsCpxDataset = new XYSeriesCollection();
    prepareCostVsCpxDataset();
    return Charts.prepareUnitScatterGraph(_costVsCpxDataset, "", "Complexity", "Base Cost");
  }

  protected void prepareAmoVsDefDataset() {
    if(_amoVsDefDataset == null) {
      _amoVsDefDataset = new XYSeriesCollection();
    }
    XYSeries series = new XYSeries("multipliedOffense vs defense", true, true);
    _amoVsDefDataset.removeAllSeries();
    for(IUnit unit : _unitService.getUnits()) {
      double x = unit.getAttacks() * unit.getOffense();
      double y = unit.getDefense();
      XYObjectDataItem item = new XYObjectDataItem(x, y, unit);
      series.add(item);
    }
    _amoVsDefDataset.addSeries(series);
  }

  protected JComponent prepareAmoVsDefGraph() {
    _amoVsDefDataset = new XYSeriesCollection();
    prepareAmoVsDefDataset();
    return Charts.prepareUnitScatterGraph(_amoVsDefDataset, "", "Attacks * Offense", "Defense");
  }

  protected void prepareCpxVsFpDataset() {
    if(_cpxVsFpDataset == null) {
      _cpxVsFpDataset = new XYSeriesCollection();
    }
    Charts.initialiseXYBeanDataSet(_cpxVsFpDataset, _unitService.getUnits(), "complexity", "firepower");
  }

  protected JComponent prepareCpxVsFpGraph() {
    _cpxVsFpDataset = new XYSeriesCollection();
    prepareCpxVsFpDataset();
    return Charts.prepareUnitScatterGraph(_cpxVsFpDataset, "", "Complexity", "Firepower");
  }

  protected void prepareCostVsFpDataset() {
    if(_costVsFpDataset == null) {
      _costVsFpDataset = new XYSeriesCollection();
    }
    Charts.initialiseXYBeanDataSet(_costVsFpDataset, _unitService.getUnits(), "cost", "firepower");
  }

  protected JComponent prepareCostVsFpGraph() {
    _cpxVsFpDataset = new XYSeriesCollection();
    prepareCostVsFpDataset();
    return Charts.prepareUnitScatterGraph(_costVsFpDataset, "", "Base Cost", "Firepower");
  }

  protected void prepareLicenseVsFpDataset() {
    if(_licenseVsFpDataset == null) {
      _licenseVsFpDataset = new XYSeriesCollection();
    }
    Charts.initialiseXYBeanDataSet(_licenseVsFpDataset, _unitService.getUnits(), "license", "firepower");
  }

  protected JComponent prepareLicenseVsFpGraph() {
    _licenseVsFpDataset = new XYSeriesCollection();
    prepareLicenseVsFpDataset();
    return Charts.prepareUnitScatterGraph(_licenseVsFpDataset, "", "License", "Firepower");
  }

  protected void prepareCompareDataset() {
    /**
     * what I want for this chart is to be able to specify which units and which
     * of these stats i want to see, and whether to use stats that are relative
     * to the overall list of units or to those units showing. (in the later
     * case then if there is but one unit it would get a perfect score!)
     */

    _compareDs.clear();
    List<IUnit> units = _unitService.getUnits();

    List<IUnit> selectedUnits = _compareTypesSelector.getSelectedTypes();
    double[] amoRange = _unitService.getRange("multipliedOffense");
    double[] defenseRange = _unitService.getRange("defense");
    double[] costRange = _unitService.getRange("cost");
    double[] pdRange = _unitService.getRange("pd");
    double[] licenseRange = _unitService.getRange("license");
    double[] cpxRange = _unitService.getRange("complexity");
    double maxPrecision = getMaxPrecision(units, pdRange);
    double[] lfRange = _unitService.getRange("logisticsFactor");
    double maxDeployability = getMaxDeployability(units, lfRange);
    double maxBudget = getMaxBudget(units, costRange);

    for(IUnit unit : selectedUnits) {
      if(unit != null) {
        double amoPercent = (double) (unit.getAttacks() * unit.getOffense()) / amoRange[1];
        //double attackPercent = (double)unit.getAttacks() / attacksRange[1];
        //double offensePercent = (double)unit.getOffense() / offenseRange[1];
        double defensePercent = (double) unit.getDefense() / defenseRange[1];
        double precision = pdRange[1] - unit.getPd();
        double precisionPercent = precision / maxPrecision;
        double deployability = calculateDeployability(unit, lfRange);
        double deployabilityPercent = deployability / maxDeployability;
        double budget = costRange[1] - unit.getCost();
        double budgetPercent = budget / maxBudget;
        double license = licenseRange[1] - (double) unit.getLicense();
        double licensePercent = license / licenseRange[1] - licenseRange[0];
        double lfPercent = (double) UnitUtils.getLogisticsFactor(unit) / lfRange[1];
        double cpx = cpxRange[1] - unit.getComplexity();
        double cpxPercent = cpx / cpxRange[1] - cpxRange[0];

        /**
         * there is an issue here where if the first one you add in a series
         * doesnt have >0 value (or sifnificant?) value then the plot doesnt
         * show that bar and it doesnt show a dotr at zero for the category of
         * the unit that had the zero.
         */
        final double MIN = 0.0001d;
        if(amoPercent <= MIN)
          amoPercent = MIN;
        if(defensePercent <= MIN)
          defensePercent = MIN;
        if(precisionPercent <= MIN)
          precisionPercent = MIN;
        if(deployabilityPercent <= MIN)
          deployabilityPercent = MIN;
        if(lfPercent <= MIN)
          lfPercent = MIN;
        if(budgetPercent <= MIN)
          budgetPercent = MIN;
        if(cpxPercent <= MIN)
          cpxPercent = MIN;
        if(licensePercent <= MIN)
          licensePercent = MIN;

        _compareDs.addValue(amoPercent, unit.getName(), "Weaponry");
        _compareDs.addValue(defensePercent, unit.getName(), "Defense");
        _compareDs.addValue(precisionPercent, unit.getName(), "Precision");
        _compareDs.addValue(deployabilityPercent, unit.getName(), "Deployability");
        _compareDs.addValue(lfPercent, unit.getName(), "Logistics");
        _compareDs.addValue(budgetPercent, unit.getName(), "Cost");
        _compareDs.addValue(cpxPercent, unit.getName(), "Complexity");
        _compareDs.addValue(licensePercent, unit.getName(), "License");
      }
    }
  }

  private double calculateDeployability(IUnit unit, double[] lfRange) {
    /**
     * this stat doesnt really work except for th v4 zenrin. Even a mek is only
     * half as deployable. Need to somehow logarithmise it such that while v4
     * zenrin stays at 300 other values are higher. Most units with more than a
     * tiny carrysize have practically zero deployability right now which isnt
     * really true, of course its subjective really.
     */
    double d = 0;
    if(unit.getCarry() < 0) { //Carried unit
      d = Math.sqrt(lfRange[1] / (double) (-1 * unit.getCarry()));
    } else { //Mobile unit
      d = unit.getSpeed();
    }

    return d;
  }

  private double getMaxBudget(List<IUnit> units, double[] costRange) {
    //ya know ya should put all this into one method so you dont need to
    //iterate and iterate and iterate again if ya gets wot I mean...
    double b = 0;
    for(IUnit unit : units) {
      double budget = costRange[1] - unit.getCost();
      if(budget > b) {
        b = budget;
      }
    }
    return b;
  }

  private double getMaxDeployability(List<IUnit> units, double[] lfRange) {

    double d = 0;
    for(IUnit unit : units) {
      double deployability = calculateDeployability(unit, lfRange);
      if(deployability > d) {
        d = deployability;
      }
    }
    //System.out.println("maxDeployability = " + d);
    return d;
  }

  private double getMaxPrecision(List<IUnit> units, double[] pdRange) {
    double p = 0;
    for(IUnit unit : units) {
      double precision = pdRange[1] - unit.getPd();
      if(precision > p) {
        p = precision;
      }
    }
    return p;
  }

  protected JComponent prepareCompareChart() {
    _compareTypesSelector = new UnitTypesSelector(_unitService);
    _compareDs = new DefaultCategoryDataset(); //needs selector to exist
    prepareCompareDataset();
    SpiderWebPlot plot = new SpiderWebPlot(_compareDs);
    JFreeChart chart = new JFreeChart(plot);
    ChartPanel panel = new ChartPanel(chart);
    panel.setPreferredSize(CHART_DIMENSION);
    panel.setPopupMenu(null);
    panel.setInitialDelay(0);

    _compareTypesSelector.addFieldChangeListener(new IFieldChangeListener() {
      @Override
      public void valueChanged(Object field) {
        prepareCompareDataset();
      }
    });

    JPanel fullPanel = new JPanel();
    GridBagConstraints gbc = GuiUtils.prepGridBag(fullPanel);
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 1;
    gbc.weighty = 1;
    fullPanel.add(_compareTypesSelector, gbc);
    gbc.gridy++;
    fullPanel.add(panel, gbc);
    JScrollPane scrollPane = new JScrollPane(fullPanel);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getVerticalScrollBar().setBlockIncrement(24);
    return scrollPane;
  }

}
