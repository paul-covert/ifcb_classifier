package ifcb_classifier_gui;

/**
 * This program is a Java GUI implementation of WHOI's IFCB Classifier: https://github.com/WHOIGit/ifcb_classifier
 * 
 * This program includes an updated version of the IFCB Classifier's Python scripts that can work on the latest versions of PyTorch,
 * as well as a graphical user interface for ease of use and visualization of data.
 * 
 * I am an employee of Fisheries and Oceans Canada and am not affiliated with the Woods Hole Oceanographic Institution,
 * nor the developers of the original IFCB Classifier code.
 * 
 * - Holly LeBlond
 */

public abstract class ProgramInfo {
	
	public static String getProgramName() {
		return "IFCB Classifier";
	}
	
	public static String getPackageName() {
		return "ifcb_classifier_gui";
	}
	
	public static String getDeveloperName() {
		return "Holly LeBlond";
	}
	
	public static String getContactEmail() {
		return "wtleblond@gmail.com";
	}
	
	public static String getVersion() {
		return "1.01a";
	}
	
}