package core;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.plugin.FolderOpener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageJNormalization {

	private static final String path = "F:\\2017-11-27_Normalized_Images_for_Mosaic_C_algorithm";
	private static final String pathOutput = "F:\\2017-12-06_Normalized_Images_for_Mosaic_C_algorithm_results";

	public static void main(final String[] args) {
		final ImageJNormalization ijn = new ImageJNormalization();
		final File inputDir = new File(ImageJNormalization.path);
		ijn.normalize(inputDir);
	}

	private final ImageJ ij;

	public ImageJNormalization() {
		this.ij = new ImageJ();
	}

	public void normalize(final File inputDir) {
		final File[] snrDirectories = inputDir.listFiles();
		for (final File snrDir : snrDirectories) {
			if (!snrDir.isDirectory()) {
				continue;
			}
			final String snrDirName = snrDir.getName();
			final File[] cases = snrDir.listFiles();
			for (final File singleCase : cases) {
				if (!singleCase.isDirectory()) {
					continue;
				}
				final String singleCaseDirName = singleCase.getName();
				final List<File> fileNames = new ArrayList<File>();
				final File[] images = singleCase.listFiles();

				for (final File image : images) {
					if (image.isDirectory()
							|| !image.getName().endsWith(".tif")) {
						continue;
					}
					fileNames.add(image);
				}

				// ImagePlus imgPlus = this.open(singleCase.getAbsolutePath());

				// final Macro_Runner mr = new Macro_Runner();
				String input = fileNames.get(0).getAbsolutePath();
				input = input.replaceAll("\\\\", "\\\\\\\\");
				final String macro1 = "run(\"Image Sequence...\", \"open="
						+ input + " sort\");";
				final String ret1 = IJ.runMacro(macro1);
				System.out.println(ret1);
				// mr.runMacro("Image Sequence...",
				// "open=" + singleCase.getAbsolutePath() + "  sort use");

				final String macro2 = "run(\"Enhance Contrast...\", \"saturated=0.3 normalize process_all\");";
				final String ret2 = IJ.runMacro(macro2);
				System.out.println(ret2);
				// mr.runMacro("Enhance Contrast...",
				// "saturated=0.3 normalize process_all");

				// final ContrastEnhancer ce = new ContrastEnhancer();
				// ce.setNormalize(true);
				// ce.setProcessStack(true);
				// ce.run("");

				final String output1 = ImageJNormalization.pathOutput
						+ File.separator + snrDirName;
				final File dir1 = new File(output1);
				dir1.mkdir();

				final String output2 = output1 + File.separator
						+ singleCaseDirName;
				final File dir2 = new File(output2);
				dir2.mkdir();

				String output = output2 + File.separator
						+ fileNames.get(0).getName();
				output = output.replaceAll("\\\\", "\\\\\\\\");
				// System.out.println(output);

				final String macro3 = "run(\"Image Sequence... \", \"format=TIFF name=test_ start=1 digits=3 save="
						+ output + "\");";
				final String ret3 = IJ.runMacro(macro3);
				System.out.println(ret3);
				// mr.runMacro("Image Sequence... ", "format=TIFF save=" +
				// output);

				final String macro4 = "close();";
				final String ret4 = IJ.runMacro(macro4);
				System.out.println(ret4);
			}
		}
	}

	public ImagePlus open(final String path) {
		final FolderOpener fo = new FolderOpener();
		// fo.saveImage(true);
		fo.openAsVirtualStack(true);
		return fo.openFolder(path);
	}
}
