package net.progressit.scriptz.scripts;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.EventBus;

import net.miginfocom.swing.MigLayout;
import net.progressit.scriptz.folderui.DrawPanel;
import net.progressit.scriptz.folderui.Scanner;
import net.progressit.scriptz.folderui.Scanner.FolderDetails;

public class FolderUI  extends JInternalFrame implements ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public FolderUI() {
		super("Folder UI", true, true, true, true);
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final Scanner scanner = new Scanner();
	
	private DrawPanel drawPanel = new DrawPanel();
	private JScrollPane spDrawPanel = new JScrollPane(drawPanel);
	private JPanel pnlBrowse = new JPanel(new MigLayout("insets 0", "[][grow, fill][][][]", "[]"));
	private JTextField tfPath = new JTextField();
	private JButton btnBrowse = new JButton("Browse...");
	private JCheckBox cbCountDisplay = new JCheckBox("Show No. of Files", false);
	private JButton btnScan = new JButton("Scan");
	private JPanel pnlStatus = new JPanel(new MigLayout("insets 0", "[grow, fill]", "[]"));
	private JLabel lblStatus = new JLabel("Ready.");
	public void init() {
		add(spDrawPanel, BorderLayout.CENTER);
		add(pnlBrowse, BorderLayout.NORTH);
		add(pnlStatus, BorderLayout.SOUTH);

		pnlBrowse.add(new JLabel("Folder to scan"));
		pnlBrowse.add(tfPath);
		pnlBrowse.add(btnBrowse);
		pnlBrowse.add(cbCountDisplay);
		pnlBrowse.add(btnScan);
		
		pnlStatus.add(lblStatus);

		tfPath.setText(System.getProperty("user.home"));

		addHandlers();

	}

	private void addHandlers() {
		MouseWheelListener[] listeners = spDrawPanel.getMouseWheelListeners();
		for (MouseWheelListener lis : listeners) {
			spDrawPanel.removeMouseWheelListener(lis);
		}

		spDrawPanel.addMouseWheelListener((e) -> {
			//System.out.println(e);
			if (e.isControlDown()) {
				int rotation = -1 * e.getWheelRotation();
				rotation %= 3;

				double scale = (5d + dbl(rotation)) / 5d;
				int width = (int) (dbl(drawPanel.getWidth()) * scale);
				int height = (int) (dbl(drawPanel.getWidth()) * scale);
				width = (width < spDrawPanel.getWidth()) ? spDrawPanel.getWidth() : width;
				height = (height < spDrawPanel.getHeight()) ? spDrawPanel.getHeight() : height;
				drawPanel.setPreferredSize(new Dimension(width, height));
				drawPanel.setSize(new Dimension(width, height));
				drawPanel.repaint();
			} else {
				// pass the event on to the scroll pane
				// getParent().dispatchEvent(e);
			}
		});

		btnBrowse.addActionListener((e) -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File( tfPath.getText() ));
			chooser.setDialogTitle("Choose Folder to Scan...");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			// disable the "All files" option.
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File result = chooser.getSelectedFile();
				if(!result.isDirectory()) {
					result = chooser.getCurrentDirectory();
				}
				tfPath.setText( result.toPath().toString() );
			}
		});
		
		btnScan.addActionListener( (e)->{
			lblStatus.setText("Scanning...");
			new Thread( ()->{
				
				try {
					FolderDetails details = scanner.scan(cbCountDisplay.isSelected(), Paths.get(tfPath.getText()) );
					
					SwingUtilities.invokeLater( ()->{
						drawPanel.setDetails(details);
					} );
					lblStatus.setText("Ready.");
				} catch (IOException e1) {
					lblStatus.setText( e1.toString() );
				}
				
			} ).start();
		} );
	}

	private double dbl(long val) {
		return ((double) val);
	}
}