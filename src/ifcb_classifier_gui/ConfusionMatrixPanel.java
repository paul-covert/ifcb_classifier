package ifcb_classifier_gui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;

/**
 * The panel that displays a confusion matrix after a training or validation run.
 */
public class ConfusionMatrixPanel extends JScrollPane {
	
	protected CompletedRunObject cRO;
	
	public JPanel contentPanel;
	
	public ConfusionMatrixPanel(CompletedRunObject cRO) {
		this.cRO = cRO;
		
		contentPanel = new JPanel(new GridLayout(cRO.classLabels.size()+2, cRO.classLabels.size()+2, 5, 5));
		contentPanel.setBorder(new TitledBorder(""));
		
		contentPanel.add(new JLabel(""));
		for (int i = 0; i < cRO.classLabels.size(); i++) {
			String label = abbr((String) cRO.classLabels.get(i));
			contentPanel.add(new JLabel(EZUtils.makeHTML("<I>"+label+"</I>")));
		}
		contentPanel.add(new JLabel("Recall"));
		
		for (int i = 0; i < cRO.classLabels.size(); i++) {
			String label = abbr((String) cRO.classLabels.get(i));
			contentPanel.add(new JLabel(EZUtils.makeHTML("<I>"+label+"</I>")));
			for (int j = 0; j < cRO.classLabels.size(); j++) {
				JLabel newLabel = new JLabel(String.valueOf(cRO.confMatrix[i][j]));
				if (i == j)
					newLabel.setForeground(EZUtils.GREEN);
				else
					newLabel.setForeground(EZUtils.RED);
				contentPanel.add(newLabel);	
			}
			contentPanel.add(new JLabel(EZUtils.oneDigit(cRO.recalls[i])+"%"));
		}
		
		contentPanel.add(new JLabel("Precision"));
		for (int i = 0; i < cRO.precisions.length; i++)
			contentPanel.add(new JLabel(EZUtils.oneDigit(cRO.precisions[i])+"%"));
		contentPanel.add(new JLabel(EZUtils.makeHTML("<B>"+EZUtils.oneDigit(cRO.accuracy)+"%</B>")));
		
		this.getViewport().add(contentPanel);
	}
	
	public String abbr(String label) {
		String[] labelSplit = label.split(" ");
		if (labelSplit.length >= 3)
			label = labelSplit[0].substring(0,1)+". "+labelSplit[1].substring(0,1)+". "+labelSplit[2].substring(0,1)+".";
		else if (labelSplit.length == 2)
			label = labelSplit[0].substring(0,3)+". "+labelSplit[1].substring(0,3)+".";
		else if (label.length() > 8)
			if (label.charAt(7) == ' ')
				label = label.substring(0, 7)+".";
			else
				label = label.substring(0, 8)+".";
		return label;
	}
}