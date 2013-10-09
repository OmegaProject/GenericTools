package core;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.RenderedOp;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

public class ImageComparator {
	private final File workingDir;
	private final File compareDir;

	private Integer numberOfFrames;
	private Integer numberOfDifferentPixels;
	private Integer totalDifference;
	private Integer maxDifference;
	private Integer minDifference;
	private Double meanDifference;
	private Integer totalNumberOfPixels;
	private Double meanDifferentPixelsPerImage;

	public ImageComparator(final File workingDir, final File compareDir) {
		this.workingDir = workingDir;
		this.compareDir = compareDir;
		this.totalNumberOfPixels = 0;
		this.numberOfDifferentPixels = 0;
		this.numberOfFrames = 0;
		this.meanDifferentPixelsPerImage = 0.0;
		this.totalDifference = 0;
		this.meanDifference = 0.0;
		this.maxDifference = -1;
		this.minDifference = -1;
	}

	public Integer getNumberOfFrames() {
		return this.numberOfFrames;
	}

	public Integer getNumberOfDifferentPixels() {
		return this.numberOfDifferentPixels;
	}

	public Integer getTotalNumberOfPixels() {
		return this.totalNumberOfPixels;
	}

	public Integer getTotalDifference() {
		return this.totalDifference;
	}

	public Double getMeanDifference() {
		return this.meanDifference;
	}

	public Integer getMaxDifference() {
		return this.maxDifference;
	}

	public Integer getMinDifference() {
		return this.minDifference;
	}

	public Double getMeanDifferentPixelPerImage() {
		return this.meanDifferentPixelsPerImage;
	}

	public void compareImages(final boolean isDistr) throws IOException {
		String f1Name = "";
		String f2Name = "";
		String f1ext = "";
		String f2ext = "";
		int index1 = -1;
		int index2 = -1;

		for (final File f1 : this.workingDir.listFiles()) {
			index1 = f1.getName().lastIndexOf("_");
			index2 = f1.getName().lastIndexOf(".");
			f1ext = f1.getName().substring(index2 + 1);
			if (!f1ext.equals("tiff") & !f1ext.equals("tif")) {
				continue;
			}
			f1Name = f1.getName().substring(index1 + 1, index2);
			for (final File f2 : this.compareDir.listFiles()) {
				index1 = f2.getName().lastIndexOf("_");
				index2 = f2.getName().lastIndexOf(".");
				f2Name = f2.getName().substring(index1 + 1, index2);
				f2ext = f2.getName().substring(index2 + 1);
				if (!f2ext.equals("tiff") & !f2ext.equals("tif")) {
					continue;
				}
				if (f1Name.equals(f2Name)) {
					if (isDistr) {
						this.compareImageWithDistr(f1, f2);
					} else {
						this.compareImage(f1, f2);
					}
					this.numberOfFrames++;
				}
			}
		}

		this.meanDifference = (double) this.totalDifference
		        / (double) this.numberOfDifferentPixels;
		this.meanDifferentPixelsPerImage = (double) this.numberOfDifferentPixels
		        / (double) this.numberOfFrames;
	}

	private void compareImage(final File originalFile, final File compareFile)
	        throws IOException {

		final RenderedOp ROImg1 = JAI
		        .create("fileload", originalFile.getPath());
		final RenderedOp ROImg2 = JAI.create("fileload", compareFile.getPath());

		final int bitSize1 = ROImg1.getColorModel().getPixelSize();
		final int bitSize2 = ROImg2.getColorModel().getPixelSize();

		if (bitSize1 != bitSize2)
			return; // Should be error

		final int maxValue = (2 ^ bitSize1) - 1;

		if (this.minDifference == -1) {
			this.minDifference = maxValue;
		}
		if (this.maxDifference == -1) {
			this.maxDifference = 0;
		}

		final int h = ROImg1.getHeight();
		final int w = ROImg1.getWidth();
		final Raster r1 = ROImg1.getData();
		final Raster r2 = ROImg2.getData();
		final DataBuffer db1 = r1.getDataBuffer();
		final DataBuffer db2 = r2.getDataBuffer();

		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				final int pix1 = db1.getElem((y * w) + x);
				final int pix2 = db2.getElem((y * w) + x);
				// r1.getPixel(x, y, iArray);
				// r2.getPixel(x, y, iArray);
				if (pix1 != pix2) {
					this.numberOfDifferentPixels++;
					final Integer diff = pix2 - pix1;
					if (this.minDifference > diff) {
						this.minDifference = diff;
					}
					if (this.maxDifference < diff) {
						this.maxDifference = diff;
					}
					this.totalDifference += Math.abs(diff);
				}
			}
		}
		this.totalNumberOfPixels += h * w;
	}

	private void compareImageWithDistr(final File originalFile,
	        final File compareFile) throws IOException {
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

		final int maxValue = (int) (Math.pow(2, bitSize1) - 1);

		if (bitSize1 != bitSize2)
			return; // Should be error

		if (this.minDifference == -1) {
			this.minDifference = maxValue;
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

		if ((h1 != h2) | (w1 != w2))
			return; // Should be error

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
				} else if (newValue1 > maxValue) {
					newValue1 = (double) maxValue;
				}

				final Integer oldValue2 = pix2;
				// final Poisson distr2 = new Poisson(oldValue2, random);
				// Double newValue2 = distr2.nextDouble();
				Double newValue2 = oldValue2 + randomValues.get(counter);
				if (newValue2 < 0) {
					newValue2 = 0.0;
				} else if (newValue2 > maxValue) {
					newValue2 = (double) maxValue;
				}
				final Integer newVal1 = (int) Math.rint(newValue1);
				final Integer newVal2 = (int) Math.rint(newValue2);
				if (!newVal1.equals(newVal2)) {
					this.numberOfDifferentPixels++;
					final Integer diff = newVal2 - newVal1;
					if (this.minDifference > diff) {
						this.minDifference = diff;
					}
					if (this.maxDifference < diff) {
						this.maxDifference = diff;
					}
					this.totalDifference += Math.abs(diff);
				}
				counter++;
			}
		}
		this.totalNumberOfPixels += h1 * w1;
	}
}
