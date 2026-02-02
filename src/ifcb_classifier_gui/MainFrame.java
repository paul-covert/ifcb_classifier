package ifcb_classifier_gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The main GUI panel.
 */
public class MainFrame extends JFrame {
	
	public static final int IDLE = 0;
	public static final int TRAINING = 1;
	public static final int VALIDATING = 2;
	public static final int TESTING = 3;
	public static final int CREATING_DATASET_CONFIG = 4;
	
	public static final Dimension CARD_PANEL_SIZE = new Dimension(250, 250);
	
	public static final String BATCH_SIZE_TOOLTIP = "Number of images per batch. The higher the faster, although the Python script may crash if it tries to load too much at once.";
	public static final String LOADERS_TOOLTIP = "Number of data-loading threads. 4 per GPU is typical. The higher the faster, although the Python script may crash if it tries to load too much at once.";
	public static final String RUN_ID_TOOLTIP = "Name of the folder where run results will be stored.";
	public static final String RUN_TYPE_TOOLTIP = "Whether to use raw image data or .roi data from a bin folder.";
	public static final String RUN_INPUT_TOOLTIP = "Path to input data.";
	public static final String RUN_OUTPUT_TOOLTIP = "Path to folder where run results will be output.";
	public static final String RUN_MODEL_TOOLTIP = "Path to .ptl file containing the desired training model.";
	
	public ClassifierParameters params;
	
	protected CommandManager commandManager;
	
	public JTabbedPane tabbedPane;
	public JPanel trainPanel;
	public JPanel trainOptionsPanel;
	public JPanel trainResultsPanel;
	public JPanel trainCardPanel;
	public CardLayout trainCardLayout;
	public JPanel trainEmptyPanel;
	public JPanel validationPanel;
	public JPanel validationOptionsPanel;
	public JPanel validationResultsPanel;
	public JPanel validationCardPanel;
	public CardLayout validationCardLayout;
	public JPanel validationEmptyPanel;
	public JPanel testPanel;
	public JPanel testOptionsPanel;
	public JPanel testResultsPanel;
	public JPanel testCardPanel;
	public CardLayout testCardLayout;
	public JPanel testEmptyPanel;
	
	public JTextField trainIDField;
	public JButton trainDatasetConfigButton;
	public JTextField trainSeedField;
	public JButton trainRandomizeButton;
	public JTextField trainInputFolderField;
	public JButton trainInputFolderButton;
	public JTextField trainOutputFolderField;
	public JButton trainOutputFolderButton;
	public JComboBox<String> trainModelBox;
	public JButton trainSettingsButton;
	public JSpinner trainBatchSizeSpinner;
	public JSpinner trainLoadersSpinner;
	public JButton trainButton;
	
	public JLabel trainStatusLabel;
	public JLabel trainEpochNumberLabel;
	public JProgressBar trainEpochTrainingBar;
	public JProgressBar trainEpochValidationBar;
	public JLabel trainBestEpochLabel;
	public JLabel trainBestTrainLossLabel;
	public JLabel trainBestValLossLabel;
	public JLabel trainBestValF1WLabel;
	public JLabel trainBestValF1MLabel;
	public JLabel trainLastEpochLabel;
	public JLabel trainLastTrainLossLabel;
	public JLabel trainLastValLossLabel;
	public JLabel trainLastValF1WLabel;
	public JLabel trainLastValF1MLabel;
	
	public JTextField validationIDField;
	public JComboBox<String> validationTypeBox;
	public JTextField validationInputFolderField;
	public JButton validationInputFolderButton;
	public JTextField validationOutputFolderField;
	public JButton validationOutputFolderButton;
	public JTextField validationModelFileField;
	public JButton validationModelFileButton;
	public JSpinner validationBatchSizeSpinner;
	public JSpinner validationLoadersSpinner;
	public JButton validationButton;
	
	public JLabel validationStatusLabel;
	public JProgressBar validationTestingBar;
	public JLabel validationOverallAccuracyLabel;
	public JLabel validationBestPrecisionLabel;
	public JLabel validationWorstPrecisionLabel;
	public JLabel validationBestRecallLabel;
	public JLabel validationWorstRecallLabel;
	//public JButton validationConfusionMatrixButton;
	
	public JTextField testIDField;
	public JComboBox<String> testTypeBox;
	public JTextField testInputFolderField;
	public JButton testInputFolderButton;
	public JTextField testOutputFolderField;
	public JButton testOutputFolderButton;
	public JTextField testModelFileField;
	public JButton testModelFileButton;
	public JSpinner testBatchSizeSpinner;
	public JSpinner testLoadersSpinner;
	public JButton testButton;
	
	public JLabel testStatusLabel;
	public JProgressBar testBinBar;
	public JProgressBar testBatchBar;
	public JLabel testFileCountLabel;
	public JLabel testMeanScoreLabel;
	
	public SpinnerNumberModel batchSizeSpinnerModel;
	public SpinnerNumberModel loadersSpinnerModel;
	
	private int currentMode = IDLE;
	private int roiFilesFound = 0;
	
	public MainFrame() {
		setTitle(ProgramInfo.getProgramName());
		setSize(500, 500);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new FlowLayout(FlowLayout.CENTER));
		
		params = new ClassifierParameters();
		this.setIconImage(EZUtils.ICON.getImage());
		
		tabbedPane = new JTabbedPane();
		
		trainPanel = new JPanel(new GridBagLayout());
		GridBagConstraints b = new GridBagConstraints();
		b.anchor = b.CENTER;
		b.fill = b.HORIZONTAL;
		b.gridy = 0;
		b.gridx = 0;
		
		trainOptionsPanel = new JPanel(new GridBagLayout());		
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = c.WEST;
		c.fill = c.HORIZONTAL;
		c.gridy = 0;
		c.gridx = 0;
		c.insets = new Insets(5,5,5,5);
		
		trainOptionsPanel.add(new JLabel("Training ID"), c);
		c.gridx += 1;
		trainIDField = new JTextField();
		trainIDField.setPreferredSize(new Dimension(86, 19)); // Horizontal fill extends the components below.
		trainIDField.setDocument(JValidCharFilter());
		trainIDField.setToolTipText("The name of the resulting model.");
		trainIDField.setText("model");
		trainIDField.addFocusListener(new OldValueListener(trainIDField));
		trainOptionsPanel.add(trainIDField, c);
		c.gridx += 1;
		c.gridwidth = 3;
		trainDatasetConfigButton = new JButton("Use dataset config");
		trainDatasetConfigButton.addActionListener(new DatasetConfigListener(this));
		trainOptionsPanel.add(trainDatasetConfigButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		trainOptionsPanel.add(new JLabel("Seed"), c);
		c.gridx += 1;
		trainSeedField = new JTextField();
		trainSeedField.setDocument(JIntFilter());
		trainSeedField.addFocusListener(new TextFieldEmptyListener(trainSeedField, "1"));
		trainSeedField.setToolTipText("The seed for random generation functions. Running the same data with the same settings and same seed should produce identical results.");
		trainOptionsPanel.add(trainSeedField, c);
		c.gridx += 1;
		c.gridwidth = 2;
		trainRandomizeButton = new JButton("Randomize");
		trainRandomizeButton.addActionListener(new RandomNumberListener(trainSeedField));
		trainOptionsPanel.add(trainRandomizeButton, c);
		trainRandomizeButton.doClick();
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = c.HORIZONTAL;
		trainOptionsPanel.add(new JLabel("Input folder"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		trainInputFolderField = new JTextField(20);
		trainInputFolderField.setEnabled(false);
		trainInputFolderField.setToolTipText("Path to folder containing IFCB image data, presumably sorted into folders named by species.");
		trainOptionsPanel.add(trainInputFolderField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		trainInputFolderButton = new JButton("Select");
		trainInputFolderButton.addActionListener(new FolderChooserListener(this, trainInputFolderField, 2));
		trainOptionsPanel.add(trainInputFolderButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		trainOptionsPanel.add(new JLabel("Output folder"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		trainOutputFolderField = new JTextField(20);
		trainOutputFolderField.setEnabled(false);
		trainOutputFolderField.setText(EZUtils.getSourcePath()+"python/training-output/");
		trainOutputFolderField.setToolTipText("Path to folder where the resulting model and results will be output.");
		trainOptionsPanel.add(trainOutputFolderField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		trainOutputFolderButton = new JButton("Select");
		trainOutputFolderButton.addActionListener(new FolderChooserListener(this, trainOutputFolderField, 0));
		trainOptionsPanel.add(trainOutputFolderButton, c);
		
		c.gridy++;
		c.gridx = 0;
		trainOptionsPanel.add(new JLabel("Model"), c);
		c.gridx += 1;
		trainModelBox = new JComboBox<String>(new String[] {"alexnet","densenet121","densenet161","densenet169","densenet201","inception_v3","inception_v4",
				"resnet18","resnet34","resnet50","resnet101","resnet152","squeezenet","vgg11","vgg13","vgg16","vgg19"});
		trainModelBox.setSelectedItem("inception_v3");
		trainModelBox.setToolTipText("PyTorch model architecture. inception_v3 is recommended for the best results, while squeezenet will process the fastest.");
		trainOptionsPanel.add(trainModelBox, c);
		c.gridx += 1;
		c.gridwidth = 3;
		trainSettingsButton = new JButton("Settings...");
		trainSettingsButton.addActionListener(new SettingsButtonListener(this));
		trainOptionsPanel.add(trainSettingsButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = c.NONE;
		trainOptionsPanel.add(new JLabel("Batch size"), c);
		c.gridx += 1;
		batchSizeSpinnerModel = new SpinnerNumberModel(100, 1, null, 5);
		trainBatchSizeSpinner = new JSpinner(batchSizeSpinnerModel);
		trainBatchSizeSpinner.setPreferredSize(new Dimension(70, 19));
		trainBatchSizeSpinner.setToolTipText(BATCH_SIZE_TOOLTIP);
		trainOptionsPanel.add(trainBatchSizeSpinner, c);
		c.gridx += 1;
		trainOptionsPanel.add(new JLabel("Loaders"), c);
		c.gridx += 1;
		c.gridwidth = 2;
		loadersSpinnerModel = new SpinnerNumberModel(4, 1, null, 1);
		trainLoadersSpinner = new JSpinner(loadersSpinnerModel);
		trainLoadersSpinner.setPreferredSize(new Dimension(70, 19));
		trainLoadersSpinner.setToolTipText(LOADERS_TOOLTIP);
		trainOptionsPanel.add(trainLoadersSpinner, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 5;
		c.fill = c.HORIZONTAL;
		trainButton = new JButton("TRAIN");
		trainButton.addActionListener(new TrainButtonListener(this));
		trainOptionsPanel.add(trainButton, c);
		
		trainPanel.add(trainOptionsPanel, b);
		
		trainResultsPanel = new JPanel(new GridBagLayout());		
		c = new GridBagConstraints();
		c.anchor = c.WEST;
		c.fill = c.HORIZONTAL;
		c.gridy = 0;
		c.gridx = 0;
		c.insets = new Insets(5,5,5,5);
		
		trainResultsPanel.add(new JLabel("Status:"), c);
		c.gridx += 1;
		trainStatusLabel = new JLabel("Idle");
		trainResultsPanel.add(trainStatusLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		trainResultsPanel.add(new JLabel("Current epoch:"), c);
		c.gridx += 1;
		trainEpochNumberLabel = new JLabel("");
		trainResultsPanel.add(trainEpochNumberLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		trainEpochTrainingBar = new JProgressBar();
		trainEpochTrainingBar.setPreferredSize(new Dimension(200, 20));
		trainEpochTrainingBar.setValue(0);
		trainEpochTrainingBar.setStringPainted(true);
		trainEpochTrainingBar.setString("Training: Idle");
		trainResultsPanel.add(trainEpochTrainingBar, c);
		
		c.gridy++;
		trainEpochValidationBar = new JProgressBar();
		trainEpochValidationBar.setPreferredSize(new Dimension(200, 20));
		trainEpochValidationBar.setValue(0);
		trainEpochValidationBar.setStringPainted(true);
		trainEpochValidationBar.setString("Validation: Idle");
		trainResultsPanel.add(trainEpochValidationBar, c);
		
		c.gridy++;
		c.gridwidth = 1;
		trainResultsPanel.add(new JLabel(EZUtils.makeHTML("<U>Best epoch:</U>")), c);
		c.gridx += 1;
		trainBestEpochLabel = new JLabel("");
		trainResultsPanel.add(trainBestEpochLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		JPanel bestEpochStatsPanel = new JPanel(new GridLayout(2, 4, 5, 5));
		bestEpochStatsPanel.add(new JLabel("train_loss:", JLabel.RIGHT));
		trainBestTrainLossLabel = new JLabel("");
		bestEpochStatsPanel.add(trainBestTrainLossLabel);
		bestEpochStatsPanel.add(new JLabel("val_f1_w:", JLabel.RIGHT));
		trainBestValF1WLabel = new JLabel("");
		bestEpochStatsPanel.add(trainBestValF1WLabel);
		bestEpochStatsPanel.add(new JLabel("val_loss:", JLabel.RIGHT));
		trainBestValLossLabel = new JLabel("");
		bestEpochStatsPanel.add(trainBestValLossLabel);
		bestEpochStatsPanel.add(new JLabel("val_f1_m:", JLabel.RIGHT));
		trainBestValF1MLabel = new JLabel("");
		bestEpochStatsPanel.add(trainBestValF1MLabel);
		trainResultsPanel.add(bestEpochStatsPanel, c);
		
		c.gridy++;
		c.gridwidth = 1;
		trainResultsPanel.add(new JLabel(EZUtils.makeHTML("<U>Last epoch:</U>")), c);
		c.gridx += 1;
		trainLastEpochLabel = new JLabel("");
		trainResultsPanel.add(trainLastEpochLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		JPanel lastEpochStatsPanel = new JPanel(new GridLayout(2, 4, 5, 5));
		lastEpochStatsPanel.add(new JLabel("train_loss:", JLabel.RIGHT));
		trainLastTrainLossLabel = new JLabel("");
		lastEpochStatsPanel.add(trainLastTrainLossLabel);
		lastEpochStatsPanel.add(new JLabel("val_f1_w:", JLabel.RIGHT));
		trainLastValF1WLabel = new JLabel("");
		lastEpochStatsPanel.add(trainLastValF1WLabel);
		lastEpochStatsPanel.add(new JLabel("val_loss:", JLabel.RIGHT));
		trainLastValLossLabel = new JLabel("");
		lastEpochStatsPanel.add(trainLastValLossLabel);
		lastEpochStatsPanel.add(new JLabel("val_f1_m:", JLabel.RIGHT));
		trainLastValF1MLabel = new JLabel("");
		lastEpochStatsPanel.add(trainLastValF1MLabel);
		trainResultsPanel.add(lastEpochStatsPanel, c);
		
		b.gridx += 1;
		trainPanel.add(trainResultsPanel, b);
		
		trainCardPanel = new JPanel(new CardLayout());
		trainCardLayout = (CardLayout) trainCardPanel.getLayout();
		trainCardPanel.setPreferredSize(CARD_PANEL_SIZE);
		
		trainEmptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		trainEmptyPanel.add(new JLabel("(Confusion matrix will appear here after training and validation.)"));
		trainCardPanel.add(trainEmptyPanel, "empty");
		
		trainCardLayout.show(trainCardPanel, "empty");
		
		b.gridy++;
		b.gridx = 0;
		b.gridwidth = 2;
		trainPanel.add(trainCardPanel, b);
		
		tabbedPane.add("Training", trainPanel);
		
		validationPanel = new JPanel(new GridBagLayout());
		b = new GridBagConstraints();
		b.anchor = b.CENTER;
		b.fill = b.HORIZONTAL;
		b.gridy = 0;
		b.gridx = 0;
		
		validationOptionsPanel = new JPanel(new GridBagLayout());		
		c = new GridBagConstraints();
		c.anchor = c.WEST;
		c.gridy = 0;
		c.gridx = 0;
		c.insets = new Insets(5,5,5,5);
		
		c.gridwidth = 5;
		JLabel validationNoteLabel = new JLabel("(For validation of a labelled testing set outside of training.)", JLabel.CENTER);
		validationNoteLabel.setForeground(Color.GRAY);
		validationOptionsPanel.add(validationNoteLabel, c);
		
		c.gridy++;
		c.gridwidth = 1;
		validationOptionsPanel.add(new JLabel("Run ID"), c);
		c.gridx += 1;
		validationIDField = new JTextField();
		validationIDField.setPreferredSize(new Dimension(86, 19));
		validationIDField.setToolTipText(RUN_ID_TOOLTIP);
		validationIDField.setText("model");
		validationOptionsPanel.add(validationIDField, c);
		
		c.gridy++;
		c.gridx = 0;
		validationOptionsPanel.add(new JLabel("Input type"), c);
		c.gridx += 1;
		validationTypeBox = new JComboBox<String>(new String[] {"bin","img"});
		validationTypeBox.setSelectedItem("img");
		validationTypeBox.setPreferredSize(new Dimension(validationTypeBox.getPreferredSize().width+5, validationTypeBox.getPreferredSize().height));
		validationTypeBox.setEnabled(false);
		validationTypeBox.setToolTipText(RUN_TYPE_TOOLTIP);
		validationOptionsPanel.add(validationTypeBox, c);
		
		c.gridy++;
		c.gridx = 0;
		c.fill = c.HORIZONTAL;
		validationOptionsPanel.add(new JLabel("Input folder"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		validationInputFolderField = new JTextField(20);
		validationInputFolderField.setEnabled(false);
		validationInputFolderField.setToolTipText(RUN_INPUT_TOOLTIP);
		validationOptionsPanel.add(validationInputFolderField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		c.fill = c.NONE;
		validationInputFolderButton = new JButton("Select");
		validationInputFolderButton.addActionListener(new FolderChooserListener(this, validationInputFolderField, 1));
		validationOptionsPanel.add(validationInputFolderButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.fill = c.HORIZONTAL;
		validationOptionsPanel.add(new JLabel("Output folder"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		validationOutputFolderField = new JTextField(20);
		validationOutputFolderField.setEnabled(false);
		validationOutputFolderField.setToolTipText(RUN_OUTPUT_TOOLTIP);
		validationOutputFolderField.setText(EZUtils.getSourcePath()+"python/run-output/");
		validationOptionsPanel.add(validationOutputFolderField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		c.fill = c.NONE;
		validationOutputFolderButton = new JButton("Select");
		validationOutputFolderButton.addActionListener(new FolderChooserListener(this, validationOutputFolderField, 0));
		validationOptionsPanel.add(validationOutputFolderButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.fill = c.HORIZONTAL;
		validationOptionsPanel.add(new JLabel("Model file"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		validationModelFileField = new JTextField(20);
		validationModelFileField.setEnabled(false);
		validationModelFileField.setToolTipText(RUN_MODEL_TOOLTIP);
		validationOptionsPanel.add(validationModelFileField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		c.fill = c.NONE;
		validationModelFileButton = new JButton("Select");
		validationModelFileButton.addActionListener(new ModelFileListener(this, validationModelFileField));
		validationOptionsPanel.add(validationModelFileButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = c.NONE;
		validationOptionsPanel.add(new JLabel("Batch size"), c);
		c.gridx += 1;
		validationBatchSizeSpinner = new JSpinner(batchSizeSpinnerModel);
		validationBatchSizeSpinner.setPreferredSize(new Dimension(70, 19));
		validationBatchSizeSpinner.setToolTipText(BATCH_SIZE_TOOLTIP);
		validationOptionsPanel.add(validationBatchSizeSpinner, c);
		c.gridx += 1;
		validationOptionsPanel.add(new JLabel("Loaders"), c);
		c.gridx += 1;
		c.gridwidth = 2;
		validationLoadersSpinner = new JSpinner(loadersSpinnerModel);
		validationLoadersSpinner.setPreferredSize(new Dimension(70, 19));
		validationLoadersSpinner.setToolTipText(LOADERS_TOOLTIP);
		validationOptionsPanel.add(validationLoadersSpinner, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 5;
		c.fill = c.HORIZONTAL;
		validationButton = new JButton("RUN");
		validationButton.addActionListener(new ValidationButtonListener(this));
		validationOptionsPanel.add(validationButton, c);
		
		validationPanel.add(validationOptionsPanel, b);
		
		validationResultsPanel = new JPanel(new GridBagLayout());		
		c = new GridBagConstraints();
		c.anchor = c.WEST;
		c.fill = c.NONE;
		c.gridy = 0;
		c.gridx = 0;
		c.insets = new Insets(5,5,5,5);
		
		validationResultsPanel.add(new JLabel("Status:"), c);
		c.gridx += 1;
		validationStatusLabel = new JLabel("Idle");
		validationResultsPanel.add(validationStatusLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		validationTestingBar = new JProgressBar();
		validationTestingBar.setPreferredSize(new Dimension(200, 20));
		validationTestingBar.setValue(0);
		validationTestingBar.setStringPainted(true);
		validationTestingBar.setString("Testing: Idle");
		validationResultsPanel.add(validationTestingBar, c);
		
		c.gridy++;
		c.gridwidth = 1;
		validationResultsPanel.add(new JLabel(EZUtils.makeHTML("<U>Accuracy:</U>")), c);
		c.gridx += 1;
		validationOverallAccuracyLabel = new JLabel("");
		validationResultsPanel.add(validationOverallAccuracyLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		validationResultsPanel.add(new JLabel(EZUtils.makeHTML("<U>Precision:</U>")), c);
		
		c.gridy++;
		validationResultsPanel.add(new JLabel("Best:", JLabel.RIGHT), c);
		c.gridx += 1;
		validationBestPrecisionLabel = new JLabel("");
		validationBestPrecisionLabel.setForeground(EZUtils.GREEN);
		validationResultsPanel.add(validationBestPrecisionLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		validationResultsPanel.add(new JLabel("Worst:", JLabel.RIGHT), c);
		c.gridx += 1;
		validationWorstPrecisionLabel = new JLabel("");
		validationWorstPrecisionLabel.setForeground(EZUtils.RED);
		validationResultsPanel.add(validationWorstPrecisionLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		validationResultsPanel.add(new JLabel(EZUtils.makeHTML("<U>Recall:</U>")), c);
		
		c.gridy++;
		validationResultsPanel.add(new JLabel("Best:", JLabel.RIGHT), c);
		c.gridx += 1;
		validationBestRecallLabel = new JLabel("");
		validationBestRecallLabel.setForeground(EZUtils.GREEN);
		validationResultsPanel.add(validationBestRecallLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		validationResultsPanel.add(new JLabel("Worst:", JLabel.RIGHT), c);
		c.gridx += 1;
		validationWorstRecallLabel = new JLabel("");
		validationWorstRecallLabel.setForeground(EZUtils.RED);
		validationResultsPanel.add(validationWorstRecallLabel, c);
		
		b.gridx += 1;
		validationPanel.add(validationResultsPanel, b);
		
		validationCardPanel = new JPanel(new CardLayout());
		validationCardLayout = (CardLayout) validationCardPanel.getLayout();
		validationCardPanel.setPreferredSize(CARD_PANEL_SIZE);
		
		validationEmptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		validationEmptyPanel.add(new JLabel("(Confusion matrix will appear here after validation.)"));
		validationCardPanel.add(validationEmptyPanel, "empty");
		
		validationCardLayout.show(validationCardPanel, "empty");
		
		b.gridy++;
		b.gridx = 0;
		b.gridwidth = 2;
		validationPanel.add(validationCardPanel, b);
		
		tabbedPane.add("Validation", validationPanel);
		
		testPanel = new JPanel(new GridBagLayout());
		b = new GridBagConstraints();
		b.anchor = b.CENTER;
		b.fill = b.HORIZONTAL;
		b.gridy = 0;
		b.gridx = 0;
		
		testOptionsPanel = new JPanel(new GridBagLayout());		
		c = new GridBagConstraints();
		c.anchor = c.WEST;
		c.gridy = 0;
		c.gridx = 0;
		c.insets = new Insets(5,5,5,5);
		
		c.gridwidth = 5;
		JLabel testNoteLabel = new JLabel("(For running predictions on unlabelled data.)", JLabel.CENTER);
		testNoteLabel.setForeground(Color.GRAY);
		testOptionsPanel.add(testNoteLabel, c);
		
		c.gridy++;
		c.gridwidth = 1;
		testOptionsPanel.add(new JLabel("Run ID"), c);
		c.gridx += 1;
		testIDField = new JTextField();
		testIDField.setPreferredSize(new Dimension(86, 19));
		testIDField.setToolTipText(RUN_ID_TOOLTIP);
		testIDField.setText("model");
		testOptionsPanel.add(testIDField, c);
		
		c.gridy++;
		c.gridx = 0;
		testOptionsPanel.add(new JLabel("Input type"), c);
		c.gridx += 1;
		testTypeBox = new JComboBox<String>(new String[] {"bin","img"});
		testTypeBox.setSelectedItem("bin");
		testTypeBox.setPreferredSize(new Dimension(testTypeBox.getPreferredSize().width+5, testTypeBox.getPreferredSize().height));
		testTypeBox.setToolTipText(RUN_TYPE_TOOLTIP);
		testOptionsPanel.add(testTypeBox, c);
		
		c.gridy++;
		c.gridx = 0;
		c.fill = c.HORIZONTAL;
		testOptionsPanel.add(new JLabel("Input folder"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		testInputFolderField = new JTextField(20);
		testInputFolderField.setEnabled(false);
		testInputFolderField.setToolTipText(RUN_INPUT_TOOLTIP);
		testOptionsPanel.add(testInputFolderField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		c.fill = c.NONE;
		testInputFolderButton = new JButton("Select");
		testInputFolderButton.addActionListener(new FolderChooserListener(this, testInputFolderField, 0));
		testOptionsPanel.add(testInputFolderButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.fill = c.HORIZONTAL;
		testOptionsPanel.add(new JLabel("Output folder"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		testOutputFolderField = new JTextField(20);
		testOutputFolderField.setEnabled(false);
		testOutputFolderField.setToolTipText(RUN_OUTPUT_TOOLTIP);
		testOutputFolderField.setText(EZUtils.getSourcePath()+"python/run-output/");
		testOptionsPanel.add(testOutputFolderField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		c.fill = c.NONE;
		testOutputFolderButton = new JButton("Select");
		testOutputFolderButton.addActionListener(new FolderChooserListener(this, testOutputFolderField, 0));
		testOptionsPanel.add(testOutputFolderButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.fill = c.HORIZONTAL;
		testOptionsPanel.add(new JLabel("Model file"), c);
		c.gridx += 1;
		c.gridwidth = 3;
		testModelFileField = new JTextField(20);
		testModelFileField.setEnabled(false);
		testModelFileField.setToolTipText(RUN_MODEL_TOOLTIP);
		testOptionsPanel.add(testModelFileField, c);
		c.gridx += c.gridwidth;
		c.gridwidth = 1;
		c.fill = c.NONE;
		testModelFileButton = new JButton("Select");
		testModelFileButton.addActionListener(new ModelFileListener(this, testModelFileField));
		testOptionsPanel.add(testModelFileButton, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		c.fill = c.NONE;
		testOptionsPanel.add(new JLabel("Batch size"), c);
		c.gridx += 1;
		testBatchSizeSpinner = new JSpinner(batchSizeSpinnerModel);
		testBatchSizeSpinner.setPreferredSize(new Dimension(70, 19));
		testBatchSizeSpinner.setToolTipText(BATCH_SIZE_TOOLTIP);
		testOptionsPanel.add(testBatchSizeSpinner, c);
		c.gridx += 1;
		testOptionsPanel.add(new JLabel("Loaders"), c);
		c.gridx += 1;
		c.gridwidth = 2;
		testLoadersSpinner = new JSpinner(loadersSpinnerModel);
		testLoadersSpinner.setPreferredSize(new Dimension(70, 19));
		testLoadersSpinner.setToolTipText(LOADERS_TOOLTIP);
		testOptionsPanel.add(testLoadersSpinner, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 5;
		c.fill = c.HORIZONTAL;
		testButton = new JButton("RUN");
		testButton.addActionListener(new TestButtonListener(this));
		testOptionsPanel.add(testButton, c);
		
		testPanel.add(testOptionsPanel, b);
		
		testResultsPanel = new JPanel(new GridBagLayout());		
		c = new GridBagConstraints();
		c.anchor = c.WEST;
		c.fill = c.NONE;
		c.gridy = 0;
		c.gridx = 0;
		c.insets = new Insets(5,5,5,5);
		
		testResultsPanel.add(new JLabel("Status:"), c);
		c.gridx += 1;
		testStatusLabel = new JLabel("Idle");
		testResultsPanel.add(testStatusLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		testBinBar = new JProgressBar();
		testBinBar.setPreferredSize(new Dimension(200, 20));
		testBinBar.setValue(0);
		testBinBar.setStringPainted(true);
		testBinBar.setString("Bins: 0/?");
		testBinBar.setVisible(false);
		testResultsPanel.add(testBinBar, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 2;
		testBatchBar = new JProgressBar();
		testBatchBar.setPreferredSize(new Dimension(200, 20));
		testBatchBar.setValue(0);
		testBatchBar.setStringPainted(true);
		testBatchBar.setString("Testing: Idle");
		testResultsPanel.add(testBatchBar, c);
		
		c.gridy++;
		c.gridx = 0;
		c.gridwidth = 1;
		testResultsPanel.add(new JLabel("File count:"), c);
		c.gridx += 1;
		testFileCountLabel = new JLabel("");
		testResultsPanel.add(testFileCountLabel, c);
		
		c.gridy++;
		c.gridx = 0;
		testResultsPanel.add(new JLabel("Mean score:"), c);
		c.gridx += 1;
		testMeanScoreLabel = new JLabel("");
		testResultsPanel.add(testMeanScoreLabel, c);
		
		b.gridx += 1;
		testPanel.add(testResultsPanel, b);
		
		testCardPanel = new JPanel(new CardLayout());
		testCardLayout = (CardLayout) testCardPanel.getLayout();
		testCardPanel.setPreferredSize(CARD_PANEL_SIZE);
		
		testEmptyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		testEmptyPanel.add(new JLabel("(Graphs will appear here after testing.)"));
		testCardPanel.add(testEmptyPanel, "empty");
		
		testCardLayout.show(testCardPanel, "empty");
		
		b.gridy++;
		b.gridx = 0;
		b.gridwidth = 2;
		testPanel.add(testCardPanel, b);
		
		tabbedPane.add("Testing", testPanel);
		
		this.add(tabbedPane);
		this.pack();
		this.setVisible(true);
		
		// https://stackoverflow.com/questions/79696161/jtextfield-have-long-text-scroll-to-the-right-when-using-settext?noredirect=1#comment140578292_79696161
		trainInputFolderField.select(trainInputFolderField.getText().length(), trainInputFolderField.getText().length());
		trainOutputFolderField.select(trainOutputFolderField.getText().length(), trainOutputFolderField.getText().length());
		validationModelFileField.select(validationModelFileField.getText().length(), validationModelFileField.getText().length());
		validationInputFolderField.select(validationInputFolderField.getText().length(), validationInputFolderField.getText().length());
		validationOutputFolderField.select(validationOutputFolderField.getText().length(), validationOutputFolderField.getText().length());
		testModelFileField.select(testModelFileField.getText().length(), testModelFileField.getText().length());
		testInputFolderField.select(testInputFolderField.getText().length(), testInputFolderField.getText().length());
		testOutputFolderField.select(testOutputFolderField.getText().length(), testOutputFolderField.getText().length());
		
		//TESTING CODE
		//this.currentCompletedRun = new CompletedRunObject(validationOutputFolderField.getText()+validationIDField.getText()+"/img_results.json");
		//validationConfusionMatrixButton.setEnabled(true);
		
		//TESTING CODE
		//ConfusionMatrixPanel newCMPanel = new ConfusionMatrixPanel(new CompletedRunObject("C:/Users/Holly/eclipse-workspace/ifcb_classifier_gui/target/python/training-output/model/results.mat"));
		//trainConfusionPanel = newCMPanel;
		//trainCardPanel.add(newCMPanel, "matrix");
		//trainCardLayout.show(trainCardPanel, "matrix");
		//this.pack();
		
		//TESTING CODE
		//String jsonPath = testOutputFolderField.getText()+testIDField.getText()+"/img_results.json";
		//TestChartsPanel tcPanel = new TestChartsPanel(jsonPath, 8, new Dimension(200, 200));
		//testCardPanel.add(tcPanel, "graphs");
		//testCardLayout.show(testCardPanel, "graphs");
		
		commandManager = new CommandManager(this);
	}
	
	public class TextFieldEmptyListener implements FocusListener {
		
		public JTextField textField;
		public String replacementValue;
		
		public TextFieldEmptyListener(JTextField textField, String replacementValue) {
			this.textField = textField;
			this.replacementValue = replacementValue;
		}

		@Override
		public void focusGained(FocusEvent e) {}

		@Override
		public void focusLost(FocusEvent e) {
			String currValue = textField.getText();
			if (currValue.length() == 0)
				textField.setText(replacementValue);
		}
		
	}
	
	public class OldValueListener implements FocusListener {
		
		public JTextField inputField;
		public String oldValue;
		
		public OldValueListener(JTextField inputField) {
			this.inputField = inputField;
		}

		@Override
		public void focusGained(FocusEvent e) {
			this.oldValue = inputField.getText();
		}

		@Override
		public void focusLost(FocusEvent e) {
			if (inputField.getText().length() == 0)
				inputField.setText(oldValue);
			
		}
		
	}
	
	public class FolderChooserListener implements ActionListener {
		
		public MainFrame mainFrame;
		public JTextField textField;
		public int minimumSubfolders;
		
		public FolderChooserListener(MainFrame mainFrame, JTextField textField, int minimumSubfolders) {
			this.mainFrame = mainFrame;
			this.textField = textField;
			this.minimumSubfolders = minimumSubfolders;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fc.setMultiSelectionEnabled(false);
			if (textField.getText().length() > 0)
				fc.setCurrentDirectory(new File(textField.getText()));
			else
				fc.setCurrentDirectory(new File(EZUtils.getSourcePath()));
			int returnVal = fc.showOpenDialog(mainFrame);
			if (returnVal != fc.APPROVE_OPTION)
				return;
			if (minimumSubfolders > 0) {
				File[] dirs = fc.getSelectedFile().listFiles();
				int dir_count = 0;
				for (int i = 0; i < dirs.length; i++) {
					if (dirs[i].isDirectory() && dirs[i].list().length >= 1)
						dir_count++;
					if (dir_count >= minimumSubfolders) break;
				}
				if (dir_count < minimumSubfolders) {
					EZUtils.SimpleErrorDialog(mainFrame, "Selected input folder must contain at least "+String.valueOf(minimumSubfolders)
								+" subfolders, presumably named by species/classification label and containing IFCB image data.", 300);
					return;
				}
			}
			textField.setText(fc.getSelectedFile().getAbsolutePath().replace('\\', '/')+"/");
		}
		
	}
	
	public class DatasetConfigListener implements ActionListener {
		
		public MainFrame mainFrame;
		
		public DatasetConfigListener(MainFrame mainFrame) {
			this.mainFrame = mainFrame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			if (trainDatasetConfigButton.getText().equals("HALT")) {
				haltProgram();
			} else {
				Object[] options = {"Select existing", "Create new", "Cancel"};
				int result = JOptionPane.showOptionDialog(this.mainFrame,
					"Dataset config options:",
					ProgramInfo.getProgramName(),
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[2]);
				if (result == 0) {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fc.setAcceptAllFileFilterUsed(false);
					fc.setMultiSelectionEnabled(false);
					if (trainInputFolderField.getText().length() > 0)
						fc.setCurrentDirectory(new File(trainInputFolderField.getText()));
					else
						fc.setCurrentDirectory(new File(EZUtils.getSourcePath()));
					fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
					int fc_result = fc.showOpenDialog(mainFrame);
					if (fc_result != JFileChooser.APPROVE_OPTION) return;
					trainInputFolderField.setText(fc.getSelectedFile().getAbsolutePath().replace('\\', '/'));
				} else if (result == 1) {
					JFileChooser fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fc.setAcceptAllFileFilterUsed(false);
					fc.setMultiSelectionEnabled(true);
					if (trainInputFolderField.getText().length() > 0)
						fc.setCurrentDirectory(new File(trainInputFolderField.getText()));
					else
						fc.setCurrentDirectory(new File(EZUtils.getSourcePath()));
					fc.setDialogTitle("Select data folders for dataset config");
					int fc_result = fc.showOpenDialog(mainFrame);
					if (fc_result != JFileChooser.APPROVE_OPTION) return;
					File[] selectedFolders = fc.getSelectedFiles();
					fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fc.setAcceptAllFileFilterUsed(false);
					fc.setMultiSelectionEnabled(false);
					fc.setCurrentDirectory(new File(EZUtils.getSourcePath()));
					fc.addChoosableFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)","csv"));
					fc.setDialogTitle("Select .csv file to save dataset config to");
					fc_result = fc.showSaveDialog(mainFrame);
					if (fc_result != JFileChooser.APPROVE_OPTION) return;
					File destinationCSV = fc.getSelectedFile();
					if (!destinationCSV.getAbsolutePath().endsWith(".csv"))
						destinationCSV = new File(destinationCSV.getAbsolutePath()+".csv");
					if (destinationCSV.exists()) {
						int overwriteResult = JOptionPane.showConfirmDialog(this.mainFrame,
							"Overwrite selected file?",
							ProgramInfo.getProgramName(),
							JOptionPane.YES_NO_OPTION,
							JOptionPane.WARNING_MESSAGE);
						if (overwriteResult != JFileChooser.APPROVE_OPTION) return;
					}
					setCurrentMode(CREATING_DATASET_CONFIG);
					setProcessButtonsEnabled(false);
					setTrainingStatusToCreatingDatasetConfig();
					String command = "python neuston_util.py MAKE_DATASET_CONFIG";
					for (int i = 0; i < selectedFolders.length; i++)
						command += " \""+selectedFolders[i].getAbsolutePath()+"\"";
					command += " -o \""+destinationCSV.getAbsolutePath()+"\"";
					commandManager.addCommand(command);
				} // else do nothing
			}
		}
		
	}
	
	public class ModelFileListener implements ActionListener {
		
		protected MainFrame mainFrame;
		protected JTextField textField;
		
		public ModelFileListener(MainFrame mainFrame, JTextField textField) {
			this.mainFrame = mainFrame;
			this.textField = textField;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setAcceptAllFileFilterUsed(false);
			fc.setMultiSelectionEnabled(false);
			if (textField.getText().length() > 0)
				fc.setCurrentDirectory(new File(textField.getText()));
			else
				fc.setCurrentDirectory(new File(EZUtils.getSourcePath()+"python/training-output/"));
			fc.addChoosableFileFilter(new FileNameExtensionFilter("PyTorch model file (*.ptl)","ptl"));
			int result = fc.showOpenDialog(mainFrame);
			if (result != JFileChooser.APPROVE_OPTION) return;
			textField.setText(fc.getSelectedFile().getAbsolutePath().replace('\\', '/'));
		}
		
	}
	
	public class RandomNumberListener implements ActionListener {
		
		public JTextField textField;
		
		public RandomNumberListener(JTextField textField) {
			this.textField = textField;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			int randomNum = ThreadLocalRandom.current().nextInt(1, 999999);
			textField.setText(String.valueOf(randomNum));
		}
		
	}
	
	public class SettingsButtonListener implements ActionListener {
		
		public MainFrame mainFrame;
		
		public SettingsButtonListener(MainFrame mainFrame) {
			this.mainFrame = mainFrame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			TrainSettingsDialog settingsDialog = new TrainSettingsDialog(mainFrame);
			settingsDialog.setVisible(true);
		}
		
	}
	
	public class TrainButtonListener implements ActionListener {
		
		protected MainFrame mainFrame;
		
		public TrainButtonListener(MainFrame mainFrame) {
			this.mainFrame = mainFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (trainButton.getText().equals("HALT")) {
				haltProgram();
			} else {
				if (trainIDField.getText().length() == 0) {
					EZUtils.SimpleErrorDialog(mainFrame, "You must provide a name (training ID) for the model.", 300);
					return;
				}
				if (trainInputFolderField.getText().length() == 0) {
					EZUtils.SimpleErrorDialog(mainFrame, "An input data folder must be selected in order to train a model!", 300);
					return;
				}
				if (new File(trainOutputFolderField.getText()+trainIDField.getText()).exists()) {
					int res = JOptionPane.showConfirmDialog(mainFrame, 
							EZUtils.makeHTML("A model folder with the same training ID in the specified output folder already exists. Overwrite?", 300), 
							ProgramInfo.getProgramName(), 
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.WARNING_MESSAGE);
					if (res != JOptionPane.YES_OPTION) return;
				}
				setCurrentMode(MainFrame.TRAINING);
				setProcessButtonsEnabled(false);
				mainFrame.setTrainingStatusToAwaitingResponse();
				mainFrame.resetTrainingResultsLabelsAndBars();
				String command = "python neuston_net.py";
				command += " --batch "+String.valueOf((int) trainBatchSizeSpinner.getValue());
				command += " --loaders "+String.valueOf((int) trainLoadersSpinner.getValue());
				command += " TRAIN";
				command += " \""+trainInputFolderField.getText()+"\"";
				command += " "+String.valueOf(trainModelBox.getSelectedItem());
				command += " "+trainIDField.getText();
				command += " --seed "+trainSeedField.getText();
				command += " --outdir \""+trainOutputFolderField.getText()+trainIDField.getText()+"/\"";
				command += " --split \""+String.valueOf(params.trainSplit)+":"+String.valueOf(100-params.trainSplit)+"\"";
				command += " --class-min "+String.valueOf(params.trainClassMin);
				if (params.trainClassMaxEnabled) // (Default in neuston_net is None.)
					command += " --class-max "+String.valueOf(params.trainClassMax);
				if (params.trainEpochsStopEnabled) {
					command += " --emin "+String.valueOf(params.trainEpochsMin);
					command += " --estop "+String.valueOf(params.trainEpochsStop);
				} else
					command += " --estop 0";
				command += " --emax "+String.valueOf(params.trainEpochsMax);
				if (!params.trainFlip.equals("Off"))
					command += " --flip \""+params.trainFlip+"\"";
				if (params.trainImgNorm)
					command += " --img-norm "+String.valueOf(params.trainImgNormMean)+" "+String.valueOf(params.trainImgNormSTD);
				if (params.trainUntrain)
					command += " --untrain";
				command += " --sizing_mode \""+params.trainSizingMode+"\"";
				if (!params.trainSizingMode.equals(params.SIZING_OPTIONS[0]))
					command += " --padfill "+String.valueOf(params.trainPaddingFill);
				if (params.trainRedSquare)
					command += " --redsquare "+String.valueOf(params.trainRedSquareSize);
				//command += " --class-config "; (Remember to implement this eventually.)
				//System.out.println(command); // TODO Send to Python manager instead.
				commandManager.addCommand(command);
			}
		}
		
	}
	
	public class ValidationButtonListener implements ActionListener {
		
		protected MainFrame mainFrame;
		
		public ValidationButtonListener(MainFrame mainFrame) {
			this.mainFrame = mainFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (validationButton.getText().equals("HALT")) {
				haltProgram();
			} else {
				if (validationIDField.getText().length() == 0) {
					EZUtils.SimpleErrorDialog(mainFrame, "You must provide a name (run ID) for the run.", 300);
					return;
				}
				if (validationInputFolderField.getText().length() == 0) {
					EZUtils.SimpleErrorDialog(mainFrame, "An input data folder must be selected first!", 300);
					return;
				}
				if (new File(validationOutputFolderField.getText()+validationIDField.getText()).exists()) {
					int res = JOptionPane.showConfirmDialog(mainFrame, 
							EZUtils.makeHTML("A folder with the same run ID in the specified output folder already exists. Overwrite?", 300), 
							ProgramInfo.getProgramName(), 
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.WARNING_MESSAGE);
					if (res != JOptionPane.YES_OPTION) return;
				}
				setCurrentMode(MainFrame.VALIDATING);
				setProcessButtonsEnabled(false);
				mainFrame.setValidationStatusToAwaitingResponse();
				mainFrame.resetValidationResultsLabelsAndBars();
				String command = "python neuston_net.py";
				command += " --batch "+String.valueOf((int) validationBatchSizeSpinner.getValue());
				command += " --loaders "+String.valueOf((int) validationLoadersSpinner.getValue());
				command += " RUN";
				command += " --type "+String.valueOf(validationTypeBox.getSelectedItem());
				command += " \""+validationInputFolderField.getText()+"\"";
				command += " \""+validationModelFileField.getText()+"\"";
				command += " "+validationIDField.getText();
				command += " --outdir \""+validationOutputFolderField.getText()+validationIDField.getText()+"/\"";
				commandManager.addCommand(command);
			}
		}
		
	}
	
	public class TestButtonListener implements ActionListener {
		
		protected MainFrame mainFrame;
		
		public TestButtonListener(MainFrame mainFrame) {
			this.mainFrame = mainFrame;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (testButton.getText().equals("HALT")) {
				haltProgram();
			} else {
				if (testIDField.getText().length() == 0) {
					EZUtils.SimpleErrorDialog(mainFrame, "You must provide a name (run ID) for the run.", 300);
					return;
				}
				if (testInputFolderField.getText().length() == 0) {
					EZUtils.SimpleErrorDialog(mainFrame, "An input data folder must be selected first!", 300);
					return;
				}
				if (new File(testOutputFolderField.getText()+testIDField.getText()).exists()) {
					int res = JOptionPane.showConfirmDialog(mainFrame, 
							EZUtils.makeHTML("A folder with the same run ID in the specified output folder already exists. Overwrite?", 300), 
							ProgramInfo.getProgramName(), 
							JOptionPane.YES_NO_OPTION, 
							JOptionPane.WARNING_MESSAGE);
					if (res != JOptionPane.YES_OPTION) return;
				}
				setCurrentMode(MainFrame.TESTING);
				setProcessButtonsEnabled(false);
				File[] inputFolder = new File(testInputFolderField.getText()).listFiles();
				roiFilesFound = 0;
				if (testTypeBox.getSelectedItem().equals("bin")) {
					for (int i = 0; i < inputFolder.length; i++) {
						if (inputFolder[i].getAbsolutePath().endsWith(".roi"))
							roiFilesFound++;
					}
				}
				testBinBar.setVisible(testTypeBox.getSelectedItem().equals("bin"));
				mainFrame.setTestingStatusToAwaitingResponse();
				mainFrame.resetTestingResultsLabelsAndBars();
				String command = "python neuston_net.py";
				command += " --batch "+String.valueOf((int) testBatchSizeSpinner.getValue());
				command += " --loaders "+String.valueOf((int) testLoadersSpinner.getValue());
				command += " RUN";
				command += " --type "+String.valueOf(testTypeBox.getSelectedItem());
				if (String.valueOf(testTypeBox.getSelectedItem()).equals("bin"))
					command += " --gobig --clobber";
				command += " \""+testInputFolderField.getText()+"\"";
				command += " \""+testModelFileField.getText()+"\"";
				command += " "+testIDField.getText();
				command += " --outdir \""+testOutputFolderField.getText()+testIDField.getText()+"/\"";
				commandManager.addCommand(command);
			}
		}
		
	}
	
	public void haltProgram() {
		commandManager.haltAndRestart();
		signalHaltOccurred();
	}
	
	public void setProcessButtonsEnabled(boolean boo) {
		trainDatasetConfigButton.setEnabled(boo);
		trainButton.setEnabled(boo);
		validationButton.setEnabled(boo);
		testButton.setEnabled(boo);
		if (boo == false) {
			if (getCurrentMode() == TRAINING) {
				trainButton.setText("HALT");
				trainButton.setEnabled(true);
			} else if (getCurrentMode() == VALIDATING) {
				validationButton.setText("HALT");
				validationButton.setEnabled(true);
			} else if (getCurrentMode() == TESTING) {
				testButton.setText("HALT");
				testButton.setEnabled(true);
			} else if (getCurrentMode() == CREATING_DATASET_CONFIG) {
				trainDatasetConfigButton.setText("HALT");
				trainDatasetConfigButton.setEnabled(true);
			}
		} else {
			trainButton.setText("TRAIN");
			validationButton.setText("RUN");
			testButton.setText("RUN");
			trainDatasetConfigButton.setText("Use dataset config");
		}
	}
	
	public void setTrainingStatusToIdle() {
		trainStatusLabel.setText("Idle");
	}
	
	public void setTrainingStatusToAwaitingResponse() {
		trainStatusLabel.setText("Awaiting response...");
	}
	
	public void setTrainingStatusToCreatingDatasetConfig() {
		trainStatusLabel.setText("Creating dataset config...");
	}
	
	public void setTrainingStatusToRunning() {
		trainStatusLabel.setText("Running...");
	}
	
	public void setTrainingStatusToDone() {
		trainStatusLabel.setText("Done!");
		String matPath = trainOutputFolderField.getText()+trainIDField.getText()+"/results.mat";
		ConfusionMatrixPanel newCMPanel = new ConfusionMatrixPanel(new CompletedRunObject(matPath));
		trainCardPanel.add(newCMPanel, "matrix");
		trainCardLayout.show(trainCardPanel, "matrix");
		validationIDField.setText(trainIDField.getText());
		testIDField.setText(trainIDField.getText());
		String tipn = trainInputFolderField.getText();
		if (tipn.endsWith("training/")) {
			String possiblePath = tipn.substring(0, tipn.length()-9)+"testing/";
			if (new File(possiblePath).exists()) {
				validationInputFolderField.setText(possiblePath);
				validationInputFolderField.select(validationInputFolderField.getText().length(), validationInputFolderField.getText().length());
			}
		}
		validationModelFileField.setText(trainOutputFolderField.getText()+trainIDField.getText()+"/"+trainIDField.getText()+".ptl");
		validationModelFileField.select(validationModelFileField.getText().length(), validationModelFileField.getText().length());
		testModelFileField.setText(trainOutputFolderField.getText()+trainIDField.getText()+"/"+trainIDField.getText()+".ptl");
		testModelFileField.select(testModelFileField.getText().length(), testModelFileField.getText().length());
		setProcessButtonsEnabled(true);
		setCurrentMode(MainFrame.IDLE);
	}
	
	public void setCurrentEpoch(int num) {
		if (num < 1) {
			this.trainEpochNumberLabel.setText("");
		} else if (params.trainEpochsStopEnabled) {
			this.trainEpochNumberLabel.setText(String.valueOf(num)+" (Max: "+String.valueOf(params.trainEpochsMax)
													+", Stop: "+String.valueOf(params.trainEpochsStop)+")");
		} else {
			this.trainEpochNumberLabel.setText(String.valueOf(num)+" (Max: "+String.valueOf(params.trainEpochsMax)+")");
		}
	}
	
	public void resetEpochTrainingProgressBar() {
		this.trainEpochTrainingBar.setValue(0);
		this.trainEpochTrainingBar.setString("Training: 0/?");
	}
	
	public void setEpochTrainingProgress(int dividend, int divisor) {
		this.trainEpochTrainingBar.setValue(Math.floorDiv(100*dividend, divisor));
		this.trainEpochTrainingBar.setString("Training: "+String.valueOf(dividend)+"/"+String.valueOf(divisor));
	}
	
	public void resetEpochValidationProgressBar() {
		this.trainEpochValidationBar.setValue(0);
		this.trainEpochValidationBar.setString("Validation: 0/?");
	}
	
	public void setEpochValidationProgress(int dividend, int divisor) {
		this.trainEpochValidationBar.setValue(Math.floorDiv(100*dividend, divisor));
		this.trainEpochValidationBar.setString("Validation: "+String.valueOf(dividend)+"/"+String.valueOf(divisor));
	}
	
	/**
	 * Signals that a training epoch has finished and passes the information to be displayed.
	 */
	public void finishEpoch(EpochObject epoch) {
		this.trainLastEpochLabel.setText(String.valueOf(epoch.epochNumber));
		DecimalFormat threeDigits = new DecimalFormat("0.000");
		this.trainLastTrainLossLabel.setText(threeDigits.format(epoch.trainLoss));
		this.trainLastValLossLabel.setText(threeDigits.format(epoch.valLoss));
		this.trainLastValF1WLabel.setText(EZUtils.oneDigit(epoch.valF1W)+"%");
		this.trainLastValF1MLabel.setText(EZUtils.oneDigit(epoch.valF1M)+"%");
		if (epoch.isBest) {
			this.trainBestEpochLabel.setText(String.valueOf(epoch.epochNumber));
			this.trainBestTrainLossLabel.setText(threeDigits.format(epoch.trainLoss));
			this.trainLastTrainLossLabel.setForeground(EZUtils.GREEN);
			this.trainBestValLossLabel.setText(threeDigits.format(epoch.valLoss));
			this.trainLastValLossLabel.setForeground(EZUtils.GREEN);
			this.trainBestValF1WLabel.setText(EZUtils.oneDigit(epoch.valF1W)+"%");
			this.trainLastValF1WLabel.setForeground(EZUtils.GREEN);
			this.trainBestValF1MLabel.setText(EZUtils.oneDigit(epoch.valF1M)+"%");
			this.trainLastValF1MLabel.setForeground(EZUtils.GREEN);
		} else {
			if (epoch.trainLoss > Double.valueOf(trainBestTrainLossLabel.getText()))
				this.trainLastTrainLossLabel.setForeground(EZUtils.RED);
			else
				this.trainLastTrainLossLabel.setForeground(EZUtils.GREEN);
			if (epoch.valLoss > Double.valueOf(trainBestValLossLabel.getText()))
				this.trainLastValLossLabel.setForeground(EZUtils.RED);
			else
				this.trainLastValLossLabel.setForeground(EZUtils.GREEN);
			if (epoch.valF1W < Double.valueOf(trainBestValF1WLabel.getText().replaceAll("%", "")))
				this.trainLastValF1WLabel.setForeground(EZUtils.RED);
			else
				this.trainLastValF1WLabel.setForeground(EZUtils.GREEN);
			if (epoch.valF1M < Double.valueOf(trainBestValF1MLabel.getText().replaceAll("%", "")))
				this.trainLastValF1MLabel.setForeground(EZUtils.RED);
			else
				this.trainLastValF1MLabel.setForeground(EZUtils.GREEN);
		}
	}
	
	/**
	 * Signals that dataset configuration creation has finished.
	 */
	public void finishCreatingDatasetConfig(String producedFileName) {
		trainInputFolderField.setText(producedFileName.replace('\\', '/'));
		setCurrentMode(MainFrame.IDLE);
		setTrainingStatusToIdle();
		setProcessButtonsEnabled(true);
	}
	
	public void resetTrainingResultsLabelsAndBars() {
		this.commandManager.resetCurrentEpoch();
		this.resetEpochTrainingProgressBar();
		this.resetEpochValidationProgressBar();
		this.trainBestEpochLabel.setText("");
		this.trainBestTrainLossLabel.setText("");
		this.trainBestValLossLabel.setText("");
		this.trainBestValF1WLabel.setText("");
		this.trainBestValF1MLabel.setText("");
		this.trainLastEpochLabel.setText("");
		this.trainLastTrainLossLabel.setText("");
		this.trainLastValLossLabel.setText("");
		this.trainLastValF1WLabel.setText("");
		this.trainLastValF1MLabel.setText("");
		this.trainCardLayout.show(trainCardPanel, "empty");
	}
	
	public void setValidationStatusToIdle() {
		this.validationStatusLabel.setText("Idle");
	}
	public void setValidationStatusToAwaitingResponse() {
		validationStatusLabel.setText("Awaiting response...");
	}
	
	public void setValidationStatusToRunning() {
		this.validationStatusLabel.setText("Running...");
	}
	
	public void setValidationStatusToDone() {
		this.validationStatusLabel.setText("Done!");
		String jsonPath = validationOutputFolderField.getText()+validationIDField.getText()+"/img_results.json";
		CompletedRunObject cRO = new CompletedRunObject(jsonPath);
		this.validationOverallAccuracyLabel.setText(EZUtils.oneDigit(cRO.getAccuracy())+"%");
		this.validationBestPrecisionLabel.setText(cRO.getBestPrecisionString());
		this.validationWorstPrecisionLabel.setText(cRO.getWorstPrecisionString());
		this.validationBestRecallLabel.setText(cRO.getBestRecallString());
		this.validationWorstRecallLabel.setText(cRO.getWorstRecallString());
		ConfusionMatrixPanel newCMPanel = new ConfusionMatrixPanel(cRO);
		validationCardPanel.add(newCMPanel, "matrix");
		validationCardLayout.show(validationCardPanel, "matrix");
		setProcessButtonsEnabled(true);
		setCurrentMode(MainFrame.IDLE);
	}
	
	public void setValidationProgress(int dividend, int divisor) {
		this.validationTestingBar.setValue(Math.floorDiv(100*dividend, divisor));
		this.validationTestingBar.setString("Testing: "+String.valueOf(dividend)+"/"+String.valueOf(divisor));
	}
	
	public void resetValidationProgressBar() {
		this.validationTestingBar.setValue(0);
		this.validationTestingBar.setString("Testing: 0/?");
	}
	
	public void resetValidationResultsLabelsAndBars() {
		this.resetValidationProgressBar();
		this.validationOverallAccuracyLabel.setText("");
		this.validationBestPrecisionLabel.setText("");
		this.validationBestRecallLabel.setText("");
		this.validationWorstPrecisionLabel.setText("");
		this.validationWorstRecallLabel.setText("");
		this.validationCardLayout.show(validationCardPanel, "empty");
	}
	
	public void setTestingStatusToIdle() {
		this.testStatusLabel.setText("Idle");
	}
	public void setTestingStatusToAwaitingResponse() {
		testStatusLabel.setText("Awaiting response...");
	}
	
	public void setTestingStatusToRunning() {
		this.testStatusLabel.setText("Running...");
	}
	
	public void setTestingStatusToDone() {
		this.testStatusLabel.setText("Done!");
		if (this.roiFilesFound > 0) {
			this.testBinBar.setValue(100);
			this.testBinBar.setString("Bins: "+String.valueOf(this.roiFilesFound)+"/"+String.valueOf(this.roiFilesFound));
		}
		String outputPath = testOutputFolderField.getText()+testIDField.getText();
		TestChartsPanel tcPanel = new TestChartsPanel(outputPath, 8, new Dimension(200, 200));
		DecimalFormat twoDigits = new DecimalFormat("0.00");
		this.testFileCountLabel.setText(String.valueOf(tcPanel.getFileCount()));
		this.testMeanScoreLabel.setText(twoDigits.format(tcPanel.getMeanScore()));
		testCardPanel.add(tcPanel, "graphs");
		testCardLayout.show(testCardPanel, "graphs");
		setProcessButtonsEnabled(true);
		setCurrentMode(MainFrame.IDLE);
	}
	
	public void setTestingProgress(int dataloaderNum, int dividend, int divisor) {
		if (this.roiFilesFound > 0) {
			this.testBinBar.setValue(Math.floorDiv(100*dataloaderNum, this.roiFilesFound));
			this.testBinBar.setString("Bins: "+String.valueOf(dataloaderNum)+"/"+String.valueOf(this.roiFilesFound));
		}
		this.testBatchBar.setValue(Math.floorDiv(100*dividend, divisor));
		this.testBatchBar.setString("Batches: "+String.valueOf(dividend)+"/"+String.valueOf(divisor));
	}
	
	public void resetTestingProgressBar() {
		this.testBinBar.setValue(0);
		this.testBinBar.setString("Bins: 0/?");
		this.testBatchBar.setValue(0);
		this.testBatchBar.setString("Batches: 0/?");
	}
	
	public void resetTestingResultsLabelsAndBars() {
		this.resetTestingProgressBar();
		this.testFileCountLabel.setText("");
		this.testMeanScoreLabel.setText("");
		this.testCardLayout.show(testCardPanel, "empty");
	}
	
	/**
	 * Signals that a Python process has been halted via a halt button.
	 */
	public void signalHaltOccurred() {
		if (getCurrentMode() == this.TRAINING || getCurrentMode() == this.CREATING_DATASET_CONFIG)
			trainStatusLabel.setText("Halted!");
		else if (getCurrentMode() == this.VALIDATING)
			validationStatusLabel.setText("Halted!");
		else if (getCurrentMode() == this.TESTING)
			testStatusLabel.setText("Halted!");
		setCurrentMode(this.IDLE);
		setProcessButtonsEnabled(true);
	}
	
	/**
	 * Sets the variable that signals what the program is currently doing.
	 * Options: IDLE, TRAINING, VALIDATING, TESTING, CREATING_DATASET_CONFIG
	 */
	protected void setCurrentMode(int mode) {
		assert mode >= IDLE && mode <= CREATING_DATASET_CONFIG;
		this.currentMode = mode;
	}
	
	/**
	 * What the program is currently doing (or supposed to be doing at least!)
	 */
	public int getCurrentMode() {
		return this.currentMode;
	}
	
	/**
	 * Number of .roi files found when testing a bin folder.
	 */
	public int getROIFileCount() {
		return roiFilesFound;
	}
	
	/**
	 * Limits entry in text field to numbers only.
	 */
	public PlainDocument JIntFilter() {
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            char c = str.charAt(0);
	            if (c >= '0' && c <= '9')
	            	super.insertString(offs, str, a);
	        }
		};
		return d;
	}
	
	/**
	 * Limits entry in text field to characters that can be used in file names.
	 */
	public PlainDocument JValidCharFilter() {
		ArrayList<Character> badChars = new ArrayList<Character>(Arrays.asList('/','\\','?','%','*',':','|','"','<','>','.',',',';','=',' '));
		PlainDocument d = new PlainDocument() {
			@Override
	        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
	            Character c = str.charAt(0);
	            if (!badChars.contains(c))
	            	super.insertString(offs, str, a);
	        }
		};
		return d;
	}
	
	/**
	 * Sets the program's ClassifierParams.
	 */
	public void setParams(ClassifierParameters params) {
		this.params = params;
	}
	
	public ClassifierParameters getParams() {
		return params;
	}
	
	/**
	 * Displays an error dialog when a stack trace occurs in Python and saves the stack trace to a text file.
	 */
	public void pushStackTrace(String errorName, String stackTrace, boolean isPython) {
		File lastStackTrace = new File(EZUtils.getSourcePath()+"LastStackTrace.txt");
		try {
			FileWriter fw = new FileWriter(lastStackTrace, false);
			fw.write(stackTrace);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String message;
		if (isPython)
			message = "Python";
		else
			message = "Java";
		message += " error occurred:\n";
		message += errorName;
		Object[] options = {"View stack trace", "Return"};
		int result = JOptionPane.showOptionDialog(this,
			EZUtils.makeHTML(message, 250),
			ProgramInfo.getProgramName(),
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.ERROR_MESSAGE,
			null,
			options,
			options[0]);
		signalHaltOccurred();
		if (result != 0) return;
		if (!lastStackTrace.exists()) {
			EZUtils.SimpleErrorDialog(this, "LastStackTrace.txt no longer exists.", result);
			return;
		}
		try {
			Desktop.getDesktop().open(lastStackTrace);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
        MainFrame mainFrame = new MainFrame();
    }
}