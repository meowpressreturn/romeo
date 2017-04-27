package romeo.battle.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import romeo.battle.impl.PlayerIndexedValues;
import romeo.utils.GuiUtils;

public class ReportPdPanel extends JPanel {
  public ReportPdPanel(IBattleMetrics metrics) {
    String[] players = metrics.getPlayers();
    List<? extends Number> battleLengths = metrics.getBattleLengths();
    Number median = (Number) battleLengths.get(battleLengths.size() / 2);
    Number shortest = (Number) battleLengths.get(0);
    double average = metrics.getAverageRounds();
    Dimension chartSize = Report.getChartSize(players.length);
    double raTick = Report.getRaTick(metrics);

    //Charts - PD
    //Cumulative PD
    DefaultTableXYDataset cumulativePdDs = new DefaultTableXYDataset();
    PlayerIndexedValues cumulativePd = new PlayerIndexedValues(players);
    Map<String, XYSeries> cpdSeries = new HashMap<String, XYSeries>();
    int n = metrics.getRecordedRoundCount();
    for(int r = 0; r < n; r++) {
      for(int p = 0; p < players.length; p++) {
        String player = players[p];
        List<? extends Number> data = metrics.getPopulationDamage(player, true);
        XYSeries series = null;
        if(r == 0) { //Create the series before first use
          series = new XYSeries(player, false, false);
          cpdSeries.put(player, series);
          //nb: we cant add series to tableDS yet or we wont be able to add additonal
          //    values for other players for round 0
        }
        series = (series == null) ? (XYSeries) cpdSeries.get(player) : series;
        cumulativePd.addValue(player, ((Number) data.get(r)).doubleValue());
        series.add((double) r, cumulativePd.getValue(player));
      }
    }
    for(int p = 0; p < players.length; p++) { //Now add the series to table.
      XYSeries series = (XYSeries) cpdSeries.get(players[p]);
      cumulativePdDs.addSeries(series);
    }

    //todo: I should quite like to add a second Y axis to show total pop killed
    JFreeChart cumulativePdChart = ChartFactory.createXYStepChart("Cumulative PD (Adjusted)", "Round", "Pop Damage",
        cumulativePdDs, PlotOrientation.VERTICAL, true, true, false);
    NumberAxis cPdNa = new NumberAxis("Round");
    cPdNa.setTickUnit(new NumberTickUnit(raTick));
    XYPlot cPdPlot = (XYPlot) cumulativePdChart.getPlot();
    cPdPlot.setDomainAxis(cPdNa);

    ValueMarker medianRounds = new ValueMarker(median.doubleValue());
    medianRounds.setLabel("M");
    cPdPlot.addDomainMarker(medianRounds);

    ValueMarker shortestRounds = new ValueMarker(shortest.doubleValue());
    shortestRounds.setLabel("S");
    cPdPlot.addDomainMarker(shortestRounds);

    ValueMarker averageRounds = new ValueMarker(average);
    averageRounds.setLabel("A");
    cPdPlot.addDomainMarker(averageRounds);

    ChartPanel cumulativePdChartPanel = new ChartPanel(cumulativePdChart);
    cumulativePdChartPanel.setPreferredSize(chartSize);
    cumulativePdChartPanel.setPopupMenu(null);
    cumulativePdChartPanel.setInitialDelay(0);

    //Average PD
    //    DefaultTableXYDataset xyDsPd = new DefaultTableXYDataset();
    //    for(int p=0; p < players.length; p++)
    //    {
    //      String player = players[p];
    //      XYSeries series = new XYSeries(player,false,false);
    //      List<? extends Number> data = metrics.getPopulationDamage(player, false);
    //      for(int i=0; i < data.size(); i++)
    //      {
    //        series.add((double)i,((Number)data.get(i)).doubleValue());
    //      }
    //      xyDsPd.addSeries(series);
    //    }
    //    JFreeChart pdChart = ChartFactory.createXYStepChart("Average round PD","Round","Pop Damage",
    //                          xyDsPd,PlotOrientation.VERTICAL,true,true,false);
    //    XYPlot xyPlotPd = (XYPlot)pdChart.getPlot();
    //    
    //    NumberAxis naPd = new NumberAxis("Round");
    //    naPd.setTickUnit(new NumberTickUnit(raTick));
    //    xyPlotPd.setDomainAxis(naPd);
    //    
    //    
    //    ChartPanel pdChartPanel = new ChartPanel(pdChart);
    //    pdChartPanel.setPreferredSize(chartSize);
    //    pdChartPanel.setPopupMenu(null);
    //    pdChartPanel.setInitialDelay(0);

    //Adjusted PD chart
    DefaultTableXYDataset xyDsAdjPd = new DefaultTableXYDataset();
    for(int p = 0; p < players.length; p++) {
      String player = players[p];
      XYSeries series = new XYSeries(player, false, false);
      List<? extends Number> data = metrics.getPopulationDamage(player, false);
      for(int i = 0; i < data.size(); i++) {
        series.add((double) i, ((Number) data.get(i)).doubleValue());
      }
      xyDsAdjPd.addSeries(series);
    }
    JFreeChart pdChartAdj = ChartFactory.createXYStepChart("Adjusted average PD", "Round", "Pop Damage", xyDsAdjPd,
        PlotOrientation.VERTICAL, true, true, false);
    XYPlot adjPdPlot = (XYPlot) pdChartAdj.getPlot();
    NumberAxis xAxisPd = new NumberAxis("Round");
    xAxisPd.setTickUnit(new NumberTickUnit(raTick));
    adjPdPlot.setDomainAxis(xAxisPd); //replaces defaul DateAxis of the STepCHart
    ChartPanel pdChartPanelAdj = new ChartPanel(pdChartAdj);
    pdChartPanelAdj.setPreferredSize(chartSize);
    pdChartPanelAdj.setPopupMenu(null);
    pdChartPanelAdj.setInitialDelay(0);

    ////

    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = Report.getDefaultInsets();
    add(cumulativePdChartPanel, gbc);
    gbc.gridy++;
    add(pdChartPanelAdj, gbc);
    //    gbc.gridy++;
    //    add(pdChartPanel,gbc);
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
