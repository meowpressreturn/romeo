package romeo.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.util.EventObject;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

import romeo.Romeo;
import romeo.model.api.IServiceListener;
import romeo.players.api.IPlayer;
import romeo.players.api.IPlayerService;
import romeo.players.api.PlayerUtils;
import romeo.worlds.api.IWorldService;
import romeo.worlds.impl.HistoryChartsHelper;

/**
 * Panel that creates and shows the graphs for teamWorlds, worldTurns,
 * teamFirepower,
 */
public class GraphsPanel extends JTabbedPane implements IServiceListener {
  protected IWorldService _worldService;
  protected IPlayerService _playerService;
  protected HistoryChartsHelper _historyChartsHelper;
  protected ChartPanel _worldHistoryChartPanel;
  protected PlayerChooser _worldHistoryPlayers;
  protected ChartPanel _teamHistoryChartPanel;
  protected StatSelector _playerStatSelector;
  protected StatSelector _teamStatSelector;

  public GraphsPanel(IWorldService worldService, IPlayerService playerService, HistoryChartsHelper worldChartsHelper) {
    _worldService = Objects.requireNonNull(worldService, "worldService must not be null");
    _playerService = Objects.requireNonNull(playerService, "playerService must not be null");
    _worldService.addListener(this);
    _playerService.addListener(this);
    _historyChartsHelper = Objects.requireNonNull(worldChartsHelper, "worldChartsHelper must not be null");
    Romeo.incrementSplashProgress("Graphs");
    //World History Chart
    JComponent playerHistoryChart = prepareWorldHistoryChart();
    this.add(playerHistoryChart, "Player History");

    //Team History Chart
    JComponent teamHistoryChart = prepareTeamHistoryChart();
    this.add(teamHistoryChart, "Team History");

  }

  protected JComponent prepareWorldHistoryChart() {
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());

    XYDataset dataset = null;
    final JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true,
        true, false);
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.BLACK);
    plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    _worldHistoryChartPanel = new ChartPanel(chart);
    _worldHistoryChartPanel.setPopupMenu(null);
    panel.add(_worldHistoryChartPanel, BorderLayout.CENTER);

    //Controls Panel
    JPanel controlsPanel = new JPanel();
    _playerStatSelector = new StatSelector();
    controlsPanel.setLayout(new GridLayout());
    ItemListener listener = new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        updateWorldHistoryChart();
      }
    };

    //Player Chooser
    _worldHistoryPlayers = new PlayerChooser(_playerService);
    _worldHistoryPlayers.setAll(false); //dont want to all by default as it adds to loading time
    _worldHistoryPlayers.setItemListener(listener);
    controlsPanel.add(_worldHistoryPlayers.getComponent());

    //Add stat selector second
    _playerStatSelector.addItemListener(listener);
    controlsPanel.add(_playerStatSelector.getComponent());

    panel.add(controlsPanel, BorderLayout.SOUTH);
    updateWorldHistoryChart();
    return panel;
  }

  protected void updateWorldHistoryChart() {
    String stat = _playerStatSelector.getSelectedStatistic();
    Set<String> selections = _worldHistoryPlayers.getSelectedPlayers();
    XYPlot plot = (XYPlot) _worldHistoryChartPanel.getChart().getPlot();
    plot.getRangeAxis().setLabel(stat);
    _worldHistoryChartPanel.getChart().setTitle(stat + " by Turn");
    XYDataset dataset;
    if(StatSelector.LABEL_WORLDS.equals(stat)) {
      dataset = _historyChartsHelper.getPlayerHistoryDataset(selections, "COUNT(worldId)");
    } else if(StatSelector.LABEL_FIREPOWER.equals(stat)) {
      dataset = _historyChartsHelper.getPlayerHistoryDataset(selections, "SUM(firepower)");
    } else if(StatSelector.LABEL_LABOUR.equals(stat)) {
      dataset = _historyChartsHelper.getPlayerHistoryDataset(selections, "SUM(labour)");
    } else if(StatSelector.LABEL_CAPITAL.equals(stat)) {
      dataset = _historyChartsHelper.getPlayerHistoryDataset(selections, "SUM(capital)");
    } else {
      plot.getRangeAxis().setLabel(stat + "????");
      dataset = null;
    }

    plot.setDataset(dataset);
    //Set player colours for series
    if(dataset != null) {
      XYItemRenderer renderer = plot.getRendererForDataset(dataset);
      for(int series = 0; series < dataset.getSeriesCount(); series++) {
        String playerName = (String) dataset.getSeriesKey(series);
        IPlayer player = _playerService.loadPlayerByName(playerName);
        if(player != null) {
          renderer.setSeriesPaint(series, player.getColor());
        }
      }
    }

    //Set tooltip
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(0);
    NumberFormat nf = NumberFormat.getNumberInstance();
    renderer
        .setBaseToolTipGenerator(new StandardXYToolTipGenerator("Player {0} = {2} " + stat + " on turn {1}", nf, nf));
    renderer.setBaseShapesVisible(true);
  }

  /**
   * Apply teams colours to the series being renderered in an XYPlot
   * @param plot
   */
  private void applyTeamColors(XYPlot plot) {
    XYDataset dataset = (XYDataset) plot.getDataset();
    if(dataset != null) {
      XYItemRenderer renderer = (XYItemRenderer) plot.getRenderer(0);
      int teams = dataset.getSeriesCount();
      for(int seriesIndex = 0; seriesIndex < teams; seriesIndex++) {
        renderer.setSeriesPaint(seriesIndex, PlayerUtils.getTeamColor("" + seriesIndex));
      }
    }
  }

  protected JComponent prepareTeamHistoryChart() {
    XYDataset dataset = null;
    final JFreeChart chart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true,
        true, false);
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.BLACK);
    plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    plot.getDomainAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    _teamHistoryChartPanel = new ChartPanel(chart);
    _teamHistoryChartPanel.setPopupMenu(null);

    JPanel panel = new JPanel(new BorderLayout());
    panel.add(_teamHistoryChartPanel, BorderLayout.NORTH);

    _teamStatSelector = new StatSelector();
    panel.add(_teamStatSelector.getComponent(), BorderLayout.SOUTH);
    _teamStatSelector.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        updateTeamHistoryChart();
      }
    });

    updateTeamHistoryChart();
    return panel;
  }

  protected void updateTeamHistoryChart() {
    try {
      String stat = _teamStatSelector.getSelectedStatistic();
      XYPlot plot = (XYPlot) _teamHistoryChartPanel.getChart().getPlot();
      plot.getRangeAxis().setLabel(stat);
      _teamHistoryChartPanel.getChart().setTitle(stat + " by Turn");
      XYDataset dataset;
      switch (stat){
        case StatSelector.LABEL_WORLDS:
          dataset = _historyChartsHelper.getTeamWorldsDataset();
          break;

        case StatSelector.LABEL_FIREPOWER:
          dataset = _historyChartsHelper.getTeamFirepowerDataset();
          break;

        case StatSelector.LABEL_LABOUR:
          dataset = _historyChartsHelper.getTeamLabourDataset();
          break;

        case StatSelector.LABEL_CAPITAL:
          dataset = _historyChartsHelper.getTeamCapitalDataset();
          break;

        default:
          plot.getRangeAxis().setLabel(stat + "????");
          dataset = null;
          break;
      }

      plot.setDataset(dataset);
      applyTeamColors(plot);
      XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(0);
      NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
      renderer
          .setBaseToolTipGenerator(new StandardXYToolTipGenerator("Team {0} = {2} " + stat + " on turn {1}", nf, nf));
      renderer.setBaseShapesVisible(true);
    } catch(Exception e) {
      new ErrorDialog("Internal Error", e, false).show();
    }
  }

  @Override
  public void dataChanged(EventObject event) {
    if(event.getSource() instanceof IPlayerService || event.getSource() instanceof IWorldService) {
      updateWorldHistoryChart();
      updateTeamHistoryChart();
    }
    /**
     * TODO Implication of the above is that every save to history will cause
     * all these queries to run again and the charts to be rebuild. This is a
     * waste if they arent actually being looked at, so ideally, if their panel
     * is not currently displayed it should just drop the old dataset, and
     * reload it on demand when it needs to be painted again the first time.
     */
  }

}
