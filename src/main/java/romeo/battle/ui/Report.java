package romeo.battle.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import romeo.battle.IBattleMetrics;
import romeo.battle.PlayerSummary;
import romeo.utils.Charts;
import romeo.utils.GuiUtils;
import romeo.utils.INamed;

/**
 * This is the code that prepares the battle report stuff. Its rather ugly. The
 * report consists of a tabpane that holds panels displaying report information
 * on various aspects of the simulation. These are the {@link ReportNotesPanel},
 * {@link ReportSummaryPanel}, {@link ReportUnitsPanel},
 * {@link ReportRoundsPanel}, {@link ReportFirepowerPanel}, and
 * {@link ReportPdPanel}. This class also exposes some static utility methods
 * used by code in these panels.
 */
public class Report extends JPanel implements INamed {
  public Report(IBattleMetrics metrics) {
    List<PlayerSummary> summary = metrics.createSummary();

    JPanel notesPanel = new ReportNotesPanel(metrics.getNotes());
    JPanel summaryPanel = new ReportSummaryPanel(metrics, summary);
    JPanel survivorsPanel = new ReportUnitsPanel(metrics, summary);
    JPanel roundsPanel = new ReportRoundsPanel(metrics);
    JPanel firepowerPanel = new ReportFirepowerPanel(metrics);
    JPanel pdPanel = new ReportPdPanel(metrics);

    JScrollPane notesScroll = new JScrollPane(notesPanel);
    JScrollPane summaryScroll = new JScrollPane(summaryPanel);
    JScrollPane survivorsScroll = new JScrollPane(survivorsPanel);
    JScrollPane roundsScroll = new JScrollPane(roundsPanel);
    JScrollPane firepowerScroll = new JScrollPane(firepowerPanel);
    JScrollPane pdScroll = new JScrollPane(pdPanel);

    JTabbedPane tabPane = new JTabbedPane();
    tabPane.setMaximumSize(new Dimension(256, 256));

    tabPane.addTab("Summary", null, summaryScroll, null);
    tabPane.addTab("Rounds", null, roundsScroll, null);
    tabPane.addTab("Units", null, survivorsScroll, null);
    tabPane.addTab("Firepower", null, firepowerScroll, null);
    tabPane.addTab("Pop", null, pdScroll, null);
    tabPane.addTab("Notes", null, notesScroll, null);

    tabPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

    setLayout(new BorderLayout());
    //We need to give this panel a nominal preferredSize so that the scrollbars inside the
    //tabs will be used and the panel will fill the navigatorPanel. Without this the panel
    //expands to be as large as the largest tab ('notes' usually) and the navigatorPanel's
    //scrollbar is the one that gets used.
    setPreferredSize(new Dimension(1, 1));
    this.add(tabPane, BorderLayout.CENTER);
    GuiUtils.initScrollIncrements(this);
    this.revalidate(); //?
  }

  @Override
  public String getName() { //nb: we are conflict with a getName in JComponent here
    return "Battle Simulation Report";
  }

  public static double getRaTick(IBattleMetrics metrics) {
    List<? extends Number> battleLengths = metrics.getBattleLengths();
    int maxRound = Charts.getLastInt(battleLengths);
    double raTick = maxRound < 16 ? 1 : Math.ceil(maxRound / 16d);
    return raTick;
  }

  public static Dimension getChartSize(int playerCount) {
    int extraHeight = ((playerCount + 1) / 3) * 16;
    Dimension chartSize = new Dimension(380, 235 + extraHeight);
    return chartSize;
  }

  /**
   * Returns a copy (that you may modify) of the default insets used in the
   * report panels
   * @return defaultInsets
   */
  public static Insets getDefaultInsets() {
    return new Insets(3, 8, 3, 8);
  }
}
