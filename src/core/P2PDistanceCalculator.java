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
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class P2PDistanceCalculator implements Runnable {

	int precision = 15;

	public final static String fileName1 = "PT_Trajectories_";
	public final static String fileName2 = "ParticleLog_";

	private final static String ident1 = "% Trajectory id:";
	private final static String ident2 = "Particles of frame: ";
	private final static String ident3 = "Particle";

	private final File workingDir;
	private File trajFile;
	private final List<File> logs;
	private final Map<Integer, List<MyPoint>> computedPoints;
	private final Map<Integer, List<MyPoint>> generatedPoints;
	private final List<BigDecimal> genXElements, genYElements;
	private final List<BigDecimal> comXElements, comYElements;
	private final List<BigDecimal> distXElements, distYElements;
	private final List<BigDecimal> distABSXElements, distABSYElements;

	private double biasX, biasY, sigmaX, sigmaY;
	private double biasABSX, biasABSY, sigmaABSX, sigmaABSY;

	boolean analyzeFiltered, analyzeMerged;

	private final StringBuffer errorLog;

	private final OmegaGenericToolGUI gui;

	public P2PDistanceCalculator(final File workingDir,
	        final boolean analyzeFiltered, final boolean analyzeMerged,
	        final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;
		this.trajFile = null;
		this.logs = new ArrayList<File>();
		this.computedPoints = new HashMap<Integer, List<MyPoint>>();
		this.generatedPoints = new HashMap<Integer, List<MyPoint>>();
		this.genXElements = new ArrayList<BigDecimal>();
		this.genYElements = new ArrayList<BigDecimal>();
		this.comXElements = new ArrayList<BigDecimal>();
		this.comYElements = new ArrayList<BigDecimal>();
		this.distXElements = new ArrayList<BigDecimal>();
		this.distYElements = new ArrayList<BigDecimal>();
		this.distABSXElements = new ArrayList<BigDecimal>();
		this.distABSYElements = new ArrayList<BigDecimal>();
		this.biasX = 0.0;
		this.biasY = 0.0;
		this.sigmaX = 0.0;
		this.sigmaY = 0.0;
		this.biasABSX = 0.0;
		this.biasABSY = 0.0;
		this.sigmaABSX = 0.0;
		this.sigmaABSY = 0.0;
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

	private void getPointsFromTrajectories() throws IOException {
		final FileReader fr = new FileReader(this.trajFile);
		final BufferedReader br = new BufferedReader(fr);
		String line = br.readLine();
		boolean readParticles = false;
		while (line != null) {
			if (line.contains(P2PDistanceCalculator.ident1)) {
				readParticles = true;
				line = br.readLine();
				continue;
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
				subString1 = line;
				while (indexTab != -1) {
					subString1 = subString1.substring(indexTab + 1);
					indexTab = subString1.indexOf("\t");
					if (indexTab == -1) {
						val = subString1;
					} else {
						val = subString1.substring(0, indexTab);
					}
					values.add(val);
				}
				final int frame = Integer.valueOf(values.get(0));
				final double y = Double.valueOf(values.get(1));
				final double x = Double.valueOf(values.get(2));
				final MyPoint particle = new MyPoint(x, y, this.precision);
				List<MyPoint> frameParticles;
				if (this.computedPoints.containsKey(frame)) {
					frameParticles = this.computedPoints.get(frame);
				} else {
					frameParticles = new ArrayList<MyPoint>();
				}
				frameParticles.add(particle);
				this.computedPoints.put(frame, frameParticles);
			}
			line = br.readLine();
		}
		br.close();
		fr.close();
	}

	private void printPoints(final Map<Integer, List<MyPoint>> pointsMap) {
		for (int frame = 0; frame < pointsMap.keySet().size(); frame++) {
			System.out.println("Frame " + frame);
			for (final MyPoint particle : pointsMap.get(frame)) {
				System.out.println("Particle x: " + particle.xD.toString()
				        + " - y: " + particle.yD.toString());
			}
		}
	}

	private void getPointsFromLogs() throws IOException {
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
				if (line.contains(P2PDistanceCalculator.ident3)) {
					String subString1 = line.replace(
					        P2PDistanceCalculator.ident3, "");
					int indexTab = subString1.indexOf("\t");
					while (indexTab != -1) {
						subString1 = subString1.substring(indexTab + 1);
						indexTab = subString1.indexOf("\t");
						if (indexTab == -1) {
							val = subString1;
						} else if (indexTab == 0) {
							subString1 = subString1.substring(indexTab + 1);
							indexTab = subString1.indexOf("\t");
							val = subString1.substring(0, indexTab);
						} else {
							val = subString1.substring(0, indexTab);
						}
						values.add(val);
					}
					final double x = Double.valueOf(values.get(0));
					final double y = Double.valueOf(values.get(1));
					final MyPoint particle = new MyPoint(x, y, this.precision);
					List<MyPoint> frameParticles;
					if (this.generatedPoints.containsKey(frame)) {
						frameParticles = this.generatedPoints.get(frame);
					} else {
						frameParticles = new ArrayList<MyPoint>();
					}
					frameParticles.add(particle);
					this.generatedPoints.put(frame, frameParticles);
				}
				line = br.readLine();
			}
			br.close();
			fr.close();
		}
	}

	private void computeP2PDistances() {
		for (int frameIndex = 0; frameIndex < this.generatedPoints.keySet()
		        .size(); frameIndex++) {
			final List<MyPoint> genPoints = this.generatedPoints
			        .get(frameIndex);
			final List<MyPoint> comPoints = this.computedPoints.get(frameIndex);
			if ((genPoints == null) || (comPoints == null)) {
				this.errorLog.append("Frame " + frameIndex);
				if (genPoints == null) {
					this.errorLog.append(" genPoints is null\n");
				} else {
					this.errorLog.append(" comPoints is null\n");
				}
				continue;
			}
			for (int particleIndex = 0; particleIndex < genPoints.size(); particleIndex++) {
				final MyPoint genParticle = genPoints.get(particleIndex);
				final MyPoint compParticle = comPoints.get(particleIndex);
				final BigDecimal xDist = genParticle.xD
				        .subtract(compParticle.xD);
				final BigDecimal yDist = genParticle.yD
				        .subtract(compParticle.yD);
				this.genXElements.add(genParticle.xD);
				this.genYElements.add(genParticle.yD);
				this.comXElements.add(compParticle.xD);
				this.comYElements.add(compParticle.yD);
				this.distXElements.add(xDist);
				this.distABSXElements.add(xDist.abs());
				this.distYElements.add(yDist);
				this.distABSYElements.add(yDist.abs());
			}
		}
	}

	@Override
	public void run() {
		for (final File dataset : this.workingDir.listFiles()) {
			if (dataset.isFile()) {
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
					} else if (f.getName().contains(
					        P2PDistanceCalculator.fileName2)) {
						this.logs.add(f);
					}
				}
				if (abortFolder) {
					continue;
				}
				try {
					this.getPointsFromTrajectories();
					this.getPointsFromLogs();
				} catch (final IOException ex) {
					ex.printStackTrace();
					this.updateGUI("Error reading trajectories or logs at image");
				}
				this.computeP2PDistances();
			}
			this.computeBiasAndSigma();
			try {
				this.writeResultsFile(dataset);
				this.writeLogFile(dataset);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private void computeBiasAndSigma() {
		final StatisticalCalculator statX = new StatisticalCalculator(
		        this.distXElements, this.precision);
		final StatisticalCalculator statY = new StatisticalCalculator(
		        this.distYElements, this.precision);
		final StatisticalCalculator statABSX = new StatisticalCalculator(
		        this.distABSXElements, this.precision);
		final StatisticalCalculator statABSY = new StatisticalCalculator(
		        this.distABSYElements, this.precision);
		this.biasX = statX.getDoubleMean();
		this.biasY = statY.getDoubleMean();
		this.sigmaX = statX.getDoubleStdDev();
		this.sigmaY = statY.getDoubleStdDev();
		this.biasABSX = statABSX.getDoubleMean();
		this.biasABSY = statABSY.getDoubleMean();
		this.sigmaABSX = statABSX.getDoubleStdDev();
		this.sigmaABSY = statABSY.getDoubleStdDev();
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
		bw.write(this.errorLog.toString());
		bw.close();
		fw.close();
	}

	private void writeResultsFile(final File dir) throws IOException {
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + "P2PDistanceCalculator_Results.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		final StringBuffer output = new StringBuffer();
		output.append("Generated x elements:\t");
		output.append("Computed x elements:\t");
		output.append("Dist x elements:\t");
		output.append("Dist abs x elements:\t");
		output.append("Generated y elements:\t");
		output.append("Computed y elements:\t");
		output.append("Dist y elements:\t");
		output.append("Dist abs y elements:\n");

		for (int i = 0; i < this.genXElements.size(); i++) {
			output.append(this.genXElements.get(i)
			        .setScale(this.precision, BigDecimal.ROUND_HALF_UP)
			        .toString());
			output.append("\t");
			output.append(this.comXElements.get(i)
			        .setScale(this.precision, BigDecimal.ROUND_HALF_UP)
			        .toString());
			output.append("\t");
			output.append(this.distXElements.get(i)
			        .setScale(this.precision, BigDecimal.ROUND_HALF_UP)
			        .toString());
			output.append("\t");
			output.append(this.distABSXElements.get(i)
			        .setScale(this.precision, BigDecimal.ROUND_HALF_UP)
			        .toString());
			output.append("\t");
			output.append(this.genYElements.get(i)
			        .setScale(this.precision, BigDecimal.ROUND_HALF_UP)
			        .toString());
			output.append("\t");
			output.append(this.comYElements.get(i)
			        .setScale(this.precision, BigDecimal.ROUND_HALF_UP)
			        .toString());
			output.append("\t");
			output.append(this.distYElements.get(i)
			        .setScale(this.precision, BigDecimal.ROUND_HALF_UP)
			        .toString());
			output.append("\t");
			output.append(this.distABSYElements.get(i)
			        .setScale(this.precision, BigDecimal.ROUND_HALF_UP)
			        .toString());
			output.append("\t");

			switch (i) {
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
			default:
				break;
			}
			output.append("\n");
		}

		bw.write(output.toString());
		bw.close();
		fw.close();
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
