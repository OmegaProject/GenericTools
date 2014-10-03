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

public class DAndSMSSDataAggregator implements Runnable {

	private static boolean BROWNIAN_MODE = false;

	public final static String fileName1 = "D_values_";
	public final static String fileName2 = "SMSS_values_";

	public final static String resultsFileName = "DataAggregator_Results";
	public final static String logFileName = "DataAggregator_Log";

	private final File workingDir;
	// L D + data
	private final Map<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>> smssValuesMap;
	private final Map<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>> dValuesMap;

	private final Map<Double, Map<Double, List<Double>>> smssBrownValuesMap;
	private Map<Double, Map<Double, List<Double>>> dBrownValuesMap;

	private final StringBuffer errorLog;

	private final OmegaGenericToolGUI gui;

	public DAndSMSSDataAggregator(final File workingDir,
	        final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;

		this.smssValuesMap = new LinkedHashMap<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>>();
		this.dValuesMap = new LinkedHashMap<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>>();

		this.smssBrownValuesMap = new LinkedHashMap<Double, Map<Double, List<Double>>>();
		this.dBrownValuesMap = new LinkedHashMap<Double, Map<Double, List<Double>>>();

		this.errorLog = new StringBuffer();
		this.gui = gui;
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					DAndSMSSDataAggregator.this.gui.appendOutput(update);

				}
			});
		} catch (final InvocationTargetException ex) {
			ex.printStackTrace();
		} catch (final InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	private void importValuesFull(final File f) throws IOException {
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);

		Map<Double, Map<Double, Map<Double, Map<Double, List<Double>>>>> map;
		if (f.getName().contains(DAndSMSSDataAggregator.fileName1)) {
			map = this.dValuesMap;
		} else {
			map = this.smssValuesMap;
		}

		String line = br.readLine();
		while (line != null) {
			final String[] tokens = line.split(";");
			final String title = tokens[0];

			// 1 1.291059 20.000000 0.000000 0.000050
			// ID 0
			// SNR 1
			// L 2
			// SMSS 3
			// D 4
			final String[] titleTokens = title.split(" ");
			final Double snr = Double.valueOf(titleTokens[1]);
			final Double l = Double.valueOf(titleTokens[2]);
			final Double smss = Double.valueOf(titleTokens[3]);
			final Double d = Double.valueOf(titleTokens[4]);

			Map<Double, Map<Double, Map<Double, List<Double>>>> lengthMap;
			if (map.containsKey(snr)) {
				lengthMap = map.get(snr);
			} else {
				lengthMap = new LinkedHashMap<Double, Map<Double, Map<Double, List<Double>>>>();
			}

			Map<Double, Map<Double, List<Double>>> smssMap;
			if (lengthMap.containsKey(l)) {
				smssMap = lengthMap.get(l);
			} else {
				smssMap = new LinkedHashMap<Double, Map<Double, List<Double>>>();
			}

			Map<Double, List<Double>> dMap;
			if (smssMap.containsKey(smss)) {
				dMap = smssMap.get(smss);
			} else {
				dMap = new LinkedHashMap<Double, List<Double>>();

			}

			List<Double> values;
			if (dMap.containsKey(d)) {
				values = dMap.get(d);
			} else {
				values = new ArrayList<Double>();
			}

			for (int i = 2; i < tokens.length; i++) {
				final String token = tokens[i];
				values.add(Double.valueOf(token));
			}

			dMap.put(d, values);
			smssMap.put(smss, dMap);
			lengthMap.put(l, smssMap);
			map.put(snr, lengthMap);

			line = br.readLine();
		}

		br.close();
		fr.close();
	}

	private void importValues(final File f) throws IOException {
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);

		Map<Double, Map<Double, List<Double>>> map;
		if (f.getName().contains(DAndSMSSDataAggregator.fileName1)) {
			map = this.dBrownValuesMap;
		} else {
			map = this.smssBrownValuesMap;
		}

		String line = br.readLine();
		while (line != null) {
			final String[] tokens = line.split(";");
			final String title = tokens[0];

			// 1 1.291059 20.000000 0.000000 0.000050
			// ID 0
			// SNR 1
			// L 2
			// SMSS 3
			// D 4
			final String[] titleTokens = title.split(" ");
			final Double l = Double.valueOf(titleTokens[1]);
			final Double d = Double.valueOf(titleTokens[2]);

			Map<Double, List<Double>> lengthMap;
			if (map.containsKey(l)) {
				lengthMap = map.get(l);
			} else {
				lengthMap = new LinkedHashMap<Double, List<Double>>();
			}

			List<Double> values;
			if (lengthMap.containsKey(d)) {
				values = lengthMap.get(d);
			} else {
				values = new ArrayList<Double>();
			}

			for (int i = 2; i < tokens.length; i++) {
				final String token = tokens[i];
				values.add(Double.valueOf(token));
			}

			lengthMap.put(d, values);
			map.put(l, lengthMap);

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
				if (file.getName().startsWith(DAndSMSSDataAggregator.fileName2)) {
					this.updateGUI("File\t" + file.getName());
					this.errorLog.append("File\t" + file.getName() + "\n");

					try {
						if (!DAndSMSSDataAggregator.BROWNIAN_MODE) {
							this.importValuesFull(file);
						} else {
							this.importValues(file);
						}
					} catch (final Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

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
				if (file.getName().startsWith(DAndSMSSDataAggregator.fileName1)) {
					this.updateGUI("File\t" + file.getName());
					this.errorLog.append("File\t" + file.getName() + "\n");

					try {
						if (!DAndSMSSDataAggregator.BROWNIAN_MODE) {
							this.importValuesFull(file);
						} else {
							this.importValues(file);
						}
					} catch (final Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		try {
			this.updateGUI("Aggregating SMSS and D data");
			if (!DAndSMSSDataAggregator.BROWNIAN_MODE) {
				this.writeAggregatedValuesFull(this.workingDir);
			} else {
				this.writeAggregatedValues(this.workingDir);
			}

		} catch (final Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.dBrownValuesMap = null;

		try {
			this.writeLogFile(this.workingDir);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.updateGUI("Finished");
		this.errorLog.append("Finished\n");
	}

	private void writeLogFile(final File dir) throws IOException {
		final String fileName = DAndSMSSDataAggregator.logFileName + ".txt";
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + fileName);
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.errorLog.toString());
		bw.close();
		fw.close();
	}

	private void writeAggregatedValuesFull(final File dir) throws IOException {
		final String fileName = DAndSMSSDataAggregator.resultsFileName + ".txt";
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + fileName);
		final FileWriter fw = new FileWriter(resultsFile, true);
		final BufferedWriter bw = new BufferedWriter(fw);

		for (final Double inputSNR : this.smssValuesMap.keySet()) {
			final Map<Double, Map<Double, Map<Double, List<Double>>>> lenghtSMSSMap = this.smssValuesMap
			        .get(inputSNR);
			final Map<Double, Map<Double, Map<Double, List<Double>>>> lenghtDMap = this.dValuesMap
			        .get(inputSNR);
			for (final Double inputL : lenghtSMSSMap.keySet()) {
				Integer rowCounter = 1;
				final Map<Double, Map<Double, List<Double>>> smssSMSSMap = lenghtSMSSMap
				        .get(inputL);
				final Map<Double, Map<Double, List<Double>>> smssDMap = lenghtDMap
				        .get(inputL);
				for (final Double inputSMSS : smssSMSSMap.keySet()) {
					final Map<Double, List<Double>> dSMSSMap = smssSMSSMap
					        .get(inputSMSS);
					final Map<Double, List<Double>> dDMap = smssDMap
					        .get(inputSMSS);
					for (final Double inputD : dSMSSMap.keySet()) {
						final List<Double> smssValues = dSMSSMap.get(inputD);
						final List<Double> dValues = dDMap.get(inputD);
						for (int i = 0; i < smssValues.size(); i++) {
							final Double outputSMSS = smssValues.get(i);
							final Double outputD = dValues.get(i);

							bw.write(String.valueOf(rowCounter));
							bw.write("\t");
							bw.write(String.valueOf(inputSMSS));
							bw.write("\t");
							bw.write(String.valueOf(inputL));
							bw.write("\t");
							bw.write(String.valueOf(inputD));
							bw.write("\t\t");
							bw.write(String.valueOf(outputSMSS));
							bw.write("\t\t\t\t");
							bw.write(String.valueOf(outputD));
							bw.write("\n");
							rowCounter++;
						}
					}
				}
			}
		}
		bw.close();
		fw.close();
	}

	private void writeAggregatedValues(final File dir) throws IOException {
		final String fileName = DAndSMSSDataAggregator.resultsFileName + ".txt";
		final File resultsFile = new File(dir.getAbsolutePath()
		        + File.separatorChar + fileName);
		final FileWriter fw = new FileWriter(resultsFile, true);
		final BufferedWriter bw = new BufferedWriter(fw);

		final Double inputSMSS = 0.5;
		for (final Double inputL : this.smssBrownValuesMap.keySet()) {
			Integer rowCounter = 1;
			final Map<Double, List<Double>> lenghtSMSSMap = this.smssBrownValuesMap
			        .get(inputL);
			final Map<Double, List<Double>> lenghtDMap = this.dBrownValuesMap
			        .get(inputL);
			for (final Double inputD : lenghtSMSSMap.keySet()) {
				final List<Double> smssValues = lenghtSMSSMap.get(inputD);
				final List<Double> dValues = lenghtDMap.get(inputD);
				for (int i = 0; i < smssValues.size(); i++) {
					final Double outputSMSS = smssValues.get(i);
					final Double outputD = dValues.get(i);

					bw.write(String.valueOf(rowCounter));
					bw.write("\t");
					bw.write(String.valueOf(inputSMSS));
					bw.write("\t");
					bw.write(String.valueOf(inputL));
					bw.write("\t");
					bw.write(String.valueOf(inputD));
					bw.write("\t\t");
					bw.write(String.valueOf(outputSMSS));
					bw.write("\t\t\t\t");
					bw.write(String.valueOf(outputD));
					bw.write("\n");
					rowCounter++;
				}
			}
		}

		// TODO
		bw.close();
		fw.close();

	}
}
