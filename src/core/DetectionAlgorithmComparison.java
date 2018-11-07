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

public class DetectionAlgorithmComparison {

	private static final String pathInput = "F:\\2018-03-21_BiasAndSigmaEstimation_Detection_Comparison";

	public static void main(final String[] args) throws IOException {
		final Map<Integer, Map<String, List<String>>> resultsMap = new HashMap<Integer, Map<String, List<String>>>();
		Integer maxFrameIndex = 0;
		final File dir = new File(DetectionAlgorithmComparison.pathInput);
		final Map<String, String> fileNames = new HashMap<String, String>();
		if (!dir.exists() || dir.isFile())
			return;
		for (final File detResultsFile : dir.listFiles()) {
			if (!detResultsFile.isFile()) {
				continue;
			}
			final String fileName = detResultsFile.getName()
					.replace(".txt", "");
			fileNames.put(fileName, DetectionAlgorithmComparison.pathInput
					+ File.separator + "SPACED_" + fileName + ".txt");
			FileReader fr = null;
			try {
				fr = new FileReader(detResultsFile);
			} catch (final IOException e) {
				e.printStackTrace();
			}
			if (fr == null)
				return;
			final BufferedReader br = new BufferedReader(fr);

			String line = null;
			try {
				line = br.readLine();
			} catch (final IOException e) {
				e.printStackTrace();
			}
			while (line != null) {
				final String[] tokens = line.split("\t");
				final Integer frameIndex = Integer.valueOf(tokens[0]);
				if (frameIndex > maxFrameIndex) {
					maxFrameIndex = frameIndex;
				}
				Map<String, List<String>> pointsMap;
				if (resultsMap.containsKey(frameIndex)) {
					pointsMap = resultsMap.get(frameIndex);
				} else {
					pointsMap = new HashMap<String, List<String>>();
				}
				List<String> points;
				if (pointsMap.containsKey(fileName)) {
					points = pointsMap.get(fileName);
				} else {
					points = new ArrayList<String>();
				}
				points.add(line);
				pointsMap.put(fileName, points);
				resultsMap.put(frameIndex, pointsMap);
				try {
					line = br.readLine();
				} catch (final IOException e) {
					e.printStackTrace();
				}
			}
			br.close();
			fr.close();
		}

		// for (final String fileName : fileNames) {
		// final File f = new File(fileName);
		// if (!f.exists()) {
		// f.createNewFile();
		// }
		// }

		for (int i = 0; i <= maxFrameIndex; i++) {
			if (resultsMap.get(i) != null) {
				final Map<String, List<String>> pointsMap = resultsMap.get(i);
				int maxSize = 0;
				for (final String fileName : pointsMap.keySet()) {
					if (maxSize < pointsMap.get(fileName).size()) {
						maxSize = pointsMap.get(fileName).size();
					}
				}
				for (final String fileName : fileNames.keySet()) {
					final String outputFile = fileNames.get(fileName);
					final File f = new File(outputFile);
					final List<String> points = pointsMap.get(fileName);
					final FileWriter fw = new FileWriter(f, true);
					final BufferedWriter bw = new BufferedWriter(fw);
					for (int p = 0; p < maxSize; p++) {
						if ((points != null) && (points.size() > p)
								&& (points.get(p) != null)) {
							bw.write(points.get(p));
							bw.write("\n");
						} else {
							bw.write("\n");
						}
					}
					bw.close();
					fw.close();
				}
			}
		}
	}
}
