package core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MosaicClientServerInputCreator {

	private static final String pathInput = "F:\\2018-03-30_Images_16bit_Signal_154.70_1000_Moving_BiasAndSigmaEstimation_Detection_Algo_C_Original";

	// private static final String pathOutput =
	// "F:\\2017-11-27_Normalized_Images_for_Mosaic_C_algorithm_results";
	
	public static void main(final String[] args) {
		final File dir = new File(MosaicClientServerInputCreator.pathInput);
		if (!dir.exists() || dir.isFile())
			return;
		for (final File snrCase : dir.listFiles()) {
			if (!snrCase.isDirectory()) {
				continue;
			}
			for (final File singleCase : snrCase.listFiles()) {
				if (!singleCase.isDirectory()) {
					continue;
				}
				final String pathLogFolder = singleCase.getAbsolutePath()
						+ File.separator + "logs";
				final File logFolder = new File(pathLogFolder);
				if (!logFolder.exists()) {
					logFolder.mkdir();
				}
				final File output = new File(logFolder.getAbsolutePath()
						+ File.separator + "InputFile.txt");
				FileWriter fw = null;
				try {
					fw = new FileWriter(output);
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				final BufferedWriter bw = new BufferedWriter(fw);
				
				final List<String> fileNameList = new ArrayList<String>();
				for (final File f : singleCase.listFiles()) {
					if (f.isDirectory() || !f.getName().endsWith(".tif")) {
						continue;
					}
					fileNameList.add(f.getAbsolutePath());
				}
				
				final String resFileName = snrCase.getName() + "_"
						+ singleCase.getName() + ".txt";
				
				try {
					bw.write("radius=3\n");
					bw.write("cutoff=0\n");
					bw.write("percentile=0.9\n");
					bw.write("displacement=3\n");
					bw.write("linkrange=5\n");
					bw.write("verbose=1\n");
					bw.write("results=" + resFileName + "\n");
					// bw.write("list=1," + fileNameList.size() + ",1\n");
					// bw.write("file=test_%d.tif\n");
					for (final String fileName : fileNameList) {
						bw.write("file=" + fileName + "\n");
					}
					bw.close();
					fw.close();
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
