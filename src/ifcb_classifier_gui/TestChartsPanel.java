package ifcb_classifier_gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JPanel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.jhdf.HdfFile;
import io.jhdf.api.Node;
import io.jhdf.object.datatype.VariableLength;
import io.jhdf.api.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 * The panel display results data in charts after a test run.
 */
public class TestChartsPanel extends JFXPanel {
	
	protected static final double[] THRESHOLDS = new double[] {0.6, 0.7, 0.8, 0.9};
	
	protected int fileCount;
	protected double meanScore;
	
	public TestChartsPanel(String outputFolderPath, int maxClassesVisible, Dimension size) {
		super();
		this.setPreferredSize(size);
		
		File outputDir = new File(outputFolderPath);
		LinkedList<File> folderQueue = new LinkedList<File>();
		LinkedList<File> outputList = new LinkedList<File>();
		folderQueue.add(outputDir);
		while (folderQueue.size() > 0) {
			File currDir = folderQueue.pop();
			File[] currFiles = currDir.listFiles();
			for (int i = 0; i < currFiles.length; i++) {
				if (currFiles[i].isDirectory())
					folderQueue.add(currFiles[i]);
				else if (currFiles[i].getAbsolutePath().endsWith(".h5") || currFiles[i].getAbsolutePath().endsWith(".json"))
					outputList.add(currFiles[i]);
			}
		}
		HashMap<String, LabelWithCounts> dataMap = new HashMap<String, LabelWithCounts>();
		double scoreSum = 0.0;
		this.fileCount = 0;
		while (outputList.size() > 0) {
			File currFile = outputList.pop();
			ArrayList<String> currLabels = new ArrayList<String>();
			ArrayList<float[]> currScores = new ArrayList<float[]>();
			if (currFile.getAbsolutePath().endsWith(".h5")) {
				try {
					HdfFile hdfFile = new HdfFile(currFile);
					Map<String, Node> childMap = hdfFile.getChildren();
					io.jhdf.api.Dataset ds = (io.jhdf.api.Dataset) childMap.get("class_labels");
					String[] h5ClassLabels = (String[]) ds.getData();
					for (int i = 0; i < h5ClassLabels.length; i++)
						currLabels.add(h5ClassLabels[i]);
					ds = (io.jhdf.api.Dataset) childMap.get("output_scores");
					float[][] outputScores = (float[][]) ds.getData();
					for (int i = 0; i < outputScores.length; i++)
						currScores.add(outputScores[i]);
					hdfFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else { // ends with .json
				try {
					JSONParser parser = new JSONParser();
					String jsonString = ((JSONObject) parser.parse(new FileReader(currFile))).toJSONString();
					jsonString = jsonString.replaceAll("\\uFEFF", "");
					JSONObject obj = (JSONObject) parser.parse(jsonString);
					JSONArray jsonClassLabels = (JSONArray) obj.get("class_labels");
					for (int i = 0; i < jsonClassLabels.size(); i++)
						currLabels.add((String) jsonClassLabels.get(i));
					JSONArray jsonOutputScores = (JSONArray) obj.get("output_scores");
					for (int i = 0; i < jsonOutputScores.size(); i++) {
						JSONArray row = (JSONArray) jsonOutputScores.get(i);
						float[] floatRow = new float[row.size()];
						for (int j = 0; j < row.size(); j++) {
							Double val = (double) row.get(j);
							floatRow[j] = val.floatValue();
						}
						currScores.add(floatRow);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			for (int i = 0; i < currLabels.size(); i++) {
				if (!dataMap.containsKey(currLabels.get(i)))
					dataMap.put(currLabels.get(i), new LabelWithCounts(currLabels.get(i)));
			}
			this.fileCount += currScores.size();
			for (int i = 0; i < currScores.size(); i++) {
				float[] row = currScores.get(i);
				float maxVal = row[0];
				int maxIndex = 0;
				for (int j = 1; j < row.length; j++) {
					float val = row[j];
					if (val > maxVal) {
						maxVal = val;
						maxIndex = j;
					}
				}
				scoreSum += maxVal;
				dataMap.get(currLabels.get(maxIndex)).addOne(maxVal);
			}
		}
		this.meanScore = scoreSum / this.fileCount;
		
		try {
			
			ArrayList<LabelWithCounts> sortedData = new ArrayList<LabelWithCounts>();
			Iterator<String> it = dataMap.keySet().iterator();
			while (it.hasNext())
				sortedData.add(dataMap.get(it.next()));
			sortedData.sort(Comparator.comparingInt(a -> ((LabelWithCounts) a).getSum()).reversed());
			
			FlowPane flowPane = new FlowPane();
			flowPane.setAlignment(Pos.CENTER);
			flowPane.setPadding(new Insets(5,5,5,5));
			Scene scene = new Scene(flowPane);
			
			// Kudos: https://docs.oracle.com/javafx/2/charts/pie-chart.htm
			int totalSum = 0;
			for (int i = 0; i < sortedData.size(); i++)
				totalSum += sortedData.get(i).getSum();
			double remainder = 100.0;
			ObservableList<PieChart.Data> chartData = FXCollections.observableArrayList();
			for (int i = 0; i < sortedData.size() && i < maxClassesVisible; i++) {
				if (sortedData.size() == maxClassesVisible || i < maxClassesVisible - 1) {
					LabelWithCounts lwc = sortedData.get(i);
					if (lwc.getSum() == 0) break;
					double pieSlice = 100*lwc.getSum()/totalSum;
					chartData.add(new PieChart.Data(abbr(lwc.label), pieSlice));
					remainder -= pieSlice;
				} else {
					chartData.add(new PieChart.Data("Others", remainder));
				}
			}
			PieChart pieChart = new PieChart(chartData);
			pieChart.setPrefSize(MainFrame.CARD_PANEL_SIZE.getHeight(), MainFrame.CARD_PANEL_SIZE.getHeight());
			pieChart.setLegendVisible(true);
			pieChart.setLabelsVisible(false);
			pieChart.setStartAngle(90);
			pieChart.setPadding(new Insets(0,0,0,0));
			flowPane.getChildren().add(pieChart);
			
			// Kudos: https://docs.oracle.com/javafx/2/charts/bar-chart.htm
			CategoryAxis xAxis = new CategoryAxis();
			xAxis.setLabel("prediction score");
		    NumberAxis yAxis = new NumberAxis();
		    yAxis.setLabel("n");
		    StackedBarChart<String, Number> barChart = new StackedBarChart<String, Number>(xAxis, yAxis);
		    barChart.setPrefSize(MainFrame.CARD_PANEL_SIZE.getHeight(), MainFrame.CARD_PANEL_SIZE.getHeight());
			String[] thresh_labels = new String[THRESHOLDS.length+1];
			for (int i = 0; i < THRESHOLDS.length; i++)
				thresh_labels[i] = "<"+String.valueOf(THRESHOLDS[i]);
			thresh_labels[THRESHOLDS.length] = "<=1.0";
			ArrayList<XYChart.Series<String, Number>> seriesList = new ArrayList<XYChart.Series<String, Number>>();
			for (int i = 0; i < sortedData.size() && i < maxClassesVisible; i++) {
				LabelWithCounts lwc = sortedData.get(i);
				if (lwc.getSum() == 0) break;
				XYChart.Series<String, Number> series = new XYChart.Series<String, Number>();
				if (sortedData.size() == maxClassesVisible || i < maxClassesVisible - 1) {
					series.setName(abbr(lwc.label));
					for (int j = 0; j < THRESHOLDS.length+1; j++)
						series.getData().add(new XYChart.Data<String, Number>(thresh_labels[j], lwc.counts[j]));
				} else {
					series.setName("Others");
					for (int j = 0; j < THRESHOLDS.length+1; j++) {
						int thresh_count = 0;
						for (int k = i; k < sortedData.size(); k++)
							thresh_count += sortedData.get(k).counts[j];
						series.getData().add(new XYChart.Data<String, Number>(thresh_labels[j], thresh_count));
					}
				}
				barChart.getData().add(series);
			}
			barChart.setLegendVisible(false);
			flowPane.getChildren().add(barChart);
			
			this.setScene(scene);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public class LabelWithCounts {
		
		public String label;
		public int[] counts;
		
		public LabelWithCounts(String label) {
			this.label = label;
			this.counts = new int[THRESHOLDS.length+1];
			for (int i = 0; i < this.counts.length; i++)
				this.counts[i] = 0;
		}
		
		public void addOne(double pValue) {
			for (int i = 0; i < THRESHOLDS.length; i++) {
				if (pValue < THRESHOLDS[i]) {
					this.counts[i]++;
					return;
				}
				this.counts[THRESHOLDS.length]++;
			}
		}
		
		public int getSum() {
			int sum = 0;
			for (int i = 0; i < this.counts.length; i++)
				sum += this.counts[i];
			return sum;
		}
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
	
	public int getFileCount() {
		return fileCount;
	}
	
	public double getMeanScore() {
		return meanScore;
	}
}