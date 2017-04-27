package romeo.battle.ui;

import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;

import romeo.utils.Convert;
import romeo.utils.GuiUtils;

public class ReportNotesPanel extends JPanel {
  public ReportNotesPanel(String notes) {
    String notesHtml = "<html>" + notes + "</html>";
    notesHtml = Convert.replace(notesHtml, "\n", "<br>");
    JLabel notesLabel = new JLabel(notesHtml);
    notesLabel.setVerticalTextPosition(JLabel.TOP);

    GridBagConstraints gbc = GuiUtils.prepGridBag(this);
    gbc.insets = Report.getDefaultInsets();
    gbc.weightx = 2;
    add(notesLabel, gbc);
    gbc.weightx = 1;
    gbc.weighty = 1;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridy++;
    //gbc.gridx = 1;
    add(new JLabel(), gbc); //Filler
  }
}
