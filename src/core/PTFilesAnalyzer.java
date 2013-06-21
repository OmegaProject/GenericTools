package core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.Format;

public class PTFilesAnalyzer {
	private final File workingDir;
	private File framesFile;
	private File trajsFile;
	private Double totalSNR_C;
	private Double totalSNR_C_M;
	private Double totalSNR_B_P;
	private Double totalSNR_B_P_M;
	private Double totalSNR_B_G;
	private Double totalSNR_B_G_M;
	private Double meanSNR_C;
	private Double meanSNR_C_M;
	private Double meanSNR_B_P;
	private Double meanSNR_B_P_M;
	private Double meanSNR_B_G;
	private Double meanSNR_B_G_M;
	private int numberOfFrames;
	private Double totalParticleSize;
	private int totalParticles;
	private int meanParticlePerFrame;
	private Double meanParticleSize;

	private boolean generateTrajectories;

	public PTFilesAnalyzer(final File workingDir) {
		this.workingDir = workingDir;
		this.totalSNR_C = 0.0;
		this.totalSNR_C_M = 0.0;
		this.totalSNR_B_P = 0.0;
		this.totalSNR_B_P_M = 0.0;
		this.totalSNR_B_G = 0.0;
		this.totalSNR_B_G_M = 0.0;
		this.totalParticleSize = 0.0;
		this.totalParticles = 0;
		this.meanParticleSize = 0.0;
		this.numberOfFrames = 0;
	}

	public void analyzeFramesFile(final File framesFile) throws IOException {
		this.framesFile = framesFile;
		String line;
		final FileReader fr = new FileReader(this.framesFile);
		final BufferedReader br = new BufferedReader(fr);

		line = br.readLine();
		while (line != null) {
			if (line.startsWith("\tSNR:")) {
				this.numberOfFrames++;
				final int beginSubIndex = line.indexOf("SNR_C:");
				final int endSubIndex = line.indexOf("SNR_C_m");
				String subString = line.substring(beginSubIndex, endSubIndex);
				subString = subString.replace("SNR_C:", "");
				subString = subString.replace("\t", "");
				// subString = subString.replace("\t", "");
				// subString = subString.replaceFirst("/\\d++", "");
				final Double SNR_C = Double.parseDouble(subString);
				this.totalSNR_C += SNR_C;

				final int begin2SubIndex = line.indexOf("SNR_C_m:");
				final int end2SubIndex = line.length();
				String subString2 = line
				        .substring(begin2SubIndex, end2SubIndex);
				subString2 = subString2.replace("SNR_C_m:", "");
				final Double SNR__C_m = Double.parseDouble(subString2);
				this.totalSNR_C_M += SNR__C_m;
			} else if (line.startsWith("\tSNR_B_P:")) {
				final int beginSubIndex = line.indexOf("SNR_B_P:");
				final int endSubIndex = line.indexOf("SNR_B_P_m");
				String subString = line.substring(beginSubIndex, endSubIndex);
				subString = subString.replace("SNR_B_P:", "");
				subString = subString.replace("\t", "");
				// subString = subString.replace("\t", "");
				// subString = subString.replaceFirst("/\\d++", "");
				final Double SNR_B_P = Double.parseDouble(subString);
				this.totalSNR_B_P += SNR_B_P;

				final int begin2SubIndex = line.indexOf("SNR_B_P_m:");
				final int end2SubIndex = line.indexOf("SNR_B_G");
				String subString2 = line
				        .substring(begin2SubIndex, end2SubIndex);
				subString2 = subString2.replace("SNR_B_P_m:", "");
				subString2 = subString2.replace("\t", "");
				final Double SNR_B_P_m = Double.parseDouble(subString2);
				this.totalSNR_B_P_M += SNR_B_P_m;

				final int begin3SubIndex = line.indexOf("SNR_B_G:");
				final int end3SubIndex = line.indexOf("SNR_B_G_m");
				String subString3 = line
				        .substring(begin3SubIndex, end3SubIndex);
				subString3 = subString3.replace("SNR_B_G:", "");
				subString3 = subString3.replace("\t", "");
				final Double SNR_B_G = Double.parseDouble(subString3);
				this.totalSNR_B_G += SNR_B_G;

				final int begin4SubIndex = line.indexOf("SNR_B_G_m:");
				final int end4SubIndex = line.length();
				String subString4 = line
				        .substring(begin4SubIndex, end4SubIndex);
				subString4 = subString4.replace("SNR_B_G_m:", "");
				final Double SNR_B_G_m = Double.parseDouble(subString4);
				this.totalSNR_B_G_M += SNR_B_G_m;
			}
			line = br.readLine();
		}

		this.meanSNR_C = this.totalSNR_C / this.numberOfFrames;
		this.meanSNR_C_M = this.totalSNR_C_M / this.numberOfFrames;

		this.meanSNR_B_P = this.totalSNR_B_P / this.numberOfFrames;
		this.meanSNR_B_P_M = this.totalSNR_B_P_M / this.numberOfFrames;

		this.meanSNR_B_G = this.totalSNR_B_G / this.numberOfFrames;
		this.meanSNR_B_G_M = this.totalSNR_B_G_M / this.numberOfFrames;

		br.close();
		fr.close();
	}

	public void analyzeTrajectoriesFile(final File trajsFile)
	        throws IOException {
		this.trajsFile = trajsFile;
		String line;
		final FileReader fr = new FileReader(this.trajsFile);
		final BufferedReader br = new BufferedReader(fr);

		File f = null;
		FileWriter fw = null;
		BufferedWriter bw = null;
		int trajNumber = -1;
		line = br.readLine();
		while (line != null) {
			if (line.startsWith("Stat VP:")) {
				this.totalParticles++;
				final int beginSubIndex = line.indexOf("SP:");
				final int endSubIndex = line.indexOf("S:");
				String subString = line.substring(beginSubIndex, endSubIndex);
				subString = subString.replace("SP:", "");
				subString = subString.replace("\t", "");
				// subString = subString.replace("\t", "");
				// subString = subString.replaceFirst("/\\d++", "");
				final Double particleSize = Double.parseDouble(subString);
				this.totalParticleSize += particleSize;
			} else if (this.generateTrajectories
			        && line.startsWith("% Trajectory id: ")) {
				if (bw != null) {
					bw.close();
					fw.close();
				}
				trajNumber = Integer.valueOf(line.replace("% Trajectory id: ",
				        "")) + 1;

				if (trajNumber < 10) {
					f = new File(this.workingDir + "\\SPT_onlyone_00"
					        + trajNumber + ".xy");
				} else if (trajNumber < 100) {
					f = new File(this.workingDir + "\\SPT_onlyone_0"
					        + trajNumber + ".xy");
				} else {
					f = new File(this.workingDir + "\\SPT_onlyone_"
					        + trajNumber + ".xy");
				}
				fw = new FileWriter(f);
				bw = new BufferedWriter(fw);
			} else if (this.generateTrajectories && line.startsWith("\t")) {
				final int stringIndex1 = line.indexOf("\t");
				final int stringIndex2 = line.indexOf("\t", stringIndex1 + 1);
				String subString = line.substring(stringIndex1, stringIndex2);
				subString = subString.replace("\t", "");
				subString = subString.replace("\t", "");
				final double particleIndex = Double.valueOf(subString);

				final int stringIndex3 = line.indexOf("\t", stringIndex2 + 1);
				subString = line.substring(stringIndex2, stringIndex3);
				subString = subString.replace("\t", "");
				subString = subString.replace("\t", "");
				final double particleX = Double.valueOf(subString);

				final int stringIndex4 = line.indexOf("\t", stringIndex3 + 1);
				subString = line.substring(stringIndex3, stringIndex4);
				subString = subString.replace("\t", "");
				subString = subString.replace("\t", "");
				final double particleY = Double.valueOf(subString);

				final Format formatter = new DecimalFormat("0.0000000E00");
				final StringBuffer buf = new StringBuffer();
				buf.append("   ");
				buf.append(formatter.format(particleIndex));
				buf.append("   ");
				buf.append(formatter.format(particleX));
				buf.append("   ");
				buf.append(formatter.format(particleY));
				buf.append("\n");

				bw.write(buf.toString());
			}
			line = br.readLine();
		}

		if (bw != null) {
			bw.close();
			fw.close();
		}

		this.meanParticlePerFrame = this.totalParticles / this.numberOfFrames;
		this.meanParticleSize = this.totalParticleSize / this.totalParticles;

		br.close();
		fr.close();

		this.appendResultsToFile(new File(this.workingDir
		        + "\\toolsResults.txt"));
	}

	public void printResults() {
		System.out.println("Frames\t" + this.framesFile);
		System.out.println("Trajs\t" + this.trajsFile);
		System.out.println("SNR_C\t" + this.getMeanSNR_C());
		System.out.println("SNR_C_M\t" + this.getMeanSNR_C_M());
		System.out.println("SNR_B_P\t" + this.getMeanSNR_B_P());
		System.out.println("SNR_B_P_M\t" + this.getMeanSNR_B_P_M());
		System.out.println("SNR_B_G\t" + this.getMeanSNR_B_G());
		System.out.println("SNR_B_G_M\t" + this.getMeanSNR_B_G_M());
		System.out.println("Particles\t" + this.getTotalParticles());
		System.out.println("Particles Per Frame\t"
		        + this.getMeanParticlePerFrame());
		System.out.println("Mean size\t" + this.getMeanParticleSize());
		System.out
		        .println("####################################################\n");
	}

	public void appendResultsToFile(final File file) throws IOException {
		final FileWriter fw = new FileWriter(file, true);
		final BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Frames\t" + this.framesFile + "\n");
		bw.write("Trajs\t" + this.trajsFile + "\n");
		bw.write("SNR_C\t" + this.getMeanSNR_C() + "\n");
		bw.write("SNR_C_M\t" + this.getMeanSNR_C_M() + "\n");
		bw.write("SNR_B_P\t" + this.getMeanSNR_B_P() + "\n");
		bw.write("SNR_B_P_M\t" + this.getMeanSNR_B_P_M() + "\n");
		bw.write("SNR_B_G\t" + this.getMeanSNR_B_G() + "\n");
		bw.write("SNR_B_G_M\t" + this.getMeanSNR_B_G_M() + "\n");
		bw.write("Particles\t" + this.getTotalParticles() + "\n");
		bw.write("Particles Per Frame\t" + this.getMeanParticlePerFrame()
		        + "\n");
		bw.write("Mean size\t" + this.getMeanParticleSize() + "\n");
		bw.write("####################################################\n");
		bw.close();
		fw.close();
	}

	public static void main(final String[] args) throws IOException {
		final File workingDir = new File(
		        "C:\\Workspaces\\SYD\\SYD(x64)_20120717\\appl.omega.ParticleTrackerPTCoproc_8_V3_PUSH_wx\\");
		final String fileName1 = "PT_Frames_";
		final String fileName2 = "PT_Trajectories_";
		final String dateName = "20130606_170325";
		final String extName = ".txt";
		final File framesFile = new File(fileName1 + dateName + extName);
		final File trajsFile = new File(fileName2 + dateName + extName);
		final PTFilesAnalyzer finder = new PTFilesAnalyzer(workingDir);

		finder.analyzeFramesFile(framesFile);
		finder.analyzeTrajectoriesFile(trajsFile);

		finder.printResults();
	}

	public Double getMeanSNR_C() {
		return this.meanSNR_C;
	}

	public Double getMeanSNR_C_M() {
		return this.meanSNR_C_M;
	}

	public Double getMeanSNR_B_P() {
		return this.meanSNR_B_P;
	}

	public Double getMeanSNR_B_P_M() {
		return this.meanSNR_B_P_M;
	}

	public Double getMeanSNR_B_G() {
		return this.meanSNR_B_G;
	}

	public Double getMeanSNR_B_G_M() {
		return this.meanSNR_B_G_M;
	}

	public int getTotalParticles() {
		return this.totalParticles;
	}

	public int getMeanParticlePerFrame() {
		return this.meanParticlePerFrame;
	}

	public Double getMeanParticleSize() {
		return this.meanParticleSize;
	}

	public String getFramesFileName() {
		return this.framesFile.getName();
	}

	public String getTrajsFileName() {
		return this.trajsFile.getName();
	}

	public void setGenerateTrajectories(final boolean tof) {
		this.generateTrajectories = tof;
	}
}
