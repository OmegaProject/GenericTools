package core;

import gui.OmegaGenericToolGUI;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
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
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;
import javax.swing.SwingUtilities;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

public class ImageComparator implements Runnable {
	private final File workingDir;
	private final File compareDir;

	private final Map<Integer, String> comparisonResults;
	private final Map<Integer, String> comparisonLogResults;

	private Integer numberOfFrames;
	private Integer numberOfDifferentPixels;
	private Integer totalDifference;
	private Integer maxDifference;
	private Integer minDifference;
	private Double meanDifference;
	private Integer totalNumberOfPixels;
	private Double meanDifferentPixelsPerImage;
	private Integer countDifferentFrames;

	private Integer numberOfFramesLog;
	private Integer numberOfDifferentPixelsLog;
	private Double totalDifferenceLog;
	private Double maxDifferenceLog;
	private Double minDifferenceLog;
	private Double meanDifferenceLog;
	private Integer totalNumberOfPixelsLog;
	private Double meanDifferentPixelsPerImageLog;
	private Integer countDifferentFramesLog;

	private final boolean isDistr;
	private final boolean isLog;

	private int maxValue;

	private final OmegaGenericToolGUI gui;

	public ImageComparator(final File workingDir, final File compareDir,
	        final boolean isDistr, final boolean isLog,
	        final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;
		this.compareDir = compareDir;

		this.comparisonResults = new HashMap<Integer, String>();
		this.comparisonLogResults = new HashMap<Integer, String>();

		this.totalNumberOfPixels = 0;
		this.numberOfDifferentPixels = 0;
		this.numberOfFrames = 0;
		this.meanDifferentPixelsPerImage = 0.0;
		this.totalDifference = 0;
		this.meanDifference = 0.0;
		this.maxDifference = -1;
		this.minDifference = -1;
		this.countDifferentFrames = 0;

		this.totalNumberOfPixelsLog = 0;
		this.numberOfDifferentPixelsLog = 0;
		this.numberOfFramesLog = 0;
		this.meanDifferentPixelsPerImageLog = 0.0;
		this.totalDifferenceLog = 0.0;
		this.meanDifferenceLog = 0.0;
		this.maxDifferenceLog = -1.0;
		this.minDifferenceLog = -1.0;
		this.countDifferentFramesLog = 0;

		this.maxValue = -1;

		this.isDistr = isDistr;
		this.isLog = isLog;

		this.gui = gui;
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					ImageComparator.this.gui.appendOutput(update);

				}
			});
		} catch (final InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void run() {
		String f1Name = "";
		String f2Name = "";
		String f1ext = "";
		String f2ext = "";
		int index1 = -1;
		int index2 = -1;

		int workingDirIndex = 0;
		for (final File f1 : this.workingDir.listFiles()) {
			if (f1.isDirectory()) {
				workingDirIndex++;
				continue;
			}
			index1 = f1.getName().lastIndexOf("_");
			index2 = f1.getName().lastIndexOf(".");
			f1ext = f1.getName().substring(index2 + 1);
			if (!f1ext.equals("tiff") & !f1ext.equals("tif")) {
				workingDirIndex++;
				continue;
			}
			f1Name = f1.getName().substring(index1 + 1, index2);
			// .replace("0", "");
			int compareDirIndex = 0;
			for (final File f2 : this.compareDir.listFiles()) {
				if (f2.isDirectory()) {
					compareDirIndex++;
					continue;
				}
				index1 = f2.getName().lastIndexOf("_");
				index2 = f2.getName().lastIndexOf(".");
				f2Name = f2.getName().substring(index1 + 1, index2);
				// .replace("0", "");
				f2ext = f2.getName().substring(index2 + 1);
				if (!f2ext.equals("tiff") & !f2ext.equals("tif")) {
					compareDirIndex++;
					continue;
				}
				this.updateGUI("Analyzing file in working dir "
				        + workingDirIndex + "/"
				        + this.workingDir.listFiles().length
				        + "in compare dir " + compareDirIndex + "/"
				        + this.compareDir.listFiles().length);
				if (f1Name.equals(f2Name)) {
					this.updateGUI("Compare " + f1Name + "VS" + f2Name + "...");
					try {
						if (this.isDistr) {
							if (this.compareImageWithDistr(f1, f2)) {
								this.countDifferentFrames++;
							}
						} else {
							if (this.compareImage(f1, f2)) {
								this.countDifferentFrames++;
							}
						}
						if (this.isLog) {
							if (this.compareImageLog(f1, f2)) {
								this.countDifferentFramesLog++;
							}
							this.numberOfFramesLog++;
						}
					} catch (final IOException e) {
						// TODO gestire eccezione
						e.printStackTrace();
					}

					this.numberOfFrames++;
					this.updateGUI("done");
				}
				compareDirIndex++;
			}
			workingDirIndex++;
		}

		this.meanDifference = (double) this.totalDifference
		        / (double) this.numberOfDifferentPixels;
		this.meanDifferentPixelsPerImage = (double) this.numberOfDifferentPixels
		        / (double) this.numberOfFrames;

		if (this.isLog) {
			this.meanDifferenceLog = this.totalDifferenceLog
			        / (double) this.numberOfDifferentPixelsLog;
			this.meanDifferentPixelsPerImageLog = (double) this.numberOfDifferentPixelsLog
			        / (double) this.numberOfFramesLog;
		}
		try {
			final String results = this.getResultsString();
			this.updateGUI(results);
			this.writeResultsFiles();
		} catch (final IOException e) {
			// TODO gestire eccezione
			e.printStackTrace();
		}
	}

	private boolean compareImageLog(final File originalImg,
	        final File compareImg) throws IOException {
		final StringBuffer results = new StringBuffer();

		final int resultsKey = this.getPostfix(originalImg);

		if (this.minDifferenceLog == -1) {
			this.minDifferenceLog = (double) this.maxValue;
		}
		if (this.maxDifferenceLog == -1) {
			this.maxDifferenceLog = 0.0;
		}

		final File originalLog = new File(originalImg.getParentFile()
		        .getAbsolutePath()
		        + File.separatorChar
		        + "Logs"
		        + File.separatorChar
		        + "FullLog_"
		        + originalImg.getName().replace(".tif", ".txt"));
		final File compareLog = new File(compareImg.getParentFile()
		        .getAbsolutePath()
		        + File.separatorChar
		        + "Logs"
		        + File.separatorChar
		        + compareImg.getName().replace(".tif", ".txt"));
		results.append("ImageLog workingDir: " + originalLog.getName()
		        + " vs compareDir: " + compareLog.getName() + "\n");

		final FileReader frW = new FileReader(originalLog);
		final BufferedReader brW = new BufferedReader(frW);

		final FileReader frC = new FileReader(compareLog);
		final BufferedReader brC = new BufferedReader(frC);

		String line1 = brW.readLine();
		String line2 = brC.readLine();

		boolean isDiff = false;
		int y = 0;
		while (((line1 != null) && !line1.isEmpty())
		        && ((line2 != null) && !line2.isEmpty())) {
			int index1 = line1.indexOf("\t");
			int index2 = line2.indexOf("\t");
			int x = 0;
			while ((index1 != -1) && (index2 != -1)) {
				String val1, val2;
				val1 = line1.substring(0, index1);
				line1 = line1.substring(index1 + 1);

				val2 = line2.substring(0, index2);
				line2 = line2.substring(index2 + 1);

				index1 = line1.indexOf("\t");
				index2 = line2.indexOf("\t");

				final BigDecimal d1 = new BigDecimal(val1);
				final BigDecimal d2 = new BigDecimal(val2);
				if (d1.compareTo(d2) != 0) {
					isDiff = true;
					results.append("X:\t" + x + "\tY:\t" + y + "\t" + d1 + "\t"
					        + d2 + "\n");
					this.numberOfDifferentPixelsLog++;
					final Double diff = Math.abs(d1.subtract(d2).doubleValue());
					if (this.minDifferenceLog > diff) {
						this.minDifferenceLog = diff;
					}
					if (this.maxDifferenceLog < diff) {
						this.maxDifferenceLog = diff;
					}
					this.totalDifferenceLog += diff;
				}
				this.totalNumberOfPixelsLog++;
				x++;
			}
			y++;
			line1 = brW.readLine();
			line2 = brC.readLine();
		}

		brW.close();
		brC.close();

		frW.close();
		frC.close();

		if (isDiff) {
			this.comparisonLogResults.put(resultsKey, results.toString());
		}
		return isDiff;
	}

	private boolean compareImage(final File originalFile, final File compareFile)
	        throws IOException {
		final int resultsKey = this.getPostfix(originalFile);

		final StringBuffer results = new StringBuffer();
		results.append("Image workingDir: " + originalFile.getName()
		        + " vs compareDir: " + compareFile.getName() + "\n");

		final RenderedOp ROImg1 = JAI
		        .create("fileload", originalFile.getPath());
		final RenderedOp ROImg2 = JAI.create("fileload", compareFile.getPath());

		final int bitSize1 = ROImg1.getColorModel().getPixelSize();
		final int bitSize2 = ROImg2.getColorModel().getPixelSize();

		if (bitSize1 != bitSize2) {
			results.append("Different bitSize");
			this.comparisonResults.put(resultsKey, results.toString());
			return true; // Should be error
		}

		if (this.maxValue == -1) {
			this.maxValue = (int) (Math.pow(2, bitSize1) - 1);
		}

		if (this.minDifference == -1) {
			this.minDifference = this.maxValue;
		}
		if (this.maxDifference == -1) {
			this.maxDifference = 0;
		}

		final int h1 = ROImg1.getHeight();
		final int w1 = ROImg1.getWidth();
		final int h2 = ROImg1.getHeight();
		final int w2 = ROImg1.getWidth();
		final Raster r1 = ROImg1.getData();
		final Raster r2 = ROImg2.getData();
		final DataBuffer db1 = r1.getDataBuffer();
		final DataBuffer db2 = r2.getDataBuffer();

		if ((h1 != h2) | (w1 != w2)) {
			results.append("Different ");
			if (h1 != h2) {
				results.append("height\n");
			} else {
				results.append("width\n");
			}
			this.comparisonResults.put(resultsKey, results.toString());
			return true; // Should be error
		}

		boolean isDiff = false;
		for (int x = 0; x < w1; x++) {
			for (int y = 0; y < h1; y++) {
				final int pix1 = db1.getElem((y * w1) + x);
				final int pix2 = db2.getElem((y * w1) + x);
				// r1.getPixel(x, y, iArray);
				// r2.getPixel(x, y, iArray);
				if (pix1 != pix2) {
					isDiff = true;
					results.append("X:\t" + x + "\tY:\t" + y + "\t" + pix1
					        + "\t" + pix2 + "\n");
					this.numberOfDifferentPixels++;
					final Integer diff = Math.abs(pix2 - pix1);
					if (this.minDifference > diff) {
						this.minDifference = diff;
					}
					if (this.maxDifference < diff) {
						this.maxDifference = diff;
					}
					this.totalDifference += diff;
				}
			}
		}
		this.totalNumberOfPixels += h1 * w1;
		if (isDiff) {
			this.comparisonResults.put(resultsKey, results.toString());
		}
		return isDiff;
	}

	private boolean compareImageWithDistr(final File originalFile,
	        final File compareFile) throws IOException {
		final int resultsKey = this.getPostfix(originalFile);

		final StringBuffer results = new StringBuffer();
		results.append("Image workingDir: " + originalFile.getName()
		        + " vs compareDir: " + compareFile.getName() + "\n");

		final RandomEngine random = new MersenneTwister(10);
		final List<Double> randomValues = new ArrayList<Double>();
		for (int i = 0; i < 100; i++) {
			final Double randomVal = random.raw();
			System.out.println(randomVal);
			randomValues.add(randomVal);
		}

		final RenderedOp ROImg1 = JAI
		        .create("fileload", originalFile.getPath());
		final RenderedOp ROImg2 = JAI.create("fileload", compareFile.getPath());

		final int bitSize1 = ROImg1.getColorModel().getPixelSize();
		final int bitSize2 = ROImg2.getColorModel().getPixelSize();

		if (bitSize1 != bitSize2) {
			results.append("Different bitSize");
			this.comparisonResults.put(resultsKey, results.toString());
			return true; // Should be error
		}

		if (this.maxValue == -1) {
			this.maxValue = (int) (Math.pow(2, bitSize1) - 1);
		}

		if (this.minDifference == -1) {
			this.minDifference = this.maxValue;
		}
		if (this.maxDifference == -1) {
			this.maxDifference = 0;
		}

		final int h1 = ROImg1.getHeight();
		final int w1 = ROImg1.getWidth();
		final int h2 = ROImg1.getHeight();
		final int w2 = ROImg1.getWidth();
		final Raster r1 = ROImg1.getData();
		final Raster r2 = ROImg2.getData();
		final DataBuffer db1 = r1.getDataBuffer();
		final DataBuffer db2 = r2.getDataBuffer();

		if ((h1 != h2) | (w1 != w2)) {
			results.append("Different ");
			if (h1 != h2) {
				results.append("height\n");
			} else {
				results.append("width\n");
			}
			this.comparisonResults.put(resultsKey, results.toString());
			return true; // Should be error
		}

		boolean isDiff = false;
		int counter = 0;
		for (int x = 0; x < w1; x++) {
			for (int y = 0; y < h1; y++) {
				if (counter >= 100) {
					counter = 0;
				}

				final int pix1 = db1.getElem((y * w1) + x);
				final int pix2 = db2.getElem((y * w1) + x);

				final Integer oldValue1 = pix1;
				// final Poisson distr = new Poisson(oldValue1, random);
				// Double newValue1 = distr.nextDouble();
				Double newValue1 = oldValue1 + randomValues.get(counter);
				if (newValue1 < 0) {
					newValue1 = 0.0;
				} else if (newValue1 > this.maxValue) {
					newValue1 = (double) this.maxValue;
				}

				final Integer oldValue2 = pix2;
				// final Poisson distr2 = new Poisson(oldValue2, random);
				// Double newValue2 = distr2.nextDouble();
				Double newValue2 = oldValue2 + randomValues.get(counter);
				if (newValue2 < 0) {
					newValue2 = 0.0;
				} else if (newValue2 > this.maxValue) {
					newValue2 = (double) this.maxValue;
				}
				final Integer newVal1 = (int) Math.rint(newValue1);
				final Integer newVal2 = (int) Math.rint(newValue2);
				if (!newVal1.equals(newVal2)) {
					isDiff = true;
					results.append("X:\t" + x + "\tY:\t" + y + "\t" + pix1
					        + "\t" + pix2 + "\n");
					this.numberOfDifferentPixels++;
					final Integer diff = Math.abs(newVal2 - newVal1);
					if (this.minDifference > diff) {
						this.minDifference = diff;
					}
					if (this.maxDifference < diff) {
						this.maxDifference = diff;
					}
					this.totalDifference += diff;
				}
				counter++;
			}
		}
		this.totalNumberOfPixels += h1 * w1;
		if (isDiff) {
			this.comparisonResults.put(resultsKey, results.toString());
		}
		return isDiff;
	}

	private String getResultsString() {
		final StringBuffer sb = new StringBuffer();
		sb.append("Image comparison results:\n");
		sb.append("Total frames:\t" + this.numberOfFrames + "\n");
		sb.append("Different frames:\t" + this.countDifferentFrames + "\n");
		sb.append("Total pixels:\t" + this.totalNumberOfPixels + "\n");
		sb.append("Different pixels:\t" + this.numberOfDifferentPixels + "\n");
		sb.append("Min difference:\t" + this.minDifference + "\n");
		sb.append("Mean difference:\t" + this.meanDifference + "\n");
		sb.append("Max difference:\t" + this.maxDifference + "\n");
		sb.append("Mean different pixels per image:\t"
		        + this.meanDifferentPixelsPerImage + "\n");
		sb.append("###################\n");
		if (this.isLog) {
			sb.append("Log comparison results:\n");
			sb.append("Total frames:\t" + this.numberOfFramesLog + "\n");
			sb.append("Different frames:\t" + this.countDifferentFramesLog
			        + "\n");
			sb.append("Total pixels:\t" + this.totalNumberOfPixelsLog + "\n");
			sb.append("Different pixels:\t" + this.numberOfDifferentPixelsLog
			        + "\n");
			sb.append("Min difference:\t" + this.minDifferenceLog + "\n");
			sb.append("Mean difference:\t" + this.meanDifferenceLog + "\n");
			sb.append("Max difference:\t" + this.maxDifferenceLog + "\n");
			sb.append("Mean different pixels per image:\t"
			        + this.meanDifferentPixelsPerImageLog + "\n");
			sb.append("###################\n");
		}
		return sb.toString();
	}

	private void writeResultsFiles() throws IOException {
		final File compareResultsDir = new File(
		        this.workingDir.getAbsolutePath() + File.separatorChar
		                + "compareWith" + this.compareDir.getName());
		if (!compareResultsDir.exists()) {
			compareResultsDir.mkdir();
		}
		final File resultsFile = new File(compareResultsDir.getAbsolutePath()
		        + File.separatorChar + "ImageComparator_Results.txt");
		if (!resultsFile.exists()) {
			resultsFile.createNewFile();
		}
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.getResultsString());
		bw.close();
		fw.close();

		if (this.isLog && !this.comparisonResults.isEmpty()) {
			for (final Integer key : this.comparisonLogResults.keySet()) {
				final String logs = this.comparisonLogResults.get(key);
				final String imgs = this.comparisonResults.get(key);

				if (imgs == null) {
					this.writeSpecificFiles(compareResultsDir,
					        "ImageComparator_CompareLogResult", logs, key);
					continue;
				} else {
					final String mergedBuffer = this
					        .mergeLogsString(logs, imgs);
					this.writeSpecificFiles(compareResultsDir,
					        "ImageComparator_CompareResultMerged",
					        mergedBuffer, key);
				}
			}
		} else {
			for (final Integer key : this.comparisonLogResults.keySet()) {
				final String logs = this.comparisonLogResults.get(key);
				this.writeSpecificFiles(compareResultsDir,
				        "ImageComparator_CompareLogResult", logs, key);
			}
			for (final Integer key : this.comparisonResults.keySet()) {
				final String imgs = this.comparisonResults.get(key);
				this.writeSpecificFiles(compareResultsDir,
				        "ImageComparator_CompareResult", imgs, key);
			}
		}
	}

	private void writeSpecificFiles(final File dir, final String fileName,
	        final String buffer, final int key) throws IOException {
		final File compareResultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + fileName + key + ".txt");
		if (!compareResultsFile.exists()) {
			compareResultsFile.createNewFile();
		}

		final FileWriter fws = new FileWriter(compareResultsFile);
		final BufferedWriter bws = new BufferedWriter(fws);
		bws.write(buffer);
		bws.close();
		fws.close();
	}

	private String mergeLogsString(final String logs, final String imgs) {
		final String[] logsArray = logs.split("\n");
		final String[] imgsArray = imgs.split("\n");

		final StringBuffer mergedResults = new StringBuffer();
		mergedResults.append("Logs values\t\t\t\t\t\t\tImages values\n");
		for (final String logString : logsArray) {
			mergedResults.append(logString);
			mergedResults.append("\t");
			String checkString = "";
			int x = 0;
			int y = 0;
			if (logString.startsWith("X:\t")) {
				final String newString = logString.replace("X:\t", "");
				final String xS = newString.substring(0,
				        newString.indexOf("\t"));
				x = Integer.valueOf(xS);
				String yS = newString.substring(newString.indexOf("\t") + 1)
				        .replace("Y:\t", "");
				yS = yS.substring(0, yS.indexOf("\t"));
				y = Integer.valueOf(yS);
				checkString = "X:\t" + x + "\tY:\t" + y;
			} else {
				mergedResults.append("\n");
				checkString = logString.replace("ImageLog ", "");
				checkString = checkString.replace("FullLog_", "");
				checkString = checkString.replace(".txt", ".tif");
			}

			boolean found = false;
			for (final String imgString : imgsArray) {
				if (imgString.contains(checkString)) {
					if (logString.startsWith("X:\t")) {
						final String newString = imgString.replace("X:\t", "");
						final String xS = newString.substring(0,
						        newString.indexOf("\t"));
						final int xImg = Integer.valueOf(xS);
						String yS = newString.substring(
						        newString.indexOf("\t") + 1)
						        .replace("Y:\t", "");
						yS = yS.substring(0, yS.indexOf("\t"));
						final int yImg = Integer.valueOf(yS);
						if ((x != xImg) || (y != yImg)) {
							continue;
						}
					}
					found = true;
					mergedResults.append(imgString + "\n");
					break;
				}
			}
			if (!found) {
				mergedResults.append("\n");
			}
		}

		return mergedResults.toString();
	}

	private int getPostfix(final File originalImg) {
		final String origName = originalImg.getName();
		final int indexOfUnder = origName.indexOf("_");
		String stringIndex = null;
		if (indexOfUnder == -1) {
			stringIndex = origName.replaceAll("\\D+", "");
		} else {
			stringIndex = origName.substring(indexOfUnder + 1);
		}

		stringIndex = stringIndex.replace(".tif", "");
		final int postfix = Integer.valueOf(stringIndex);
		return postfix;
	}

	private String generatePostfix(final Integer numberOfDigits,
	        final Integer index) {
		final StringBuffer postfix = new StringBuffer();
		int maxValue = 1;
		for (int k = 0; k < numberOfDigits; k++) {
			postfix.append("0");
			maxValue *= 10;
		}
		int counter = 0;
		for (int k = 1; k < maxValue; k *= 10) {
			final int i = (int) Math.floor((index / 10));
			if (i < k) {
				break;
			}
			counter++;
		}

		final int start = numberOfDigits - counter - 1;
		final int end = numberOfDigits;
		postfix.replace(start, end, index.toString());
		return postfix.toString();
	}
}
