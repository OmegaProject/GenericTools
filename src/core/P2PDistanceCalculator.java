package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class P2PDistanceCalculator {
	public final static String fileName1 = "PT_Trajectories_";
	public final static String fileName2 = "PT_Log_";

	private final static String ident1 = "% Trajectory id:";
	private final static String ident2 = "Particles of frame: ";
	private final static String ident3 = "Particle";

	private final File trajectories;
	private final List<File> logs;
	private final Map<Integer, List<MyPoint>> computedPoints;
	private final Map<Integer, List<MyPoint>> generatedPoints;
	private final List<Double> genXElements, genYElements;
	private final List<Double> comXElements, comYElements;
	private final List<Double> distXElements, distYElements;

	private double biasX, biasY, sigmaX, sigmaY;

	public P2PDistanceCalculator(final File trajectories, final List<File> logs) {
		this.trajectories = trajectories;
		this.logs = logs;
		this.biasX = 0.0;
		this.biasY = 0.0;
		this.sigmaX = 0.0;
		this.sigmaY = 0.0;
		this.computedPoints = new HashMap<Integer, List<MyPoint>>();
		this.generatedPoints = new HashMap<Integer, List<MyPoint>>();
		this.genXElements = new ArrayList<Double>();
		this.genYElements = new ArrayList<Double>();
		this.comXElements = new ArrayList<Double>();
		this.comYElements = new ArrayList<Double>();
		this.distXElements = new ArrayList<Double>();
		this.distYElements = new ArrayList<Double>();
	}

	private void getPointsFromTrajectories() throws IOException {
		final FileReader fr = new FileReader(this.trajectories);
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
				this.distYElements.add(yDist);
			}
		}
	}

	public void computeBiasAndSigma() throws IOException {
		this.getPointsFromTrajectories();
		this.getPointsFromLogs();
		this.computeP2PDistances();
		final StatisticalCalculator statX = new StatisticalCalculator(
		        this.distXElements);
		final StatisticalCalculator statY = new StatisticalCalculator(
		        this.distYElements);
		this.biasX = statX.getMean();
		this.biasY = statY.getMean();
		this.sigmaX = statX.getStdDev();
		this.sigmaY = statY.getStdDev();
	}

	public void writeResultsFile(final File dir) throws IOException {
		final File resultsFile = new File(dir + "/statResults.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		final StringBuffer genXElements = new StringBuffer(
		        "Generated x elements:\t");
		for (final double genXEle : this.genXElements) {
			genXElements.append(genXEle);
			genXElements.append("\t");
		}
		genXElements.append("\n");
		final StringBuffer comXElements = new StringBuffer(
		        "computed x elements:\t");
		for (final double comXEle : this.comXElements) {
			comXElements.append(comXEle);
			comXElements.append("\t");
		}
		comXElements.append("\n");
		final StringBuffer distXElements = new StringBuffer(
		        "Dist x elements:\t");
		for (final double distXEle : this.distXElements) {
			distXElements.append(distXEle);
			distXElements.append("\t");
		}
		distXElements.append("\n");
		final StringBuffer genYElements = new StringBuffer(
		        "Generated y elements:\t");
		for (final double genYEle : this.genYElements) {
			genYElements.append(genYEle);
			genYElements.append("\t");
		}
		genYElements.append("\n");
		final StringBuffer comYElements = new StringBuffer(
		        "computed y elements:\t");
		for (final double comYEle : this.comYElements) {
			comYElements.append(comYEle);
			comYElements.append("\t");
		}
		comYElements.append("\n");
		final StringBuffer distYElements = new StringBuffer(
		        "Dist y elements:\t");
		for (final double distYEle : this.distYElements) {
			distYElements.append(distYEle);
			distYElements.append("\t");
		}
		distYElements.append("\n");
		bw.write(genXElements.toString());
		bw.write(comXElements.toString());
		bw.write(distXElements.toString());
		bw.write(genYElements.toString());
		bw.write(comYElements.toString());
		bw.write(distYElements.toString());
		bw.write("Bias X:\t" + this.biasX + "\n");
		bw.write("Sigma X:\t" + this.sigmaX + "\n");
		bw.write("Bias Y:\t" + this.biasY + "\n");
		bw.write("Sigma Y:\t" + this.sigmaY + "\n");
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
