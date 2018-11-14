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

public class P2PDistanceCalculator_MosaicCOutput_Displacement implements
		Runnable {
	
	int precision = 15;
	
	// public final static String fileName1 = "PT_Trajectories_";
	// public final static String fileName1 = "SDOutput.txt";
	// public final static String fileName1 =
	// "FilterPTTrackerReport_Results.txt";
	public final String fileName1 = "Standard_1000_";
	public final static String fileName2 = "ParticleLog_";
	
	private final static String ident1 = "%	 3rd column: y coordinate left-right from 0 to 299";
	private final static String ident2 = "Particles of frame: ";
	private final static String ident3 = "Particle";
	private final static String ident4 = "trajectories found";
	
	private final File inputDir, outputDir;
	private File trajFile;
	private final List<File> logs;
	private final Map<Integer, Map<Integer, MyPoint>> tracks;
	private final Map<Integer, List<Integer>> tracksFrames;
	private final Map<String, Map<Integer, List<MyPoint>>> computedPoints;
	private final Map<String, Map<Integer, List<MyPoint>>> generatedPoints;
	// private final Map<String, BigDecimal> genXElements, genYElements;
	// private final Map<String, BigDecimal> comXElements, comYElements;
	// private final Map<String, BigDecimal> distXElements, distYElements;
	// private final Map<String, BigDecimal> distABSXElements, distABSYElements;

	final Map<Integer, Map<Integer, BigDecimal>> distsXMap, diffsXMap,
			distsXAbsMap, diffsXAbs1Map, diffsXAbs2Map, diffsXAbsMap;
	final Map<Integer, Map<Integer, BigDecimal>> distsYMap, diffsYMap,
			distsYAbsMap, diffsYAbs1Map, diffsYAbs2Map, diffsYAbsMap;
	
	private final Map<Integer, Double> biasesX, biasesY, sigmasX, sigmasY;
	private Double biasX, biasY, sigmaX, sigmaY;
	private Double biasX_abs, biasY_abs, sigmaX_abs, sigmaY_abs;
	private Double biasX_abs1, biasY_abs1, sigmaX_abs1, sigmaY_abs1;
	private Double biasX_abs2, biasY_abs2, sigmaX_abs2, sigmaY_abs2;
	// private double biasABSX, biasABSY, sigmaABSX, sigmaABSY;
	// private double euclidianBias, euclidianSigma;
	
	boolean analyzeFiltered, analyzeMerged;
	
	private final StringBuffer errorLog;
	
	private final OmegaGenericToolGUI gui;
	
	public P2PDistanceCalculator_MosaicCOutput_Displacement(
			final File inputDir, final File outputDir,
			final boolean analyzeFiltered, final boolean analyzeMerged,
			final OmegaGenericToolGUI gui) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.trajFile = null;
		this.logs = new ArrayList<File>();
		this.tracks = new HashMap<Integer, Map<Integer, MyPoint>>();
		this.tracksFrames = new HashMap<Integer, List<Integer>>();
		this.computedPoints = new HashMap<String, Map<Integer, List<MyPoint>>>();
		this.generatedPoints = new HashMap<String, Map<Integer, List<MyPoint>>>();

		this.distsXMap = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.diffsXMap = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.distsXAbsMap = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.diffsXAbs1Map = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.diffsXAbs2Map = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.diffsXAbsMap = new HashMap<Integer, Map<Integer, BigDecimal>>();
		
		this.distsYMap = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.diffsYMap = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.distsYAbsMap = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.diffsYAbs1Map = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.diffsYAbs2Map = new HashMap<Integer, Map<Integer, BigDecimal>>();
		this.diffsYAbsMap = new HashMap<Integer, Map<Integer, BigDecimal>>();

		this.biasX = 0.0;
		this.biasY = 0.0;
		this.sigmaX = 0.0;
		this.sigmaY = 0.0;
		
		this.biasX_abs = 0.0;
		this.biasY_abs = 0.0;
		this.sigmaX_abs = 0.0;
		this.sigmaY_abs = 0.0;
		
		this.biasX_abs1 = 0.0;
		this.biasY_abs1 = 0.0;
		this.sigmaX_abs1 = 0.0;
		this.sigmaY_abs1 = 0.0;
		
		this.biasX_abs2 = 0.0;
		this.biasY_abs2 = 0.0;
		this.sigmaX_abs2 = 0.0;
		this.sigmaY_abs2 = 0.0;
		
		this.biasesX = new HashMap<Integer, Double>();
		this.biasesY = new HashMap<Integer, Double>();
		this.sigmasX = new HashMap<Integer, Double>();
		this.sigmasY = new HashMap<Integer, Double>();
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
					P2PDistanceCalculator_MosaicCOutput_Displacement.this.gui
							.appendOutput(update);
					
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
		int tracksCounter = 0;
		boolean trackingStarted = false;
		while (line != null) {
			if (line.contains(P2PDistanceCalculator_MosaicCOutput_Displacement.ident1)) {
				trackingStarted = true;
				line = br.readLine();
				if (line.isEmpty()) {
					line = br.readLine();
				}
				continue;
			} else if (line
					.contains(P2PDistanceCalculator_MosaicCOutput_Displacement.ident4)) {
				trackingStarted = false;
			} else if (trackingStarted && line.isEmpty()) {
				tracksCounter++;
			} else if (trackingStarted) {
				String subString1, val;
				final List<String> values = new ArrayList<String>();
				int indexTab = line.indexOf(" ");
				if (hasTrackTag && ((indexTab != -1) && (indexTab != 0))) {
					val = line.substring(0, indexTab);
					values.add(val);
				}
				subString1 = line;
				while (indexTab != -1) {
					if (subString1.startsWith(" ")) {
						subString1 = subString1.substring(indexTab + 1);
						indexTab = subString1.indexOf(" ");
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
					indexTab = subString1.indexOf(" ");
				}
				final int frame = Integer.valueOf(values.get(0));// - 1;
				final double x = Double.valueOf(values.get(2));// - .5;
				final double y = Double.valueOf(values.get(1));// - .5;
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
				Map<Integer, MyPoint> track;
				if (this.tracks.containsKey(tracksCounter)) {
					track = this.tracks.get(tracksCounter);
				} else {
					track = new HashMap<Integer, MyPoint>();
				}
				List<Integer> tracksFrame;
				if (this.tracksFrames.containsKey(tracksCounter)) {
					tracksFrame = this.tracksFrames.get(tracksCounter);
				} else {
					tracksFrame = new ArrayList<Integer>();
				}
				tracksFrame.add(frame);
				this.tracksFrames.put(tracksCounter, tracksFrame);
				track.put(frame, particle);
				this.tracks.put(tracksCounter, track);
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
			final String frameIndex = line
					.replace(
							P2PDistanceCalculator_MosaicCOutput_Displacement.ident2,
							"");
			final int frame = Integer.valueOf(frameIndex) - 1;
			line = br.readLine();
			while (line != null) {
				String val;
				line = line.replaceAll(" ", "\t");
				if (line.contains(P2PDistanceCalculator_MosaicCOutput_Displacement.ident3)) {
					String subString1 = line
							.replace(
									P2PDistanceCalculator_MosaicCOutput_Displacement.ident3,
									"");
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
		final BigDecimal gt = new BigDecimal(0.27);
		for (int t = 0; t < this.tracks.keySet().size(); t++) {
			final List<Integer> trackFrames = this.tracksFrames.get(t);
			if (trackFrames.size() == 1) {
				continue;
			}
			Map<Integer, BigDecimal> distsX, diffsX, distsY, diffsY;
			Map<Integer, BigDecimal> distsXAbs, diffsXAbs, diffsXAbs1, diffsXAbs2, distsYAbs, diffsYAbs, diffsYAbs1, diffsYAbs2;
			if (this.distsXMap.containsKey(t)) {
				distsX = this.distsXMap.get(t);
				distsXAbs = this.distsXAbsMap.get(t);
			} else {
				distsX = new HashMap<Integer, BigDecimal>();
				distsXAbs = new HashMap<Integer, BigDecimal>();
			}
			if (this.diffsXMap.containsKey(t)) {
				diffsX = this.diffsXMap.get(t);
				diffsXAbs = this.diffsXAbsMap.get(t);
				diffsXAbs1 = this.diffsXAbs1Map.get(t);
				diffsXAbs2 = this.diffsXAbs2Map.get(t);
			} else {
				diffsX = new HashMap<Integer, BigDecimal>();
				diffsXAbs = new HashMap<Integer, BigDecimal>();
				diffsXAbs1 = new HashMap<Integer, BigDecimal>();
				diffsXAbs2 = new HashMap<Integer, BigDecimal>();
			}
			if (this.distsYMap.containsKey(t)) {
				distsY = this.distsYMap.get(t);
				distsYAbs = this.distsYAbsMap.get(t);
			} else {
				distsY = new HashMap<Integer, BigDecimal>();
				distsYAbs = new HashMap<Integer, BigDecimal>();
			}
			if (this.diffsYMap.containsKey(t)) {
				diffsY = this.diffsYMap.get(t);
				diffsYAbs = this.diffsYAbsMap.get(t);
				diffsYAbs1 = this.diffsXAbs1Map.get(t);
				diffsYAbs2 = this.diffsXAbs1Map.get(t);
			} else {
				diffsY = new HashMap<Integer, BigDecimal>();
				diffsYAbs = new HashMap<Integer, BigDecimal>();
				diffsYAbs1 = new HashMap<Integer, BigDecimal>();
				diffsYAbs2 = new HashMap<Integer, BigDecimal>();
			}
			final Map<Integer, MyPoint> track = this.tracks.get(t);
			int counter = 0;
			for (int i = 0; i < trackFrames.size(); i++) {
				final int index1 = trackFrames.get(i);
				if (trackFrames.size() <= (i + 1)) {
					continue;
				}
				final int index2 = trackFrames.get(i + 1);
				final int linkrange = index2 - index1;
				final MyPoint p1 = track.get(index1);
				final MyPoint p2 = track.get(index2);
				final BigDecimal distX = p2.xD.subtract(p1.xD);
				final BigDecimal distY = p2.yD.subtract(p1.yD);
				final Double proDistX = distX.doubleValue() / linkrange;
				final Double proDistY = distY.doubleValue() / linkrange;
				final BigDecimal proDistX_bd = new BigDecimal(proDistX);
				final BigDecimal proDistY_bd = new BigDecimal(proDistY);
				final BigDecimal proDistX_bd_abs = new BigDecimal(
						Math.abs(proDistX));
				final BigDecimal proDistY_bd_abs = new BigDecimal(
						Math.abs(proDistY));
				final BigDecimal diffX = gt.subtract(proDistX_bd);
				final BigDecimal diffY = gt.subtract(proDistY_bd);
				final BigDecimal diffX_abs1 = gt.subtract(proDistX_bd_abs);
				final BigDecimal diffY_abs1 = gt.subtract(proDistY_bd_abs);
				final BigDecimal diffX_abs = gt.subtract(proDistX_bd_abs).abs();
				final BigDecimal diffY_abs = gt.subtract(proDistY_bd_abs).abs();
				final BigDecimal diffX_abs2 = gt.subtract(proDistX_bd).abs();
				final BigDecimal diffY_abs2 = gt.subtract(proDistY_bd).abs();
				diffsX.put(counter, diffX);
				diffsY.put(counter, diffY);
				diffsXAbs.put(counter, diffX_abs);
				diffsYAbs.put(counter, diffY_abs);
				diffsXAbs1.put(counter, diffX_abs1);
				diffsYAbs1.put(counter, diffY_abs1);
				diffsXAbs2.put(counter, diffX_abs2);
				diffsYAbs2.put(counter, diffY_abs2);
				counter++;
			}
			this.distsXMap.put(t, distsX);
			this.diffsXMap.put(t, diffsX);
			this.distsYMap.put(t, distsY);
			this.diffsYMap.put(t, diffsY);
			this.distsXAbsMap.put(t, distsXAbs);
			this.diffsXAbsMap.put(t, diffsXAbs);
			this.distsYAbsMap.put(t, distsYAbs);
			this.diffsYAbsMap.put(t, diffsYAbs);
			this.diffsXAbs1Map.put(t, diffsXAbs1);
			this.diffsYAbs1Map.put(t, diffsYAbs1);
			this.diffsXAbs2Map.put(t, diffsXAbs2);
			this.diffsYAbs2Map.put(t, diffsYAbs2);
		}
	}
	
	@Override
	public void run() {
		for (final File dataset : this.inputDir.listFiles()) {
			if (dataset.isFile() || !dataset.getName().contains("Standard_")) {
				continue;
			}
			final File outputDir1 = new File(this.outputDir.getAbsolutePath()
					+ File.separator + dataset.getName());
			if (!outputDir1.exists()) {
				continue;
			}
			this.updateGUI("Dataset\t" + dataset.getName());
			this.errorLog.append("Dataset\t" + dataset.getName() + "\n");

			this.perDatasetReset();
			for (final File image : dataset.listFiles()) {
				if (image.isFile()) {
					continue;
				}
				final String imageTag = image.getName();
				final File outputDir2 = new File(outputDir1.getAbsolutePath()
						+ File.separator + image.getName());
				this.updateGUI("Image\t" + imageTag);
				this.errorLog.append("Image\t" + imageTag + "\n");
				final File logsDir = new File(image.getPath()
						+ File.separatorChar + "logs");
				if (!logsDir.exists()) {
					continue;
				}
				final File outputDir3 = new File(outputDir2.getAbsolutePath()
						+ File.separator + logsDir.getName());
				this.perImageReset();
				boolean abortFolder = false;

				if (outputDir3.exists()) {
					for (final File f : outputDir3.listFiles()) {
						boolean check = f.getName().contains(this.fileName1);
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
							if (f.getName().contains(this.fileName1)) {
								this.trajFile = f;
							}
						}
					}
				}
				for (final File f : logsDir.listFiles()) {
					if (f.getName()
							.contains(
									P2PDistanceCalculator_MosaicCOutput_Displacement.fileName2)) {
						this.logs.add(f);
					}
				}
				if (abortFolder) {
					continue;
				}
				try {
					boolean hasTrackTag = true;
					if (this.fileName1.contains("SDOutput")
							|| this.fileName1.contains("SD_Output")
							|| this.fileName1.contains("SDOutputSingle")
							|| this.fileName1.contains("SD_OutputSingle")
							|| this.fileName1.contains("FilterMosaicReport")
							|| this.fileName1.contains("FilterPTTrackerReport")
							|| this.fileName1.contains("FilterSDReport")
							|| this.fileName1.contains("Standard_")) {
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
				this.writeResultsFile(outputDir1);
				this.writeLogFile(outputDir1);
			} catch (final IOException ex) {
				ex.printStackTrace();
			}
		}
		this.updateGUI("***P2P COMPLETED***");
	}
	
	private void computeBiasAndSigma() {
		final List<BigDecimal> allDiffX = new ArrayList<BigDecimal>();
		final List<BigDecimal> allDiffY = new ArrayList<BigDecimal>();
		final List<BigDecimal> allDiffX_abs = new ArrayList<BigDecimal>();
		final List<BigDecimal> allDiffY_abs = new ArrayList<BigDecimal>();
		final List<BigDecimal> allDiffX_abs1 = new ArrayList<BigDecimal>();
		final List<BigDecimal> allDiffY_abs1 = new ArrayList<BigDecimal>();
		final List<BigDecimal> allDiffX_abs2 = new ArrayList<BigDecimal>();
		final List<BigDecimal> allDiffY_abs2 = new ArrayList<BigDecimal>();
		for (final Integer track : this.tracksFrames.keySet()) {
			final Map<Integer, BigDecimal> diffsX = this.diffsXMap.get(track);
			final Map<Integer, BigDecimal> diffsY = this.diffsYMap.get(track);
			final Map<Integer, BigDecimal> diffsXAbs = this.diffsXAbsMap
					.get(track);
			final Map<Integer, BigDecimal> diffsYAbs = this.diffsYAbsMap
					.get(track);
			final Map<Integer, BigDecimal> diffsXAbs1 = this.diffsXAbs1Map
					.get(track);
			final Map<Integer, BigDecimal> diffsYAbs1 = this.diffsYAbs1Map
					.get(track);
			final Map<Integer, BigDecimal> diffsXAbs2 = this.diffsXAbs2Map
					.get(track);
			final Map<Integer, BigDecimal> diffsYAbs2 = this.diffsYAbs2Map
					.get(track);
			if ((diffsX == null) || (diffsY == null)) {
				continue;
			}
			for (final Integer frame : diffsX.keySet()) {
				final BigDecimal diffX = diffsX.get(frame);
				final BigDecimal diffY = diffsY.get(frame);
				final BigDecimal diffX_abs = diffsXAbs.get(frame);
				final BigDecimal diffY_abs = diffsYAbs.get(frame);
				final BigDecimal diffX_abs1 = diffsXAbs1.get(frame);
				final BigDecimal diffY_abs1 = diffsYAbs1.get(frame);
				final BigDecimal diffX_abs2 = diffsXAbs2.get(frame);
				final BigDecimal diffY_abs2 = diffsYAbs2.get(frame);
				allDiffX.add(diffX);
				allDiffY.add(diffY);
				allDiffX_abs.add(diffX_abs);
				allDiffY_abs.add(diffY_abs);
				allDiffX_abs1.add(diffX_abs1);
				allDiffY_abs1.add(diffY_abs1);
				allDiffX_abs2.add(diffX_abs2);
				allDiffY_abs2.add(diffY_abs2);
			}
		}
		final StatisticalCalculator statX = new StatisticalCalculator(allDiffX,
				this.precision);
		final StatisticalCalculator statY = new StatisticalCalculator(allDiffY,
				this.precision);
		final StatisticalCalculator statX_abs = new StatisticalCalculator(
				allDiffX_abs, this.precision);
		final StatisticalCalculator statY_abs = new StatisticalCalculator(
				allDiffY_abs, this.precision);
		final StatisticalCalculator statX_abs1 = new StatisticalCalculator(
				allDiffX_abs1, this.precision);
		final StatisticalCalculator statY_abs1 = new StatisticalCalculator(
				allDiffY_abs1, this.precision);
		final StatisticalCalculator statX_abs2 = new StatisticalCalculator(
				allDiffX_abs2, this.precision);
		final StatisticalCalculator statY_abs2 = new StatisticalCalculator(
				allDiffY_abs2, this.precision);
		this.biasX = statX.getDoubleMean();
		this.biasY = statY.getDoubleMean();
		this.sigmaX = statX.getDoubleStdDev();
		this.sigmaY = statY.getDoubleStdDev();
		
		this.biasX_abs = statX_abs.getDoubleMean();
		this.biasY_abs = statY_abs.getDoubleMean();
		this.sigmaX_abs = statX_abs.getDoubleStdDev();
		this.sigmaY_abs = statY_abs.getDoubleStdDev();
		
		this.biasX_abs1 = statX_abs1.getDoubleMean();
		this.biasY_abs1 = statY_abs1.getDoubleMean();
		this.sigmaX_abs1 = statX_abs1.getDoubleStdDev();
		this.sigmaY_abs1 = statY_abs1.getDoubleStdDev();
		
		this.biasX_abs2 = statX_abs2.getDoubleMean();
		this.biasY_abs2 = statY_abs2.getDoubleMean();
		this.sigmaX_abs2 = statX_abs2.getDoubleStdDev();
		this.sigmaY_abs2 = statY_abs2.getDoubleStdDev();
	}
	
	public void perImageReset() {
		this.trajFile = null;
		this.logs.clear();
		this.computedPoints.clear();
		this.generatedPoints.clear();
	}
	
	public void perDatasetReset() {
		this.diffsXMap.clear();
		this.distsXMap.clear();
		this.diffsYMap.clear();
		this.distsYMap.clear();
		this.diffsXAbsMap.clear();
		this.diffsXAbs1Map.clear();
		this.diffsXAbs2Map.clear();
		this.distsXAbsMap.clear();
		this.diffsYAbsMap.clear();
		this.diffsYAbs1Map.clear();
		this.diffsYAbs2Map.clear();
		this.distsYAbsMap.clear();
		this.biasesX.clear();
		this.biasesY.clear();
		this.sigmasX.clear();
		this.sigmasY.clear();
		this.biasX = 0.0;
		this.biasY = 0.0;
		this.sigmaX = 0.0;
		this.sigmaY = 0.0;
		this.biasX_abs = 0.0;
		this.biasY_abs = 0.0;
		this.sigmaX_abs = 0.0;
		this.sigmaY_abs = 0.0;
		this.biasX_abs1 = 0.0;
		this.biasY_abs1 = 0.0;
		this.sigmaX_abs1 = 0.0;
		this.sigmaY_abs1 = 0.0;
		this.biasX_abs2 = 0.0;
		this.biasY_abs2 = 0.0;
		this.sigmaX_abs2 = 0.0;
		this.sigmaY_abs2 = 0.0;
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
		final File resultsFile = new File(
				dir.getAbsolutePath()
						+ File.separatorChar
						+ "P2PDistanceCalculator_MosaicCOutput_Displacement_Results.txt");
		final File resultsFile2 = new File(
				dir.getAbsolutePath()
						+ File.separatorChar
						+ "P2PDistanceCalculator_MosaicCOutput_Displacement_Results_distrib.txt");
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		final FileWriter fw2 = new FileWriter(resultsFile2);
		final BufferedWriter bw2 = new BufferedWriter(fw2);
		final StringBuffer output = new StringBuffer();

		for (int t = 0; t < this.tracks.size(); t++) {

			final StringBuffer distribOutput = new StringBuffer();
			this.tracksFrames.get(t);
			final Map<Integer, BigDecimal> diffsX = this.diffsXMap.get(t);
			final Map<Integer, BigDecimal> diffsY = this.diffsYMap.get(t);
			if ((diffsX == null) || (diffsY == null)) {
				continue;
			}
			for (int i = 0; i < diffsX.size(); i++) {
				distribOutput.append(diffsX.get(i));
				distribOutput.append("\t");
				distribOutput.append(diffsY.get(i));
			}
			bw2.write(distribOutput.toString());
		}

		output.append("Bias X:\t");
		output.append(this.biasX);
		output.append("\n");
		output.append("Bias Y:\t");
		output.append(this.biasY);
		output.append("\n");
		output.append("Sigma X:\t");
		output.append(this.sigmaX);
		output.append("\n");
		output.append("Sigma Y:\t");
		output.append(this.sigmaY);
		output.append("\n");
		output.append("\n");
		output.append("Bias X Abs:\t");
		output.append(this.biasX_abs);
		output.append("\n");
		output.append("Bias Y Abs:\t");
		output.append(this.biasY_abs);
		output.append("\n");
		output.append("Sigma X Abs:\t");
		output.append(this.sigmaX_abs);
		output.append("\n");
		output.append("Sigma Y Abs:\t");
		output.append(this.sigmaY_abs);
		output.append("\n");
		output.append("\n");
		output.append("Bias X Abs 1:\t");
		output.append(this.biasX_abs1);
		output.append("\n");
		output.append("Bias Y Abs 1:\t");
		output.append(this.biasY_abs1);
		output.append("\n");
		output.append("Sigma X Abs 1:\t");
		output.append(this.sigmaX_abs1);
		output.append("\n");
		output.append("Sigma Y Abs 1:\t");
		output.append(this.sigmaY_abs1);
		output.append("\n");
		output.append("\n");
		output.append("Bias X Abs 2:\t");
		output.append(this.biasX_abs2);
		output.append("\n");
		output.append("Bias Y Abs 2:\t");
		output.append(this.biasY_abs2);
		output.append("\n");
		output.append("Sigma X Abs 2:\t");
		output.append(this.sigmaX_abs2);
		output.append("\n");
		output.append("Sigma Y Abs 2:\t");
		output.append(this.sigmaY_abs2);
		output.append("\n");
		bw.write(output.toString());

		bw.close();
		fw.close();
		bw2.close();
		fw2.close();
	}
}
