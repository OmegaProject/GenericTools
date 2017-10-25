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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SDBatchRunnerSingleFolder implements Runnable {
	
	private final File inputDir, outputDir;
	private final Double cutoff;
	private final Float percentile, threshold;
	private final boolean percAbs;
	private final int radius, c, z;

	private final OmegaGenericToolGUI gui;
	
	// RADIUS 3
	// CUTOFF 0.001
	// PERCENTILE 0.500

	public SDBatchRunnerSingleFolder(final OmegaGenericToolGUI gui,
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
		for (final File f1 : this.inputDir.listFiles()) {
			if (!f1.isDirectory()) {
				continue;
			}
			
			final File outputDir1 = new File(this.outputDir + File.separator
					+ f1.getName());
			if (!outputDir1.exists()) {
				outputDir1.mkdir();
			}

			final File outputDir2 = new File(outputDir1.getAbsolutePath()
					+ File.separator + "logs");
			if (!outputDir2.exists()) {
				outputDir2.mkdir();
			}

			final List<SDWorker2> workers = new ArrayList<SDWorker2>();
			Float gMin = Float.MAX_VALUE, gMax = 0F;
			final Map<Integer, ImagePlus> images = new LinkedHashMap<Integer, ImagePlus>();
			final File test = new File(outputDir2.getAbsolutePath()
					+ File.separator + "SD_Output.txt");
			// final File test = new File(f1.getAbsoluteFile() + File.separator
			// + "logs" + File.separator + "SD_Output.txt");
			if (test.exists()) {
				this.gui.appendOutput(f1.getName() + " previously analyzed");
				try {
					bwl.write(f1.getName() + " previously analyzed\n");
				} catch (final IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				continue;
			}
			for (final File f3 : f1.listFiles()) {
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
			
			for (final Integer index : images.keySet()) {
				final ImagePlus is = images.get(index);
				final ImageStack lis = is.getImageStack();
				final SDWorker2 worker = new SDWorker2(lis, index - 1,
						this.radius, this.cutoff, this.percentile,
						this.threshold, this.percAbs, this.c, this.z);
				worker.setGlobalMax(gMax);
				worker.setGlobalMin(gMin);
				// exec.submit(worker);
				exec.execute(worker);
				workers.add(worker);
			}

			this.gui.appendOutput(f1.getName() + " all workers launched");
			try {
				bwl.write(f1.getName() + " all workers launched\n");
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

			this.gui.appendOutput(f1.getName() + " all workers completed");
			try {
				bwl.write(f1.getName() + " all workers completed\n");
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int counter = 0;
			final File output = new File(outputDir2.getAbsolutePath()
					+ File.separator + "SD_Output.txt");
			// final File output = new File(f1.getAbsoluteFile() +
			// File.separator
			// + "logs" + File.separator + "SD_Output.txt");
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
					final List<OmegaROI> particles = worker
							.getResultingParticles();
					final Map<OmegaROI, Map<String, Object>> values = worker
							.getParticlesAdditionalValues();
					final StringBuffer sb = new StringBuffer();
					for (final OmegaROI roi : particles) {
						sb.append(roi.getFrameIndex());
						sb.append("\t");
						sb.append(roi.getX());
						sb.append("\t");
						sb.append(roi.getY());
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
			
			this.gui.appendOutput(f1.getName() + " finished");
			try {
				bwl.write(f1.getName() + " finished\n");
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
		}
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
