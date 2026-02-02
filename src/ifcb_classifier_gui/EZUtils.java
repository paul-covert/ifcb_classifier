package ifcb_classifier_gui;

import java.awt.Color;
import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * Abstract class for commonly-used functions throughout the program.
 */
public abstract class EZUtils {
	
	public static final Color GREEN = new Color(0, 215, 0);
	public static final Color RED = Color.RED;
	
	public static final ImageIcon ICON = new ImageIcon(getSourcePath()+"sheldon.png");
	
	/**
	 * Retrieves the path to the source folder of this program.
	 */
	public static String getSourcePath() {
		// Kudos: https://stackoverflow.com/questions/11747833/getting-filesystem-path-of-class-being-executed
		String srcPath = EZUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1);
		String[] srcSplit = srcPath.split("/");
		srcPath = srcPath.substring(0, srcPath.length()-srcSplit[srcSplit.length-1].length());
		return srcPath;
	}
	
	/**
	 * Formats a string in HTML.
	 */
	public static String makeHTML(String inp) {
		return String.format("<html>%1s", inp);
	}
	
	/**
	 * Formats a string in HTML to a text box of the specified width.
	 */
	public static String makeHTML(String inp, int width) {
		return String.format("<html><body style='width: %1spx'>%1s", width, inp);
	}
	
	/**
	 * Formats a double to a string with one decimal digit.
	 */
	public static String oneDigit(double inp) {
		DecimalFormat dFormat = new DecimalFormat("0.0");
		return dFormat.format(inp);
	}
	
	/**
	 * Streamlined error dialog with an editable message and length.
	 */
	public static void SimpleErrorDialog(Component parentFrame, String message, int width) {
		JOptionPane.showMessageDialog(parentFrame, makeHTML(message, width), ProgramInfo.getProgramName(), JOptionPane.ERROR_MESSAGE);
	}
	
}