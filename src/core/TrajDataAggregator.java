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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class TrajDataAggregator implements Runnable {

	public final static String fileName1 = "track_";

	private final Map<Integer, List<Double>> xCoords;
	private final Map<Integer, List<Double>> xNoiseCoords;

	private final Map<Integer, List<Double>> yCoords;
	private final Map<Integer, List<Double>> yNoiseCoords;

	private final File workingDir;
	private final OmegaGenericToolGUI gui;

	public TrajDataAggregator(final File workingDir,
	        final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;
		this.xCoords = new HashMap<Integer, List<Double>>();
		this.xNoiseCoords = new HashMap<Integer, List<Double>>();
		this.yCoords = new HashMap<Integer, List<Double>>();
		this.yNoiseCoords = new HashMap<Integer, List<Double>>();
		this.gui = gui;
	}

	@Override
	public void run() {
		final List<File> trajFiles = new ArrayList<File>();
		final List<File> noisyTrajFiles = new ArrayList<File>();
		final File resultsFile = new File(this.workingDir.getPath()
		        + "\\aggregateData.txt");

		int totalFiles = 0;
		for (final File trajFile : this.workingDir.listFiles()) {
			final String trackFileName = trajFile.getName();
			if (!trackFileName.contains(".out")
			        || trackFileName.contains("_noise")) {
				continue;
			}
			totalFiles++;
		}

		int analyzedFiles = 0;
		for (final File trajFile : this.workingDir.listFiles()) {
			final String trackFileName = trajFile.getName();
			if (!trackFileName.contains(".out")
			        || trackFileName.contains("_noise")) {
				continue;
			}

			final int index = trackFileName.lastIndexOf(".");
			String noisyTrackFileName = trackFileName.substring(0, index);
			noisyTrackFileName += "_noise.out";
			final File noisyTrajFile = new File(
			        this.workingDir.getAbsolutePath() + File.separatorChar
			                + noisyTrackFileName);

			trajFiles.add(trajFile);
			noisyTrajFiles.add(noisyTrajFile);
			analyzedFiles++;
			this.updateGUI("File: " + analyzedFiles + "/" + totalFiles);
		}
		try {

			this.aggregateTrajData(trajFiles.toArray(),
			        noisyTrajFiles.toArray(), resultsFile);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.updateGUI("Trajectories aggregated");
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					TrajDataAggregator.this.gui.appendOutput(update);

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

	public void aggregateTrajData(final Object[] objects,
	        final Object[] objects2, final File resultsFile) throws IOException {
		for (int i = 0; i < objects.length; i++) {
			this.aggregateTrajData(i, objects[i], objects2[i]);
		}
		this.appendResultsToFile(resultsFile);
	}

	private void aggregateTrajData(final int index, final Object object1,
	        final Object object2) throws IOException {
		final File trajFile = (File) object1;
		final File noisyTrajFile = (File) object2;
		String line;

		List<Double> xCoord = this.xCoords.get(index);
		List<Double> yCoord = this.yCoords.get(index);

		if (xCoord == null) {
			xCoord = new ArrayList<Double>();
			this.xCoords.put(index, xCoord);
		}
		if (yCoord == null) {
			yCoord = new ArrayList<Double>();
			this.yCoords.put(index, yCoord);
		}

		final FileReader fr1 = new FileReader(trajFile);
		final BufferedReader br1 = new BufferedReader(fr1);
		line = br1.readLine();
		while (line != null) {
			final String s1 = line.substring(line.indexOf("\t") + 1,
			        line.lastIndexOf("\t"));
			final Double x = Double.valueOf(s1);
			xCoord.add(x);
			final String s2 = line.substring(line.lastIndexOf("\t") + 1);
			final Double y = Double.valueOf(s2);
			yCoord.add(y);
			line = br1.readLine();
		}
		br1.close();
		fr1.close();

		List<Double> xNoiseCoord = this.xNoiseCoords.get(index);
		List<Double> yNoiseCoord = this.yNoiseCoords.get(index);

		if (xNoiseCoord == null) {
			xNoiseCoord = new ArrayList<Double>();
			this.xNoiseCoords.put(index, xNoiseCoord);
		}
		if (yNoiseCoord == null) {
			yNoiseCoord = new ArrayList<Double>();
			this.yNoiseCoords.put(index, yNoiseCoord);
		}

		final FileReader fr2 = new FileReader(noisyTrajFile);
		final BufferedReader br2 = new BufferedReader(fr2);
		line = br2.readLine();
		while (line != null) {
			final String s1 = line.substring(line.indexOf("\t") + 1,
			        line.lastIndexOf("\t"));
			final Double x = Double.valueOf(s1);
			xNoiseCoord.add(x);
			final String s2 = line.substring(line.lastIndexOf("\t") + 1);
			final Double y = Double.valueOf(s2);
			yNoiseCoord.add(y);
			line = br2.readLine();
		}
		br2.close();
		fr2.close();
	}

	public void appendResultsToFile(final File file) throws IOException {
		final FileWriter fw = new FileWriter(file, true);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write("X\tY\tX N\tY N\n");
		for (int i = 0; i < this.xCoords.size(); i++) {
			final List<Double> xCoord = this.xCoords.get(i);
			final List<Double> yCoord = this.yCoords.get(i);
			final List<Double> xNoiseCoord = this.xNoiseCoords.get(i);
			final List<Double> yNoiseCoord = this.yNoiseCoords.get(i);

			for (int k = 0; k < xCoord.size(); k++) {

				bw.write(xCoord.get(k) + "\t" + yCoord.get(k) + "\t"
				        + xNoiseCoord.get(k) + "\t" + yNoiseCoord.get(k) + "\n");
			}
		}
		bw.close();
		fw.close();
	}
}
