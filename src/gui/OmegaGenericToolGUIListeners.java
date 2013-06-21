package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
					System.out.println(parentDirectory.getName());
					if (!parentDirectory.isDirectory()
					        || parentDirectory.getName().toLowerCase().contains(".bin")) {
						continue;
					}

					for (final File imageDirectory : parentDirectory.listFiles()) {
						System.out.println(imageDirectory.getName());
						if (!imageDirectory.isDirectory()) {
							continue;
						}

						File snrResults = null;
						File framesFile = null;
						File trajsFile = null;
						for (final File file3 : imageDirectory.listFiles()) {
							final String framesFileName = file3.getName();
							if (!framesFileName.contains(OmegaGenericToolGUI.fileName1)) {
								continue;
							}
							System.out.println(framesFileName);
							String trajsFileName = framesFileName.replace(
							        OmegaGenericToolGUI.fileName1, "");
							final int index = trajsFileName.indexOf("_");
							final String snr = trajsFileName
							        .substring(index + 1);
							trajsFileName = OmegaGenericToolGUI.fileName2 + trajsFileName;
							framesFile = file3;
							trajsFile = new File(imageDirectory + "\\" + trajsFileName);

							snrResults = new File(parentDirectory.getPath()
							        + "\\toolsResults_" + snr + ".txt");

							if ((trajsFile != null) && (framesFile != null)
							        && (snrResults != null)) {
								final PTFilesAnalyzer mSNRfinder = new PTFilesAnalyzer(
								        imageDirectory);
								mSNRfinder.setGenerateTrajectories(false);
								try {
									mSNRfinder.analyzeFramesFile(framesFile);
									mSNRfinder
									        .analyzeTrajectoriesFile(trajsFile);

									mainGUI.appendSNRResult(mSNRfinder);
									mSNRfinder.appendResultsToFile(snrResults);
								} catch (final IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							} else {
								mainGUI.appendResultsText("Files PTFrames_* and/or PTTrajectories_* not found in folder "
								        + imageDirectory.getPath());
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

				        for (final File file1 : directory.listFiles()) {
					        System.out.println(file1.getName());
					        if (!file1.isDirectory()
					                || file1.getName().toLowerCase()
					                        .contains(".bin")) {
						        continue;
					        }

					        for (final File file2 : file1.listFiles()) {
						        System.out.println(file2.getName());
						        if (!file2.isDirectory()) {
							        continue;
						        }

						        File trajsFile = null;
						        for (final File file3 : file2.listFiles()) {
							        System.out.println(file3.getName());
							        if (file3.getName().contains(
							                OmegaGenericToolGUI.fileName2)) {
								        trajsFile = file3;
								        System.out.println("Set trajsFile");
							        }
						        }

						        if ((trajsFile != null)) {
							        final PTFilesAnalyzer mSNRfinder = new PTFilesAnalyzer(
							                file2);
							        mSNRfinder.setGenerateTrajectories(true);
							        try {
								        mSNRfinder
								                .analyzeTrajectoriesFile(trajsFile);
							        } catch (final IOException e) {
								        // TODO Auto-generated catch block
								        e.printStackTrace();
							        }
						        } else {
							        mainGUI.appendResultsText("Files PTTrajectories_* not found in folder "
							                + file2.getPath());
						        }
					        }
				        }
			        }
		        });
	}
}
