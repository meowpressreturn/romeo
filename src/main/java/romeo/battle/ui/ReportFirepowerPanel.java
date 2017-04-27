package romeo.battle.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

import romeo.battle.IBattleMetrics;
import romeo.utils.GuiUtils;

public class ReportFirepowerPanel extends JPanel {
  public ReportFirepowerPanel(IBattleMetrics metrics) {
    String[] players = metrics.getPlayers();
    List<? extends Number> battleLengths = metrics.getBattleLengths();
    Number median = (Number) battleLengths.get(battleLengths.size() / 2);
    Dimension chartSize = Report.getChartSize(players.length);
    double raTick = Report.getRaTick(metrics);

    //Charts - firepower
    //System.out.println("Prepare average firepower chart");
    //    DefaultTableXYDataset xyDs = new DefaultTableXYDataset();
    //    for(int p=0; p < players.length; p++)
    //    {
    //      String player = players[p];
    //      XYSeries series = new XYSeries(player,false,false);
    //      List<? extends Number> data = metrics.getFirepower(player, false);
    //      for(int i=0; i < data.size(); i++)
    //      { //Copy data into series
    //        series.add( i, data.get(i).doubleValue() );
    //      }
    //      xyDs.addSeries(series);
    //    }
    //    JFreeChart fpChart = ChartFactory.createXYStepChart("Average post round firepower","Round","Firepower",xyDs,PlotOrientation.VERTICAL,true,true,false);
    //    XYPlot xyPlot = (XYPlot)fpChart.getPlot();
    //    //System.out.println(xyPlot.getDomainAxis().getClass()); //for a step chart it defaults to DATE!!
    //    NumberAxis na = new NumberAxis("Round");
    //    na.setTickUnit(new NumberTickUnit(raTick));
    //    xyPlot.setDomainAxis(na);
    //    
    //    //NumberAxis xAxis = (NumberAxis)xyPlot.getDomainAxis();
    //    //xAxis.setTickUnit(new NumberTickUnit(raTick));
    //    //xAxis.setLowerBound(1d);
    //    ChartPanel fpChartPanel = new ChartPanel(fpChart);
    //    fpChartPanel.setPreferredSize(chartSize);
    //    fpChartPanel.setPopupMenu(null);
    //    fpChartPanel.setInitialDelay(0);

    //System.out.println("Prepare adjusted average firepower chart");
    DefaultTableXYDataset xyDsAdj = new DefaultTableXYDataset();
    for(int p = 0; p < players.length; p++) {
      String player = players[p];
      XYSeries series = new XYSeries(player, false, false);
      List<? extends Number> data = metrics.getFirepower(player, true);
      for(int i = 0; i < data.size(); i++) { //Copy data into series
        series.add((double) i, ((Number) data.get(i)).doubleValue());
      }
      xyDsAdj.addSeries(series);
    }
    JFreeChart fpChartAdj = ChartFactory.createXYStepChart("Adjusted average post round firepower", "Round",
        "Firepower", xyDsAdj, PlotOrientation.VERTICAL, true, true, false);
    XYPlot adjFpPlot = (XYPlot) fpChartAdj.getPlot();
    NumberAxis xAxis = new NumberAxis("Round");
    xAxis.setTickUnit(new NumberTickUnit(raTick));
    adjFpPlot.setDomainAxis(xAxis); //replaces defaul DateAxis of the STepCHart

    ValueMarker medianRoundsFp = new ValueMarker(median.doubleValue());
    medianRoundsFp.setLabel("M");
    adjFpPlot.addDomainMarker(medianRoundsFp);

    ChartPanel fpChartPanelAdj = new ChartPanel(fpChartAdj);
    fpChartPanelAdj.setPreferredSize(chartSize);
    fpChartPanelAdj.setPopupMenu(null);
    fpChartPanelAdj.setInitialDelay(0);

    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = Report.getDefaultInsets();
    add(fpChartPanelAdj, gbc);
    //    gbc.gridy++;
    //    add(fpChartPanel,gbc);
    gbc.gridy++;
    //Filler
    gbc.gridy++;
    gbc.gridx = 0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 2;
    gbc.weighty = 2;
    add(new JLabel(""), gbc);
    revalidate(); //?
  }
}
