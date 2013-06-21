package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;

import core.PTFilesAnalyzer;

public class OmegaGenericToolGUI {

	public static void main(final String[] args) {
		final OmegaGenericToolGUI mainGUI = new OmegaGenericToolGUI();
		mainGUI.createAndShowGUI();
	}

	final static String fileName1 = "PT_Frames_";
	final static String fileName2 = "PT_Trajectories_";

	private JFrame mainFrame;

	private JButton dirChooserButt;
	private JFileChooser dirChooserDialog;
	private JLabel currentDirLbl;

	private JTextArea results_txtA;

	private JButton generateTrajFiles_btt;
	private JButton computeSNRData_btt;

	/**
	 * Create the main frame and invoke all the needed methods
	 * 
	 * @since 0.0
	 */
	private void createAndShowGUI() {
		// Create and set up the window.
		this.mainFrame = new JFrame("Image tools");
		this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.dirChooserDialog = new JFileChooser();
		final String s = System.getProperty("user.dir");
		this.dirChooserDialog.setCurrentDirectory(new File(s));
		this.dirChooserDialog.setDialogTitle("Working directory chooser");
		this.dirChooserDialog.setMultiSelectionEnabled(false);
		this.dirChooserDialog
		        .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		this.addWidgets();

		this.addListeners();

		this.setDefaultValues();

		// Display the window.
		this.mainFrame.pack();
		this.mainFrame.setVisible(true);
	}

	/**
	 * Create all the needed panels and invoke the relative methods
	 * 
	 * @since 0.0
	 */
	private void addWidgets() {
		this.mainFrame.getContentPane().setLayout(new BorderLayout());

		this.mainFrame.getContentPane().add(
		        this.generateImageMainInformationsPanel(), BorderLayout.NORTH);
		this.addHorizontalSeparator();

		this.mainFrame.getContentPane().add(this.generateResultsPanel(),
		        BorderLayout.CENTER);
	}

	/**
	 * Add an horizontal separator
	 * 
	 * @since 0.0
	 */
	private void addHorizontalSeparator() {
		this.mainFrame.getContentPane().add(Box.createVerticalStrut(5));
		this.mainFrame.getContentPane().add(new JSeparator());
		this.mainFrame.getContentPane().add(Box.createVerticalStrut(5));
	}

	/**
	 * Create the main informations panel
	 * 
	 * @return
	 * 
	 * @since 0.0
	 */
	private JPanel generateImageMainInformationsPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JPanel topSubPanel = new JPanel();
		topSubPanel.setLayout(new GridLayout(3, 1));

		topSubPanel.add(new JLabel("Actual folder:"));
		final String s = this.dirChooserDialog.getCurrentDirectory().getPath();
		this.currentDirLbl = new JLabel(s);
		topSubPanel.add(this.currentDirLbl);
		this.dirChooserButt = new JButton("Choose the working directory");
		topSubPanel.add(this.dirChooserButt);

		panel.add(topSubPanel, BorderLayout.NORTH);

		final JPanel subPanel = new JPanel();
		subPanel.setLayout(new GridLayout(2, 1));

		this.generateTrajFiles_btt = new JButton("Generate trajectories files");
		subPanel.add(this.generateTrajFiles_btt);

		this.computeSNRData_btt = new JButton("Compute SNR data");
		subPanel.add(this.computeSNRData_btt);

		panel.add(subPanel, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Create the results panel
	 * 
	 * @return
	 * 
	 * @since 0.2
	 */
	private JPanel generateResultsPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		this.results_txtA = new JTextArea(10, 25);
		this.results_txtA.setLineWrap(true);
		this.results_txtA.setWrapStyleWord(true);
		this.results_txtA.setEditable(false);
		this.results_txtA.setRows(5);

		final JScrollPane scrollPane = new JScrollPane(this.results_txtA);

		// scrollPane.setPreferredSize(new Dimension(200, 200));

		// final DefaultCaret caret = (DefaultCaret)
		// this.results_txtA.getCaret();
		// caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

		// scrollPane.add(this.results_txtA);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	/**
	 * Add all the needed listeners
	 * 
	 * @since 0.0
	 */
	private void addListeners() {
		OmegaGenericToolGUIListeners.addWorkingDirChooser(this);
		OmegaGenericToolGUIListeners.addGenerateTrajFiles(this);
		OmegaGenericToolGUIListeners.addComputeSNRData(this);
	}

	private void setDefaultValues() {

	}

	public JButton getGenerateTrajFilesButt() {
		return this.generateTrajFiles_btt;
	}

	public JButton getComputeSNRDataButt() {
		return this.computeSNRData_btt;
	}

	/**
	 * Return the given results string to the results text area. A newline is
	 * inserted after each results string
	 * 
	 * @return
	 * 
	 * @since 0.2
	 */
	public void appendResultsText(final String results) {
		this.results_txtA.append(results);
		this.results_txtA.append("\n");
		this.results_txtA.setCaretPosition(this.results_txtA.getDocument()
		        .getLength());
	}

	public File getWorkingDirectory() {
		if (this.dirChooserDialog.getSelectedFile() != null)
			return this.dirChooserDialog.getSelectedFile();
		else
			return this.dirChooserDialog.getCurrentDirectory();
	}

	public JButton getDirChooserButton() {
		return this.dirChooserButt;
	}

	public JFileChooser getDirChooserDialog() {
		return this.dirChooserDialog;
	}

	public void setNewCurrentDirLbl(final String s) {
		this.currentDirLbl.setText(s);
	}

	public void appendSNRResult(final PTFilesAnalyzer mSNRfinder) {
		this.appendResultsText("Frames: " + mSNRfinder.getFramesFileName());
		this.appendResultsText("Trajs: " + mSNRfinder.getTrajsFileName());
		this.appendResultsText("SNR_C: " + mSNRfinder.getMeanSNR_C());
		this.appendResultsText("SNR_C_M: " + mSNRfinder.getMeanSNR_C_M());
		this.appendResultsText("SNR_B_P: " + mSNRfinder.getMeanSNR_B_P());
		this.appendResultsText("SNR_B_P_M: " + mSNRfinder.getMeanSNR_B_P_M());
		this.appendResultsText("SNR_B_G: " + mSNRfinder.getMeanSNR_B_G());
		this.appendResultsText("SNR_B_G_M: " + mSNRfinder.getMeanSNR_B_G_M());
		this.appendResultsText("Particles: " + mSNRfinder.getTotalParticles());
		this.appendResultsText("Particles Per Frame: "
		        + mSNRfinder.getMeanParticlePerFrame());
		this.appendResultsText("Mean size: " + mSNRfinder.getMeanParticleSize());
		this.appendResultsText("#######################");
	}
}
