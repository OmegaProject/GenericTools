package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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

	private JButton outputDirChooser_btt;
	private JFileChooser outputDirDialog_fCh;
	private JLabel outputCurrentDirLbl;
	
	private JTextArea results_txtA;
	
	private JButton generateSingleTrajFiles_btt;
	private JButton computeSNRData_btt;
	private JButton computeSNRMeanData_btt;
	private JButton computeP2PDist_btt;
	
	private JButton compareDirChooser_btt;
	private JButton compare_btt;
	private JButton compareWithDistr_btt;
	private JFileChooser compareDirDialog_fCh;
	private JLabel compareCurrentDirLbl;
	
	private JButton calculateDOnTrajData_btt;
	private JButton aggregateTrajData_btt;
	
	private JTextField filterTrajLenght_txt;
	private JCheckBox analyzeOnlyTraj_chkb, mergeTraj_chkb;
	private JCheckBox analyzeFilteredTraj_chkb, analyzeMergedTraj_chkb,
			analyzeImageLog_chkb, analyzeSingleImage_chkb;
	private JButton analyzeAndFilterTraj_btt;
	
	private JButton computeSMSSAndDMeans_btt;
	private JButton aggregateSMSSAndD_btt;
	private JButton consolidateSMSSAndD_btt;

	private JButton runMosaicPD_btt;

	private JButton filterSDReport_btt, filterPTReport_btt,
			filterMosaicReport_btt;
	
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

		this.outputDirDialog_fCh = new JFileChooser();
		this.outputDirDialog_fCh.setCurrentDirectory(new File(s));
		this.outputDirDialog_fCh.setDialogTitle("Output directory chooser");
		this.outputDirDialog_fCh.setMultiSelectionEnabled(false);
		this.outputDirDialog_fCh
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
		final Container cont = this.mainFrame.getContentPane();
		cont.setLayout(new BorderLayout());
		
		final JPanel workingPanel = this.createWorkingPanel();
		final JPanel comparePanel = this.createComparePanel();
		final JPanel outputPanel = this.createOutputPanel();
		final JPanel buttonPanel1 = this.createButtonPanel1();
		final JPanel buttonPanel2 = this.createButtonPanel2();
		final JPanel buttonPanel3 = this.createButtonPanel3();
		final JPanel optionsTrajPanel = this.createOptionsTrajPanel();
		final JPanel filterTrajPanel = this.createTrajFilterPanel();
		
		final JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		
		final JPanel folderPanel = new JPanel();
		folderPanel.setLayout(new GridLayout(3, 1));
		folderPanel.add(workingPanel);
		folderPanel.add(comparePanel);
		folderPanel.add(outputPanel);
		
		topPanel.add(folderPanel, BorderLayout.NORTH);
		topPanel.add(optionsTrajPanel, BorderLayout.SOUTH);
		
		final JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		
		mainPanel.add(topPanel, BorderLayout.NORTH);
		mainPanel.add(buttonPanel1, BorderLayout.WEST);
		mainPanel.add(buttonPanel2, BorderLayout.CENTER);
		mainPanel.add(buttonPanel3, BorderLayout.EAST);
		mainPanel.add(filterTrajPanel, BorderLayout.SOUTH);
		
		cont.add(mainPanel, BorderLayout.NORTH);
		
		final JPanel resultsPanel = this.generateResultsPanel();
		cont.add(resultsPanel, BorderLayout.CENTER);
	}
	
	public JPanel createWorkingPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));
		
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		
		p.add(new JLabel("Current working folder:        "), BorderLayout.WEST);
		final String workingFolder = this.workingDirDialog_fCh
				.getCurrentDirectory().getPath();
		this.workingCurrentDirLbl = new JLabel(workingFolder);
		p.add(this.workingCurrentDirLbl, BorderLayout.CENTER);
		panel.add(p);
		this.workingDirChooser_btt = new JButton("Choose the working directory");
		panel.add(this.workingDirChooser_btt);
		
		return panel;
	}
	
	private JPanel createComparePanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));
		
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		p.add(new JLabel("Current compare folder:      "), BorderLayout.WEST);
		final String compareFolder = this.compareDirDialog_fCh
				.getCurrentDirectory().getPath();
		this.compareCurrentDirLbl = new JLabel(compareFolder);
		p.add(this.compareCurrentDirLbl, BorderLayout.CENTER);
		panel.add(p);
		this.compareDirChooser_btt = new JButton("Choose the compare directory");
		panel.add(this.compareDirChooser_btt);
		
		return panel;
	}
	
	private JPanel createOutputPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(2, 1));
		
		final JPanel p = new JPanel();
		p.setLayout(new BorderLayout());

		p.add(new JLabel("Current output folder:           "),
				BorderLayout.WEST);
		final String outputFolder = this.outputDirDialog_fCh
				.getCurrentDirectory().getPath();
		this.outputCurrentDirLbl = new JLabel(outputFolder);
		p.add(this.outputCurrentDirLbl, BorderLayout.CENTER);
		panel.add(p);
		this.outputDirChooser_btt = new JButton("Choose the output directory");
		panel.add(this.outputDirChooser_btt);
		
		return panel;
	}

	public JPanel createButtonPanel1() {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(5, 1));
		
		this.generateSingleTrajFiles_btt = new JButton(
				"Generate single traj files");
		panel.add(this.generateSingleTrajFiles_btt);
		
		this.computeSNRData_btt = new JButton("Compute SNR data");
		panel.add(this.computeSNRData_btt);
		
		this.computeSNRMeanData_btt = new JButton("Compute SNR mean data");
		panel.add(this.computeSNRMeanData_btt);
		
		this.computeP2PDist_btt = new JButton("Compute P2P distance");
		panel.add(this.computeP2PDist_btt);

		this.filterSDReport_btt = new JButton("Filter SD Report");
		panel.add(this.filterSDReport_btt);
		
		return panel;
	}
	
	public JPanel createButtonPanel2() {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(5, 1));
		
		this.compare_btt = new JButton("Compare images");
		panel.add(this.compare_btt);
		
		this.compareWithDistr_btt = new JButton("Compare images with distr");
		panel.add(this.compareWithDistr_btt);
		
		this.calculateDOnTrajData_btt = new JButton("Calculate D traj data");
		panel.add(this.calculateDOnTrajData_btt);
		
		this.aggregateTrajData_btt = new JButton("Aggregate traj data");
		panel.add(this.aggregateTrajData_btt);

		this.filterPTReport_btt = new JButton("Filter PT Report");
		panel.add(this.filterPTReport_btt);
		
		return panel;
	}
	
	public JPanel createButtonPanel3() {
		final JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(5, 1));
		
		this.computeSMSSAndDMeans_btt = new JButton("Compute SMSS and D means");
		panel.add(this.computeSMSSAndDMeans_btt);
		
		this.aggregateSMSSAndD_btt = new JButton("Aggregate SMSS and D data");
		panel.add(this.aggregateSMSSAndD_btt);
		
		this.consolidateSMSSAndD_btt = new JButton(
				"Consolidate SMSS and D files");
		panel.add(this.consolidateSMSSAndD_btt);

		this.runMosaicPD_btt = new JButton("Run MOSAIC PD");
		panel.add(this.runMosaicPD_btt);

		this.filterMosaicReport_btt = new JButton("Filter Mosaic Report");
		panel.add(this.filterMosaicReport_btt);
		
		return panel;
	}
	
	private JPanel createTrajFilterPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		
		final JLabel lbl = new JLabel("Insert min traj lenght");
		panel.add(lbl);
		
		this.filterTrajLenght_txt = new JTextField();
		this.filterTrajLenght_txt.setPreferredSize(new Dimension(200, 27));
		panel.add(this.filterTrajLenght_txt);
		
		this.analyzeOnlyTraj_chkb = new JCheckBox("Analyze only");
		panel.add(this.analyzeOnlyTraj_chkb);
		
		this.mergeTraj_chkb = new JCheckBox("Merge trajectories");
		panel.add(this.mergeTraj_chkb);
		
		this.analyzeAndFilterTraj_btt = new JButton("Filter trajectories");
		panel.add(this.analyzeAndFilterTraj_btt);
		
		return panel;
	}
	
	private JPanel createOptionsTrajPanel() {
		final JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		
		this.analyzeFilteredTraj_chkb = new JCheckBox("Analyze filtered traj");
		panel.add(this.analyzeFilteredTraj_chkb);
		
		this.analyzeMergedTraj_chkb = new JCheckBox("Analyze merged traj");
		panel.add(this.analyzeMergedTraj_chkb);
		
		this.analyzeImageLog_chkb = new JCheckBox("Analyze image log");
		panel.add(this.analyzeImageLog_chkb);

		this.analyzeSingleImage_chkb = new JCheckBox("Analyze single image");
		panel.add(this.analyzeSingleImage_chkb);
		
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
		OmegaGenericToolGUIListeners.addCompareDirChooser(this);
		OmegaGenericToolGUIListeners.addOutputDirChooser(this);

		OmegaGenericToolGUIListeners.addGenerateSingleTrajFiles(this);
		// OmegaGenericToolGUIListeners.addComputeSNRData(this);
		OmegaGenericToolGUIListeners.addComputeMeanSNRData(this);
		
		OmegaGenericToolGUIListeners.addComputeP2PDistance(this);
		
		OmegaGenericToolGUIListeners.addCompareImages(this);
		OmegaGenericToolGUIListeners.addCompareImagesWithDistr(this);
		
		OmegaGenericToolGUIListeners.addCalculateDOnTrajData(this);
		OmegaGenericToolGUIListeners.addAggregateTrajData(this);
		
		OmegaGenericToolGUIListeners.addAnalyzeAndFilterTrajectories(this);
		
		OmegaGenericToolGUIListeners.addComputeSMSSAndDMeans(this);
		OmegaGenericToolGUIListeners.addAggregateSMSSAndDData(this);
		OmegaGenericToolGUIListeners.addConsolidateSMSSAndDFiles(this);
		
		OmegaGenericToolGUIListeners.addRunMosaicPD(this);
		
		OmegaGenericToolGUIListeners.addFilterMosaicReport(this);
		OmegaGenericToolGUIListeners.addFilterPTReport(this);
		OmegaGenericToolGUIListeners.addFilterSDReport(this);
	}
	
	private void setDefaultValues() {
		
	}

	public JButton getFilterMosaicReport() {
		return this.filterMosaicReport_btt;
	}
	
	public JButton getFilterSDReport() {
		return this.filterSDReport_btt;
	}
	
	public JButton getFilterPTReport() {
		return this.filterPTReport_btt;
	}
	
	public JButton getRunMosaicPDButt() {
		return this.runMosaicPD_btt;
	}
	
	public JButton getConsolidateSMSSAndDFilesButt() {
		return this.consolidateSMSSAndD_btt;
	}

	public JButton getAggregateSMSSAndDDataButt() {
		return this.aggregateSMSSAndD_btt;
	}
	
	public JButton getComputeSMSSAndDMeansButt() {
		return this.computeSMSSAndDMeans_btt;
	}
	
	public JButton getGenerateSingleTrajFilesButt() {
		return this.generateSingleTrajFiles_btt;
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
	
	public JButton getCalculateDOnTrajDataButt() {
		return this.calculateDOnTrajData_btt;
	}
	
	public JButton getAggregateTrajDataButt() {
		return this.aggregateTrajData_btt;
	}
	
	public JButton getAnalyzeAndFilterTrajButt() {
		return this.analyzeAndFilterTraj_btt;
	}
	
	public boolean isAnalyzeOnly() {
		return this.analyzeOnlyTraj_chkb.isSelected();
	}
	
	public boolean isMergeTraj() {
		return this.mergeTraj_chkb.isSelected();
	}
	
	public boolean isAnalyzeFilteredTraj() {
		return this.analyzeFilteredTraj_chkb.isSelected();
	}
	
	public boolean isAnalyzeMergedTraj() {
		return this.analyzeMergedTraj_chkb.isSelected();
	}
	
	public boolean isAnalyzeImageLog() {
		return this.analyzeImageLog_chkb.isSelected();
	}
	
	public boolean isPDRunningSingleImage() {
		return this.analyzeSingleImage_chkb.isSelected();
	}
	
	public double getTrajFilter() {
		return Double.valueOf(this.filterTrajLenght_txt.getText());
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
	
	public File getOutputDirectory() {
		if (this.outputDirDialog_fCh.getSelectedFile() != null)
			return this.outputDirDialog_fCh.getSelectedFile();
		else
			return this.outputDirDialog_fCh.getCurrentDirectory();
	}
	
	public JButton getWorkingDirChooserButton() {
		return this.workingDirChooser_btt;
	}
	
	public JButton getCompareDirChooserButton() {
		return this.compareDirChooser_btt;
	}

	public JButton getOutputDirChooserButton() {
		return this.outputDirChooser_btt;
	}
	
	public JFileChooser getWorkingDirChooserDialog() {
		return this.workingDirDialog_fCh;
	}
	
	public JFileChooser getCompareDirChooserDialog() {
		return this.compareDirDialog_fCh;
	}

	public JFileChooser getOutputDirChooserDialog() {
		return this.outputDirDialog_fCh;
	}
	
	public void setNewWorkingCurrentDirLbl(final String s) {
		this.workingCurrentDirLbl.setText(s);
	}
	
	public void setNewCompareCurrentDirLbl(final String s) {
		this.compareCurrentDirLbl.setText(s);
	}

	public void setNewOutputCurrentDirLbl(final String s) {
		this.outputCurrentDirLbl.setText(s);
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
	
	public void appendOutput(final String output) {
		this.results_txtA.append(output);
		this.results_txtA.append("\n");
		this.results_txtA.setCaretPosition(this.results_txtA.getDocument()
				.getLength());
	}
}
