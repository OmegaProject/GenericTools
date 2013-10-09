package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import core.ImageComparator;
import core.P2PDistanceCalculator;
import core.PTFilesAnalyzer;
import core.TrajDataAggregator;

public class OmegaGenericToolGUIListeners {
	public static void addAggregateTrajData(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getAggregateTrajDataButt().addActionListener(
		        new ActionListener() {
			        @Override
			        public void actionPerformed(final ActionEvent arg0) {
				        final File directory = mainGUI.getWorkingDirectory();
				        final List<File> trajFiles = new ArrayList<File>();
				        final List<File> noisyTrajFiles = new ArrayList<File>();
				        final File resultsFile = new File(directory.getPath()
				                + "\\aggregateData.txt");

				        for (final File trajFile : directory.listFiles()) {
					        final String trackFileName = trajFile.getName();
					        if (!trackFileName.contains(".out")
					                || trackFileName.contains("_noise")) {
						        continue;
					        }

					        final int index = trackFileName.lastIndexOf(".");
					        String noisyTrackFileName = trackFileName
					                .substring(0, index);
					        noisyTrackFileName += "_noise.out";
					        final File noisyTrajFile = new File(directory
					                + "\\" + noisyTrackFileName);

					        trajFiles.add(trajFile);
					        noisyTrajFiles.add(noisyTrajFile);
				        }
				        final TrajDataAggregator tda = new TrajDataAggregator();
				        try {

					        tda.aggregateTrajData(trajFiles.toArray(),
					                noisyTrajFiles.toArray(), resultsFile);
				        } catch (final IOException e) {
					        // TODO Auto-generated catch block
					        e.printStackTrace();
				        }
			        }
		        });
	}

	public static void addCompareDirChooser(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getCompareDirChooserButton().addActionListener(
		        new ActionListener() {
			        @Override
			        public void actionPerformed(final ActionEvent arg0) {
				        final JFileChooser fc = mainGUI
				                .getCompareDirChooserDialog();
				        fc.showOpenDialog(fc);
				        if (fc.getSelectedFile() != null) {
					        mainGUI.setNewCompareCurrentDirLbl(fc
					                .getSelectedFile().getPath());
				        } else {
					        mainGUI.setNewCompareCurrentDirLbl(fc
					                .getCurrentDirectory().getPath());
				        }
			        }
		        });
	}

	public static void addCompareImagesWithDistr(
	        final OmegaGenericToolGUI mainGUI) {
		mainGUI.getCompareWithDistriButt().addActionListener(
		        new ActionListener() {
			        @Override
			        public void actionPerformed(final ActionEvent e) {
				        final File workingDir = mainGUI.getWorkingDirectory();
				        final File compareDir = mainGUI.getCompareDirectory();
				        final ImageComparator mSNRfinder = new ImageComparator(
				                workingDir, compareDir);

				        try {
					        mSNRfinder.compareImages(true);
				        } catch (final IOException e1) {
					        // TODO Auto-generated catch block
					        e1.printStackTrace();
				        }

				        final Integer numberOfFrames = mSNRfinder
				                .getNumberOfFrames();
				        final Integer numberOfDifferentPixels = mSNRfinder
				                .getNumberOfDifferentPixels();
				        final Integer totalNumberOfPixels = mSNRfinder
				                .getTotalNumberOfPixels();
				        final Integer minDifference = mSNRfinder
				                .getMinDifference();
				        final Integer maxDifference = mSNRfinder
				                .getMaxDifference();
				        final Double meanDifference = mSNRfinder
				                .getMeanDifference();
				        final Double meanDifferentPixelsPerImage = mSNRfinder
				                .getMeanDifferentPixelPerImage();

				        mainGUI.appendResultsText("Comparison results with distr:");
				        mainGUI.appendResultsText("Frames:\t" + numberOfFrames);
				        mainGUI.appendResultsText("Different pixels:\t"
				                + numberOfDifferentPixels);
				        mainGUI.appendResultsText("Total pixels:\t"
				                + totalNumberOfPixels);
				        mainGUI.appendResultsText("Min difference:\t"
				                + minDifference);
				        mainGUI.appendResultsText("Mean difference:\t"
				                + meanDifference);
				        mainGUI.appendResultsText("Max difference:\t"
				                + maxDifference);
				        mainGUI.appendResultsText("Mean different pixels per image:\t"
				                + meanDifferentPixelsPerImage);
				        mainGUI.appendResultsText("###################");
			        }
		        });
	}

	public static void addCompareImages(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getCompareButt().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final File workingDir = mainGUI.getWorkingDirectory();
				final File compareDir = mainGUI.getCompareDirectory();
				final ImageComparator mSNRfinder = new ImageComparator(
				        workingDir, compareDir);

				try {
					mSNRfinder.compareImages(false);
				} catch (final IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				final Integer numberOfFrames = mSNRfinder.getNumberOfFrames();
				final Integer numberOfDifferentPixels = mSNRfinder
				        .getNumberOfDifferentPixels();
				final Integer totalNumberOfPixels = mSNRfinder
				        .getTotalNumberOfPixels();
				final Integer minDifference = mSNRfinder.getMinDifference();
				final Integer maxDifference = mSNRfinder.getMaxDifference();
				final Double meanDifference = mSNRfinder.getMeanDifference();
				final Double meanDifferentPixelsPerImage = mSNRfinder
				        .getMeanDifferentPixelPerImage();

				mainGUI.appendResultsText("Comparison results:");
				mainGUI.appendResultsText("Frames:\t" + numberOfFrames);
				mainGUI.appendResultsText("Different pixels:\t"
				        + numberOfDifferentPixels);
				mainGUI.appendResultsText("Total pixels:\t"
				        + totalNumberOfPixels);
				mainGUI.appendResultsText("Min difference:\t" + minDifference);
				mainGUI.appendResultsText("Mean difference:\t" + meanDifference);
				mainGUI.appendResultsText("Max difference:\t" + maxDifference);
				mainGUI.appendResultsText("Mean different pixels per image:\t"
				        + meanDifferentPixelsPerImage);
				mainGUI.appendResultsText("###################");
			}
		});
	}

	public static void addWorkingDirChooser(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getWorkingDirChooserButton().addActionListener(
		        new ActionListener() {

			        @Override
			        public void actionPerformed(final ActionEvent evt) {
				        final JFileChooser fc = mainGUI
				                .getWorkingDirChooserDialog();
				        fc.showOpenDialog(fc);
				        if (fc.getSelectedFile() != null) {
					        mainGUI.setNewWorkingCurrentDirLbl(fc
					                .getSelectedFile().getPath());
				        } else {
					        mainGUI.setNewWorkingCurrentDirLbl(fc
					                .getCurrentDirectory().getPath());
				        }
			        }
		        });
	}

	public static void addComputeMeanSNRData(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getComputeMeanSNRDataButt().addActionListener(
		        new ActionListener() {
			        @Override
			        public void actionPerformed(final ActionEvent evt) {
				        final File directory = mainGUI.getWorkingDirectory();

				        for (final File parentDirectory : directory.listFiles()) {
					        if (!parentDirectory.isDirectory()
					                || parentDirectory.getName().toLowerCase()
					                        .contains(".bin")) {
						        continue;
					        }

					        mainGUI.appendResultsText("Folder\t"
					                + parentDirectory.getName());

					        for (final File imageDirectory : parentDirectory
					                .listFiles()) {
						        if (!imageDirectory.isDirectory()) {
							        continue;
						        }
						        final Map<Double, List<StringBuffer[]>> map = new HashMap<Double, List<StringBuffer[]>>();

						        mainGUI.appendResultsText("Dataset\t"
						                + imageDirectory.getName());
						        File framesFile = null;
						        File trajsFile = null;
						        for (final File file : imageDirectory
						                .listFiles()) {
							        final String framesFileName = file
							                .getName();
							        if (imageDirectory
							                .getName()
							                .equals("appl.omega.ParticleTrackerPTCoproc_8_V3_PUSH_wx")) {
								        System.out.println("File: "
								                + framesFileName);
							        }
							        if (!framesFileName
							                .contains(PTFilesAnalyzer.fileName1)) {
								        continue;
							        }
							        System.out.println("File: "
							                + framesFileName);
							        String trajsFileName = framesFileName
							                .replace(PTFilesAnalyzer.fileName1,
							                        "");
							        final int index = trajsFileName
							                .lastIndexOf("_");
							        trajsFileName.substring(index + 1);
							        trajsFileName = PTFilesAnalyzer.fileName2
							                + trajsFileName;
							        framesFile = file;
							        trajsFile = new File(imageDirectory + "\\"
							                + trajsFileName);

							        if ((trajsFile != null)
							                && (framesFile != null)) {
								        final PTFilesAnalyzer mSNRfinder = new PTFilesAnalyzer(
								                imageDirectory);
								        try {
									        mSNRfinder
									                .computeSNRDataAndAppendToList(
									                        framesFile,
									                        trajsFile, map);
									        mainGUI.appendSNRResult(mSNRfinder);
								        } catch (final IOException ex) {
									        // TODO Auto-generated catch block
									        ex.printStackTrace();
								        }
							        } else {
								        mainGUI.appendResultsText("Files PTFrames_* and/or PTTrajectories_* not found in folder "
								                + imageDirectory.getPath());
								        mainGUI.appendResultsText("####################################################");
							        }
							        trajsFile = null;
							        framesFile = null;
						        }
						        int tot = -1;
						        for (final Double d : map.keySet()) {
							        final List<StringBuffer[]> list = map
							                .get(d);
							        tot = list.size();
							        break;
						        }

						        try {
							        final File snrResults = new File(
							                parentDirectory.getPath()
							                        + "\\toolsResults_"
							                        + ".txt");
							        final FileWriter fw = new FileWriter(
							                snrResults, true);
							        final BufferedWriter bw = new BufferedWriter(
							                fw);
							        for (int i = 0; i < tot; i++) {
								        for (int k = 0; k < 11; k++) {
									        BigDecimal z = new BigDecimal(0);
									        for (; z.compareTo(new BigDecimal(
									                1.0)) == -1; z = z
									                .add(new BigDecimal(0.01))) {
										        z = z.setScale(
										                2,
										                BigDecimal.ROUND_HALF_UP);
										        final List<StringBuffer[]> list = map
										                .get(z.doubleValue());
										        if (list == null) {
											        continue;
										        }
										        final StringBuffer sb = list
										                .get(i)[k];
										        bw.write(sb.toString());
									        }
									        bw.write("\n");
								        }
								        bw.write("\n");
							        }
							        bw.close();
							        fw.close();
						        } catch (final IOException e) {
							        // TODO Auto-generated catch
							        // block
							        e.printStackTrace();
						        }
					        }
				        }
			        }
		        });
	}

	public static void addComputeP2PDistance(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getComputeP2PDistanceButt().addActionListener(
		        new ActionListener() {
			        @Override
			        public void actionPerformed(final ActionEvent evt) {
				        final File directory = mainGUI.getWorkingDirectory();

				        for (final File parentDirectory : directory.listFiles()) {
					        if (!parentDirectory.isDirectory()
					                || parentDirectory.getName().toLowerCase()
					                        .contains(".bin")) {
						        continue;
					        }

					        mainGUI.appendResultsText("Folder\t"
					                + parentDirectory.getName());

					        for (final File imageDirectory : parentDirectory
					                .listFiles()) {
						        if (!imageDirectory.isDirectory()) {
							        continue;
						        }
						        new HashMap<Double, List<StringBuffer[]>>();

						        mainGUI.appendResultsText("Dataset\t"
						                + imageDirectory.getName());
						        final File logsDir = new File(imageDirectory
						                .getPath() + "/logs");
						        File trajectories = null;
						        final List<File> logs = new ArrayList<File>();
						        for (final File file : logsDir.listFiles()) {
							        final String framesFileName = file
							                .getName();
							        if (!framesFileName
							                .contains(P2PDistanceCalculator.fileName1)
							                && !framesFileName
							                        .contains(P2PDistanceCalculator.fileName2)) {
								        continue;
							        }

							        System.out.println("File: "
							                + framesFileName);
							        if (framesFileName
							                .contains(P2PDistanceCalculator.fileName2)) {
								        // GET generated positions
								        logs.add(file);
							        } else {
								        trajectories = file;
								        // GET computed positions
							        }
						        }
						        final P2PDistanceCalculator p2pCalc = new P2PDistanceCalculator(
						                trajectories, logs);
						        // feed particles list to a computer and
						        // computer distances

						        try {
							        p2pCalc.computeBiasAndSigma();
							        p2pCalc.writeResultsFile(logsDir);
						        } catch (final IOException e) {
							        // TODO Auto-generated catch
							        // block
							        e.printStackTrace();
						        }
					        }
				        }
			        }
		        });
	}

	public static void addGenerateTrajFiles(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getGenerateTrajFilesButt().addActionListener(
		        new ActionListener() {

			        @Override
			        public void actionPerformed(final ActionEvent evt) {

				        final File directory = mainGUI.getWorkingDirectory();

				        for (final File parentDirectory : directory.listFiles()) {
					        if (!parentDirectory.isDirectory()
					                || parentDirectory.getName().toLowerCase()
					                        .contains(".bin")) {
						        continue;
					        }

					        mainGUI.appendResultsText("Folder\t"
					                + parentDirectory.getName());
					        for (final File imageDirectory : parentDirectory
					                .listFiles()) {
						        if (!imageDirectory.isDirectory()) {
							        continue;
						        }
						        mainGUI.appendResultsText("Dataset\t"
						                + imageDirectory.getName());
						        File trajsFile = null;
						        for (final File file : imageDirectory
						                .listFiles()) {
							        if (file.getName().contains(
							                PTFilesAnalyzer.fileName2)) {
								        trajsFile = file;
							        }
						        }

						        if ((trajsFile != null)) {
							        final PTFilesAnalyzer mSNRfinder = new PTFilesAnalyzer(
							                imageDirectory);
							        try {
								        mSNRfinder
								                .generateSingleTrajectories(trajsFile);
							        } catch (final IOException ex) {
								        // TODO Auto-generated catch block
								        ex.printStackTrace();
								        mainGUI.appendResultsText(ex
								                .getMessage());
							        }

							        for (final String error : mSNRfinder.errors) {
								        mainGUI.appendResultsText(error);
							        }
						        } else {
							        mainGUI.appendResultsText("Files PTTrajectories_* not found in folder "
							                + imageDirectory.getPath());
						        }
					        }
				        }
				        mainGUI.appendResultsText("Finished");
			        }
		        });
	}
}
