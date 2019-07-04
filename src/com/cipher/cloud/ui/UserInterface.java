package com.cipher.cloud.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.cipher.cloud.automation.UseSelenium;

/**
 * 
 * @author sighil.sivadas
 * 
 */
public class UserInterface extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6533309631482822793L;
	private JPanel contentPane;
	private JTextField inputFileField;
	private JTextField outputFileField;
	private JFileChooser fileChooser;
	private JTextField objectRepositoryField;
	private JTextPane displayTextPane;
	private JButton configureExecuteTestsButton;
	private JButton inputFileButton;
	private JButton outputFileButton;
	private JButton objectRepositoryButton;
	private JButton resetButton;

	private JCheckBoxMenuItem alwaysOnTopSubMenu;

	private static UserInterface uiInstance = null;

	public static UserInterface getInstance() {
		if (uiInstance == null) {
			synchronized (UserInterface.class) {
				if (uiInstance == null) {
					uiInstance = new UserInterface();
				}
			}
		}
		return uiInstance;
	}

	// public static void main(String[] args) {
	// EventQueue.invokeLater(new Runnable() {
	// @Override
	// public void run() {
	// uiInstance = UserInterface.getInstance();
	// uiInstance.setVisible(true);
	// }
	// });
	// }

	public UserInterface() {
		setBackground(Color.WHITE);
		setTitle("Test As a Service");
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 650, 650);

		contentPane = new JPanel();
		contentPane.setBackground(Color.WHITE);
		contentPane.setForeground(Color.BLACK);
		contentPane.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		initMenu();
		initFileSelectionPanel();
		initExecuteButtonsPanel();
		initConsolePanel();
	}

	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setBounds(0, 0, 634, 21);
		contentPane.add(menuBar);

		JMenu optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);

		alwaysOnTopSubMenu = new JCheckBoxMenuItem("Always on top");
		alwaysOnTopSubMenu.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if (alwaysOnTopSubMenu.isSelected()) {
					setAlwaysOnTop(true);
				} else {
					setAlwaysOnTop(false);
				}
			}
		});
		optionsMenu.add(alwaysOnTopSubMenu);
	}

	private void initFileSelectionPanel() {

		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		fileChooser.setDialogTitle("Select File");
		fileChooser.setSelectedFile(new File("C:/Selenium/"));

		JPanel fileSelectionPanel = new JPanel();
		fileSelectionPanel.setBackground(Color.WHITE);
		fileSelectionPanel.setBounds(10, 101, 614, 134);
		contentPane.add(fileSelectionPanel);
		fileSelectionPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Directory Selection", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
		fileSelectionPanel.setLayout(null);

		inputFileButton = new JButton("Browse");
		inputFileButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				int status = fileChooser.showOpenDialog(UserInterface.this);
				if (status == 0) {
					File file = fileChooser.getSelectedFile();
					inputFileField.setText(file.getAbsolutePath());
				}
			}
		});
		inputFileButton.setBounds(499, 27, 105, 23);
		fileSelectionPanel.add(inputFileButton);

		inputFileField = new JTextField();
		inputFileField.setBounds(111, 28, 385, 20);
		fileSelectionPanel.add(inputFileField);
		inputFileField.setEditable(false);
		inputFileField.setColumns(10);
		inputFileField.setText("C:/Selenium/Input_Files");

		outputFileButton = new JButton("Browse");
		outputFileButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent arg0) {
				int status = fileChooser.showOpenDialog(UserInterface.this);
				if (status == 0) {
					File file = fileChooser.getSelectedFile();
					outputFileField.setText(file.getAbsolutePath());
				}
			}
		});
		outputFileButton.setBounds(499, 58, 105, 23);
		fileSelectionPanel.add(outputFileButton);

		outputFileField = new JTextField();
		outputFileField.setBounds(111, 59, 385, 20);
		outputFileField.setEditable(false);
		fileSelectionPanel.add(outputFileField);
		outputFileField.setColumns(10);
		outputFileField.setText("C:/Selenium/Output_Files");

		JLabel InputLocationLabel = new JLabel("Input Location");
		InputLocationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		InputLocationLabel.setBounds(10, 31, 91, 14);
		fileSelectionPanel.add(InputLocationLabel);

		JLabel outputLocationLabel = new JLabel("Output Location");
		outputLocationLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		outputLocationLabel.setBounds(10, 62, 91, 14);
		fileSelectionPanel.add(outputLocationLabel);

		JLabel ObjectRepositoryLabel = new JLabel("Object Repository");
		ObjectRepositoryLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		ObjectRepositoryLabel.setBounds(10, 93, 91, 14);
		fileSelectionPanel.add(ObjectRepositoryLabel);

		objectRepositoryField = new JTextField();
		objectRepositoryField.setBounds(111, 90, 385, 20);
		fileSelectionPanel.add(objectRepositoryField);
		objectRepositoryField.setEditable(false);
		objectRepositoryField.setColumns(10);
		objectRepositoryField.setText("C:/Selenium/Input_Files/ObjectRepository.xlsx");
		objectRepositoryButton = new JButton("Browse");
		objectRepositoryButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				int status = fileChooser.showOpenDialog(UserInterface.this);
				if (status == 0) {
					File file = fileChooser.getSelectedFile();
					objectRepositoryField.setText(file.getAbsolutePath());
				}
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
		});
		objectRepositoryButton.setBounds(499, 89, 105, 23);
		fileSelectionPanel.add(objectRepositoryButton);
	}

	private void initExecuteButtonsPanel() {
		JPanel executeButtonsPanel = new JPanel();
		executeButtonsPanel.setBackground(Color.WHITE);
		executeButtonsPanel.setBounds(10, 32, 614, 58);
		contentPane.add(executeButtonsPanel);
		executeButtonsPanel.setLayout(null);

		configureExecuteTestsButton = new JButton("Configure Tests");
		configureExecuteTestsButton.setBorder(null);
		configureExecuteTestsButton.setOpaque(false);
		configureExecuteTestsButton.setBounds(474, 0, 130, 46);
		executeButtonsPanel.add(configureExecuteTestsButton);

		resetButton = new JButton("Reset");
		resetButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				inputFileField.setEnabled(true);
				outputFileField.setEnabled(true);
				objectRepositoryField.setEnabled(true);
				inputFileButton.setEnabled(true);
				outputFileButton.setEnabled(true);
				objectRepositoryButton.setEnabled(true);
				configureExecuteTestsButton.setText("Configure Tests");
				configureExecuteTestsButton.setEnabled(true);
				displayTextPane.setText("");
			}
		});
		resetButton.setBounds(391, 0, 73, 46);
		executeButtonsPanel.add(resetButton);

		JLabel lblCCSeleniumExecutor = new JLabel("Test As a Service");
		lblCCSeleniumExecutor.setBorder(new MatteBorder(0, 0, 1, 0,
				new Color(0, 0, 0)));
		lblCCSeleniumExecutor.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblCCSeleniumExecutor.setHorizontalAlignment(SwingConstants.CENTER);
		lblCCSeleniumExecutor.setBounds(10, 4, 371, 30);
		executeButtonsPanel.add(lblCCSeleniumExecutor);

		configureExecuteTestsButton.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {
					
					public void run() {
						resetButton.setEnabled(false);
						configureExecuteTestsButton.setEnabled(false);
						if (configureExecuteTestsButton.getText().equals("Configure Tests")) {

							if (inputFileField.getText().equals("") || outputFileField.getText().equals("") || objectRepositoryField.getText().equals("")) {
								setConsole("Please select all mandatory fields", false);
								resetButton.setEnabled(true);
								return;
							}

							inputFileField.setEnabled(false);
							outputFileField.setEnabled(false);
							objectRepositoryField.setEnabled(false);
							inputFileButton.setEnabled(false);
							outputFileButton.setEnabled(false);
							objectRepositoryButton.setEnabled(false);

							if (UseSelenium.setConfiguration(inputFileField.getText(), outputFileField.getText(), objectRepositoryField.getText()).equals("")) {
								configureExecuteTestsButton.setText("Execute Tests");
								configureExecuteTestsButton.setEnabled(true);
								setConsole("> Configuration Successful", true);
							} else {
								setConsole("> Configuration Not Successful", false);
							}
						} else {
							configureExecuteTestsButton.setEnabled(false);
							UseSelenium.runSelenium(inputFileField.getText());
						}
						resetButton.setEnabled(true);
					}
				}).start();
			}
		});
	}

	private void initConsolePanel() {
		JPanel consolePanel = new JPanel();
		consolePanel.setBackground(Color.WHITE);
		consolePanel.setBounds(10, 246, 614, 351);
		consolePanel.setBorder(new TitledBorder(null, "Console Output", TitledBorder.LEADING, TitledBorder.TOP, null, Color.BLUE));
		consolePanel.setLayout(null);

		displayTextPane = new JTextPane();
		displayTextPane.setForeground(Color.GREEN);
		displayTextPane.setBackground(Color.BLACK);
		displayTextPane.setBounds(0, 0, 100, 50);
		displayTextPane.setEditable(false);
		JScrollPane scroll = new JScrollPane(displayTextPane);
		scroll.setBounds(10, 21, 594, 319);
		scroll.setEnabled(false);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		consolePanel.add(scroll);
		contentPane.add(consolePanel);

		StyledDocument sDoc = displayTextPane.getStyledDocument();
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
		StyleConstants.setFontFamily(def, "SansSerif");

		Style s1 = sDoc.addStyle("error", def);
		StyleConstants.setForeground(s1, Color.red);

		Style s2 = sDoc.addStyle("pass", def);
		StyleConstants.setForeground(s2, Color.green);
	}

	public void setConsole(String text, boolean passed) {
		StyledDocument sDoc = displayTextPane.getStyledDocument();

		try {
			if (passed) {
				sDoc.insertString(0, " > " + text + "\n", sDoc.getStyle("pass"));
			} else {
				sDoc.insertString(0, " > " + text + "\n", sDoc.getStyle("error"));
			}
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
}
