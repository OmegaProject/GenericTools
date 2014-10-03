package core;

import gui.OmegaGenericToolGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

public class DAndSMSSMeansCalculator implements Runnable {
	public final static String fileName1 = "D_values_";
	public final static String fileName2 = "SMSS_values_";

	public final static String resultsFileName = "DAndSMSSMeansCalculator_Results";
	public final static String logFileName = "DAndSMSSMeansCalculator_Log";

	private final File workingDir;
	// SNR L SMSS D
	private Map<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>> smssValuesMap;
	private Map<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>> dValuesMap;
	// L SNR SMSS D
	// SMSS SNR L D
	// D SNR L SMSS

	private Map<Double, Map<Double, List<Double>>> snrSMSSDiff, dSMSSDiff,
	        lSMSSDiff;
	private final Map<Double, Map<Double, List<Double>>> smssSMSSDiff;
	private Map<Double, Map<Double, List<Double>>> snrDDiff, smssDDiff, lDDiff;
	// aggiungere smssSMSSDiff e dDDiff
	private final Map<Double, Map<Double, List<Double>>> dDDiff;

	private final StringBuffer errorLog;

	private final OmegaGenericToolGUI gui;

	public DAndSMSSMeansCalculator(final File workingDir,
	        final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;

		this.smssValuesMap = new LinkedHashMap<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>>();
		this.dValuesMap = new LinkedHashMap<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>>();

		this.snrSMSSDiff = new LinkedHashMap<Double, Map<Double, List<Double>>>();
		this.dSMSSDiff = new LinkedHashMap<Double, Map<Double, List<Double>>>();
		this.lSMSSDiff = new LinkedHashMap<Double, Map<Double, List<Double>>>();
		this.smssSMSSDiff = new LinkedHashMap<Double, Map<Double, List<Double>>>();
		this.snrDDiff = new LinkedHashMap<Double, Map<Double, List<Double>>>();
		this.smssDDiff = new LinkedHashMap<Double, Map<Double, List<Double>>>();
		this.lDDiff = new LinkedHashMap<Double, Map<Double, List<Double>>>();
		this.dDDiff = new LinkedHashMap<Double, Map<Double, List<Double>>>();

		this.errorLog = new StringBuffer();
		this.gui = gui;
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					DAndSMSSMeansCalculator.this.gui.appendOutput(update);

				}
			});
		} catch (final InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	private void importValues(final File f) throws IOException {
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);

		Map<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>> map;
		if (f.getName().contains(DAndSMSSMeansCalculator.fileName1)) {
			map = this.dValuesMap;
		} else {
			map = this.smssValuesMap;
		}

		String line = br.readLine();
		while (line != null) {
			final String[] tokens = line.split(";");
			final String title = tokens[0];

			final String[] titleTokens = title.split(" ");
			final double SNR = Double.valueOf(titleTokens[1]);
			final double length = Double.valueOf(titleTokens[2]);
			final double SMSS = Double.valueOf(titleTokens[3]);
			final double D = Double.valueOf(titleTokens[4]);

			Map<Double, Map<Double, Map<Double, List<Double>>>> lengthMap;
			if (map.containsKey(SNR)) {
				lengthMap = map.get(SNR);
			} else {
				lengthMap = new LinkedHashMap<Double, Map<Double, Map<Double, List<Double>>>>();
			}

			Map<Double, Map<Double, List<Double>>> smssMap;
			if (lengthMap.containsKey(length)) {
				smssMap = lengthMap.get(length);
			} else {
				smssMap = new LinkedHashMap<Double, Map<Double, List<Double>>>();
			}

			Map<Double, List<Double>> dMap;
			if (smssMap.containsKey(SMSS)) {
				dMap = smssMap.get(SMSS);
			} else {
				dMap = new LinkedHashMap<Double, List<Double>>();

			}

			List<Double> values;
			if (dMap.containsKey(D)) {
				values = dMap.get(D);
			} else {
				values = new ArrayList<Double>();
			}

			for (int i = 2; i < tokens.length; i++) {
				final String token = tokens[i];
				values.add(Double.valueOf(token));
			}

			dMap.put(D, values);
			smssMap.put(SMSS, dMap);
			lengthMap.put(length, smssMap);
			map.put(SNR, lengthMap);

			line = br.readLine();
		}

		br.close();
		fr.close();
	}

	@Override
	public void run() {
		for (final File set : this.workingDir.listFiles()) {
			if (set.isFile()) {
				continue;
			}
			this.updateGUI("Set\t" + set.getName());
			this.errorLog.append("Set\t" + set.getName() + "\n");
			for (final File file : set.listFiles()) {
				if (!file.isFile()) {
					continue;
				}
				if (file.getName()
				        .startsWith(DAndSMSSMeansCalculator.fileName2)) {
					this.updateGUI("Image\t" + file.getName());
					this.errorLog.append("Image\t" + file.getName() + "\n");

					try {
						this.importValues(file);
					} catch (final Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		try {
			this.updateGUI("Computing SMSS Diffs");
			this.computeSMSSDiffs();
			this.updateGUI("Writing SMSS results");
			this.writeSingleSMSSResultsFile(this.workingDir);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.smssValuesMap = null;
		this.snrSMSSDiff = null;
		this.lSMSSDiff = null;
		this.dSMSSDiff = null;

		System.gc();

		for (final File set : this.workingDir.listFiles()) {
			if (set.isFile()) {
				continue;
			}
			this.updateGUI("Set\t" + set.getName());
			this.errorLog.append("Set\t" + set.getName() + "\n");
			for (final File file : set.listFiles()) {
				if (!file.isFile()) {
					continue;
				}
				if (file.getName()
				        .startsWith(DAndSMSSMeansCalculator.fileName1)) {
					this.updateGUI("Image\t" + file.getName());
					this.errorLog.append("Image\t" + file.getName() + "\n");

					try {
						this.importValues(file);
					} catch (final Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		try {
			this.updateGUI("Computing D Diffs");
			this.computeDDiffs();
			this.updateGUI("Writing D results");
			this.writeSingleDResultsFile(this.workingDir);
		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.dValuesMap = null;
		this.snrDDiff = null;
		this.lDDiff = null;
		this.smssDDiff = null;

		try {
			this.writeLogFile(this.workingDir);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.updateGUI("Finished");
		this.errorLog.append("Finished\n");
	}

	private void computeDDiffs() {
		Map<Double, List<Double>> snrDDiff, smssDDiff, lDDiff, dDDiff;
		for (final Double SNR : this.dValuesMap.keySet()) {
			final Map<Double, Map<Double, Map<Double, List<Double>>>> lengthMap = this.dValuesMap
			        .get(SNR);
			if (this.snrDDiff.containsKey(SNR)) {
				snrDDiff = this.snrDDiff.get(SNR);
			} else {
				snrDDiff = new LinkedHashMap<Double, List<Double>>();
			}
			for (final Double L : lengthMap.keySet()) {
				final Map<Double, Map<Double, List<Double>>> smssMap = lengthMap
				        .get(L);
				if (this.lDDiff.containsKey(L)) {
					lDDiff = this.lDDiff.get(L);
				} else {
					lDDiff = new LinkedHashMap<Double, List<Double>>();
				}
				for (final Double SMSS : smssMap.keySet()) {
					final Map<Double, List<Double>> dMap = smssMap.get(SMSS);
					if (this.smssDDiff.containsKey(SMSS)) {
						smssDDiff = this.smssDDiff.get(SMSS);
					} else {
						smssDDiff = new LinkedHashMap<Double, List<Double>>();
					}
					for (final Double D : dMap.keySet()) {

						// D VS D special loop
						if (this.dDDiff.containsKey(D)) {
							dDDiff = this.dDDiff.get(D);
						} else {
							dDDiff = new LinkedHashMap<Double, List<Double>>();
						}
						for (final Double D2 : dMap.keySet()) {
							final List<Double> values = dMap.get(D2);
							List<Double> dDMean;
							if (dDDiff.containsKey(D2)) {
								dDMean = dDDiff.get(D2);
							} else {
								dDMean = new ArrayList<Double>();
							}
							for (final Double val : values) {
								dDMean.add(val - D2);
							}
							dDDiff.put(D2, dDMean);
						}
						this.dDDiff.put(D, dDDiff);
						// D VS D special loop END

						final List<Double> values = dMap.get(D);
						List<Double> snrDMean;
						if (snrDDiff.containsKey(D)) {
							snrDMean = snrDDiff.get(D);
						} else {
							snrDMean = new ArrayList<Double>();
						}
						List<Double> lDMean;
						if (lDDiff.containsKey(D)) {
							lDMean = lDDiff.get(D);
						} else {
							lDMean = new ArrayList<Double>();
						}
						List<Double> smssDMean;
						if (smssDDiff.containsKey(D)) {
							smssDMean = smssDDiff.get(D);
						} else {
							smssDMean = new ArrayList<Double>();
						}
						for (final Double val : values) {
							snrDMean.add(val - D);
							lDMean.add(val - D);
							smssDMean.add(val - D);
						}
						snrDDiff.put(D, snrDMean);
						lDDiff.put(D, lDMean);
						smssDDiff.put(D, smssDMean);
					}
					this.smssDDiff.put(SMSS, smssDDiff);
				}
				this.lDDiff.put(L, lDDiff);
			}
			this.snrDDiff.put(SNR, snrDDiff);
			this.dValuesMap.put(SNR, null);
		}
	}

	private void computeSMSSDiffs() {
		Map<Double, List<Double>> snrSMSSDiff, dSMSSDiff, lSMSSDiff, smssSMSSDiff;
		for (final Double SNR : this.smssValuesMap.keySet()) {
			final Map<Double, Map<Double, Map<Double, List<Double>>>> lengthMap = this.smssValuesMap
			        .get(SNR);
			if (this.snrSMSSDiff.containsKey(SNR)) {
				snrSMSSDiff = this.snrSMSSDiff.get(SNR);
			} else {
				snrSMSSDiff = new LinkedHashMap<Double, List<Double>>();
			}
			for (final Double L : lengthMap.keySet()) {
				final Map<Double, Map<Double, List<Double>>> smssMap = lengthMap
				        .get(L);
				if (this.lSMSSDiff.containsKey(L)) {
					lSMSSDiff = this.lSMSSDiff.get(L);
				} else {
					lSMSSDiff = new LinkedHashMap<Double, List<Double>>();
				}
				for (final Double SMSS : smssMap.keySet()) {
					final Map<Double, List<Double>> dMap = smssMap.get(SMSS);

					// SMSS VS SMSS special loop
					if (this.smssSMSSDiff.containsKey(SMSS)) {
						smssSMSSDiff = this.smssSMSSDiff.get(SMSS);
					} else {
						smssSMSSDiff = new LinkedHashMap<Double, List<Double>>();
					}
					for (final Double SMSS2 : smssMap.keySet()) {
						List<Double> smssSMSSMean;
						if (smssSMSSDiff.containsKey(SMSS2)) {
							smssSMSSMean = snrSMSSDiff.get(SMSS2);
						} else {
							smssSMSSMean = new ArrayList<Double>();
						}
						for (final Double D : dMap.keySet()) {
							this.updateGUI("SNR " + SNR + " L " + L + " SMSS "
							        + SMSS + " SMSS2 " + SMSS2 + " D " + D);
							final List<Double> values = dMap.get(D);
							for (final Double val : values) {
								smssSMSSMean.add(val - SMSS2);
							}
						}
						smssSMSSDiff.put(SMSS2, smssSMSSMean);
					}
					this.smssSMSSDiff.put(SMSS, smssSMSSDiff);
					// SMSS VS SMSS special loop END

					List<Double> snrSMSSMean;
					if (snrSMSSDiff.containsKey(SMSS)) {
						snrSMSSMean = snrSMSSDiff.get(SMSS);
					} else {
						snrSMSSMean = new ArrayList<Double>();
					}
					List<Double> lSMSSMean;
					if (lSMSSDiff.containsKey(SMSS)) {
						lSMSSMean = lSMSSDiff.get(SMSS);
					} else {
						lSMSSMean = new ArrayList<Double>();
					}
					for (final Double D : dMap.keySet()) {
						this.updateGUI("SNR " + SNR + " L " + L + " SMSS "
						        + SMSS + " D " + D);

						final List<Double> values = dMap.get(D);
						if (this.dSMSSDiff.containsKey(D)) {
							dSMSSDiff = this.dSMSSDiff.get(D);
						} else {
							dSMSSDiff = new LinkedHashMap<Double, List<Double>>();
						}
						List<Double> dSMSSMean;
						if (dSMSSDiff.containsKey(D)) {
							dSMSSMean = dSMSSDiff.get(D);
						} else {
							dSMSSMean = new ArrayList<Double>();
						}
						for (final Double val : values) {
							snrSMSSMean.add(val - SMSS);
							lSMSSMean.add(val - SMSS);
							dSMSSMean.add(val - SMSS);
						}
						dSMSSDiff.put(SMSS, dSMSSMean);
						this.dSMSSDiff.put(D, dSMSSDiff);
					}
					snrSMSSDiff.put(SMSS, snrSMSSMean);
					lSMSSDiff.put(SMSS, lSMSSMean);
				}
				this.lSMSSDiff.put(L, lSMSSDiff);
			}
			this.snrSMSSDiff.put(SNR, snrSMSSDiff);
			this.smssValuesMap.put(SNR, null);
		}
	}

	private void computeSingleStats(final String key1, final String key2,
	        final StringBuffer buffer,
	        final Map<Double, Map<Double, List<Double>>> diffsMap) {
		for (final Double firstKey : diffsMap.keySet()) {
			final Map<Double, List<Double>> innerMap = diffsMap.get(firstKey);
			for (final Double secondKey : innerMap.keySet()) {
				final List<Double> values = innerMap.get(secondKey);
				final double[] valuesArray = new double[values.size()];
				for (int i = 0; i < values.size(); i++) {
					valuesArray[i] = values.get(i);
				}

				buffer.append(key1);
				buffer.append("\t");
				buffer.append(firstKey);
				buffer.append("\n");

				buffer.append(key2);
				buffer.append("\t");
				buffer.append(secondKey);
				buffer.append("\n");

				buffer.append("#Values\t");
				buffer.append(values.size());
				buffer.append("\n");

				buffer.append("Mean\t");
				buffer.append(Stats2.mean(valuesArray));
				buffer.append("\n");

				buffer.append("StDev\t");
				buffer.append(Stats2.standardDeviationN(valuesArray));
				buffer.append("\n");

				buffer.append("Var\t");
				buffer.append(Stats2.varianceN(valuesArray));
				buffer.append("\n");

				buffer.append("StDev1\t");
				buffer.append(Stats2.standardDeviationN1(valuesArray));
				buffer.append("\n");

				buffer.append("Var1\t");
				buffer.append(Stats2.varianceN1(valuesArray));
				buffer.append("\n");
				buffer.append("***************************************\n");
			}
			buffer.append("#######################################\n");
		}
		buffer.append("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
	}

	private void writeLogFile(final File dir) throws IOException {
		final String fileName = DAndSMSSMeansCalculator.logFileName + ".txt";
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + fileName);
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.errorLog.toString());
		bw.close();
		fw.close();
	}

	private void writeSingleSMSSResultsFile(final File dir) throws IOException {
		StringBuffer buffer = new StringBuffer();

		String fileName = DAndSMSSMeansCalculator.resultsFileName
		        + "_SNR_SMSS.txt";
		File resultsFile = new File(dir.getAbsolutePath() + File.separatorChar
		        + fileName);
		FileWriter fw = new FileWriter(resultsFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		this.computeSingleStats("SNR", "SMSS", buffer, this.snrSMSSDiff);
		bw.write(buffer.toString());
		// TODO
		bw.close();
		fw.close();

		fileName = DAndSMSSMeansCalculator.resultsFileName + "_L_SMSS.txt";
		resultsFile = new File(dir.getAbsolutePath() + File.separatorChar
		        + fileName);
		fw = new FileWriter(resultsFile, true);
		bw = new BufferedWriter(fw);
		buffer = new StringBuffer();
		this.computeSingleStats("L", "SMSS", buffer, this.lSMSSDiff);
		bw.write(buffer.toString());
		// TODO
		bw.close();
		fw.close();

		fileName = DAndSMSSMeansCalculator.resultsFileName + "_D_SMSS.txt";
		resultsFile = new File(dir.getAbsolutePath() + File.separatorChar
		        + fileName);
		fw = new FileWriter(resultsFile, true);
		bw = new BufferedWriter(fw);
		buffer = new StringBuffer();
		this.computeSingleStats("D", "SMSS", buffer, this.dSMSSDiff);
		bw.write(buffer.toString());
		// TODO
		bw.close();
		fw.close();

	}

	private void writeSingleDResultsFile(final File dir) throws IOException {
		StringBuffer buffer = new StringBuffer();

		String fileName = DAndSMSSMeansCalculator.resultsFileName
		        + "_SNR_D.txt";
		File resultsFile = new File(dir.getAbsolutePath() + File.separatorChar
		        + fileName);
		FileWriter fw = new FileWriter(resultsFile, true);
		BufferedWriter bw = new BufferedWriter(fw);
		this.computeSingleStats("SNR", "D", buffer, this.snrDDiff);
		bw.write(buffer.toString());
		// TODO
		bw.close();
		fw.close();

		fileName = DAndSMSSMeansCalculator.resultsFileName + "_L_D.txt";
		resultsFile = new File(dir.getAbsolutePath() + File.separatorChar
		        + fileName);
		fw = new FileWriter(resultsFile, true);
		bw = new BufferedWriter(fw);
		buffer = new StringBuffer();
		this.computeSingleStats("L", "D", buffer, this.lDDiff);
		bw.write(buffer.toString());
		// TODO
		bw.close();
		fw.close();

		fileName = DAndSMSSMeansCalculator.resultsFileName + "_SMSS_D.txt";
		resultsFile = new File(dir.getAbsolutePath() + File.separatorChar
		        + fileName);
		fw = new FileWriter(resultsFile, true);
		bw = new BufferedWriter(fw);
		buffer = new StringBuffer();
		this.computeSingleStats("SMSS", "D", buffer, this.smssDDiff);
		bw.write(buffer.toString());
		// TODO
		bw.close();
		fw.close();
	}
}
