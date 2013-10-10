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

public class P2PDistanceCalculator implements Runnable {
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
	private final List<Double> genXElements, genYElements;
	private final List<Double> comXElements, comYElements;
	private final List<Double> distXElements, distYElements;
	private final List<Double> distABSXElements, distABSYElements;

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
		this.genXElements = new ArrayList<Double>();
		this.genYElements = new ArrayList<Double>();
		this.comXElements = new ArrayList<Double>();
		this.comYElements = new ArrayList<Double>();
		this.distXElements = new ArrayList<Double>();
		this.distYElements = new ArrayList<Double>();
		this.distABSXElements = new ArrayList<Double>();
		this.distABSYElements = new ArrayList<Double>();
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
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			// TODO Auto-generated catch block
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
				final MyPoint particle = new MyPoint(x, y);
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
					final MyPoint particle = new MyPoint(x, y);
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
				final double xDist = genParticle.xD.subtract(compParticle.xD)
				        .doubleValue();
				final double yDist = genParticle.yD.subtract(compParticle.yD)
				        .doubleValue();
				this.genXElements.add(genParticle.xD.doubleValue());
				this.genYElements.add(genParticle.yD.doubleValue());
				this.comXElements.add(compParticle.xD.doubleValue());
				this.comYElements.add(compParticle.yD.doubleValue());
				this.distXElements.add(xDist);
				this.distABSXElements.add(Math.abs(xDist));
				this.distYElements.add(yDist);
				this.distABSYElements.add(Math.abs(yDist));
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
					// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				ex.printStackTrace();
			}
		}
	}

	private void computeBiasAndSigma() {
		final StatisticalCalculator statX = new StatisticalCalculator(
		        this.distXElements);
		final StatisticalCalculator statY = new StatisticalCalculator(
		        this.distYElements);
		final StatisticalCalculator statABSX = new StatisticalCalculator(
		        this.distABSXElements);
		final StatisticalCalculator statABSY = new StatisticalCalculator(
		        this.distABSYElements);
		this.biasX = statX.getMean();
		this.biasY = statY.getMean();
		this.sigmaX = statX.getStdDev();
		this.sigmaY = statY.getStdDev();
		this.biasABSX = statABSX.getMean();
		this.biasABSY = statABSY.getMean();
		this.sigmaABSX = statABSX.getStdDev();
		this.sigmaABSY = statABSY.getStdDev();
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
		final StringBuffer genXElements = new StringBuffer(
		        "Generated x elements:\t");
		int index = 0;
		for (final double genXEle : this.genXElements) {
			genXElements.append(genXEle);
			if ((index % 10000) == 0) {
				genXElements.append("\n");
			} else {
				genXElements.append("\t");
			}
			index++;
		}
		genXElements.append("\n");

		final StringBuffer comXElements = new StringBuffer(
		        "Computed x elements:\t");
		index = 0;
		for (final double comXEle : this.comXElements) {
			comXElements.append(comXEle);
			if ((index % 10000) == 0) {
				comXElements.append("\n");
			} else {
				comXElements.append("\t");
			}
			index++;
		}
		comXElements.append("\n");

		final StringBuffer distXElements = new StringBuffer(
		        "Dist x elements:\t");
		index = 0;
		for (final double distXEle : this.distXElements) {
			distXElements.append(distXEle);
			if ((index % 10000) == 0) {
				distXElements.append("\n");
			} else {
				distXElements.append("\t");
			}
			index++;
		}
		distXElements.append("\n");

		final StringBuffer distABSXElements = new StringBuffer(
		        "Dist abs x elements:\t");
		index = 0;
		for (final double distXEle : this.distABSXElements) {
			distABSXElements.append(distXEle);
			if ((index % 10000) == 0) {
				distABSXElements.append("\n");
			} else {
				distABSXElements.append("\t");
			}
			index++;
		}
		distABSXElements.append("\n");

		final StringBuffer genYElements = new StringBuffer(
		        "Generated y elements:\t");
		index = 0;
		for (final double genYEle : this.genYElements) {
			genYElements.append(genYEle);
			if ((index % 10000) == 0) {
				comXElements.append("\n");
			} else {
				comXElements.append("\t");
			}
			index++;
		}
		genYElements.append("\n");

		final StringBuffer comYElements = new StringBuffer(
		        "Computed y elements:\t");
		index = 0;
		for (final double comYEle : this.comYElements) {
			comYElements.append(comYEle);
			if ((index % 10000) == 0) {
				comYElements.append("\n");
			} else {
				comYElements.append("\t");
			}
			index++;
		}
		comYElements.append("\n");

		final StringBuffer distYElements = new StringBuffer(
		        "Dist y elements:\t");
		index = 0;
		for (final double distYEle : this.distYElements) {
			distYElements.append(distYEle);
			if ((index % 10000) == 0) {
				distYElements.append("\n");
			} else {
				distYElements.append("\t");
			}
			index++;
		}
		distYElements.append("\n");

		final StringBuffer distABSYElements = new StringBuffer(
		        "Dist y elements:\t");
		index = 0;
		for (final double distYEle : this.distABSYElements) {
			distABSYElements.append(distYEle);
			if ((index % 10000) == 0) {
				distABSYElements.append("\n");
			} else {
				distABSYElements.append("\t");
			}
			index++;
		}
		distABSYElements.append("\n");

		bw.write(genXElements.toString());
		bw.write("# of elements:\t" + this.genXElements.size() + "\n");
		bw.write(comXElements.toString());
		bw.write("# of elements:\t" + this.comXElements.size() + "\n");
		bw.write(distXElements.toString());
		bw.write("# of elements:\t" + this.distXElements.size() + "\n");
		bw.write(distABSXElements.toString());
		bw.write("# of elements:\t" + this.distABSXElements.size() + "\n");
		bw.write(genYElements.toString());
		bw.write("# of elements:\t" + this.genYElements.size() + "\n");
		bw.write(comYElements.toString());
		bw.write("# of elements:\t" + this.comYElements.size() + "\n");
		bw.write(distYElements.toString());
		bw.write("# of elements:\t" + this.distYElements.size() + "\n");
		bw.write(distABSYElements.toString());
		bw.write("# of elements:\t" + this.distABSYElements.size() + "\n");
		bw.write("Bias X:\t" + this.biasX + "\n");
		bw.write("Bias Y:\t" + this.biasY + "\n");
		bw.write("Bias ABS X:\t" + this.biasABSX + "\n");
		bw.write("Bias ABS Y:\t" + this.biasABSY + "\n");
		bw.write("Sigma X:\t" + this.sigmaX + "\n");
		bw.write("Sigma Y:\t" + this.sigmaY + "\n");
		bw.write("Sigma ABS X:\t" + this.sigmaABSX + "\n");
		bw.write("Sigma ABS Y:\t" + this.sigmaABSY + "\n");
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
