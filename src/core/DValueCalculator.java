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

public class DValueCalculator implements Runnable {

	public final static Double MICRON_PER_PIXEL = 1.0;
	public final static Double SECOND_PER_FRAME = 1.0;

	public final static String fileName1 = "L_";

	public final static String resultsFileName = "DValueCalculator_Results";
	public final static String deltaTandMuFileName = "DValueCalculator_DeltaTAndMu";
	public final static String logFileName = "DValueCalculator_Log";

	private final File workingDir;
	private final Map<Integer, List<Double>> xMap;
	private final Map<Integer, List<Double>> yMap;

	private final Map<Integer, Double> trajDMap;
	private final Map<Integer, List<Double>> deltaTMap;
	private final Map<Integer, List<Double>> muMap;

	private final Map<String, Double> dMap;

	private final Integer filter;

	private final StringBuffer errorLog;

	private final OmegaGenericToolGUI gui;

	public DValueCalculator(final File workingDir, final double filter,
	        final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;
		this.xMap = new LinkedHashMap<Integer, List<Double>>();
		this.yMap = new LinkedHashMap<Integer, List<Double>>();

		this.trajDMap = new LinkedHashMap<Integer, Double>();

		this.deltaTMap = new LinkedHashMap<Integer, List<Double>>();
		this.muMap = new LinkedHashMap<Integer, List<Double>>();

		this.dMap = new LinkedHashMap<String, Double>();

		this.filter = (int) filter;

		this.errorLog = new StringBuffer();
		this.gui = gui;
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					DValueCalculator.this.gui.appendOutput(update);

				}
			});
		} catch (final InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	private int getPointsFromTrajectories(final File f) throws IOException {
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);

		final String fileName = f.getName();
		final int posIndexStart = fileName.indexOf("_");
		final int posIndexEnd = fileName.indexOf(".");
		final String trajIndex = fileName.substring(posIndexStart + 1,
		        posIndexEnd);
		final Integer trajIndexVal = Integer.valueOf(trajIndex);

		String line = br.readLine();
		while (line != null) {
			final String[] tokens = line.split("\t");
			final String index = tokens[0];
			final String x = tokens[1];
			final String y = tokens[2];

			Integer.valueOf(index);
			final double xVal = Double.valueOf(x);
			final double yVal = Double.valueOf(y);

			List<Double> xVals;
			if (this.xMap.containsKey(trajIndexVal)) {
				xVals = this.xMap.get(trajIndexVal);
			} else {
				xVals = new ArrayList<Double>();
			}
			xVals.add(xVal * DValueCalculator.MICRON_PER_PIXEL);
			this.xMap.put(trajIndexVal, xVals);

			List<Double> yVals;
			if (this.yMap.containsKey(trajIndexVal)) {
				yVals = this.yMap.get(trajIndexVal);
			} else {
				yVals = new ArrayList<Double>();
			}
			yVals.add(yVal * DValueCalculator.MICRON_PER_PIXEL);
			this.yMap.put(trajIndexVal, yVals);

			line = br.readLine();
		}
		br.close();
		fr.close();

		return trajIndexVal;
	}

	@Override
	public void run() {
		for (final File dataset : this.workingDir.listFiles()) {
			if (dataset.isFile()
			        || !dataset.getName().contains(DValueCalculator.fileName1)) {
				continue;
			}
			this.updateGUI("Dataset\t" + dataset.getName());
			this.errorLog.append("Dataset\t" + dataset.getName() + "\n");

			this.perDatasetReset();
			for (final File image : dataset.listFiles()) {
				if (image.isFile()) {
					continue;
				}
				this.updateGUI("Image\t" + image.getName());
				this.errorLog.append("Image\t" + image.getName() + "\n");
				this.perImageReset();
				for (final File f : image.listFiles()) {
					if (f.getName().contains(DValueCalculator.resultsFileName)
					        || f.getName().contains(
					                DValueCalculator.logFileName)
					        || f.getName().contains(
					                DValueCalculator.deltaTandMuFileName)) {
						continue;
					}
					try {
						this.getPointsFromTrajectories(f);
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
				final double meanD = this.computeD();
				this.dMap.put(image.getName(), meanD);
				try {
					this.writeTrajectoryDeltaTAndMuFile(image);
					this.writeSingleResultsFile(image);
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			try {
				this.writeResultsFile(dataset);
				this.writeLogFile(dataset);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
		this.updateGUI("Finished");
		this.errorLog.append("Finished\n");
	}

	private double computeD() {
		double meanD = 0.0;
		int counter = 0;
		for (final int trajIndex : this.xMap.keySet()) {
			final List<Double> x = this.xMap.get(trajIndex);
			final List<Double> y = this.yMap.get(trajIndex);
			final double[] xVals = new double[x.size()];
			final double[] yVals = new double[y.size()];
			for (int i = 0; i < x.size(); i++) {
				xVals[i] = x.get(i);
				yVals[i] = y.get(i);
			}
			final Stats2 stats = new Stats2(xVals, yVals,
			        DValueCalculator.SECOND_PER_FRAME, null);
			Double d = null;
			final ArrayList<Double> deltaTList = new ArrayList<Double>();
			final ArrayList<Double> muList = new ArrayList<Double>();
			if (this.filter == 0) {
				d = stats.log_delta_t__log_mu__gamma__D(2, deltaTList, muList)[2][3];
			} else {
				d = stats.delta_t__mu__D(2, deltaTList, muList)[2][3];
			}

			this.deltaTMap.put(trajIndex, deltaTList);
			this.muMap.put(trajIndex, muList);
			this.trajDMap.put(trajIndex, d);

			meanD += d;
			counter++;
		}
		meanD /= counter;
		return meanD;
	}

	public void perImageReset() {
		this.xMap.clear();
		this.yMap.clear();
		this.deltaTMap.clear();
		this.muMap.clear();
		this.trajDMap.clear();
	}

	public void perDatasetReset() {
		this.dMap.clear();
	}

	private void writeLogFile(final File dir) throws IOException {
		String fileName = DValueCalculator.logFileName + "_V1.txt";
		if (this.filter == 1) {
			fileName = DValueCalculator.logFileName + "_V4.txt";
		}
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + fileName);
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.errorLog.toString());
		bw.close();
		fw.close();
	}

	private void writeTrajectoryDeltaTAndMuFile(final File dir)
	        throws IOException {
		for (final Integer trajIndex : this.deltaTMap.keySet()) {
			final String fileName = DValueCalculator.deltaTandMuFileName + "_"
			        + trajIndex + ".txt";
			final File resultsFile = new File(dir.getAbsolutePath()
			        + File.separatorChar + fileName);
			final FileWriter fw = new FileWriter(resultsFile, true);
			final BufferedWriter bw = new BufferedWriter(fw);
			final StringBuffer output = new StringBuffer();
			final List<Double> deltaTList = this.deltaTMap.get(trajIndex);
			final List<Double> muList = this.muMap.get(trajIndex);
			output.append("DeltaT\tMu\n");
			for (int index = 0; index < deltaTList.size(); index++) {
				output.append(deltaTList.get(index) + "\t" + muList.get(index)
				        + "\n");
			}
			bw.write(output.toString());
			bw.close();
			fw.close();
		}
	}

	private void writeSingleResultsFile(final File dir) throws IOException {
		String fileName = DValueCalculator.resultsFileName + "_V1.txt";
		if (this.filter == 1) {
			fileName = DValueCalculator.resultsFileName + "_V4.txt";
		}
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + fileName);
		final FileWriter fw = new FileWriter(resultsFile, true);
		final BufferedWriter bw = new BufferedWriter(fw);
		final StringBuffer output = new StringBuffer();
		for (final Integer trajIndex : this.trajDMap.keySet()) {
			final double d = this.trajDMap.get(trajIndex);
			output.append(trajIndex + "\t" + d + "\n");
		}
		bw.write(output.toString());
		bw.close();
		fw.close();
	}

	private void writeResultsFile(final File dir) throws IOException {
		String fileName = DValueCalculator.resultsFileName + "_V1.txt";
		if (this.filter == 1) {
			fileName = DValueCalculator.resultsFileName + "_V4.txt";
		}
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + fileName);
		final FileWriter fw = new FileWriter(resultsFile, true);
		final BufferedWriter bw = new BufferedWriter(fw);
		final StringBuffer output = new StringBuffer();
		for (final String s : this.dMap.keySet()) {
			final double d = this.dMap.get(s);
			output.append(s + "\t" + d + "\n");
		}
		bw.write(output.toString());
		bw.close();
		fw.close();
	}
}
