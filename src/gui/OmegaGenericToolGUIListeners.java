package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;

import core.PTFilesAnalyzer;

public class OmegaGenericToolGUIListeners {

	public static void addWorkingDirChooser(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getDirChooserButton().addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent evt) {
				final JFileChooser fc = mainGUI.getDirChooserDialog();
				fc.showOpenDialog(fc);
				if (fc.getSelectedFile() != null) {
					mainGUI.setNewCurrentDirLbl(fc.getSelectedFile().getPath());
				} else {
					mainGUI.setNewCurrentDirLbl(fc.getCurrentDirectory()
					        .getPath());
				}
			}
		});
	}

	public static void addComputeSNRData(final OmegaGenericToolGUI mainGUI) {
		mainGUI.getComputeSNRDataButt().addActionListener(new ActionListener() {
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

					File snrResults = null;
					for (final File imageDirectory : parentDirectory
					        .listFiles()) {
						if (!imageDirectory.isDirectory()) {
							continue;
						}
						mainGUI.appendResultsText("Dataset\t"
						        + imageDirectory.getName());
						File framesFile = null;
						File trajsFile = null;
						for (final File file : imageDirectory.listFiles()) {
							final String framesFileName = file.getName();
							if (imageDirectory
							        .getName()
							        .equals("appl.omega.ParticleTrackerPTCoproc_8_V3_PUSH_wx")) {
								System.out.println("File: " + framesFileName);
							}
							if (!framesFileName
							        .contains(OmegaGenericToolGUI.fileName1)) {
								continue;
							}
							System.out.println("File: " + framesFileName);
							String trajsFileName = framesFileName.replace(
							        OmegaGenericToolGUI.fileName1, "");
							final int index = trajsFileName.lastIndexOf("_");
							final String snr = trajsFileName
							        .substring(index + 1);
							trajsFileName = OmegaGenericToolGUI.fileName2
							        + trajsFileName;
							framesFile = file;
							trajsFile = new File(imageDirectory + "\\"
							        + trajsFileName);

							snrResults = new File(parentDirectory.getPath()
							        + "\\toolsResults_" + snr + ".txt");

							try {
								final FileWriter fw = new FileWriter(
								        snrResults, true);
								final BufferedWriter bw = new BufferedWriter(fw);
								bw.write("Dataset\t" + imageDirectory.getName()
								        + "\n");
								bw.close();
								fw.close();
							} catch (final IOException ex) {
								// TODO Auto-generated catch block
								ex.printStackTrace();
							}

							if ((trajsFile != null) && (framesFile != null)
							        && (snrResults != null)) {
								final PTFilesAnalyzer mSNRfinder = new PTFilesAnalyzer(
								        imageDirectory);
								try {
									mSNRfinder.computeSNRData(framesFile,
									        trajsFile, snrResults);
									mainGUI.appendSNRResult(mSNRfinder);
								} catch (final IOException ex) {
									// TODO Auto-generated catch block
									ex.printStackTrace();
								}
							} else {
								mainGUI.appendResultsText("Files PTFrames_* and/or PTTrajectories_* not found in folder "
								        + imageDirectory.getPath());
								mainGUI.appendResultsText("####################################################");
								try {
									final FileWriter fw = new FileWriter(
									        snrResults, true);
									final BufferedWriter bw = new BufferedWriter(
									        fw);
									bw.write("Files PTFrames_* and/or PTTrajectories_* not found in this folder");
									bw.write("####################################################\n");
									bw.close();
									fw.close();
								} catch (final IOException ex) {
									// TODO Auto-generated catch block
									ex.printStackTrace();
								}
							}
							trajsFile = null;
							framesFile = null;
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
							                OmegaGenericToolGUI.fileName2)) {
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
							        }
						        } else {
							        mainGUI.appendResultsText("Files PTTrajectories_* not found in folder "
							                + imageDirectory.getPath());
						        }
					        }
				        }
			        }
		        });
	}
}
