package core;

import gui.OmegaGenericToolGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class FilterSDReport implements Runnable {
	
	private final static String fileIdent = "SD_Output.txt";
	
	private final File inputDir, outputDir;
	private final List<File> logs;
	private final Map<Integer, List<String>> points;
	
	private final StringBuffer errorLog;
	
	private final OmegaGenericToolGUI gui;

	private Integer maxFrameIndex;

	private final boolean invertPoints = false;
	private final boolean addBias = false;
	private final boolean startFromOne = false;
	
	public FilterSDReport(final File inputDir, final File outputDir,
			final OmegaGenericToolGUI gui) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.logs = new ArrayList<File>();
		this.points = new LinkedHashMap<Integer, List<String>>();
		this.errorLog = new StringBuffer();
		this.gui = gui;
		this.maxFrameIndex = 0;
	}
	
	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				
				@Override
				public void run() {
					FilterSDReport.this.gui.appendOutput(update);
					
				}
			});
		} catch (final InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		for (final File datasetCase : this.inputDir.listFiles()) {
			if (!datasetCase.isDirectory()) {
				continue;
			}
			this.updateGUI("Dataset\t" + datasetCase.getName());
			this.errorLog.append("Dataset\t" + datasetCase.getName() + "\n");
			final File outputDir1 = new File(this.outputDir.getAbsolutePath()
					+ File.separator + datasetCase.getName());
			if (!outputDir1.exists()) {
				continue;
			}

			for (final File imageCase : datasetCase.listFiles()) {
				if (!imageCase.isDirectory()) {
					continue;
				}
				final File outputDir2 = new File(outputDir1.getAbsolutePath()
						+ File.separator + imageCase.getName());
				if (!outputDir2.exists()) {
					continue;
				}

				this.updateGUI("Image\t" + imageCase.getName());
				this.errorLog.append("Image\t" + imageCase.getName() + "\n");
				final File logsDir = new File(imageCase.getPath()
						+ File.separatorChar + "logs");
				if (!logsDir.exists()) {
					continue;
				}
				final File outputDir3 = new File(outputDir2.getAbsolutePath()
						+ File.separator + logsDir.getName());
				if (!outputDir3.exists()) {
					continue;
				}

				for (final File f : outputDir3.listFiles()) {
					if (!f.getName().startsWith(FilterSDReport.fileIdent)) {
						continue;
					}
					
					FileReader fr = null;
					try {
						fr = new FileReader(f);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					if (fr == null)
						return;
					final BufferedReader br = new BufferedReader(fr);
					
					String line = null;
					try {
						line = br.readLine();
					} catch (final IOException e) {
						e.printStackTrace();
					}
					boolean saveNextLine = true;
					List<String> localPoints;
					while (line != null) {
						if (line.isEmpty()) {
							saveNextLine = false;
						} else if (saveNextLine) {

							// line = line.replaceFirst("\t", "");
							
							final String[] tokens = line.split("\t");
							String toWrite = "";

							Integer frameIndex = Integer.valueOf(tokens[0]);
							if (this.startFromOne) {
								frameIndex -= 1;
							}
							
							if (frameIndex > this.maxFrameIndex) {
								this.maxFrameIndex = frameIndex;
							}
							if (this.points.containsKey(frameIndex)) {
								localPoints = this.points.get(frameIndex);
							} else {
								localPoints = new ArrayList<String>();
							}
							Double x, y;
							if (this.invertPoints) {
								x = Double.valueOf(tokens[2]);
								y = Double.valueOf(tokens[1]);
							} else {
								x = Double.valueOf(tokens[1]);
								y = Double.valueOf(tokens[2]);
							}
							if (this.addBias) {
								x += 0.5;
								y += 0.5;
							}
							toWrite += String.valueOf(frameIndex);
							toWrite += "\t";
							toWrite += String.valueOf(x);
							toWrite += "\t";
							toWrite += String.valueOf(y);
							localPoints.add(toWrite);
							this.points.put(frameIndex, localPoints);
						} else {
							saveNextLine = false;
						}
						try {
							line = br.readLine();
						} catch (final IOException e) {
							e.printStackTrace();
						}
					}
					try {
						this.writeLogFile(outputDir3);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					try {
						this.writeResultsFile(outputDir3);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		this.updateGUI("***FILTER SD REPORT COMPLETED***");
	}
	
	private void writeLogFile(final File dir) throws IOException {
		final File resultsFile = new File(dir.getAbsolutePath()
				+ File.separatorChar + "FilterSDReport_Log.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.errorLog.toString());
		bw.close();
		fw.close();
	}
	
	private void writeResultsFile(final File dir) throws IOException {
		final File resultsFile = new File(dir.getAbsolutePath()
				+ File.separatorChar + "FilterSDReport_Results.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		
		for (int i = 0; i <= this.maxFrameIndex; i++) {
			// for (final String frame : this.points.keySet()) {
			// bw.write(frame);
			if (this.points.get(i) != null) {
				for (final String point : this.points.get(i)) {
					bw.write(point);
					bw.write("\n");
				}
			}
		}
		
		bw.close();
		fw.close();
	}
}
