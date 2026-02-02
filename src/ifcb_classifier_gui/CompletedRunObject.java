package ifcb_classifier_gui;

import java.io.FileReader;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5File;
import us.hebi.matlab.mat.types.Cell;
import us.hebi.matlab.mat.types.Matrix;

/**
 * Object that parses and stores results info after a training or validation run.
 */
public class CompletedRunObject {
	
	protected String filePath;
	protected ArrayList<String> classLabels;
	protected int[][] confMatrix;
	protected double accuracy;
	protected double[] precisions;
	protected double[] recalls;
	
	public CompletedRunObject(String filePath) {
		assert (filePath.endsWith(".mat") || filePath.endsWith(".json"));
		this.filePath = filePath;
		try {
			if (filePath.endsWith(".mat")) {
				Mat5File mFile = Mat5.readFromFile(filePath);
				classLabels = new ArrayList<String>();
				Cell cell = mFile.getCell("class_labels");
				for (int i = 0; i < cell.getNumElements(); i++)
					classLabels.add(cell.get(i).toString().replaceAll("'", ""));
				Matrix matrix = mFile.getMatrix("confusion_matrix");
				confMatrix = new int[classLabels.size()][classLabels.size()];
				for (int i = 0; i < matrix.getNumRows(); i++)
					for (int j = 0; j < matrix.getNumCols(); j++)
						confMatrix[i][j] = matrix.getInt(i, j);
			} else { // .json
				// Kudos: https://stackoverflow.com/questions/36362619/parsing-a-json-file-in-java-using-json-simple
				JSONParser parser = new JSONParser();
				String jsonString = ((JSONObject) parser.parse(new FileReader(filePath))).toJSONString();
				jsonString = jsonString.replaceAll("\\uFEFF", "");
				JSONObject obj = (JSONObject) parser.parse(jsonString);
				JSONArray jsonClassLabels = (JSONArray) obj.get("class_labels");
				classLabels = new ArrayList<String>();
				for (int i = 0; i < jsonClassLabels.size(); i++)
					classLabels.add((String) jsonClassLabels.get(i));
				confMatrix = new int[classLabels.size()][classLabels.size()];
				for (int i = 0; i < confMatrix.length; i++)
					for (int j = 0; j < confMatrix[i].length; j++)
						confMatrix[i][j] = 0;
				JSONArray output_classes = (JSONArray) obj.get("output_classes");
				JSONArray input_images = (JSONArray) obj.get("input_images");
				for (int i = 0; i < input_images.size(); i++) {
					String[] slashSplit = ((String) input_images.get(i)).split("/");
					slashSplit = slashSplit[slashSplit.length-1].substring(1).split("\\\\");
					int ac_species = classLabels.indexOf(slashSplit[0]);
					int pr_species = ((Long) output_classes.get(i)).intValue();
					confMatrix[ac_species][pr_species]++;
				}
			}
			int sum = 0;
			int totalCorrect = 0;
			int[] rowSums = new int[classLabels.size()];
			int[] columnSums = new int[classLabels.size()];
			int[] correctCount = new int[classLabels.size()];
			for (int i = 0; i < confMatrix.length; i++) {
				rowSums[i] = 0;
				for (int j = 0; j < confMatrix[i].length; j++) {
					if (i == 0) {
						columnSums[j] = 0;
					}
					sum += confMatrix[i][j];
					rowSums[i] += confMatrix[i][j];
					columnSums[j] += confMatrix[i][j];
					if (i == j) {
						totalCorrect += confMatrix[i][j];
						correctCount[i] = confMatrix[i][j];
					}
				}
			}
			accuracy = 100*Double.valueOf(totalCorrect)/sum;
			precisions = new double[classLabels.size()];
			recalls = new double[classLabels.size()];
			for (int i = 0; i < classLabels.size(); i++) {
				if (columnSums[i] > 0)
					precisions[i] = 100*Double.valueOf(correctCount[i])/columnSums[i];
				else
					precisions[i] = Double.NaN;
				if (rowSums[i] > 0)
					recalls[i] = 100*Double.valueOf(correctCount[i])/rowSums[i];
				else
					recalls[i] = Double.NaN;
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public ArrayList<String> getClassLabels() {
		return classLabels;
	}
	
	public int[][] getConfusionMatrix() {
		return confMatrix;
	}
	
	public double getAccuracy() {
		return accuracy;
	}
	
	public double getPrecisionOf(int classIndex) {
		return precisions[classIndex];
	}
	
	public double getRecallOf(int classIndex) {
		return recalls[classIndex];
	}
	
	public String getBestPrecisionString() {
		int bestIndex = 0;
		for (int i = 1; i < classLabels.size(); i++)
			if (precisions[bestIndex] == Double.NaN || (precisions[i] != Double.NaN && precisions[i] > precisions[bestIndex]))
				bestIndex = i;
		return EZUtils.oneDigit(precisions[bestIndex])+"% ("+abbr(classLabels.get(bestIndex))+")";
	}
	
	public String getWorstPrecisionString() {
		int worstIndex = classLabels.size()-1;
		for (int i = worstIndex-1; i >= 0; i--)
			if (precisions[worstIndex] == Double.NaN || (precisions[i] != Double.NaN && precisions[i] < precisions[worstIndex]))
				worstIndex = i;
		return EZUtils.oneDigit(precisions[worstIndex])+"% ("+abbr(classLabels.get(worstIndex))+")";
	}
	
	public String getBestRecallString() {
		int bestIndex = 0;
		for (int i = 1; i < classLabels.size(); i++)
			if (recalls[bestIndex] == Double.NaN || (recalls[i] != Double.NaN && recalls[i] > recalls[bestIndex]))
				bestIndex = i;
		return EZUtils.oneDigit(recalls[bestIndex])+"% ("+abbr(classLabels.get(bestIndex))+")";
	}
	
	public String getWorstRecallString() {
		int worstIndex = classLabels.size()-1;
		for (int i = worstIndex-1; i >= 0; i--)
			if (recalls[worstIndex] == Double.NaN || (recalls[i] != Double.NaN && recalls[i] < recalls[worstIndex]))
				worstIndex = i;
		return EZUtils.oneDigit(recalls[worstIndex])+"% ("+abbr(classLabels.get(worstIndex))+")";
	}
	
	public String abbr(String inp) {
		if (inp.length() > 15)
			inp = inp.substring(0, 15)+"...";
		return inp;
	}
	
}