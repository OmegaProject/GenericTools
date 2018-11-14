package core;

import gui.OmegaGenericToolGUI;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.SwingUtilities;

public class DAndSMSSFilesConsolidator implements Runnable {

	private final File workingDir;

	private final String D_FOLDER = "D";
	private final String SMSS_FOLDER = "SMSS";

	private final String SEPARATOR = ";";

	// public final static String resultsFileName = "FileConsolidator_Results";
	public final static String logFileName = "FileConsolidator_Log";

	// L D + data
	private final Map<Double, Map<Double, Map<Double, Map<Double, Double>>>> smssValuesMap;
	private final Map<Double, Map<Double, Map<Double, Map<Double, Double>>>> dValuesMap;

	private final StringBuffer errorLog;

	private final OmegaGenericToolGUI gui;

	public DAndSMSSFilesConsolidator(final File workingDir,
			final OmegaGenericToolGUI gui) {
		this.workingDir = workingDir;

		this.smssValuesMap = new LinkedHashMap<Double, Map<Double, Map<Double, Map<Double, Double>>>>();
		this.dValuesMap = new LinkedHashMap<Double, Map<Double, Map<Double, Map<Double, Double>>>>();

		this.errorLog = new StringBuffer();
		this.gui = gui;
	}

	public void updateGUI(final String update) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					DAndSMSSFilesConsolidator.this.gui.appendOutput(update);

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
		boolean isD = false;
		Map<Double, Map<Double, Map<Double, Map<Double, Double>>>> map;
		if (f.getName().contains(this.D_FOLDER)) {
			map = this.dValuesMap;
			isD = true;
		} else {
			map = this.smssValuesMap;
		}

		String line = br.readLine();
		while (line != null) {
			final String[] tokens = line.split(";");
			final String title = tokens[0];

			// 1 1.291059 10.000000 0.000000 0.000050;***;
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

			double value;
			if (isD) {
				value = d;
			} else {
				value = smss;
			}
			Map<Double, Map<Double, Map<Double, Double>>> lengthMap;
			if (map.containsKey(snr)) {
				lengthMap = map.get(snr);
			} else {
				lengthMap = new LinkedHashMap<Double, Map<Double, Map<Double, Double>>>();
			}

			Map<Double, Map<Double, Double>> smssMap;
			if (lengthMap.containsKey(l)) {
				smssMap = lengthMap.get(l);
			} else {
				smssMap = new LinkedHashMap<Double, Map<Double, Double>>();
			}

			Map<Double, Double> dMap;
			if (smssMap.containsKey(smss)) {
				dMap = smssMap.get(smss);
			} else {
				dMap = new LinkedHashMap<Double, Double>();

			}
			
			final double[] values = new double[tokens.length - 2];
			for (int i = 2; i < tokens.length; i++) {
				final String token = tokens[i];
				values[i - 2] = value - Double.valueOf(token);
			}
			final Double stdev = Stats2.standardDeviationN(values);

			dMap.put(d, stdev);
			smssMap.put(smss, dMap);
			lengthMap.put(l, smssMap);
			map.put(snr, lengthMap);

			line = br.readLine();
		}

		br.close();
		fr.close();
	}

	@Override
	public void run() {
		this.updateGUI("Working dir\t" + this.workingDir.getName());
		this.errorLog
				.append("Working dir\t" + this.workingDir.getName() + "\n");
		final String[] tokens = this.workingDir.getName().split("_");
		final String date = tokens[0];
		final String tag1 = tokens[1];
		final String ld = tokens[2];
		final String tag2 = tokens[3];
		for (final File set : this.workingDir.listFiles()) {
			if (set.isFile()) {
				continue;
			}
			if (!set.getName().equals(this.D_FOLDER)
					&& !set.getName().equals(this.SMSS_FOLDER)) {
				continue;
			}

			this.updateGUI("Folder\t" + set.getName());
			this.errorLog.append("Folder\t" + set.getName() + "\n");
			for (final File file : set.listFiles()) {
				if (!file.isFile()) {
					continue;
				}
				this.updateGUI("File\t" + file.getName());
				this.errorLog.append("File\t" + file.getName() + "\n");
				try {
					this.importValuesFull(file);
				} catch (final IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
			}
		}

		try {
			this.writeConsolidateValuesFull(date, ld, tag1, tag2,
					this.workingDir);
		} catch (final IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}

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
		String fileName = DAndSMSSFilesConsolidator.logFileName;
		fileName += ".txt";
		final File resultsFile = new File(dir.getAbsolutePath()
				+ File.separatorChar + fileName);
		final FileWriter fw = new FileWriter(resultsFile);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write(this.errorLog.toString());
		bw.close();
		fw.close();
	}

	private void writeConsolidateValuesFull(final String date, final String ld,
			final String tag1, final String tag2, final File dir)
			throws IOException {
		final String fileName = date + "_" + ld + "_interpolation_data_";
		String fileNameD = fileName + this.D_FOLDER;
		String fileNameSMSS = fileName + this.SMSS_FOLDER;
		fileNameD += ".csv";
		fileNameSMSS += ".csv";
		final File resultsFileD = new File(dir.getAbsolutePath()
				+ File.separatorChar + fileNameD);
		final File resultsFileSMSS = new File(dir.getAbsolutePath()
				+ File.separatorChar + fileNameSMSS);
		final FileWriter fwSMSS = new FileWriter(resultsFileSMSS, true);
		final BufferedWriter bwSMSS = new BufferedWriter(fwSMSS);
		final FileWriter fwD = new FileWriter(resultsFileD, true);
		final BufferedWriter bwD = new BufferedWriter(fwD);

		for (final Double inputSNR : this.smssValuesMap.keySet()) {
			final Map<Double, Map<Double, Map<Double, Double>>> lenghtSMSSMap = this.smssValuesMap
					.get(inputSNR);
			final Map<Double, Map<Double, Map<Double, Double>>> lenghtDMap = this.dValuesMap
					.get(inputSNR);
			for (final Double inputL : lenghtSMSSMap.keySet()) {
				final Map<Double, Map<Double, Double>> smssSMSSMap = lenghtSMSSMap
						.get(inputL);
				final Map<Double, Map<Double, Double>> smssDMap = lenghtDMap
						.get(inputL);
				for (final Double inputSMSS : smssSMSSMap.keySet()) {
					final Map<Double, Double> dSMSSMap = smssSMSSMap
							.get(inputSMSS);
					final Map<Double, Double> dDMap = smssDMap.get(inputSMSS);
					for (final Double inputD : dSMSSMap.keySet()) {
						final Double outputSMSS = dSMSSMap.get(inputD);
						final Double outputD = dDMap.get(inputD);
						
						bwD.write(String.valueOf(inputSNR));
						bwSMSS.write(String.valueOf(inputSNR));
						bwD.write(String.valueOf(this.SEPARATOR));
						bwSMSS.write(String.valueOf(this.SEPARATOR));
						bwD.write(String.valueOf(inputL));
						bwSMSS.write(String.valueOf(inputL));
						bwD.write(String.valueOf(this.SEPARATOR));
						bwSMSS.write(String.valueOf(this.SEPARATOR));
						bwD.write(String.valueOf(inputSMSS));
						bwSMSS.write(String.valueOf(inputSMSS));
						bwD.write(String.valueOf(this.SEPARATOR));
						bwSMSS.write(String.valueOf(this.SEPARATOR));
						bwD.write(String.valueOf(inputD));
						bwSMSS.write(String.valueOf(inputD));
						bwD.write(String.valueOf(this.SEPARATOR));
						bwSMSS.write(String.valueOf(this.SEPARATOR));
						bwD.write(String.valueOf(outputD));
						bwSMSS.write(String.valueOf(outputSMSS));
						bwD.write(String.valueOf("\n"));
						bwSMSS.write(String.valueOf("\n"));
					}
				}
			}
		}
		bwSMSS.close();
		fwSMSS.close();
		bwD.close();
		fwD.close();
	}
}
