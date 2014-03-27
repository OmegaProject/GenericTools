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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class TrajectoriesAnalyzerAndFilter implements Runnable {
	public final static String fileName1 = "PT_Trajectories_";

	private final static String ident1 = "% Trajectory id: ";
	private final static String ident2 = "trajectories found";

	private final File workingDir;
	private File trajFile;

	private String fileNamePostfix;
	private String fileHeader;

	private int trajCounter;
	private int filteredTrajCounter;

	private final boolean onlyAnalysis, analyzeFiltered, analyzeMerged;
	private final boolean mergeTrajectories;
	private final double filterTrajLenght;

	private final Map<Integer, List<String>> trajectories;
	private final Map<Integer, List<String>> filterTrajectories;

	private final StringBuffer log;

	private final OmegaGenericToolGUI gui;

	private final List<String> filesWithoutTrajectoriesAboveFilter;
	private final Map<Integer, Integer> trajLenghtDistri;
	private int totalFileAnalyzed;
	private int numberOfTrajectoriesAboveFilter;
	private int numberOfFileWithOnlyOneTraj;
	private double meanTrajPerFile;
	private String maxTrajPerFileAbsPath;
	private int maxTrajPerFile;
	private String minTrajPerFileAbsPath;
	private int minTrajPerFile;
	private int totalTrajectoriesAnalyzed;
	private double meanTrajLenght;
	private String maxTrajLenghtAbsPath;
	private int maxTrajLenght;
	private String minTrajLenghtAbsPath;
	private int minTrajLenght;

	public TrajectoriesAnalyzerAndFilter(final File workingDir,
	        final double filter, final boolean onlyAnalysis,
	        final boolean mergeTrajectories, final boolean analyzeFiltered,
	        final boolean analyzeMerged, final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;
		this.trajFile = null;
		this.trajectories = new HashMap<Integer, List<String>>();
		this.filterTrajectories = new HashMap<Integer, List<String>>();
		this.fileHeader = null;
		this.trajCounter = 0;
		this.filteredTrajCounter = 0;
		this.onlyAnalysis = onlyAnalysis;
		this.analyzeFiltered = analyzeFiltered;
		this.analyzeMerged = analyzeMerged;
		this.mergeTrajectories = mergeTrajectories;
		this.filterTrajLenght = filter;
		this.log = new StringBuffer();
		this.gui = gui;

		this.filesWithoutTrajectoriesAboveFilter = new ArrayList<String>();
		this.trajLenghtDistri = new HashMap<Integer, Integer>();
		this.totalFileAnalyzed = 0;
		this.numberOfTrajectoriesAboveFilter = 0;
		this.numberOfFileWithOnlyOneTraj = 0;
		this.meanTrajPerFile = 0;
		this.maxTrajPerFileAbsPath = null;
		this.maxTrajPerFile = 0;
		this.minTrajPerFileAbsPath = null;
		this.minTrajPerFile = Integer.MAX_VALUE;
		this.totalTrajectoriesAnalyzed = 0;
		this.meanTrajLenght = 0;
		this.maxTrajLenghtAbsPath = null;
		this.maxTrajLenght = 0;
		this.minTrajLenghtAbsPath = null;
		this.minTrajLenght = Integer.MAX_VALUE;
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					TrajectoriesAnalyzerAndFilter.this.gui.appendOutput(update);

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
				boolean abortFolder = false;
				for (final File f : logsDir.listFiles()) {
					boolean check = f.getName().contains(
					        TrajectoriesAnalyzerAndFilter.fileName1);
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
							string.append("\n");
							this.log.append(string.toString());
							this.updateGUI(string.toString());
							abortFolder = true;
						} else {
							this.trajFile = f;
							this.fileNamePostfix = f
							        .getName()
							        .replace(
							                TrajectoriesAnalyzerAndFilter.fileName1,
							                "");
							this.fileNamePostfix = this.fileNamePostfix
							        .replace(".txt", "");
						}
					}
				}
				if (abortFolder) {
					continue;
				}
				try {
					this.getTrajectoriesFromFile();
				} catch (final IOException ex) {
					ex.printStackTrace();
					this.updateGUI("Error reading trajectories at image");
				}

				this.analyseTrajectories();
				if (!this.onlyAnalysis) {
					if (this.mergeTrajectories) {
						this.mergeTrajectories();
					} else {
						this.filterTrajectories();
					}
					try {
						this.writeFilteredTrajectoriesToFile(logsDir);
					} catch (final IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			try {
				this.meanTrajPerFile /= this.totalFileAnalyzed;
				this.meanTrajLenght /= this.totalTrajectoriesAnalyzed;
				this.writeTrajectoriesAnalysisToFile(dataset);
				this.writeLogFile(dataset);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public void perImageReset() {
		this.trajectories.clear();
		this.filterTrajectories.clear();
		this.fileHeader = null;
		this.trajFile = null;
		this.trajCounter = 0;
		this.filteredTrajCounter = 0;
	}

	public void perDatasetReset() {
		this.filesWithoutTrajectoriesAboveFilter.clear();
		this.trajLenghtDistri.clear();
		this.totalFileAnalyzed = 0;
		this.numberOfTrajectoriesAboveFilter = 0;
		this.numberOfFileWithOnlyOneTraj = 0;
		this.meanTrajPerFile = 0;
		this.maxTrajPerFileAbsPath = null;
		this.maxTrajPerFile = 0;
		this.minTrajPerFileAbsPath = null;
		this.minTrajPerFile = Integer.MAX_VALUE;
		this.totalTrajectoriesAnalyzed = 0;
		this.meanTrajLenght = 0;
		this.maxTrajLenghtAbsPath = null;
		this.maxTrajLenght = 0;
		this.minTrajLenghtAbsPath = null;
		this.minTrajLenght = Integer.MAX_VALUE;
	}

	private void getTrajectoriesFromFile() throws IOException {
		final FileReader fr = new FileReader(this.trajFile);
		final BufferedReader br = new BufferedReader(fr);
		final StringBuffer fileHeaderBuffer = new StringBuffer();
		int trajIndex = -1;
		List<String> traj = new ArrayList<String>();
		String line = br.readLine();
		boolean readParticles = false;
		while (line != null) {
			if (line.contains(TrajectoriesAnalyzerAndFilter.ident1)) {
				readParticles = true;
				trajIndex = Integer.valueOf(line.replace(
				        TrajectoriesAnalyzerAndFilter.ident1, ""));
				traj = new ArrayList<String>();
				this.trajCounter++;
			} else if (line.isEmpty() && readParticles) {
				readParticles = false;
				this.trajectories.put(trajIndex, traj);
				trajIndex = -1;
			} else if (!readParticles
			        && !line.contains(TrajectoriesAnalyzerAndFilter.ident2)) {
				fileHeaderBuffer.append(line + "\n");
			} else if (readParticles) {
				traj.add(line);
			}

			line = br.readLine();
		}
		br.close();
		fr.close();

		this.fileHeader = fileHeaderBuffer.toString();
	}

	private void analyseTrajectories() {
		boolean hasTrajAboveLimit = false;
		for (final Integer index : this.trajectories.keySet()) {
			final List<String> trajPoints = this.trajectories.get(index);
			final int trajLenght = trajPoints.size();

			int trajCount = 1;
			if (this.trajLenghtDistri.keySet().contains(trajLenght)) {
				trajCount = this.trajLenghtDistri.get(trajLenght);
				trajCount++;
			}
			this.trajLenghtDistri.put(trajLenght, trajCount);

			if (trajLenght > this.maxTrajLenght) {
				this.maxTrajLenght = trajLenght;
				this.maxTrajLenghtAbsPath = this.trajFile.getAbsolutePath();
			}
			if (trajLenght < this.minTrajLenght) {
				this.minTrajLenght = trajLenght;
				this.minTrajLenghtAbsPath = this.trajFile.getAbsolutePath();
			}
			if (trajLenght >= this.filterTrajLenght) {
				this.numberOfTrajectoriesAboveFilter++;
				hasTrajAboveLimit = true;
			}
			this.meanTrajLenght += trajLenght;
			this.totalTrajectoriesAnalyzed++;
		}
		if (!hasTrajAboveLimit) {
			this.filesWithoutTrajectoriesAboveFilter.add(this.trajFile
			        .getAbsolutePath());
		}
		final int numberOfTraj = this.trajectories.keySet().size();
		if (numberOfTraj == 1) {
			this.numberOfFileWithOnlyOneTraj++;
		}
		if (numberOfTraj > this.maxTrajPerFile) {
			this.maxTrajPerFile = numberOfTraj;
			this.maxTrajPerFileAbsPath = this.trajFile.getAbsolutePath();
		}
		if (numberOfTraj < this.minTrajPerFile) {
			this.minTrajPerFile = numberOfTraj;
			this.minTrajPerFileAbsPath = this.trajFile.getAbsolutePath();
		}
		this.meanTrajPerFile += numberOfTraj;
		this.totalFileAnalyzed++;
	}

	private void mergeTrajectories() {
		final Map<Integer, String> points = new HashMap<Integer, String>();
		for (final Integer index : this.trajectories.keySet()) {
			final List<String> trajPoints = this.trajectories.get(index);
			for (final String s : trajPoints) {
				final String s2 = s.substring(s.indexOf("\t") + 1);
				final String frame = s2.substring(0, s2.indexOf("\t"));
				final int frameIndex = Integer.valueOf(frame);
				if (!points.containsKey(frameIndex)) {
					points.put(frameIndex, s);
				} else {
					points.remove(frameIndex);
				}
			}
		}

		final List<String> trajPointsNew = new ArrayList<String>();
		final List<Integer> frameIndexes = new ArrayList<Integer>(
		        points.keySet());
		Collections.sort(frameIndexes);
		for (final Integer index : frameIndexes) {
			trajPointsNew.add(points.get(index));
		}
		this.filterTrajectories.put(0, trajPointsNew);
		this.filteredTrajCounter = 1;
	}

	private void filterTrajectories() {
		int trajIndex = 0;
		for (final Integer index : this.trajectories.keySet()) {
			final List<String> trajPoints = this.trajectories.get(index);
			if (trajPoints.size() >= this.filterTrajLenght) {
				this.filterTrajectories.put(trajIndex, trajPoints);
				this.filteredTrajCounter++;
			}
			trajIndex++;
		}
	}

	private void writeLogFile(final File dir) throws IOException {
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + "TAF_Log.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.log.toString());
		bw.close();
		fw.close();
	}

	private void writeFilteredTrajectoriesToFile(final File dir)
	        throws IOException {
		final File resultsFile;

		if (!this.mergeTrajectories) {
			resultsFile = new File(dir.getAbsolutePath() + File.separatorChar
			        + TrajectoriesAnalyzerAndFilter.fileName1
			        + this.fileNamePostfix + "_TAF_TrajFilter.txt");
		} else {
			resultsFile = new File(dir.getAbsolutePath() + File.separatorChar
			        + TrajectoriesAnalyzerAndFilter.fileName1
			        + this.fileNamePostfix + "_TAF_TrajMerge.txt");
		}
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);

		bw.write(this.fileHeader);
		for (int i = 0; i < this.filteredTrajCounter; i++) {
			final List<String> trajPoints = this.filterTrajectories.get(i);
			if (trajPoints == null) {
				continue;
			}
			bw.write(TrajectoriesAnalyzerAndFilter.ident1 + i + "\n");
			for (final String s : trajPoints) {
				bw.write(s + "\n");
			}
			bw.write("\n");
		}
		bw.write("% " + this.filteredTrajCounter + " "
		        + TrajectoriesAnalyzerAndFilter.ident2);

		bw.close();
		fw.close();
	}

	private void writeTrajectoriesAnalysisToFile(final File dir)
	        throws IOException {
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + "TAF_TrajAnalysis.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);

		bw.write("# of file analyzed:\t" + this.totalFileAnalyzed + "\n");
		bw.write("Mean traj per file:\t" + this.meanTrajPerFile + "\n");
		bw.write("Max traj per file:\t" + this.maxTrajPerFile + "\n");
		bw.write("Found in:\t" + this.maxTrajPerFileAbsPath + "\n");
		bw.write("Min traj per file:\t" + this.minTrajPerFile + "\n");
		bw.write("Found in:\t" + this.minTrajPerFileAbsPath + "\n");
		bw.write("\n");
		bw.write("# of trajectories analyzed:\t"
		        + this.totalTrajectoriesAnalyzed + "\n");
		bw.write("Mean traj lenght:\t" + this.meanTrajLenght + "\n");
		bw.write("Max traj lenght:\t" + this.maxTrajLenght + "\n");
		bw.write("Found in:\t" + this.maxTrajLenghtAbsPath + "\n");
		bw.write("Min traj lenght:\t" + this.minTrajLenght + "\n");
		bw.write("Found in:\t" + this.minTrajLenghtAbsPath + "\n");
		bw.write("# of file with only 1 traj:\t"
		        + this.numberOfFileWithOnlyOneTraj + "\n");
		bw.write("\n");
		bw.write("TrajDistri:\n");
		final List<Integer> lenghts = new ArrayList<Integer>(
		        this.trajLenghtDistri.keySet());
		Collections.sort(lenghts);
		for (final Integer i : lenghts) {
			final int count = this.trajLenghtDistri.get(i);
			bw.write("Lenght:\t" + i + "\tcount:\t" + count + "\n");
		}
		bw.write("\n");
		bw.write("Trajectory filter lenght:\t" + this.filterTrajLenght + "\n");
		bw.write("# of trajectories above filter:\t"
		        + this.numberOfTrajectoriesAboveFilter + "\n");
		bw.write("# of files without trajectories above filter:\t"
		        + this.filesWithoutTrajectoriesAboveFilter.size() + "\n");
		for (final String s : this.filesWithoutTrajectoriesAboveFilter) {
			bw.write(s + "\n");
		}

		bw.close();
		fw.close();
	}
}
