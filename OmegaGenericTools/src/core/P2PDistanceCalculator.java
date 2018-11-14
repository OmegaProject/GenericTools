package core;

import gui.OmegaGenericToolGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class P2PDistanceCalculator implements Runnable {

	int precision = 15;

	// public final static String fileName1 = "PT_Trajectories_";
	public final static String fileName1 = "SDOutput.txt";
	// public final static String fileName1 =
	// "FilterPTTrackerReport_Results.txt";
	// public final String fileName1 =
	// "FilterMosaicReport_Results_3.2_INV_NOBIAS.txt";
	public final static String fileName2 = "ParticleLog_";

	private final static String ident1 = "% Trajectory id:";
	private final static String ident2 = "Particles of frame: ";
	private final static String ident3 = "Particle";
	
	String snrs[] = { "1.291059", "1.990510", "2.846111", "3.494379",
			"4.556798", "6.516668", "8.832892", "11.632132", "15.067460",
			"19.326731", "24.642859", "31.306549" };
	String signals[] = { "15.00", "18.58", "23.90", "28.73", "38.10", "60.80",
			"97.00", "154.70", "246.60", "393.30", "627.10", "1000.00" };

	private final boolean swapComputedPoints = false;
	private final boolean addBiasToComputedPoints = false;

	private final File inputDir, outputDir;
	private File trajFile;
	private final List<File> logs;
	private final Map<String, Map<Integer, List<MyPoint>>> computedPoints;
	private final Map<String, Map<Integer, List<MyPoint>>> generatedPoints;
	private final Map<String, BigDecimal> genXElements, genYElements;
	private final Map<String, BigDecimal> comXElements, comYElements;
	private final Map<String, BigDecimal> distXElements, distYElements;
	private final Map<String, BigDecimal> distABSXElements, distABSYElements;

	private double biasX, biasY, sigmaX, sigmaY;
	private double biasABSX, biasABSY, sigmaABSX, sigmaABSY;
	private double euclidianBias, euclidianSigma;

	boolean analyzeFiltered, analyzeMerged;

	private final StringBuffer errorLog;

	private final OmegaGenericToolGUI gui;

	public P2PDistanceCalculator(final File inputDir, final File outputDir,
			final boolean analyzeFiltered, final boolean analyzeMerged,
			final OmegaGenericToolGUI gui) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.trajFile = null;
		this.logs = new ArrayList<File>();
		this.computedPoints = new HashMap<String, Map<Integer, List<MyPoint>>>();
		this.generatedPoints = new HashMap<String, Map<Integer, List<MyPoint>>>();
		this.genXElements = new HashMap<String, BigDecimal>();
		this.genYElements = new HashMap<String, BigDecimal>();
		this.comXElements = new HashMap<String, BigDecimal>();
		this.comYElements = new HashMap<String, BigDecimal>();
		this.distXElements = new HashMap<String, BigDecimal>();
		this.distYElements = new HashMap<String, BigDecimal>();
		this.distABSXElements = new HashMap<String, BigDecimal>();
		this.distABSYElements = new HashMap<String, BigDecimal>();
		this.biasX = 0.0;
		this.biasY = 0.0;
		this.sigmaX = 0.0;
		this.sigmaY = 0.0;
		this.biasABSX = 0.0;
		this.biasABSY = 0.0;
		this.sigmaABSX = 0.0;
		this.sigmaABSY = 0.0;
		this.euclidianBias = 0.0;
		this.euclidianSigma = 0.0;
		this.errorLog = new StringBuffer();
		this.gui = gui;

		this.analyzeFiltered = analyzeFiltered;
		this.analyzeMerged = analyzeMerged;
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					P2PDistanceCalculator.this.gui.appendOutput(update);

				}
			});
		} catch (final InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	private void getPointsFromTrajectories(final String imageTag,
			final boolean hasTrackTag) throws IOException {
		final FileReader fr = new FileReader(this.trajFile);
		final BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		boolean readParticles = false;
		while (line != null) {
			if (hasTrackTag) {
				if (line.contains(P2PDistanceCalculator.ident1)) {
					readParticles = true;
					line = br.readLine();
					continue;
				}
			} else {
				readParticles = true;
			}
			if (line.isEmpty()) {
				readParticles = false;
				line = br.readLine();
				continue;
			}
			if (readParticles) {
				String subString1, val;
				final List<String> values = new ArrayList<String>();
				int indexTab = line.indexOf("\t");
				if (hasTrackTag && ((indexTab != -1) && (indexTab != 0))) {
					val = line.substring(0, indexTab);
					values.add(val);
				}
				subString1 = line;
				while (indexTab != -1) {
					if (subString1.startsWith("\t")) {
						subString1 = subString1.substring(indexTab + 1);
						indexTab = subString1.indexOf("\t");
					}
					if (indexTab == -1) {
						val = subString1;
					} else {
						val = subString1.substring(0, indexTab);
						subString1 = subString1.substring(indexTab);
					}
					if (!val.isEmpty()) {
						values.add(val);
					}
					indexTab = subString1.indexOf("\t");
				}
				final int frame = Integer.valueOf(values.get(0));// - 1;
				final double x, y;
				if (!this.swapComputedPoints) {
					x = Double.valueOf(values.get(1));
					y = Double.valueOf(values.get(2));
				} else {
					x = Double.valueOf(values.get(2));
					y = Double.valueOf(values.get(1));
				}
				if (this.addBiasToComputedPoints) {
					x -= 0.5;
					y -= 0.5;
				}
				
				final MyPoint particle = new MyPoint(x, y, this.precision);
				Map<Integer, List<MyPoint>> imageParticles;
				List<MyPoint> frameParticles;
				if (this.computedPoints.containsKey(imageTag)) {
					imageParticles = this.computedPoints.get(imageTag);
				} else {
					imageParticles = new LinkedHashMap<Integer, List<MyPoint>>();
				}
				if (imageParticles.containsKey(frame)) {
					frameParticles = imageParticles.get(frame);
				} else {
					frameParticles = new ArrayList<MyPoint>();
				}
				frameParticles.add(particle);
				imageParticles.put(frame, frameParticles);
				this.computedPoints.put(imageTag, imageParticles);
			}
			line = br.readLine();
		}
		br.close();
		fr.close();
	}

	private void printPoints(final Map<Integer, List<MyPoint>> pointsMap,
			final int maxIndex) {
		for (int frame = 0; frame < maxIndex; frame++) {
			System.out.println("Frame " + frame);
			if (!pointsMap.containsKey(frame)) {
				System.out.println("- EMPTY -");
				continue;
			}
			for (final MyPoint particle : pointsMap.get(frame)) {
				System.out.println("Particle x: " + particle.xD.toString()
						+ " - y: " + particle.yD.toString());
			}
		}
	}

	private void getPointsFromLogs(final String imageTag) throws IOException {
		for (final File f : this.logs) {
			final FileReader fr = new FileReader(f);
			final BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			final List<String> values = new ArrayList<String>();
			final String frameIndex = line.replace(
					P2PDistanceCalculator.ident2, "");
			final int frame = Integer.valueOf(frameIndex) - 1;
			line = br.readLine();
			while (line != null) {
				String val;
				line = line.replaceAll(" ", "\t");
				if (line.contains(P2PDistanceCalculator.ident3)) {
					String subString1 = line.replace(
							P2PDistanceCalculator.ident3, "");
					int indexTab = subString1.indexOf("\t");
					while (indexTab != -1) {
						if (subString1.startsWith("\t")) {
							subString1 = subString1.substring(indexTab + 1);
							indexTab = subString1.indexOf("\t");
						}
						if (indexTab == -1) {
							val = subString1;
						} else {
							val = subString1.substring(0, indexTab);
							subString1 = subString1.substring(indexTab);
						}
						if (!val.isEmpty()) {
							values.add(val);
						}
						indexTab = subString1.indexOf("\t");
					}
					final double x = Double.valueOf(values.get(1));
					final double y = Double.valueOf(values.get(2));
					final MyPoint particle = new MyPoint(x, y, this.precision);
					Map<Integer, List<MyPoint>> imageParticles;
					List<MyPoint> frameParticles;
					if (this.generatedPoints.containsKey(imageTag)) {
						imageParticles = this.generatedPoints.get(imageTag);
					} else {
						imageParticles = new LinkedHashMap<Integer, List<MyPoint>>();
					}
					if (imageParticles.containsKey(frame)) {
						frameParticles = imageParticles.get(frame);
					} else {
						frameParticles = new ArrayList<MyPoint>();
					}
					frameParticles.add(particle);
					imageParticles.put(frame, frameParticles);
					this.generatedPoints.put(imageTag, imageParticles);
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		}
	}

	private void computeP2PDistances(final String imageTag) {
		int counter = 0;
		final Map<Integer, List<MyPoint>> datasetGeneratedPoints = this.generatedPoints
				.get(imageTag);
		final Map<Integer, List<MyPoint>> datasetComputedPoints = this.computedPoints
				.get(imageTag);
		for (int frameIndex = 0; frameIndex < datasetGeneratedPoints.keySet()
				.size(); frameIndex++) {
			final List<MyPoint> genPoints = datasetGeneratedPoints
					.get(frameIndex);
			final List<MyPoint> comPoints = datasetComputedPoints
					.get(frameIndex);
			if ((genPoints == null) || (comPoints == null)) {
				this.errorLog.append("Frame " + frameIndex);
				if (genPoints == null) {
					this.errorLog.append(" genPoints is null\n");
				} else {
					this.errorLog.append(" comPoints is null\n");
				}
				continue;
			}
			if (genPoints.size() != comPoints.size()) {
				this.errorLog.append("Frame " + frameIndex
						+ " comPoints is bigger than genPoints ("
						+ comPoints.size() + " VS " + genPoints.size() + ")");
			}
			for (int particleIndex = 0; particleIndex < genPoints.size(); particleIndex++) {
				final MyPoint genParticle = genPoints.get(particleIndex);
				final MyPoint compParticle = comPoints.get(particleIndex);
				final BigDecimal xDist = genParticle.xD
						.subtract(compParticle.xD);
				final BigDecimal yDist = genParticle.yD
						.subtract(compParticle.yD);
				final String key = imageTag + "_" + frameIndex + "_" + counter;
				this.genXElements.put(key, genParticle.xD);
				this.genYElements.put(key, genParticle.yD);
				this.comXElements.put(key, compParticle.xD);
				this.comYElements.put(key, compParticle.yD);
				this.distXElements.put(key, xDist);
				this.distABSXElements.put(key, xDist.abs());
				this.distYElements.put(key, yDist);
				this.distABSYElements.put(key, yDist.abs());
				counter++;
			}
		}
	}

	@Override
	public void run() {
		for (final File dataset : this.inputDir.listFiles()) {
			if (dataset.isFile() || !dataset.getName().contains("Special_")) {
				continue;
			}
			final File outputDir1 = new File(this.outputDir.getAbsolutePath()
					+ File.separator + dataset.getName());
			if (!outputDir1.exists()) {
				// continue;
				outputDir1.mkdir();
			}
			this.updateGUI("Dataset\t" + dataset.getName());
			this.errorLog.append("Dataset\t" + dataset.getName() + "\n");

			final String tokens[] = dataset.getName().split("_");
			final String signal = tokens[2];
			int index = -1;
			for (int i = 0; i < this.signals.length; i++) {
				if (this.signals[i].equals(signal)) {
					index = i;
					break;
				}
			}
			String snr = "NA";
			if (index != -1) {
				snr = this.snrs[index];
			}

			this.perDatasetReset();
			for (final File image : dataset.listFiles()) {
				if (image.isFile()) {
					continue;
				}
				final String imageTag = image.getName();
				final File outputDir2 = new File(outputDir1.getAbsolutePath()
						+ File.separator + image.getName());
				// if (!outputDir2.exists()) {
				// outputDir2.mkdir();
				// }
				this.updateGUI("Image\t" + imageTag);
				this.errorLog.append("Image\t" + imageTag + "\n");
				final File logsDir = new File(image.getPath()
						+ File.separatorChar + "logs");
				if (!logsDir.exists()) {
					continue;
				}
				final File outputDir3 = new File(outputDir2.getAbsolutePath()
						+ File.separator + logsDir.getName());
				// if (!outputDir3.exists()) {
				// outputDir3.mkdir();
				// }
				this.perImageReset();
				boolean abortFolder = false;

				if (outputDir3.exists()) {
					for (final File f : outputDir3.listFiles()) {
						boolean check = f.getName().contains(
								P2PDistanceCalculator.fileName1);
						if (this.analyzeFiltered) {
							check = check
									&& f.getName().contains("TAF_TrajFilter");
						} else if (this.analyzeMerged) {
							check = check
									&& f.getName().contains("TAF_TrajMerge");
						} else {
							check = check && !f.getName().contains("TAF");
						}
						if (check) {
							if (this.trajFile != null) {
								final StringBuffer string = new StringBuffer();
								string.append("Error found in: \n");
								string.append(dataset.getAbsolutePath());
								string.append("\n");
								string.append("multiple trajectories or filtered trajectories file\n");
								string.append("\n");
								this.errorLog.append(string.toString());
								this.updateGUI(string.toString());
								abortFolder = true;
							} else {
								this.trajFile = f;
							}
						}
					}
				} else {
					if (this.trajFile == null) {
						for (final File f : logsDir.listFiles()) {
							if (f.getName().contains(
									P2PDistanceCalculator.fileName1)) {
								this.trajFile = f;
							}
						}
					}
					// outputDir3.mkdir();
				}
				for (final File f : logsDir.listFiles()) {
					if (f.getName().contains(P2PDistanceCalculator.fileName2)) {
						this.logs.add(f);
					}
				}
				if (abortFolder) {
					continue;
				}
				try {
					boolean hasTrackTag = true;
					if (P2PDistanceCalculator.fileName1.contains("SDOutput")
							|| P2PDistanceCalculator.fileName1
									.contains("SD_Output")
							|| P2PDistanceCalculator.fileName1
									.contains("SDOutputSingle")
							|| P2PDistanceCalculator.fileName1
									.contains("SD_OutputSingle")
							|| P2PDistanceCalculator.fileName1
									.contains("FilterMosaicReport")
							|| P2PDistanceCalculator.fileName1
									.contains("FilterPTTrackerReport")
							|| P2PDistanceCalculator.fileName1
									.contains("FilterSDReport")) {
						hasTrackTag = false;
					}
					this.getPointsFromTrajectories(imageTag, hasTrackTag);
					this.getPointsFromLogs(imageTag);
				} catch (final IOException ex) {
					ex.printStackTrace();
					this.updateGUI("Error reading trajectories or logs at image");
				}
				final int maxIndex = this.generatedPoints.get(imageTag)
						.keySet().size();

				System.out.println("###PRINT GEN POINTS###");
				this.printPoints(this.generatedPoints.get(imageTag), maxIndex);
				System.out.println("###PRINT COM POINTS###");
				this.printPoints(this.computedPoints.get(imageTag), maxIndex);
				this.computeP2PDistances(imageTag);
			}
			this.computeBiasAndSigma();
			try {
				this.writeResultsFile(outputDir1, signal, snr);
				this.writeLogFile(outputDir1);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
		this.updateGUI("***P2P COMPLETED***");
	}

	private void computeBiasAndSigma() {
		final Map<String, BigDecimal> euclideans = new HashMap<String, BigDecimal>();
		for (final String key : this.distXElements.keySet()) {
			final BigDecimal distX = this.distXElements.get(key);
			final BigDecimal distY = this.distYElements.get(key);
			final Double valX = distX.doubleValue();
			final Double valY = distY.doubleValue();
			final Double eucl = Math.sqrt((valX * valX) + (valY * valY));
			euclideans.put(key, new BigDecimal(eucl));
		}
		final StatisticalCalculator statX = new StatisticalCalculator(
				this.distXElements.values(), this.precision);
		final StatisticalCalculator statY = new StatisticalCalculator(
				this.distYElements.values(), this.precision);
		final StatisticalCalculator statABSX = new StatisticalCalculator(
				this.distABSXElements.values(), this.precision);
		final StatisticalCalculator statABSY = new StatisticalCalculator(
				this.distABSYElements.values(), this.precision);
		final StatisticalCalculator statEuclideans = new StatisticalCalculator(
				euclideans.values(), this.precision);
		this.biasX = statX.getDoubleMean();
		this.biasY = statY.getDoubleMean();
		this.sigmaX = statX.getDoubleStdDev();
		this.sigmaY = statY.getDoubleStdDev();
		this.biasABSX = statABSX.getDoubleMean();
		this.biasABSY = statABSY.getDoubleMean();
		this.sigmaABSX = statABSX.getDoubleStdDev();
		this.sigmaABSY = statABSY.getDoubleStdDev();
		this.euclidianBias = statEuclideans.getDoubleMean();
		this.euclidianSigma = statEuclideans.getDoubleStdDev();
	}

	public void perImageReset() {
		this.trajFile = null;
		this.logs.clear();
		this.computedPoints.clear();
		this.generatedPoints.clear();
	}

	public void perDatasetReset() {
		this.genXElements.clear();
		this.genYElements.clear();
		this.comXElements.clear();
		this.comYElements.clear();
		this.distXElements.clear();
		this.distYElements.clear();
		this.distABSXElements.clear();
		this.distABSYElements.clear();
		this.biasX = 0.0;
		this.biasY = 0.0;
		this.sigmaX = 0.0;
		this.sigmaY = 0.0;
		this.biasABSX = 0.0;
		this.biasABSY = 0.0;
		this.sigmaABSX = 0.0;
		this.sigmaABSY = 0.0;
	}

	private void writeLogFile(final File dir) throws IOException {
		final File resultsFile = new File(dir.getAbsolutePath()
				+ File.separatorChar + "P2PDistanceCalculator_Log.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Input:\t" + this.inputDir.getName() + "\n");
		bw.write("Input file :\t" + P2PDistanceCalculator.fileName1 + "\n");
		bw.write("Swap computed x-y :\t" + this.swapComputedPoints + "\n");
		bw.write("Add bias -0.5 :\t" + this.addBiasToComputedPoints + "\n");
		bw.write("\n");
		bw.write(this.errorLog.toString());
		bw.close();
		fw.close();
	}

	private void writeResultsFile(final File dir, final String signal,
			final String snr) throws IOException {
		final File resultsFile = new File(dir.getAbsolutePath()
				+ File.separatorChar + "P2PDistanceCalculator_Results.txt");
		final File resultsFile2 = new File(dir.getAbsolutePath()
				+ File.separatorChar
				+ "P2PDistanceCalculator_Results_distrib.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		final FileWriter fw2 = new FileWriter(resultsFile2);
		final BufferedWriter bw2 = new BufferedWriter(fw2);
		final StringBuffer distribOutput = new StringBuffer();
		final StringBuffer output = new StringBuffer();
		output.append("ID\t");
		output.append("Signal\t");
		output.append("SNR\t");
		output.append("Expected x\t");
		output.append("Observed x\t");
		output.append("Dist x\t");
		output.append("Dist abs x\t");
		output.append("Expected y\t");
		output.append("Observed y\t");
		output.append("Dist y\t");
		output.append("Dist abs y\n");

		int counter = 0;
		for (final String key : this.genXElements.keySet()) {
			output.append(key);
			output.append("\t");
			output.append(signal);
			output.append("\t");
			output.append(snr);
			output.append("\t");
			output.append(this.genXElements.get(key)
					.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
					.toString());
			output.append("\t");
			output.append(this.comXElements.get(key)
					.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
					.toString());
			output.append("\t");
			final String numX = this.distXElements.get(key)
					.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
					.toString();
			output.append(numX);
			output.append("\t");
			distribOutput.append(numX);
			distribOutput.append("\t");
			output.append(this.distABSXElements.get(key)
					.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
					.toString());
			output.append("\t");
			output.append(this.genYElements.get(key)
					.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
					.toString());
			output.append("\t");
			output.append(this.comYElements.get(key)
					.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
					.toString());
			output.append("\t");
			final String numY = this.distYElements.get(key)
					.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
					.toString();
			output.append(numY);
			output.append("\t");
			distribOutput.append(numY);
			output.append(this.distABSYElements.get(key)
					.setScale(this.precision, BigDecimal.ROUND_HALF_UP)
					.toString());
			output.append("\t");

			switch (counter) {
				case 0:
					output.append("# of gen X elements");
					output.append("\t");
					output.append(this.genXElements.size());
					break;
				case 1:
					output.append("# of com X elements");
					output.append("\t");
					output.append(this.comXElements.size());
					break;
				case 2:
					output.append("# of dist X elements");
					output.append("\t");
					output.append(this.distXElements.size());
					break;
				case 3:
					output.append("# of dist ABS X elements");
					output.append("\t");
					output.append(this.distABSXElements.size());
					break;
				case 4:
					output.append("# of gen Y elements");
					output.append("\t");
					output.append(this.genYElements.size());
					break;
				case 5:
					output.append("# of com Y elements");
					output.append("\t");
					output.append(this.comYElements.size());
					break;
				case 6:
					output.append("# of dist Y elements");
					output.append("\t");
					output.append(this.distYElements.size());
					break;
				case 7:
					output.append("# of dist ABS Y elements");
					output.append("\t");
					output.append(this.distABSYElements.size());
					break;
				case 8:
					output.append("Bias X:");
					output.append("\t");
					output.append(this.biasX);
					break;
				case 9:
					output.append("Bias Y:");
					output.append("\t");
					output.append(this.biasY);
					break;
				case 10:
					output.append("Bias ABS X:");
					output.append("\t");
					output.append(this.biasABSX);
					break;
				case 11:
					output.append("Bias ABS Y:");
					output.append("\t");
					output.append(this.biasABSY);
					break;
				case 12:
					output.append("Sigma X:");
					output.append("\t");
					output.append(this.sigmaX);
					break;
				case 13:
					output.append("Sigma Y:");
					output.append("\t");
					output.append(this.sigmaY);
					break;
				case 14:
					output.append("Sigma ABS X:");
					output.append("\t");
					output.append(this.sigmaABSX);
					break;
				case 15:
					output.append("Sigma ABS Y:");
					output.append("\t");
					output.append(this.sigmaABSY);
					break;
				case 16:
					output.append("Euclidian Bias:");
					output.append("\t");
					output.append(this.euclidianBias);
					break;
				case 17:
					output.append("Euclidian Sigma:");
					output.append("\t");
					output.append(this.euclidianSigma);
					break;
				default:
					break;
			}
			output.append("\n");
			distribOutput.append("\n");
			counter++;
		}

		bw.write(output.toString());
		bw2.write(distribOutput.toString());
		bw.close();
		fw.close();
		bw2.close();
		fw2.close();
	}

	public double getBiasX() {
		return this.biasX;
	}

	public double getBiasY() {
		return this.biasY;
	}

	public double getSigmaX() {
		return this.sigmaX;
	}

	public double getSigmaY() {
		return this.sigmaY;
	}
}
