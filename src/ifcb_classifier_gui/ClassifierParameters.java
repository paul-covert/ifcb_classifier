package ifcb_classifier_gui;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Object that stores the parameters for classifier runs.
 * Default values are loaded out of the .config file, so if you want to change them, edit that file.
 */
public class ClassifierParameters {
	
	public static final String[] SIZING_OPTIONS = new String[] {"resize", "centre", "random", "padresize", "padshrink"};
	public static final String[] FLIP_OPTIONS = new String[] {"Off", "x", "y", "xy", "x+V", "y+V", "xy+V"};
	
	public boolean trainUntrain;
	public boolean trainImgNorm; // double-check if this is even optional
	public double trainImgNormMean;
	public double trainImgNormSTD;
	public int trainSplit;
	public int trainClassMin;
	public boolean trainClassMaxEnabled;
	public int trainClassMax;
	public int trainEpochsMin;
	public int trainEpochsMax;
	public boolean trainEpochsStopEnabled;
	public int trainEpochsStop;
	public String trainFlip;
	public String trainSizingMode;
	public boolean trainRedSquare;
	public int trainRedSquareSize;
	public int trainPaddingFill;
	//public String trainEpochsLog; // only bother with these if Paul thinks they're necessary
	//public String trainArgsLog;
	//public boolean trainONNX;
	
	public ClassifierParameters() {
		
		trainSplit = 80;
		trainClassMin = 2;
		trainClassMaxEnabled = false;
		trainClassMax = 1000; // Disabled by default in neuston_net
		trainEpochsMin = 10;
		trainEpochsMax = 60;
		trainEpochsStopEnabled = true;
		trainEpochsStop = 10;
		trainSizingMode = "resize"; // Options are "resize", "centre", "random", "padresize", "padshrink"
		trainPaddingFill = 200;
		trainRedSquare = false;
		trainRedSquareSize = 20;
		trainFlip = "Off"; // Options are 'Off', 'x', 'y', 'xy', 'x+V', 'y+V', 'xy+V'.
		trainImgNorm = false;
		trainImgNormMean = 0.5;
		trainImgNormSTD = 0.5;
		trainUntrain = false;
		
		File config = new File(EZUtils.getSourcePath()+".config");
		if (config.exists()) {
			Scanner sc;
			try {
				sc = new Scanner(config);
				while (sc.hasNextLine()) {
					try {
						String[] split = sc.nextLine().split("=");
						if (split.length < 2)
							continue;
						String w = split[0];
						String v = split[1];
						if (w.equals("trainSplit")) {
							int val = Integer.valueOf(v);
							//assert val >= 1 && val <= 99;
							if (val < 1 || val > 99)
								throw new Exception("trainSplit must be an integer between 1 and 99.");
							trainSplit = val;
						} else if (w.equals("trainClassMin")) {
							int val = Integer.valueOf(v);
							if (val < 2)
								throw new Exception("trainClassMin must be an integer greater than 1.");
							trainClassMin = val;
						} else if (w.equals("trainClassMaxEnabled")) {
							boolean val = Boolean.valueOf(v);
							trainClassMaxEnabled = val;
						} else if (w.equals("trainClassMax")) {
							int val = Integer.valueOf(v);
							if (val < 3)
								throw new Exception("trainClassMax must be an integer greater than 2.");
							trainClassMax = val;
						} else if (w.equals("trainEpochsMin")) {
							int val = Integer.valueOf(v);
							if (val < 1)
								throw new Exception("trainEpochsMin must be a positive integer.");
							trainEpochsMin = val;
						} else if (w.equals("trainEpochsMax")) {
							int val = Integer.valueOf(v);
							if (val < 1)
								throw new Exception("trainEpochsMax must be a positive integer.");
							trainEpochsMax = val;
						} else if (w.equals("trainEpochsStopEnabled")) {
							boolean val = Boolean.valueOf(v);
							trainEpochsStopEnabled = val;
						} else if (w.equals("trainEpochsStop")) {
							int val = Integer.valueOf(v);
							if (val < 1)
								throw new Exception("trainEpochsStop must be a positive integer.");
							trainEpochsStop = val;
						} else if (w.equals("trainSizingMode")) {
							boolean found = false;
							for (String so : SIZING_OPTIONS) {
								if (v.equals(so)) {
									trainSizingMode = v;
									found = true;
									break;
								}
							}
							if (!found)
								throw new Exception("trainSizingMode must be one of \"resize\", \"centre\", \"random\", \"padresize\", \"padshrink\".");
						} else if (w.equals("trainPaddingFill")) {
							int val = Integer.valueOf(v);
							if (val < 0 || val > 255)
								throw new Exception("trainPaddingFill must be an integer between 0 and 255.");
							trainPaddingFill = val;
						} else if (w.equals("trainRedSquare")) {
							boolean val = Boolean.valueOf(v);
							trainRedSquare = val;
						} else if (w.equals("trainRedSquareSize")) {
							int val = Integer.valueOf(v);
							if (val < 1)
								throw new Exception("trainRedSquareSize must be a positive integer.");
							trainRedSquareSize = val;
						} else if (w.equals("trainFlip")) {
							boolean found = false;
							for (String fo : FLIP_OPTIONS) {
								if (v.equals(fo)) {
									trainFlip = v;
									found = true;
									break;
								}
							}
							if (!found)
								throw new Exception("trainSizingMode must be one of \"Off\", \"x\", \"y\", \"xy\", \"x+V\", \"y+V\", \"xy+V\".");
						} else if (w.equals("trainImgNorm")) {
							boolean val = Boolean.valueOf(v);
							trainImgNorm = val;
						} else if (w.equals("trainImgNormMean")) {
							double val = Double.valueOf(v);
							if (val < 0.0)
								throw new Exception("trainImgNormMean cannot be less than zero.");
							trainImgNormMean = val;
						} else if (w.equals("trainImgNormSTD")) {
							double val = Double.valueOf(v);
							if (val < 0.0)
								throw new Exception("trainImgNormSTD cannot be less than zero.");
							trainImgNormSTD = val;
						} else if (w.equals("trainUntrain")) {
							boolean val = Boolean.valueOf(v);
							trainUntrain = val;
						}
					} catch (Exception e3) {
						e3.printStackTrace();
					}
				}
				sc.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		} else {
			try {
				PrintWriter pw = new PrintWriter(config);
				StringBuilder sb = new StringBuilder();
				pw.write("trainSplit="+String.valueOf(trainSplit)+"\n");
				pw.write("trainClassMin="+String.valueOf(trainClassMin)+"\n");
				pw.write("trainClassMaxEnabled="+String.valueOf(trainClassMaxEnabled)+"\n");
				pw.write("trainClassMax="+String.valueOf(trainClassMax)+"\n");
				pw.write("trainEpochsMin="+String.valueOf(trainEpochsMin)+"\n");
				pw.write("trainEpochsMax="+String.valueOf(trainEpochsMax)+"\n");
				pw.write("trainEpochsStopEnabled="+String.valueOf(trainEpochsStopEnabled)+"\n");
				pw.write("trainEpochsStop="+String.valueOf(trainEpochsStop)+"\n");
				pw.write("trainSizingMode="+trainSizingMode+"\n");
				pw.write("trainPaddingFill="+String.valueOf(trainPaddingFill)+"\n");
				pw.write("trainRedSquare="+String.valueOf(trainRedSquare)+"\n");
				pw.write("trainRedSquareSize="+String.valueOf(trainRedSquareSize)+"\n");
				pw.write("trainFlip="+trainFlip+"\n");
				pw.write("trainImgNorm="+String.valueOf(trainImgNorm)+"\n");
				pw.write("trainImgNormMean="+String.valueOf(trainImgNormMean)+"\n");
				pw.write("trainImgNormSTD="+String.valueOf(trainImgNormSTD)+"\n");
				pw.write("trainUntrain="+String.valueOf(trainUntrain)+"\n");
				pw.write(sb.toString());
				pw.flush();
				pw.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
	}
	
}