package core;

import ij.ImagePlus;
import ij.ImageStack;

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

import edu.umassmed.omega.commons.data.trajectoryElements.OmegaROI;
import edu.umassmed.omega.sdSbalzariniPlugin.runnable.SDWorker2;

public class SDBatchRunnerMultiFolder implements Runnable {

	private final File inputDir, outputDir;
	private final Double cutoff;
	private final Float percentile, threshold;
	private final boolean percAbs;
	private final int radius, c, z;
	
	public SDBatchRunnerMultiFolder(final File inputDir, final File outputDir,
			final int radius, final double cutoff, final float percentile,
			final float threshold, final boolean percAbs, final int channel,
			final int plane) {
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.radius = radius;
		this.cutoff = cutoff;
		this.percentile = percentile;
		this.threshold = threshold;
		this.percAbs = percAbs;
		this.c = channel;
		this.z = plane;
	}
	
	@Override
	public void run() {
		if (!this.inputDir.isDirectory())
			return;
		if (!this.outputDir.isDirectory())
			return;
		
		final File log = new File(this.outputDir.getAbsoluteFile()
				+ File.separator + "SDLog.txt");
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

			try {
				bwl.write(f1.getName() + " starting\n");
			} catch (final IOException e4) {
				// TODO Auto-generated catch block
				e4.printStackTrace();
			}
			for (final File f2 : f1.listFiles()) {
				if (!f2.isDirectory()) {
					continue;
				}
				try {
					bwl.write(f2.getName() + " starting\n");
				} catch (final IOException e4) {
					// TODO Auto-generated catch block
					e4.printStackTrace();
				}
				final File outputDir2 = new File(outputDir1.getAbsolutePath()
						+ File.separator + f2.getName());
				if (!outputDir2.exists()) {
					outputDir2.mkdir();
				}
				final File outputDir3 = new File(outputDir2.getAbsolutePath()
						+ File.separator + "logs");
				if (!outputDir3.exists()) {
					outputDir3.mkdir();
				}
				final List<SDWorker2> workers = new ArrayList<SDWorker2>();
				Float gMin = Float.MAX_VALUE, gMax = 0F;
				final Map<Integer, ImagePlus> images = new LinkedHashMap<Integer, ImagePlus>();
				final File test = new File(outputDir3.getAbsolutePath()
						+ File.separator + "SD_Output.txt");
				// final File test = new File(f2.getAbsoluteFile()
				// + File.separator + "logs" + File.separator
				// + "SDOutput.txt");
				if (test.exists()) {
					try {
						bwl.write(f2.getName() + " previously analyzed\n");
					} catch (final IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					continue;
				}
				for (final File f3 : f2.listFiles()) {
					if (f3.isDirectory()) {
						continue;
					}
					final String name = f3.getName();
					String num = name.replace("test_", "");
					num = num.replace(".tif", "");
					final Integer index = Integer.valueOf(num);
					final ImagePlus is = new ImagePlus(f3.getAbsolutePath());
					Float min = Float.MAX_VALUE, max = 0F;
					min = (float) is.getStatistics().min;
					max = (float) is.getStatistics().max;
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
				
				try {
					bwl.write(f2.getName() + " all workers launched\n");
				} catch (final IOException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
				try {
					Thread.sleep(600);
				} catch (final InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
				
				try {
					bwl.write(f2.getName() + " all workers completed\n");
				} catch (final IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				int counter = 0;
				final File output = new File(outputDir3.getAbsolutePath()
						+ File.separator + "SD_Output.txt");
				// final File output = new File(f2.getAbsoluteFile()
				// + File.separator + "logs" + File.separator
				// + "SD_Output.txt");
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
							final Map<String, Object> roiValues = values
									.get(roi);
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

				try {
					bwl.write(f2.getName() + " finished\n");
					bwl.flush();
				} catch (final IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					Thread.sleep(1800);
				} catch (final InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				bwl.write(f1.getName() + " finished\n");
				bwl.flush();
			} catch (final IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				Thread.sleep(3600);
			} catch (final InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			bwl.close();
			fwl.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
