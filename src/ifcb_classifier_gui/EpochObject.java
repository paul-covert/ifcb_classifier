package ifcb_classifier_gui;

/**
 * Object for storing results data from a training epoch.
 */
public class EpochObject {
	
	public int epochNumber;
	public boolean isBest;
	public double trainLoss;
	public double valLoss;
	public double valF1W;
	public double valF1M;
	
	public EpochObject(int epochNumber, boolean isBest, double trainLoss, double valLoss, double valF1W, double valF1M) {
		this.epochNumber = epochNumber;
		this.isBest = isBest;
		this.trainLoss = trainLoss;
		this.valLoss = valLoss;
		this.valF1W = valF1W;
		this.valF1M = valF1M;
	}
	
}