package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;

import core.DAndSMSSDataAggregator;
import core.DAndSMSSFilesConsolidator;
import core.DAndSMSSMeansCalculator;
import core.DValueCalculator;
import core.ImageComparator;
import core.P2PDistanceCalculator;
import core.PTFilesAnalyzer;
import core.SDBatchRunnerSingleImage;
import core.SDSLBatchRunnerSingleFolder;
import core.SingleTrajectoryGenerator;
import core.TrajDataAggregator;
import core.TrajectoriesAnalyzerAndFilter;
import edu.umassmed.omega.plSbalzariniPlugin.PLConstants;

public class OmegaGenericToolGUIListeners {
	
	public static void addCalculateDOnTrajData(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getCalculateDOnTrajDataButt().addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent arg0) {
						final File workingDir = mainGUI.getWorkingDirectory();
						
						final double filter = mainGUI.getTrajFilter();
						
						final DValueCalculator dvc = new DValueCalculator(
								workingDir, filter, mainGUI);
						final Thread t = new Thread(dvc);
						t.start();
					}
				});
	}
	
	public static void addAggregateTrajData(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getAggregateTrajDataButt().addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent arg0) {
						final File workingDir = mainGUI.getWorkingDirectory();
						final TrajDataAggregator tda = new TrajDataAggregator(
								workingDir, mainGUI);
						final Thread t = new Thread(tda);
						t.start();
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
						final boolean analyzeImageLog = mainGUI
								.isAnalyzeImageLog();
						final ImageComparator imageComparison = new ImageComparator(
								workingDir, compareDir, true, analyzeImageLog,
								mainGUI);
						
						final Thread t = new Thread(imageComparison);
						t.start();
					}
				});
	}
	
	public static void addCompareImages(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getCompareButt().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final File workingDir = mainGUI.getWorkingDirectory();
				final File compareDir = mainGUI.getCompareDirectory();
				final boolean analyzeImageLog = mainGUI.isAnalyzeImageLog();
				final ImageComparator imageComparison = new ImageComparator(
						workingDir, compareDir, false, analyzeImageLog, mainGUI);
				
				final Thread t = new Thread(imageComparison);
				t.start();
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
						final File workingDir = mainGUI.getWorkingDirectory();
						final boolean analyzeFilteredTraj = mainGUI
								.isAnalyzeFilteredTraj();
						final boolean analyzeMergedTraj = mainGUI
								.isAnalyzeMergedTraj();
						
						final P2PDistanceCalculator calc = new P2PDistanceCalculator(
								workingDir, analyzeFilteredTraj,
								analyzeMergedTraj, mainGUI);
						
						final Thread t = new Thread(calc);
						t.start();
					}
				});
	}
	
	public static void addGenerateSingleTrajFiles(
			final OmegaGenericToolGUI mainGUI) {
		mainGUI.getGenerateSingleTrajFilesButt().addActionListener(
				new ActionListener() {
					
					@Override
					public void actionPerformed(final ActionEvent evt) {
						final File workingDir = mainGUI.getWorkingDirectory();
						final boolean analyzeFilteredTraj = mainGUI
								.isAnalyzeFilteredTraj();
						final boolean analyzeMergedTraj = mainGUI
								.isAnalyzeMergedTraj();
						
						final SingleTrajectoryGenerator stg = new SingleTrajectoryGenerator(
								workingDir, analyzeFilteredTraj,
								analyzeMergedTraj, mainGUI);
						
						final Thread t = new Thread(stg);
						t.start();
					}
				});
	}
	
	public static void addAnalyzeAndFilterTrajectories(
			final OmegaGenericToolGUI mainGUI) {
		mainGUI.getAnalyzeAndFilterTrajButt().addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent evt) {
						final File workingDir = mainGUI.getWorkingDirectory();
						
						final double filter = mainGUI.getTrajFilter();
						final boolean analysisOnly = mainGUI.isAnalyzeOnly();
						final boolean mergeTraj = mainGUI.isMergeTraj();
						final boolean analyzeFilteredTraj = mainGUI
								.isAnalyzeFilteredTraj();
						final boolean analyzeMergedTraj = mainGUI
								.isAnalyzeMergedTraj();
						
						final TrajectoriesAnalyzerAndFilter TAF = new TrajectoriesAnalyzerAndFilter(
								workingDir, filter, analysisOnly, mergeTraj,
								analyzeFilteredTraj, analyzeMergedTraj, mainGUI);
						
						final Thread t = new Thread(TAF);
						t.start();
					}
				});
	}
	
	public static void addComputeSMSSAndDMeans(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getComputeSMSSAndDMeansButt().addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent evt) {
						final File workingDir = mainGUI.getWorkingDirectory();
						
						final DAndSMSSMeansCalculator da = new DAndSMSSMeansCalculator(
								workingDir, mainGUI);
						
						final Thread t = new Thread(da);
						t.start();
					}
				});
	}
	
	public static void addAggregateSMSSAndDData(
			final OmegaGenericToolGUI mainGUI) {
		mainGUI.getAggregateSMSSAndDDataButt().addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent evt) {
						final File workingDir = mainGUI.getWorkingDirectory();
						
						final DAndSMSSDataAggregator da = new DAndSMSSDataAggregator(
								workingDir, mainGUI);
						
						final Thread t = new Thread(da);
						t.start();
					}
				});
	}

	public static void addConsolidateSMSSAndDFiles(
			final OmegaGenericToolGUI mainGUI) {
		mainGUI.getConsolidateSMSSAndDFilesButt().addActionListener(
				new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent evt) {
						final File workingDir = mainGUI.getWorkingDirectory();
						
						final DAndSMSSFilesConsolidator fc = new DAndSMSSFilesConsolidator(
								workingDir, mainGUI);
						
						final Thread t = new Thread(fc);
						t.start();
					}
				});
	}
	
	public static void addRunMosaicPD(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getRunMosaicPDButt().addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent evt) {
				final File workingDir = mainGUI.getWorkingDirectory();
				
				final boolean isRunningSingleImage = mainGUI
						.isPDRunningSingleImage();
				
				final Integer radius = 3;
				final Double cutoff = 0.35;
				final Float percentile = (float) 1.55 / 100;
				final Float threshold = (float) 1.55;
				final Boolean percAbs = false;
				final Integer z = 0, c = 0;
				final Float displacement = 3f;
				final Integer linkrange = 5;
				final String movType = PLConstants.PARAM_MOVTYPE_BROWNIAN;
				final Float objectFeature = 1f;
				final Float dynamics = 1f;
				final String optimizer = PLConstants.PARAM_OPTIMIZER_GREEDY;
				final Integer minLength = 0;
				
				if (isRunningSingleImage) {
					final SDBatchRunnerSingleImage runner = new SDBatchRunnerSingleImage(
							mainGUI, workingDir, radius, cutoff, percentile,
							threshold, percAbs, c, z);
					final Thread t = new Thread(runner);
					t.start();
				} else {
					// final SDBatchRunnerSingleFolder runner = new
					// SDBatchRunnerSingleFolder(
					// mainGUI, workingDir, radius, cutoff, percentile,
					// threshold, percAbs, c, z);
					final SDSLBatchRunnerSingleFolder runner = new SDSLBatchRunnerSingleFolder(
							mainGUI, workingDir, radius, cutoff, percentile,
							threshold, percAbs, c, z, displacement, linkrange,
							movType, objectFeature, dynamics, optimizer,
							minLength);
					final Thread t = new Thread(runner);
					t.start();
				}
			}
		});
	}
}
