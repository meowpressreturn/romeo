package romeo.battle.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import romeo.battle.IBattleMetrics;
import romeo.battle.PlayerSummary;
import romeo.fleet.model.FleetContents;
import romeo.fleet.model.FleetElement;
import romeo.ui.BeanTableModel;
import romeo.ui.NumericCellRenderer;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;

public class ReportSummaryPanel extends JPanel {
  public ReportSummaryPanel(IBattleMetrics metrics, List<PlayerSummary> summary) {
    String[] players = metrics.getPlayers();

    Dimension chartSize = Report.getChartSize(players.length);

    List<? extends Number> battleLengths = metrics.getBattleLengths();
    Number median = (Number) battleLengths.get(battleLengths.size() / 2);

    double tiedPercentage = metrics.getWinsPercentage(null) * 100d;
    JLabel tiesLabel = new JLabel("<html>" + metrics.getNumberOfBattles() + " simulated battles were fought in "
        + metrics.getTime() + " milliseconds" + "<br>Median rounds per battle was " + median + " , average "
        + Convert.toStr(metrics.getAverageRounds(), 2) + "<br>The proportion of tied battles was "
        + Convert.toStr(tiedPercentage, 2) + "% (" + metrics.getWinCount(null) + ")" + "</html>");

    //Number shortest = (Number)battleLengths.get(0);
    Number longest = (Number) battleLengths.get(battleLengths.size() - 1);

    if(longest.intValue() + 1 != metrics.getRecordedRoundCount()) {
      throw new IllegalStateException(
          "logic error in length: longest=" + longest + " recorded=" + metrics.getRecordedRoundCount());
    }

    String winsPercentLabel = "Win %";
    List<PlayerSummary> copyOfSummary = new ArrayList<PlayerSummary>(summary); //Shallow copy allowing independent sorting
    BeanTableModel summaryModel = new BeanTableModel(
        new BeanTableModel.ColumnDef[] { new BeanTableModel.ColumnDef("name", "Player"),
            //new BeanTableModel.ColumnDef("averageSurvivingFirepower","Avg FP"),
            new BeanTableModel.ColumnDef("adjustedAverageSurvivingFirepower", "Adj Avg FP"),
            new BeanTableModel.ColumnDef("winCount", "Victories"),
            new BeanTableModel.ColumnDef("winPercent", winsPercentLabel), },
        copyOfSummary);
    JTable summaryTable = new JTable(summaryModel);
    summaryTable.setDefaultRenderer(Double.class, new NumericCellRenderer(2));

    JScrollPane summaryScroll = new JScrollPane(summaryTable);
    int wpcol = summaryModel.findColumn(winsPercentLabel);
    summaryModel.setSortColumn(wpcol);
    summaryModel.setSortDescending(true);
    GuiUtils.setColumnWidths(summaryTable, new int[] { 100 });

    //20080213 - Quick hack to dump the survivors to the console
    //           to provide all digits of the stats as requested by JoelHalpern
    Log log = LogFactory.getLog(this.getClass());
    String[] playerNames = metrics.getPlayers();
    if(log.isInfoEnabled()) {
      for(int p = playerNames.length - 1; p >= 0; p--) {
        String player = playerNames[p];
        FleetContents averageSurvivors = metrics.getAverageSurvivors(player, false);
        for(FleetElement element : averageSurvivors) {
          log.info(player + ": Avg surviving " + element.getUnit().getName() + " = " + element.getQuantity());
        }
      }
    }

    //Charts
    //Summary
    //System.out.println("Prepare wins chart");
    DefaultKeyedValues winKv = new DefaultKeyedValues();
    for(int p = 0; p < players.length; p++) {
      String player = players[p];
      double percent = metrics.getWinsPercentage(player);
      int winCount = metrics.getWinCount(player);
      winKv.addValue(player + " " + Convert.toStr(percent * 100d, 2) + "%", winCount);
    }
    winKv.addValue("TIED " + Convert.toStr(metrics.getWinsPercentage(null) * 100d, 2) + "%", metrics.getWinCount(null));
    DefaultPieDataset winDs = new DefaultPieDataset(winKv);
    JFreeChart winChart = ChartFactory.createPieChart("", winDs, true, true, false);
    ((PiePlot) winChart.getPlot()).setLabelGenerator(new PieSectionLabelGenerator() {

      @Override
      @SuppressWarnings("rawtypes")
      public AttributedString generateAttributedSectionLabel(PieDataset dataset, Comparable key) {
        return null;
      }

      @Override
      @SuppressWarnings("rawtypes")
      public String generateSectionLabel(PieDataset dataset, Comparable key) {
        //TODO - change this so it shows label if its one of the top 3 or 4 results in the pie
        double value = ((Number) dataset.getValue(key)).doubleValue();
        double total = DatasetUtilities.calculatePieDatasetTotal(dataset);
        double percent = value / total;
        return percent >= 0.05 ? key.toString() : null;
      }

    });
    //((PiePlot)winChart.getPlot()).setMaximumLabelWidth(0.10d);
    ((PiePlot) winChart.getPlot()).setIgnoreZeroValues(false); //Include these so we still get labels for them
    ((PiePlot) winChart.getPlot()).setToolTipGenerator(new PieToolTipGenerator() {
      @Override
      @SuppressWarnings("rawtypes")
      public String generateToolTip(PieDataset dataset, Comparable key) {
        return key + " (" + dataset.getValue(key).intValue() + ")";
      }

    });
    ((PiePlot) winChart.getPlot()).setShadowXOffset(0);
    ((PiePlot) winChart.getPlot()).setShadowYOffset(0);
    ((PiePlot) winChart.getPlot()).setShadowPaint(Color.BLACK);

    ChartPanel winChartPanel = new ChartPanel(winChart);
    winChartPanel.setPreferredSize(chartSize);
    winChartPanel.setInitialDelay(0);
    winChartPanel.setPopupMenu(null);

    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = Report.getDefaultInsets();
    gbc.gridwidth = 2;
    add(tiesLabel, gbc);

    gbc.gridy++;
    summaryScroll.setPreferredSize(new Dimension(chartSize.width, 100));
    add(summaryScroll, gbc);

    gbc.gridx = 0;
    gbc.gridy++;
    gbc.gridwidth = 2;
    add(winChartPanel, gbc);

    //Filler
    gbc.gridy++;
    gbc.gridx = 1;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 2;
    gbc.weighty = 2;
    add(new JLabel(""), gbc);
    //end of laying out
    revalidate(); //?
  }
}
