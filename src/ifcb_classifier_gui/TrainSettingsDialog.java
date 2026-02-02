package ifcb_classifier_gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * The dialog for training settings.
 */
public class TrainSettingsDialog extends JDialog {
	
	protected int spinnerSize = 60;
	protected int sliderIncrementValue = 5;
	protected int fillSquareSize = 19;
	
	protected MainFrame mainFrame;
	
	protected JPanel mainPanel;
	protected JPanel contentPanel;
	
	protected JPanel optionsPanel;
	protected JSlider splitSlider;
	protected JLabel splitLabel;
	protected JSpinner classMinSpinner;
	protected JSpinner classMaxSpinner;
	protected JCheckBox classMaxCheck;
	protected JSpinner epochsMinSpinner;
	protected JSpinner epochsMaxSpinner;
	protected JSpinner epochsStopSpinner;
	protected JCheckBox epochsStopCheck;
	protected JComboBox<String> sizingBox;
	protected JSpinner fillSpinner;
	protected JPanel fillSquare;
	protected Graphics fillSquareGraphics;
	protected JSpinner redSquareSpinner;
	protected JCheckBox redSquareCheck;
	protected JComboBox<String> flipBox;
	protected JCheckBox imgNormCheck;
	protected JSpinner imgNormMeanSpinner;
	protected JSpinner imgNormSTDSpinner;
	protected JCheckBox untrainCheck;
	
	protected JPanel buttonPanel;
	protected JButton okButton;
	protected JButton cancelButton;
	protected JButton defaultButton;
	
	// Disclaimer: Some of these tooltips are copied from the args help functions in the original version of neuston_net.py, so credit goes to the authors of that.
	public static final String SPLIT_TOOLTIP = "Ratio of images per-class to split randomly into training and validation datasets. Randomness is affected by the seed.";
	public static final String CLASS_MIN_TOOLTIP = "Exclude classes with fewer than the specified number of instances.";
	public static final String CLASS_MAX_TOOLTIP = "Limit classes to a maximum number of instances. If multiple datasets are specified with a dataset-configuration csv, classes from lower-priority datasets are truncated first.";
	public static final String EPOCHS_MIN_TOOLTIP = "Minimum number of training epochs.";
	public static final String EPOCHS_MAX_TOOLTIP = "Maximum number of training epochs.";
	public static final String EPOCHS_STOP_TOOLTIP = "Number of epochs following a best epoch after which to stop training.";
	public static final String SIZING_TOOLTIP = "Means through which images are resized to match the required size for the model.";
	public static final String FILL_TOOLTIP = "Greyscale fill value for sizing modes that use padding.";
	public static final String RED_SQUARE_TOOLTIP = "Adds a small red square of the specified size to the top left corner of each image before resizing occurs.\n"
			+ "The purpose of this is to provide a metric for determining how much the size and aspect ratio of each image as been altered.";
	public static final String FLIP_TOOLTIP = "Training images have 50% chance of being flipped along the designated axis: (x) vertically, (y) horizontally, (xy) either/both. May optionally specify \"+V\" to include the validation dataset.";
	public static final String NORM_TOOLTIP = "Normalize images by mean and standard deviation. This is like whitebalancing.";
	public static final String UNTRAIN_TOOLTIP = "Initializes model without pretrained neurons.";
	
	public TrainSettingsDialog(MainFrame mainFrame) {
		super(mainFrame, "Classifier Settings", Dialog.ModalityType.APPLICATION_MODAL);
		this.mainFrame = mainFrame;
		
		ClassifierParameters params = mainFrame.getParams();
		
		mainPanel = new JPanel(new BorderLayout());
		contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new GridBagConstraints();
		b.anchor = b.EAST;
		b.gridy = 0;
		b.gridx = 0;
		b.insets = new Insets(5,5,5,5);
		
		optionsPanel = new JPanel(new GridBagLayout());
		optionsPanel.setBorder(new TitledBorder(""));
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = c.WEST;
		c.fill = c.HORIZONTAL;
		c.gridy = 0;
		c.gridx = 0;
		c.insets = new Insets(5,5,5,5);
		
		optionsPanel.add(new JLabel("Training-validation split"), c);
		c.gridx += 1;
		c.gridwidth = 2;
		splitSlider = new JSlider(1, 19, (int) (params.trainSplit / sliderIncrementValue));
		splitSlider.setPreferredSize(new Dimension(70, 26));
		splitSlider.addChangeListener(new SplitLabelListener());
		splitSlider.setToolTipText(SPLIT_TOOLTIP);
		optionsPanel.add(splitSlider, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		splitLabel = new JLabel(getSplitLabelString(params.trainSplit));
		optionsPanel.add(splitLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		optionsPanel.add(new JLabel("Min. class size"), c);
		c.gridx += 1;
		classMinSpinner = new JSpinner(new SpinnerNumberModel(params.trainClassMin,2,null,1));
		classMinSpinner.setPreferredSize(new Dimension(spinnerSize, 20)); // Horizontal fill extends the other spinners.
		classMinSpinner.setToolTipText(CLASS_MIN_TOOLTIP);
		optionsPanel.add(classMinSpinner, c);
		
		c.gridy++;
		c.gridx = 0;
		optionsPanel.add(new JLabel("Max. class size"), c);
		c.gridx += 1;
		classMaxSpinner = new JSpinner(new SpinnerNumberModel(params.trainClassMax,3,null,100));
		classMaxSpinner.setToolTipText(CLASS_MAX_TOOLTIP);
		optionsPanel.add(classMaxSpinner, c);
		c.gridx += 1;
		classMaxCheck = new JCheckBox();
		classMaxCheck.addActionListener(new ComponentOffSwitch(classMaxCheck, new Component[] {classMaxSpinner}));
		classMaxCheck.setToolTipText(CLASS_MAX_TOOLTIP);
		optionsPanel.add(classMaxCheck, c);
		
		c.gridy++;
		c.gridx = 0;
		optionsPanel.add(new JLabel("Min. epochs"), c);
		c.gridx += 1;
		epochsMinSpinner = new JSpinner(new SpinnerNumberModel(params.trainEpochsMin,1,null,1));
		epochsMinSpinner.setToolTipText(EPOCHS_MIN_TOOLTIP);
		optionsPanel.add(epochsMinSpinner, c);
		
		c.gridy++;
		c.gridx = 0;
		optionsPanel.add(new JLabel("Max. epochs"), c);
		c.gridx += 1;
		epochsMaxSpinner = new JSpinner(new SpinnerNumberModel(params.trainEpochsMax,1,null,5));
		epochsMaxSpinner.setToolTipText(EPOCHS_MAX_TOOLTIP);
		optionsPanel.add(epochsMaxSpinner, c);
		
		c.gridy++;
		c.gridx = 0;
		optionsPanel.add(new JLabel("Epoch stop threshold"), c);
		c.gridx += 1;
		epochsStopSpinner = new JSpinner(new SpinnerNumberModel(params.trainEpochsStop,1,null,1));
		epochsStopSpinner.setToolTipText(EPOCHS_STOP_TOOLTIP);
		optionsPanel.add(epochsStopSpinner, c);
		c.gridx += 1;
		epochsStopCheck = new JCheckBox();
		epochsStopCheck.setToolTipText(EPOCHS_STOP_TOOLTIP);
		epochsStopCheck.addActionListener(new ComponentOffSwitch(epochsStopCheck, new Component[] {epochsMinSpinner, epochsStopSpinner}));
		epochsStopCheck.setSelected(params.trainEpochsStopEnabled);
		optionsPanel.add(epochsStopCheck, c);
		
		c.gridy++;
		c.gridx = 0;
		optionsPanel.add(new JLabel("Sizing mode"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		c.fill = c.NONE;
		sizingBox = new JComboBox<String>(new String[] {"Resize", "Centre pan + padding", "Random pan + padding", "Pad to square, then resize", "Pad small, shrink large"});
		sizingBox.addActionListener(new SizingBoxListener());
		sizingBox.setToolTipText(SIZING_TOOLTIP);
		optionsPanel.add(sizingBox, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = c.HORIZONTAL;
		optionsPanel.add(new JLabel("Padding fill value"), c);
		c.gridx += 1;
		fillSpinner = new JSpinner(new SpinnerNumberModel(params.trainPaddingFill,0,255,5));
		fillSpinner.setToolTipText(FILL_TOOLTIP);
		// Listener added below
		optionsPanel.add(fillSpinner, c);
		c.gridx += 1;
		c.anchor = c.WEST;
		c.fill = c.NONE;
		c.gridwidth = 2;
		fillSquare = new JPanel() {
			@Override
		    protected void paintComponent(Graphics g) {
		        super.paintComponent(g);
		        g.setColor(new Color(params.trainPaddingFill,params.trainPaddingFill,params.trainPaddingFill));
		        g.fillRect(0, 0, fillSquareSize, fillSquareSize);
		        g.setColor(Color.BLACK);
		        g.drawRect(0, 0, fillSquareSize, fillSquareSize);
		        fillSquareGraphics = fillSquare.getGraphics();
		    }
		};
		fillSquare.setPreferredSize(new Dimension(fillSquareSize+1, fillSquareSize+1));
		fillSpinner.addChangeListener(new FillChangeListener());
		optionsPanel.add(fillSquare, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = c.HORIZONTAL;
		optionsPanel.add(new JLabel("Add red square"), c);
		c.gridx += 1;
		redSquareSpinner = new JSpinner(new SpinnerNumberModel(params.trainRedSquareSize,1,null,1));
		redSquareSpinner.setToolTipText(RED_SQUARE_TOOLTIP);
		optionsPanel.add(redSquareSpinner, c);
		c.gridx += 1;
		redSquareCheck = new JCheckBox();
		redSquareCheck.addActionListener(new ComponentOffSwitch(redSquareCheck, new Component[] {redSquareSpinner}));
		redSquareCheck.setSelected(params.trainRedSquare);
		redSquareCheck.setToolTipText(RED_SQUARE_TOOLTIP);
		optionsPanel.add(redSquareCheck, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		optionsPanel.add(new JLabel("Flip augmentation"), c);
		c.gridx += 1;
		flipBox = new JComboBox<String>(params.FLIP_OPTIONS);
		flipBox.setSelectedItem(params.trainFlip);
		flipBox.setToolTipText(FLIP_TOOLTIP);
		optionsPanel.add(flipBox, c);
		
		c.gridy++;
		c.gridx = 0;
		optionsPanel.add(new JLabel("Image normalization"), c);
		c.gridx += 1;
		imgNormCheck = new JCheckBox();
		// (Listener added after spinners are initialized.)
		imgNormCheck.setToolTipText(NORM_TOOLTIP);
		imgNormCheck.setSelected(params.trainImgNorm);
		optionsPanel.add(imgNormCheck, c);
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 4;
		c.fill = c.NONE;
		JPanel normGridPanel = new JPanel(new GridLayout(1,4,5,5));
		normGridPanel.add(new JLabel("Mean", JLabel.CENTER));
		imgNormMeanSpinner = new JSpinner(new SpinnerNumberModel(params.trainImgNormMean, 0.0, null, 0.05));
		imgNormMeanSpinner.setPreferredSize(new Dimension(spinnerSize, 20));
		imgNormMeanSpinner.setToolTipText(NORM_TOOLTIP);
		normGridPanel.add(imgNormMeanSpinner);
		normGridPanel.add(new JLabel("St. dev.", JLabel.CENTER));
		imgNormSTDSpinner = new JSpinner(new SpinnerNumberModel(params.trainImgNormSTD, 0.0, null, 0.05));
		imgNormSTDSpinner.setToolTipText(NORM_TOOLTIP);
		normGridPanel.add(imgNormSTDSpinner);
		imgNormCheck.addActionListener(new ComponentOffSwitch(imgNormCheck, new Component[] {imgNormMeanSpinner, imgNormSTDSpinner}));
		optionsPanel.add(normGridPanel, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		optionsPanel.add(new JLabel("Untrain"), c);
		c.gridx += 1;
		untrainCheck = new JCheckBox();
		untrainCheck.setToolTipText(UNTRAIN_TOOLTIP);
		untrainCheck.setSelected(params.trainUntrain);
		optionsPanel.add(untrainCheck, c);
		
		contentPanel.add(optionsPanel, b);
		
		b.gridy++;
		buttonPanel = new JPanel(new GridLayout(1,3,5,5));
		okButton = new JButton("Ok");
		okButton.addActionListener(new OkListener(this, mainFrame));
		buttonPanel.add(okButton);
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CancelListener(this));
		buttonPanel.add(cancelButton);
		defaultButton = new JButton("Defaults");
		defaultButton.addActionListener(new DefaultListener());
		buttonPanel.add(defaultButton);
		
		contentPanel.add(buttonPanel, b);
		
		mainPanel.add(contentPanel);
		
		this.add(mainPanel);
		this.pack();
		
		setupParams(params);
	}
	
	public class ComponentOffSwitch implements ActionListener {
		
		public JCheckBox checkBox;
		public Component[] components;
		
		public ComponentOffSwitch(JCheckBox checkBox, Component[] components) {
			this.checkBox = checkBox;
			this.components = components;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			for (int i = 0; i < components.length; i++)
				components[i].setEnabled(checkBox.isSelected());
		}
		
	}
	
	/**
	 * Replacement of JCheckBox.setSelected() that triggers listeners.
	 */
	public void switchOnOff(JCheckBox checkBox, boolean switchingOn) {
		checkBox.doClick();
		if (checkBox.isSelected() != switchingOn)
			checkBox.doClick();
	}
	
	public class SizingBoxListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			fillSpinner.setEnabled(sizingBox.getSelectedIndex() != 0);
			fillSquare.setVisible(sizingBox.getSelectedIndex() != 0);
		}
		
	}
	
	public class SplitLabelListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			int val = getDesiredSliderValue();
			splitLabel.setText(getSplitLabelString(val));
		}
		
	}
	
	public class OkListener implements ActionListener {
		
		protected TrainSettingsDialog dialog;
		protected MainFrame mainFrame;
		
		public OkListener(TrainSettingsDialog dialog, MainFrame mainFrame) {
			this.dialog = dialog;
			this.mainFrame = mainFrame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			ClassifierParameters newParams = retrieveParams();
			if (newParams.trainEpochsStopEnabled && newParams.trainEpochsMin > newParams.trainEpochsMax) 
				EZUtils.SimpleErrorDialog(dialog, "Minimum epochs must be less than or equal to maximum epochs.", 250);
			if (newParams.trainEpochsStopEnabled && newParams.trainEpochsStop >= newParams.trainEpochsMax)
				EZUtils.SimpleErrorDialog(dialog, "Epoch stop threshold should be less than maximum epochs, otherwise it's pointless.", 300);
			mainFrame.setParams(newParams);
			dialog.dispose();
			
		}
		
	}
	
	public class CancelListener implements ActionListener {
		
		protected TrainSettingsDialog dialog;
		
		public CancelListener(TrainSettingsDialog dialog) {
			this.dialog = dialog;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			dialog.dispose();
		}
		
	}
	
	public class DefaultListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			int val = JOptionPane.showConfirmDialog(mainFrame, 
													"Reset settings to defaults?", 
													ProgramInfo.getProgramName(), 
													JOptionPane.OK_CANCEL_OPTION, 
													JOptionPane.WARNING_MESSAGE);
			if (val == JOptionPane.OK_OPTION) 
				setupParams(new ClassifierParameters());
		}
		
	}
	
	public class FillChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent e) {
			int val = (int) fillSpinner.getValue();
			fillSquareGraphics.clearRect(0, 0, fillSquareSize, fillSquareSize);
			fillSquareGraphics.setColor(new Color(val, val, val));
			fillSquareGraphics.fillRect(0, 0, fillSquareSize, fillSquareSize);
			fillSquareGraphics.setColor(Color.BLACK);
			fillSquareGraphics.drawRect(0, 0, fillSquareSize, fillSquareSize);
		}
		
	}
	
	/**
	 * Sets the values in the components to what in the input params file.
	 */
	public void setupParams(ClassifierParameters params) {
		splitSlider.setValue((int) (params.trainSplit / sliderIncrementValue));
		classMinSpinner.setValue(params.trainClassMin);
		classMaxSpinner.setValue(params.trainClassMax);
		switchOnOff(classMaxCheck, params.trainClassMaxEnabled);
		epochsMinSpinner.setValue(params.trainEpochsMin);
		epochsMaxSpinner.setValue(params.trainEpochsMax);
		epochsStopSpinner.setValue(params.trainEpochsStop);
		switchOnOff(epochsStopCheck, params.trainEpochsStopEnabled);
		for (int i = 0; i < params.SIZING_OPTIONS.length; i++)
			if (params.trainSizingMode.equals(params.SIZING_OPTIONS[i])) {
				sizingBox.setSelectedIndex(i);
				break;
			}
		fillSpinner.setValue(params.trainPaddingFill);
		redSquareSpinner.setValue(params.trainRedSquareSize);
		switchOnOff(redSquareCheck, params.trainRedSquare);
		flipBox.setSelectedItem(params.trainFlip);
		switchOnOff(imgNormCheck, params.trainImgNorm);
		imgNormMeanSpinner.setValue(params.trainImgNormMean);
		imgNormSTDSpinner.setValue(params.trainImgNormSTD);
		untrainCheck.setSelected(params.trainUntrain);
	}
	
	/**
	 * Takes the values in the components and outputs a params object with those values.
	 */
	public ClassifierParameters retrieveParams() {
		ClassifierParameters params = new ClassifierParameters();
		params.trainSplit = getDesiredSliderValue();
		params.trainClassMin = (int) this.classMinSpinner.getValue();
		params.trainClassMaxEnabled = this.classMaxCheck.isSelected();
		if (params.trainClassMaxEnabled)
			params.trainClassMax = (int) this.classMaxSpinner.getValue();
		params.trainEpochsMax = (int) this.epochsMaxSpinner.getValue();
		params.trainEpochsStopEnabled = this.epochsStopCheck.isSelected();
		if (params.trainEpochsStopEnabled) {
			params.trainEpochsMin = (int) this.epochsMinSpinner.getValue();
			params.trainEpochsStop = (int) this.epochsStopSpinner.getValue();
		}
		params.trainSizingMode = params.SIZING_OPTIONS[sizingBox.getSelectedIndex()];
		if (!params.trainSizingMode.equals(params.SIZING_OPTIONS[0]))
			params.trainPaddingFill = (int) fillSpinner.getValue();
		params.trainRedSquare = redSquareCheck.isSelected();
		if (params.trainRedSquare)
			params.trainRedSquareSize = (int) redSquareSpinner.getValue();
		params.trainFlip = (String) this.flipBox.getSelectedItem();
		params.trainImgNorm = this.imgNormCheck.isSelected();
		if (params.trainImgNorm) {
			params.trainImgNormMean = (double) this.imgNormMeanSpinner.getValue();
			params.trainImgNormSTD = (double) this.imgNormSTDSpinner.getValue();
		}
		params.trainUntrain = this.untrainCheck.isSelected();
		return params;
	}
	
	public int getDesiredSliderValue() {
		return splitSlider.getValue() * sliderIncrementValue;
	}
	
	public String getSplitLabelString(int splitNum) {
		return String.valueOf(splitNum)+" : "+String.valueOf(100-splitNum);
	}
	
}