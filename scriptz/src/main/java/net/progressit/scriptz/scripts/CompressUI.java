package net.progressit.scriptz.scripts;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.miginfocom.swing.MigLayout;
import net.progressit.backupzui.ui.LimitLinesDocumentListener;
import net.progressit.scriptz.compressui.CompressBO;
import net.progressit.scriptz.compressui.CompressBO.CompressLogEvent;

public class CompressUI  extends JInternalFrame implements ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public CompressUI() {
		super("Compress UI", true, true, true, true);
		bus.register(this);
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final CompressBO bo = new CompressBO();
	private final FileFilter ffOnlyCLogs = new FileFilter() {
		   public String getDescription() {
		       return "Compression Logs (*.clog.json)";
		   }
		   public boolean accept(File f) {
		       if (f.isDirectory()) {
		           return true;
		       } else {
		           String filename = f.getName().toLowerCase();
		           return filename.endsWith(".clog.json") ;
		       }
		   }
	};
	
	private JPanel pnlOuter = new JPanel(new MigLayout("","[grow, fill]","[][grow, fill]"));
	private JTabbedPane tpMain = new JTabbedPane();
	private JTextArea taLog = new JTextArea();
	private JScrollPane spLog = new JScrollPane(taLog);
	
	private JPanel pnlCompress = new JPanel(new MigLayout("","[][grow, fill][]20[]","[]"));
	private JPanel pnlDecompress = new JPanel(new MigLayout("","[][grow, fill][]20[]","[]"));
	
	private JTextField tfFolderToCompress = new JTextField();
	private JButton btnBrowse = new JButton("Browse...");
	private JButton btnCompress = new JButton("Compress Folder");
	
	private JTextField tfLogToDecompress = new JTextField();
	private JButton btnBrowseLog = new JButton("Browse...");
	private JButton btnDeCompress = new JButton("De-compress with Log");
	
	
	public void init() {
		add(pnlOuter, BorderLayout.CENTER);
		
		pnlOuter.add(tpMain, "wrap");
		pnlOuter.add(spLog, "");
		
		tpMain.add("Compress", pnlCompress);
		tpMain.add("De-compress", pnlDecompress);
		
		pnlCompress.add(new JLabel("Folder to Compress"));
		pnlCompress.add(tfFolderToCompress);
		pnlCompress.add(btnBrowse);
		pnlCompress.add(btnCompress);
		
		pnlDecompress.add(new JLabel("Log to De-compress"));
		pnlDecompress.add(tfLogToDecompress);
		pnlDecompress.add(btnBrowseLog);
		pnlDecompress.add(btnDeCompress);

		tpMain.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tfFolderToCompress.setText(System.getProperty("user.home"));
		
		taLog.getDocument().addDocumentListener(
			    new LimitLinesDocumentListener(399) );
		
		addHandlers();

	}

	private void addHandlers() {
		btnBrowse.addActionListener( (e)->{ 
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File( tfFolderToCompress.getText() ));
			chooser.setDialogTitle("Choose Folder to Compress...");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			// disable the "All files" option.
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File result = chooser.getSelectedFile();
				if(!result.isDirectory()) {
					result = chooser.getCurrentDirectory();
				}
				tfFolderToCompress.setText( result.toPath().toString() );
			}
		} );
	
		btnBrowseLog.addActionListener( (e)->{ 
				JFileChooser chooser = new JFileChooser();
				File directory = new File(System.getProperty("user.home"));
				File logFile = new File( tfLogToDecompress.getText() );
				if(logFile.exists() && logFile.isFile()) {
					directory = logFile.getParentFile();
				}
				chooser.setCurrentDirectory( directory );
				chooser.setDialogTitle("Choose Log to De-compress...");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				// disable the "All files" option.
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileFilter(ffOnlyCLogs);
				if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
					File result = chooser.getSelectedFile();
					if(!result.isDirectory()) {
						result = chooser.getCurrentDirectory();
					}
					tfLogToDecompress.setText( result.toPath().toString() );
				}
			} );
		
		btnCompress.addActionListener( (e)->{ 
			new Thread( ()->{
				bo.compress( new File( tfFolderToCompress.getText() ), bus);
			} ).start();
		} );
		btnDeCompress.addActionListener( (e)->{ 
			new Thread( ()->{
				bo.decompress( new File( tfLogToDecompress.getText() ), bus);
			} ).start();
		} );
	}
	
	@Subscribe
	private void handle(CompressLogEvent event) {
		System.out.println(event.getMessage());
		SwingUtilities.invokeLater( ()->{ 
			logLn(event.getMessage());
		} );
	}
	
	private void log(String msg) {
		taLog.append(msg);
		taLog.getCaret().setDot(Integer.MAX_VALUE);
	}
	private void logLn(String msg) {
		log(msg + "\n");
	}
}