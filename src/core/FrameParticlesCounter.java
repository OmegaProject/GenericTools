package core;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 
 */

/**
 * @author Alex Rigano
 * 
 */
public class FrameParticlesCounter {
	private final File f;
	private int totalParticles;
	private int numberOfFrames;

	public FrameParticlesCounter(final File file) {
		f = file;
		totalParticles = 0;
		numberOfFrames = 0;

		try {
			countParticles();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void countParticles() throws IOException {
		String line;
		final FileReader fr = new FileReader(f);
		final BufferedReader br = new BufferedReader(fr);

		line = br.readLine();
		while (line != null) {
			if (line.startsWith("Frame")) {
				numberOfFrames++;
				final int beginSubIndex = line.indexOf("Particles");
				final int endSubIndex = line.indexOf("PS");
				String subString = line.substring(beginSubIndex, endSubIndex);
				subString = subString.replace("Particles:", "");
				subString = subString.replace("\t", "");
				subString = subString.replaceFirst("/\\d++", "");
				final int particles = Integer.parseInt(subString);
				totalParticles += particles;
			}
			line = br.readLine();
		}

		br.close();
		fr.close();
	}

	public int getNumberOfFrames() {
		return numberOfFrames;
	}

	public int getTotalParticles() {
		return totalParticles;
	}

	public double getMeanNumberOfParticlePerFrame() {
		return totalParticles / numberOfFrames;
	}

	public static void main(final String[] args) {
		final String fileRegex = "PT_Frames_";
		final String dirRegex = "";
		final String path1 = "C:/Users/Alex Rigano/Documents/ICIMSI/Progetti/Omega/_NewBenchmarks/";
		final String path2 = "00_Seq/";
		// final String path3 =
		// "20121106_PrimaVersioneSequenzialeConStatistica/";
		final String path3 = "20121119_testNewDataset/";
		final String path4 = "DS2_I1_8bit_red/";
		// final String path5 = "dataset1_results/";
		// final Path path = Paths.get(path1, path2, path3, path4, path5);
		final Path path = Paths.get(path1, path2, path3, path4);
		// final Path path = Paths.get(path1, path2, path3);
		final File directory = path.toFile();
		final File[] directories = directory.listFiles();
		for (final File dir : directories) {
			System.out.println("###" + dir.getName() + "###");
			double meanParticles = 0;
			int fileCounter = 0;
			if (!dir.isDirectory() || !dir.getName().contains(dirRegex))
				continue;
			final String specificName = dir.getName().replace(dirRegex, "");
			final File[] files = dir.listFiles();
			for (final File f : files) {
				if (!f.getName().contains(fileRegex))
					continue;
				fileCounter++;
				final FrameParticlesCounter particleCounter = new FrameParticlesCounter(
						f);
				// System.out.println("File: " + f.getName() + " frames: "
				// + particleCounter.getNumberOfFrames()
				// + " meanParticles: "
				// + particleCounter.getMeanNumberOfParticlePerFrame());
				meanParticles += particleCounter
						.getMeanNumberOfParticlePerFrame();
				System.out.println("###" + f.getName() + "particles: "
						+ particleCounter.getMeanNumberOfParticlePerFrame()
						+ "###");
			}
			meanParticles /= fileCounter;
			System.out.println(dir.getName() + " specificName : "
					+ specificName + " particles: " + meanParticles);
		}
	}
}
