package romeo.battle.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import romeo.battle.IBattleMetrics;
import romeo.battle.PlayerSummary;
import romeo.battle.UnitSummary;
import romeo.ui.BeanTableModel;
import romeo.ui.NumericCellRenderer;
import romeo.utils.Convert;
import romeo.utils.GuiUtils;

/**
 * Panel in the battle report that shows information about the numbers of units
 * that survive battles
 */
public class ReportUnitsPanel extends JPanel {
  public ReportUnitsPanel(IBattleMetrics metrics, List<PlayerSummary> summary) {
    Dimension tblSize = new Dimension(Report.getChartSize(1).width, 135); //width is ignored anyhow

    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = Report.getDefaultInsets();
    gbc.insets.bottom = 8;
    gbc.weightx = 2;

    for(int p = summary.size() - 1; p >= 0; p--) {
      PlayerSummary playerSummary = (PlayerSummary) summary.get(p);
      String player = playerSummary.getName();
      List<UnitSummary> survivors = playerSummary.getSurvivors();

      BeanTableModel model = new BeanTableModel(
          new BeanTableModel.ColumnDef[] { new BeanTableModel.ColumnDef("unit.name", "Unit"),
              new BeanTableModel.ColumnDef("initialQuantity", "Starting"),
              //new BeanTableModel.ColumnDef("averageSurvivors","Avg"),
              new BeanTableModel.ColumnDef("adjustedAverageSurvivors", "Adj Avg"), },
          survivors);
      JTable table = new JTable(model);
      table.setDefaultRenderer(Double.class, new NumericCellRenderer(1));
      JScrollPane scroll = new JScrollPane(table);

      int wins = metrics.getWinCount(player);
      double percentage = metrics.getWinsPercentage(player) * 100d;
      String title = player + " (" + wins + " wins=" + Convert.toStr(percentage, 2) + "%)";

      //      JLabel fpLabel = new JLabel(
      //          "Average surviving firepower="
      //          + Convert.toStr(metrics.getAverageSurvivingFirepower(player, false),2)
      //          + " (Adjusted="
      //          + Convert.toStr(metrics.getAverageSurvivingFirepower(player, true),2)
      //          + ")");

      JLabel fpLabel = new JLabel("Adjusted average surviving firepower="
          + Convert.toStr(metrics.getAverageSurvivingFirepower(player, true), 2) + ")");

      JPanel panel = new JPanel();
      panel.setBorder(BorderFactory.createTitledBorder(title));

      GridBagConstraints panelGbc = GuiUtils.prepGridBag(panel);
      panelGbc.insets = new Insets(1, 1, 1, 1);

      panelGbc.gridy++;
      panel.add(fpLabel, panelGbc);

      panelGbc.gridy++;
      panelGbc.weightx = 2;
      panel.add(scroll, panelGbc);
      scroll.setPreferredSize(tblSize);
      GuiUtils.setColumnWidths(table, new int[] { 150 });

      //Panel filler
      /*
       * panelGbc.gridy++; panelGbc.gridx=2; panelGbc.gridwidth=1; panelGbc.fill
       * = GridBagConstraints.BOTH; panelGbc.weightx = 2; panelGbc.weighty = 2;
       * panel.add(new JLabel(""),panelGbc);
       */
      //end of laying out
      panel.revalidate(); //?

      //Add to this panel
      gbc.gridy++;
      this.add(panel, gbc);
    }

    //This ones Filler
    gbc.gridy++;
    //gbc.gridx=2;
    gbc.gridwidth = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.weightx = 2;
    gbc.weighty = 2;
    add(new JLabel(""), gbc);
    //end of laying out
    revalidate(); //?
  }
}
