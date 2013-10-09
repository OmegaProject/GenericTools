package gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
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

	private JFrame mainFrame;

	private JButton workingDirChooser_btt;
	private JFileChooser workingDirDialog_fCh;
	private JLabel workingCurrentDirLbl;

	private JTextArea results_txtA;

	private JButton generateTrajFiles_btt;
	private JButton computeSNRData_btt;
	private JButton computeSNRMeanData_btt;
	private JButton computeP2PDist_btt;

	private JButton compareDirChooser_btt;
	private JButton compare_btt;
	private JButton compareWithDistr_btt;
	private JFileChooser compareDirDialog_fCh;
	private JLabel compareCurrentDirLbl;

	private JButton aggregateTrajData_btt;

	/**
	 * Create the main frame and invoke all the needed methods
	 * 
	 * @since 0.0
	 */
	private void createAndShowGUI() {
		// Create and set up the window.
		this.mainFrame = new JFrame("Image tools");
		this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final String s = System.getProperty("user.dir");

		this.workingDirDialog_fCh = new JFileChooser();
		this.workingDirDialog_fCh.setCurrentDirectory(new File(s));
		this.workingDirDialog_fCh.setDialogTitle("Working directory chooser");
		this.workingDirDialog_fCh.setMultiSelectionEnabled(false);
		this.workingDirDialog_fCh
		        .setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		this.compareDirDialog_fCh = new JFileChooser();
		this.compareDirDialog_fCh.setCurrentDirectory(new File(s));
		this.compareDirDialog_fCh.setDialogTitle("Compare directory chooser");
		this.compareDirDialog_fCh.setMultiSelectionEnabled(false);
		this.compareDirDialog_fCh
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
		this.mainFrame.getContentPane()
		        .setLayout(
		                new BoxLayout(this.mainFrame.getContentPane(),
		                        BoxLayout.Y_AXIS));

		this.mainFrame.getContentPane().add(this.createMainPanel());
		this.addHorizontalSeparator();

		this.mainFrame.getContentPane().add(this.createComparePanel());
		this.addHorizontalSeparator();

		this.mainFrame.getContentPane()
		        .add(this.createAggregateTrajDataPanel());
		this.addHorizontalSeparator();

		this.mainFrame.getContentPane().add(this.generateResultsPanel());
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
	private JPanel createMainPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JPanel topSubPanel = new JPanel();
		topSubPanel.setLayout(new GridLayout(3, 1));

		topSubPanel.add(new JLabel("Actual working folder:"));
		final String workingFolder = this.workingDirDialog_fCh
		        .getCurrentDirectory().getPath();
		this.workingCurrentDirLbl = new JLabel(workingFolder);
		topSubPanel.add(this.workingCurrentDirLbl);
		this.workingDirChooser_btt = new JButton("Choose the working directory");
		topSubPanel.add(this.workingDirChooser_btt);

		panel.add(topSubPanel, BorderLayout.NORTH);

		final JPanel subPanel = new JPanel();
		subPanel.setLayout(new GridLayout(4, 1));

		this.generateTrajFiles_btt = new JButton("Generate trajectories files");
		subPanel.add(this.generateTrajFiles_btt);

		this.computeSNRData_btt = new JButton("Compute SNR data");
		subPanel.add(this.computeSNRData_btt);

		this.computeSNRMeanData_btt = new JButton("Compute SNR mean data");
		subPanel.add(this.computeSNRMeanData_btt);

		this.computeP2PDist_btt = new JButton("Compute P2P distance");
		subPanel.add(this.computeP2PDist_btt);

		panel.add(subPanel, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createComparePanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JPanel subPanel = new JPanel();
		subPanel.setLayout(new GridLayout(5, 1));

		subPanel.add(new JLabel("Actual compare folder:"));
		final String compareFolder = this.compareDirDialog_fCh
		        .getCurrentDirectory().getPath();
		this.compareCurrentDirLbl = new JLabel(compareFolder);
		subPanel.add(this.compareCurrentDirLbl);
		this.compareDirChooser_btt = new JButton("Choose the compare directory");
		subPanel.add(this.compareDirChooser_btt);

		this.compare_btt = new JButton("Compare images");
		subPanel.add(this.compare_btt);

		this.compareWithDistr_btt = new JButton("Compare images with distr");
		subPanel.add(this.compareWithDistr_btt);

		panel.add(subPanel, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createAggregateTrajDataPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		final JPanel subPanel = new JPanel();
		subPanel.setLayout(new GridLayout(1, 1));

		this.aggregateTrajData_btt = new JButton("Aggregate traj data");
		subPanel.add(this.aggregateTrajData_btt);

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
		// OmegaGenericToolGUIListeners.addComputeSNRData(this);
		OmegaGenericToolGUIListeners.addComputeMeanSNRData(this);

		OmegaGenericToolGUIListeners.addComputeP2PDistance(this);

		OmegaGenericToolGUIListeners.addCompareDirChooser(this);
		OmegaGenericToolGUIListeners.addCompareImages(this);
		OmegaGenericToolGUIListeners.addCompareImagesWithDistr(this);

		OmegaGenericToolGUIListeners.addAggregateTrajData(this);
	}

	private void setDefaultValues() {

	}

	public JButton getGenerateTrajFilesButt() {
		return this.generateTrajFiles_btt;
	}

	public JButton getComputeSNRDataButt() {
		return this.computeSNRData_btt;
	}

	public JButton getComputeMeanSNRDataButt() {
		return this.computeSNRMeanData_btt;
	}

	public JButton getComputeP2PDistanceButt() {
		return this.computeP2PDist_btt;
	}

	public JButton getCompareButt() {
		return this.compare_btt;
	}

	public JButton getCompareWithDistriButt() {
		return this.compareWithDistr_btt;
	}

	public JButton getAggregateTrajDataButt() {
		return this.aggregateTrajData_btt;
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
		if (this.workingDirDialog_fCh.getSelectedFile() != null)
			return this.workingDirDialog_fCh.getSelectedFile();
		else
			return this.workingDirDialog_fCh.getCurrentDirectory();
	}

	public File getCompareDirectory() {
		if (this.compareDirDialog_fCh.getSelectedFile() != null)
			return this.compareDirDialog_fCh.getSelectedFile();
		else
			return this.compareDirDialog_fCh.getCurrentDirectory();
	}

	public JButton getWorkingDirChooserButton() {
		return this.workingDirChooser_btt;
	}

	public JButton getCompareDirChooserButton() {
		return this.compareDirChooser_btt;
	}

	public JFileChooser getWorkingDirChooserDialog() {
		return this.workingDirDialog_fCh;
	}

	public JFileChooser getCompareDirChooserDialog() {
		return this.compareDirDialog_fCh;
	}

	public void setNewWorkingCurrentDirLbl(final String s) {
		this.workingCurrentDirLbl.setText(s);
	}

	public void setNewCompareCurrentDirLbl(final String s) {
		this.compareCurrentDirLbl.setText(s);
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
