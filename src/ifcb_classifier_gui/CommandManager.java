package ifcb_classifier_gui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CommandManager {
	
	protected MainFrame mainFrame;
	
	private Process pr;
	private BufferedWriter bw = null;
	private BufferedReader br = null;
	private BufferedReader ebr = null;
	private InputPrintThread ipt = null;
	private ErrorPrintThread ept = null;
	private volatile boolean printThreadsActive;
	private final Object bwLock = new Object();
	private final Object brLock = new Object();
	private final Object ebrLock = new Object();
	private volatile ArrayList<String> commandList;
	public CommandPromptInterpreterThread cpit = null;
	
	protected int currentEpoch = 0;
	
	public CommandManager(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		this.commandList = new ArrayList<String>();
		this.cpit = new CommandPromptInterpreterThread();
		cpit.start();
	}
	
	/**
	 * Thread that initializes and sends commands to Command Prompt.
	 */
	protected class CommandPromptInterpreterThread extends Thread {
		
		protected CommandPromptInterpreterThread() {}
		
		@Override
		public void run() {
			try {
				ProcessBuilder pb = new ProcessBuilder("cmd");
				pr = pb.start();
				br = new BufferedReader(new InputStreamReader(pr.getInputStream()));
		        ebr = new BufferedReader(new InputStreamReader(pr.getErrorStream()));
	            bw = new BufferedWriter(new OutputStreamWriter(pr.getOutputStream()));
	            inputCommand("cd \""+EZUtils.getSourcePath()+"python/\"");
	            System.out.println("Command Prompt Interpreter Thread initialization successful.");
			} catch (IOException e) {
				System.out.println("Command Prompt Interpreter Thread initialization failed.");
				e.printStackTrace(System.out);
			}
			startPrintThreads();
			while (true) {
				if (commandList.size() > 0) {
					String command;
					synchronized(commandList) {
						command = commandList.remove(0);
					}
					inputCommand(command);
				}
				if (commandList.size() == 0) {
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Adds a Python command to the queue.
	 */
	public void addCommand(String inp) {
		synchronized(commandList) {
			commandList.add(inp);
		}
	}
	
	/**
	 * Directly sends a command to the interpreter.
	 */
	private void inputCommand(String command) {
		synchronized(bwLock) {
			if (bw != null) {
				try {
					System.out.println("COMMAND: "+command);
					if (command != null) {
						bw.write(command);
						bw.newLine();
						bw.flush();
					}
				} catch (IOException e) {
					System.out.println("IOException in inputCommand().");
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Forces the print threads to (re)start.
	 */
	public void startPrintThreads() {
		printThreadsActive = true;
		ipt = new InputPrintThread();
		ept = new ErrorPrintThread();
		ipt.start();
		ept.start();
	}
	
	/**
	 * Signals the print threads to not keep running in their loops.
	 */
	public void haltPrintThreads() {
		printThreadsActive = false;
	}
	
	/**
	 * Kills and restarts the Command Manager.
	 */
	public void haltAndRestart() {
		synchronized(commandList) {
			commandList.clear();
		}
		haltPrintThreads();
		synchronized(bwLock) {
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		synchronized(brLock) {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		synchronized(ebrLock) {
			try {
				ebr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		pr.destroy();
		cpit.interrupt();
		cpit = new CommandPromptInterpreterThread();
		cpit.start();
	}
	
	/**
	 * Passes non-error Python output back to Java.
	 */
	protected class InputPrintThread extends Thread {
		
		protected InputPrintThread() {}
		
		@Override
		public void run() {
			while (printThreadsActive) {
				try {
					String outpstr = "";
					//System.out.println("Thread started.");
					//System.out.println("br.ready(): "+String.valueOf(br.ready()));
					do { // (Making sure it sleeps at least once.)
						try {
							TimeUnit.MILLISECONDS.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} while (br != null && !br.ready() && printThreadsActive); // (Making sure it wakes up first.)
					while (br != null && br.ready() && (outpstr = br.readLine()) != null && printThreadsActive) {
						synchronized(brLock) {
							System.out.println("IBR: "+outpstr);
							if (outpstr.startsWith("Epoch")) {
								mainFrame.setTrainingStatusToRunning();
								String[] lineSplit = outpstr.split("\\|");
								String[] spaceSplit = lineSplit[0].split(" ");
								int lineEpoch = Integer.valueOf(spaceSplit[1].replaceAll(":", ""))+1;
								if (currentEpoch != lineEpoch) {
									currentEpoch = lineEpoch;
									mainFrame.setCurrentEpoch(currentEpoch);
									mainFrame.resetEpochTrainingProgressBar();
									mainFrame.resetEpochValidationProgressBar();
								}
								spaceSplit = lineSplit[2].split(" ");
								String[] slashSplit = spaceSplit[1].split("/");
								mainFrame.setEpochTrainingProgress(Integer.valueOf(slashSplit[0]), Integer.valueOf(slashSplit[1]));
							} else if (outpstr.startsWith("Validation DataLoader")) {
								String[] lineSplit = outpstr.split("\\|");
								String[] spaceSplit = lineSplit[2].split(" ");
								String[] slashSplit = spaceSplit[1].split("/");
								mainFrame.setEpochValidationProgress(Integer.valueOf(slashSplit[0]), Integer.valueOf(slashSplit[1]));
							} else if (outpstr.startsWith("Best Epoch")) {
								String[] commaSplit = outpstr.split(", ");
								String[] spaceSplit = commaSplit[0].split(" ");
								boolean isBest = spaceSplit[2].equals("True");
								double trainLoss = Double.valueOf(commaSplit[1].split(" ")[1]);
								double valLoss = Double.valueOf(commaSplit[2].split(" ")[1]);
								double valF1W = Double.valueOf(commaSplit[3].split("=")[1].replaceAll("%", ""));
								double valF1M = Double.valueOf(commaSplit[4].split("=")[1].replaceAll("%", ""));
								mainFrame.finishEpoch(new EpochObject(currentEpoch, isBest, trainLoss, valLoss, valF1W, valF1M));
							} else if (outpstr.startsWith("Testing DataLoader") && mainFrame.getCurrentMode() == MainFrame.VALIDATING) {
								mainFrame.setValidationStatusToRunning();
								String[] lineSplit = outpstr.split("\\|");
								String[] spaceSplit = lineSplit[2].split(" ");
								String[] slashSplit = spaceSplit[1].split("/");
								mainFrame.setValidationProgress(Integer.valueOf(slashSplit[0]), Integer.valueOf(slashSplit[1]));
							} else if (outpstr.startsWith("Testing DataLoader") && mainFrame.getCurrentMode() == MainFrame.TESTING) {
								mainFrame.setTestingStatusToRunning();
								String[] lineSplit = outpstr.split("\\|");
								String[] spaceSplit = lineSplit[0].split(" ");
								int dataloaderNum = Integer.valueOf(spaceSplit[2].replaceAll(":", ""));
								spaceSplit = lineSplit[2].split(" ");
								String[] slashSplit = spaceSplit[1].split("/");
								mainFrame.setTestingProgress(dataloaderNum, Integer.valueOf(slashSplit[0]), Integer.valueOf(slashSplit[1]));
							} else if (outpstr.startsWith("DONE!") && mainFrame.getCurrentMode() == MainFrame.TRAINING) {
								mainFrame.setTrainingStatusToDone();
							} else if (outpstr.startsWith("DONE!") && mainFrame.getCurrentMode() == MainFrame.VALIDATING) {
								mainFrame.setValidationStatusToDone();
							} else if (outpstr.startsWith("DONE!") && mainFrame.getCurrentMode() == MainFrame.TESTING) {
								mainFrame.setTestingStatusToDone();
							} else if (outpstr.startsWith("DONE:") && mainFrame.getCurrentMode() == MainFrame.CREATING_DATASET_CONFIG) {
								String[] colonSpaceSplit = outpstr.split(": ");
								mainFrame.finishCreatingDatasetConfig(colonSpaceSplit[1]);
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Sets the current training epoch to 0 and notifies the MainFrame.
	 */
	public void resetCurrentEpoch() {
		currentEpoch = 0;
		mainFrame.setCurrentEpoch(currentEpoch);
	}
	
	/**
	 * Passes Python error output back to Java.
	 */
	protected class ErrorPrintThread extends Thread {
		
		private boolean inTraceback = false;
		private StringBuilder sb = new StringBuilder();
		private String errorName = null;
		
		protected ErrorPrintThread() {}
		
		@Override
		public void run() {
			while (printThreadsActive) {
				try {
					String outpstr = "";
					do {
						try {
							TimeUnit.MILLISECONDS.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} while (ebr != null && !ebr.ready() && printThreadsActive);
					while (ebr != null && ebr.ready() && (outpstr = ebr.readLine()) != null && printThreadsActive) {
						synchronized(ebrLock) {
							if (outpstr.startsWith("Traceback"))
								inTraceback = true;
							if (inTraceback) {
								sb.append(outpstr+"\n");
								if (outpstr.contains(":"))
									errorName = outpstr.split(":")[0];
							}
							System.out.println("EBR: "+outpstr);
						}
					}
					if (inTraceback) {
						mainFrame.pushStackTrace(errorName, sb.toString(), true);
						errorName = null;
						sb = new StringBuilder();
					}
					inTraceback = false;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}