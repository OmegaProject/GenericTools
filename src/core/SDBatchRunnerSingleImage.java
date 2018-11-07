package core;

import edu.umassmed.omega.commons.data.trajectoryElements.OmegaROI;
import edu.umassmed.omega.sdSbalzariniPlugin.runnable.SDWorker2;
import gui.OmegaGenericToolGUI;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.StackStatistics;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SDBatchRunnerSingleImage implements Runnable {
	
	private final File inputDir, outputDir;
	private final Double cutoff;
	private final Float percentile, threshold;
	private final boolean percAbs;
	private final int radius, c, z;
	private Float gMin, gMax;

	private final OmegaGenericToolGUI gui;
	
	// RADIUS 3
	// CUTOFF 0.001
	// PERCENTILE 0.500

	public SDBatchRunnerSingleImage(final OmegaGenericToolGUI gui,
			final File inputDir, final File outputDir, final int radius,
			final double cutoff, final float percentile, final float threshold,
			final boolean percAbs, final int channel, final int plane) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.radius = radius;
		this.cutoff = cutoff;
		this.percentile = percentile;
		this.threshold = threshold;
		this.percAbs = percAbs;
		this.c = channel;
		this.z = plane;

		this.gMin = null;
		this.gMax = null;
		
		this.gui = gui;
	}

	@Override
	public void run() {
		if (!this.inputDir.isDirectory())
			return;
		if (!this.outputDir.isDirectory())
			return;
		
		this.gui.appendOutput("Launched on " + this.outputDir.getName());
		
		final File log = new File(this.outputDir.getAbsoluteFile()
				+ File.separator + "SD_Log.txt");
		FileWriter fwl = null;
		try {
			fwl = new FileWriter(log, false);
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (fwl == null)
			return;
		final BufferedWriter bwl = new BufferedWriter(fwl);
		final ExecutorService exec = Executors.newFixedThreadPool(5);

		final List<SDWorker2> workers = new ArrayList<SDWorker2>();
		Float gMin = Float.MAX_VALUE, gMax = 0F;
		final Map<Integer, ImagePlus> images = new LinkedHashMap<Integer, ImagePlus>();
		final File outputDir1 = new File(this.outputDir + File.separator
				+ this.inputDir.getName());
		if (!outputDir1.exists()) {
			outputDir1.mkdir();
		}
		final File outputDir2 = new File(outputDir1.getAbsolutePath()
				+ File.separator + "logs");
		if (!outputDir2.exists()) {
			outputDir2.mkdir();
		}
		final File test = new File(outputDir2.getAbsoluteFile()
				+ File.separator + "SD_OutputSingle.txt");
		// final File test = new File(this.inputDir.getAbsoluteFile()
		// + File.separator + "logs" + File.separator
		// + "SDOutputSingle.txt");
		if (test.exists()) {
			this.gui.appendOutput(this.inputDir.getName()
					+ " previously analyzed");
			try {
				bwl.write(this.inputDir.getName() + " previously analyzed\n");
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		for (final File f3 : this.inputDir.listFiles()) {
			if (f3.isDirectory() || !f3.getName().contains(".tif")) {
				continue;
			}
			final String name = f3.getName();
			String num = name.replace("test_", "");
			num = num.replace(".tif", "");
			final Integer index = Integer.valueOf(num);
			final ImagePlus is = new ImagePlus(f3.getAbsolutePath());
			Float min = Float.MAX_VALUE, max = 0F;
			final StackStatistics stack_stats = new StackStatistics(is);
			max = (float) stack_stats.max;
			min = (float) stack_stats.min;
			if (gMin > min) {
				gMin = min;
			}
			if (gMax < max) {
				gMax = max;
			}
			images.put(index, is);
		}

		try {
			Thread.sleep(600);
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.gMin = gMin;
		this.gMax = gMax;
		
		this.createReadmeFile(this.outputDir.getAbsolutePath());
		
		for (final Integer index : images.keySet()) {
			final ImagePlus is = images.get(index);
			final ImageStack lis = is.getImageStack();
			final SDWorker2 worker = new SDWorker2(lis, index - 1, this.radius,
					this.cutoff, this.percentile, this.threshold, this.percAbs,
					this.c, this.z);
			worker.setGlobalMax(gMax);
			worker.setGlobalMin(gMin);
			// exec.submit(worker);
			exec.execute(worker);
			workers.add(worker);
		}

		this.gui.appendOutput(this.inputDir.getName() + " all workers launched");
		try {
			bwl.write(this.inputDir.getName() + " all workers launched\n");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// try {
		// Thread.sleep(600);
		// } catch (final InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		
		final List<SDWorker2> completedWorkers = new ArrayList<SDWorker2>();
		while (!workers.isEmpty()) {
			for (final SDWorker2 runnable : workers) {
				if (!runnable.isJobCompleted()) {
					continue;
				}
				completedWorkers.add(runnable);
			}
			workers.removeAll(completedWorkers);
		}

		this.gui.appendOutput(this.inputDir.getName()
				+ " all workers completed");
		try {
			bwl.write(this.inputDir.getName() + " all workers completed\n");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int counter = 0;
		final File output = new File(outputDir2.getAbsolutePath()
				+ File.separator + "SD_OutputSingle.txt");
		// final File output = new File(this.inputDir.getAbsoluteFile()
		// + File.separator + "logs" + File.separator
		// + "SDOutputSingle.txt");
		FileWriter fw = null;
		try {
			fw = new FileWriter(output, false);
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (fw == null)
			return;
		final BufferedWriter bw = new BufferedWriter(fw);
		while (counter < completedWorkers.size()) {
			for (final SDWorker2 worker : completedWorkers) {
				if (worker.getIndex() != counter) {
					continue;
				}
				counter++;
				final List<OmegaROI> particles = worker.getResultingParticles();
				final Map<OmegaROI, Map<String, Object>> values = worker
						.getParticlesAdditionalValues();
				for (final OmegaROI roi : particles) {
					final MathContext mc = new MathContext(7,
							RoundingMode.HALF_UP);
					BigDecimal bdX = new BigDecimal(roi.getX());
					bdX = bdX.round(mc);
					BigDecimal bdY = new BigDecimal(roi.getY());
					bdY = bdY.round(mc);
					final StringBuffer sb = new StringBuffer();
					sb.append(roi.getFrameIndex());
					sb.append("\t");
					sb.append(bdX.toPlainString());
					sb.append("\t");
					sb.append(bdY.toPlainString());
					sb.append("\t");
					final Map<String, Object> roiValues = values.get(roi);
					for (final String s : roiValues.keySet()) {
						sb.append(roiValues.get(s));
						sb.append("\t");
					}
					sb.append("\n");
					try {
						bw.write(sb.toString());
					} catch (final IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		try {
			bw.close();
			fw.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.gui.appendOutput(this.inputDir.getName() + " finished");
		try {
			bwl.write(this.inputDir.getName() + " finished\n");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// try {
		// Thread.sleep(1800);
		// } catch (final InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		exec.shutdown();
		this.gui.appendOutput("Try to shut down exec");
		try {
			bwl.write("Try to shut down exec\n");
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			if (!exec.awaitTermination(5, TimeUnit.MINUTES)) {
				this.gui.appendOutput("Exec not shutted down after 5 minutes");
				try {
					bwl.write("Exec not shutted down after 5 minutes\n");
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				exec.shutdownNow();
				System.exit(0);
			}
		} catch (final InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			bwl.close();
			fwl.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.gui.appendOutput("###############################################");
	}

	private void createReadmeFile(final String mainDir) {
		final File readme = new File(mainDir + File.separator + "SD_Readme.txt");
		FileWriter fwl = null;
		try {
			fwl = new FileWriter(readme, false);
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (fwl == null)
			return;
		final BufferedWriter bwl = new BufferedWriter(fwl);
		final String date = DateFormat.getInstance().format(
				Calendar.getInstance().getTime());
		try {
			bwl.write(date);
			bwl.write("\n");
			bwl.write("Input:\t");
			bwl.write(this.inputDir.getAbsolutePath());
			bwl.write("\n");
			bwl.write("Parameters:\n");
			bwl.write("Radius: " + this.radius + "\n");
			bwl.write("Cutoff: " + this.cutoff + "\n");
			bwl.write("Percentile: " + this.percentile + "\n");
			bwl.write("Threshold: " + this.threshold + "\n");
			bwl.write("Perc abs: " + this.percAbs + "\n");
			bwl.write("Channel: " + this.c + "\n");
			bwl.write("Plane: " + this.z + "\n");
			bwl.write("Global min: " + this.gMin + "\n");
			bwl.write("Global max: " + this.gMax + "\n");
		} catch (final IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			bwl.close();
			fwl.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
