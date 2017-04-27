package romeo.battle.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.text.DecimalFormat;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

import romeo.battle.IBattleMetrics;
import romeo.utils.Charts;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;

public class ReportRoundsPanel extends JPanel {
  public ReportRoundsPanel(IBattleMetrics metrics) {
    Dimension chartSize = Report.getChartSize(metrics.getPlayers().length);

    List<? extends Number> battleLengths = metrics.getBattleLengths();
    Number median = (Number) battleLengths.get(battleLengths.size() / 2);
    Number shortest = (Number) battleLengths.get(0);
    Number longest = (Number) battleLengths.get(battleLengths.size() - 1);

    double numberOfBattles = metrics.getNumberOfBattles();
    NumberTickUnit battlesTickUnit = new NumberTickUnit(Math.ceil((double) numberOfBattles / 10d));

    double average = metrics.getAverageRounds();
    JLabel roundsLabel = new JLabel("<html>Median rounds per battle was " + median + " , average "
        + Convert.toStr(average, 2) + "<br>The shortest battle was " + shortest + " rounds."
        + "<br>The longest battle was " + longest + " rounds." + "</html>");

    //Charts - Rounds
    int maxRound = Charts.getLastInt(battleLengths);
    int roundCatSize = maxRound < 100 ? 5 : 10;
    //int roundChartHeight = maxRound <= 40 ? 415 : 600;
    int roundChartHeight = 415;
    int maxCat = 200;
    if(maxRound <= 40) {
      roundCatSize = 1;
      maxCat = 41;
    }
    Dimension roundChartSize = new Dimension(chartSize.width, roundChartHeight);
    KeyedValues keyedValues2 = Charts.getFrequencies(battleLengths, roundCatSize, maxCat);
    DefaultCategoryDataset dcd = Charts.addColumns(null, keyedValues2, "");
    JFreeChart roundChart = ChartFactory.createBarChart(null, "Rounds", "Battles", dcd, PlotOrientation.HORIZONTAL,
        false, true, false);
    ((NumberAxis) ((CategoryPlot) roundChart.getPlot()).getRangeAxis()).setTickUnit(battlesTickUnit);
    StandardCategoryToolTipGenerator cctg = new StandardCategoryToolTipGenerator("{2} battles had {1} rounds",
        new DecimalFormat("0"));
    ((CategoryPlot) roundChart.getPlot()).getRenderer().setBaseToolTipGenerator(cctg);
    ChartPanel colPanel = new ChartPanel(roundChart);
    colPanel.setPreferredSize(roundChartSize);
    colPanel.setPopupMenu(null);
    colPanel.setInitialDelay(0);

    //Battles reaching round chart
    DefaultTableXYDataset raDs = new DefaultTableXYDataset();
    XYSeries raSeries = new XYSeries("", false, false);
    List<? extends Number> raList = metrics.getRoundAchievement();
    for(int i = 0; i < raList.size(); i++) {
      raSeries.add((double) i, ((Number) raList.get(i)).doubleValue());
    }
    raDs.addSeries(raSeries);
    JFreeChart raChart = ChartFactory.createXYLineChart("Battles reaching round", "Round", "Battles", raDs,
        PlotOrientation.VERTICAL, false, true, false);
    double raTick = maxRound < 16 ? 1 : Math.ceil(maxRound / 16d);
    XYPlot raPlot = (XYPlot) raChart.getPlot();
    ((NumberAxis) (raPlot).getDomainAxis()).setTickUnit(new NumberTickUnit(raTick));
    ((NumberAxis) (raPlot).getRangeAxis()).setTickUnit(battlesTickUnit);

    StandardXYToolTipGenerator raTtg = new StandardXYToolTipGenerator("{2} battles had {1} or more rounds",
        new DecimalFormat("0"), new DecimalFormat("0"));
    raPlot.getRenderer().setBaseToolTipGenerator(raTtg);

    ValueMarker medianRoundsRa = new ValueMarker(median.doubleValue());
    medianRoundsRa.setLabel("M");
    raPlot.addDomainMarker(medianRoundsRa);

    ChartPanel raChartPanel = new ChartPanel(raChart);
    raChartPanel.setPreferredSize(chartSize);
    raChartPanel.setPopupMenu(null);
    raChartPanel.setInitialDelay(0);

    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = Report.getDefaultInsets();

    add(roundsLabel, gbc);
    gbc.gridy++;
    add(colPanel, gbc);
    gbc.gridy++;
    add(raChartPanel, gbc);

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
