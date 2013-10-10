package core;

import gui.OmegaGenericToolGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.text.Format;

import javax.swing.SwingUtilities;

public class SingleTrajectoryGenerator implements Runnable {

	public final static String fileName1 = "PT_Trajectories_";

	private final File workingDir;
	private File trajFile;
	public StringBuffer log;

	private final boolean analyzeFiltered, analyzeMerged;

	private final OmegaGenericToolGUI gui;

	public SingleTrajectoryGenerator(final File workingDir,
	        final boolean analyzeFiltered, final boolean analyzeMerged,
	        final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;
		this.trajFile = null;
		this.log = new StringBuffer();
		this.analyzeFiltered = analyzeFiltered;
		this.analyzeMerged = analyzeMerged;
		this.gui = gui;
	}

	private void generateSingleTrajectories(final File logsDir)
	        throws IOException {
		final FileReader fr = new FileReader(this.trajFile);
		final BufferedReader br = new BufferedReader(fr);

		File f = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		int trajNumber = -1;
		String line = br.readLine();
		int index = 0;
		while (line != null) {
			if (line.startsWith("% Trajectory id: ")) {
				if (bw != null) {
					bw.close();
					fw.close();
				}
				trajNumber = Integer.valueOf(line.replace("% Trajectory id: ",
				        "")) + 1;

				if (index < 10) {
					f = new File(logsDir.getAbsolutePath() + File.separatorChar
					        + "SPT_onlyone_00" + index + ".xy");
				} else if (index < 100) {
					f = new File(logsDir.getAbsolutePath() + File.separatorChar
					        + "SPT_onlyone_0" + index + ".xy");
				} else {
					f = new File(logsDir.getAbsolutePath() + File.separatorChar
					        + "SPT_onlyone_" + index + ".xy");
				}

				if (trajNumber > 10) {
					this.log.append(this.workingDir
					        + " has a trajectory with index > 10");
				}

				index++;

				fw = new FileWriter(f);
				bw = new BufferedWriter(fw);
			} else if (line.startsWith("\t")) {
				final int stringIndex1 = line.indexOf("\t");
				final int stringIndex2 = line.indexOf("\t", stringIndex1 + 1);
				String subString = line.substring(stringIndex1, stringIndex2);
				subString = subString.replace("\t", "");
				subString = subString.replace("\t", "");
				final double particleIndex = Double.valueOf(subString);

				final int stringIndex3 = line.indexOf("\t", stringIndex2 + 1);
				subString = line.substring(stringIndex2, stringIndex3);
				subString = subString.replace("\t", "");
				subString = subString.replace("\t", "");
				final double particleX = Double.valueOf(subString);

				final int stringIndex4 = line.indexOf("\t", stringIndex3 + 1);
				subString = line.substring(stringIndex3, stringIndex4);
				subString = subString.replace("\t", "");
				subString = subString.replace("\t", "");
				final double particleY = Double.valueOf(subString);

				final Format formatter = new DecimalFormat("0.0000000E00");
				final StringBuffer buf = new StringBuffer();
				buf.append("   ");
				buf.append(formatter.format(particleIndex));
				buf.append("   ");
				buf.append(formatter.format(particleX));
				buf.append("   ");
				buf.append(formatter.format(particleY));
				buf.append("\n");

				bw.write(buf.toString());
			}
			line = br.readLine();
		}

		if (bw != null) {
			bw.close();
			fw.close();
		}

		br.close();
		fr.close();
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					SingleTrajectoryGenerator.this.gui.appendOutput(update);

				}
			});
		} catch (final InvocationTargetException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	@Override
	public void run() {
		for (final File dataset : this.workingDir.listFiles()) {
			if (dataset.isFile()) {
				continue;
			}
			this.updateGUI("Dataset\t" + dataset.getName());
			this.log.append("Dataset\t" + dataset.getName() + "\n");

			this.perDatasetReset();
			for (final File image : dataset.listFiles()) {
				if (image.isFile()) {
					continue;
				}
				this.updateGUI("Image\t" + image.getName());
				this.log.append("Image\t" + image.getName() + "\n");
				final File logsDir = new File(image.getPath()
				        + File.separatorChar + "logs");
				if (!logsDir.exists()) {
					continue;
				}

				this.perImageReset();
				for (final File f : logsDir.listFiles()) {
					boolean check = f.getName().contains(
					        SingleTrajectoryGenerator.fileName1);
					if (this.analyzeFiltered) {
						check = check && f.getName().contains("TAF_TrajFilter");
					} else if (this.analyzeMerged) {
						check = check && f.getName().contains("TAF_TrajMerge");
					} else {
						check = check && !f.getName().contains("TAF");
					}
					if (check) {
						if (this.trajFile != null) {
							final StringBuffer string = new StringBuffer();
							string.append("Error found in: \n");
							string.append(image.getAbsolutePath());
							string.append("\n");
							string.append("multiple trajectories file\n");
							this.log.append(string.toString());
							this.updateGUI(string.toString());
						} else {
							this.trajFile = f;
						}
					}
				}
				try {
					this.generateSingleTrajectories(logsDir);
					this.writeLogFile(dataset);
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void writeLogFile(final File dir) throws IOException {
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + "TAF_Log.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.log.toString() + "\n");
		bw.close();
		fw.close();
	}

	public void perImageReset() {
		this.trajFile = null;
	}

	public void perDatasetReset() {

	}
}
