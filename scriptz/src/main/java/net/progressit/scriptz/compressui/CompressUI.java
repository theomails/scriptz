package net.progressit.scriptz.compressui;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.miginfocom.swing.MigLayout;
import net.progressit.backupzui.ui.LimitLinesDocumentListener;
import net.progressit.scriptz.compressui.CompressBO.CompressLogEvent;
import net.progressit.scriptz.compressui.CompressBO.CompressionPlan;
import net.progressit.scriptz.core.ScriptInternalFrame;

public class CompressUI  extends  ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public CompressUI() {
		super("Compress UI", true, true, true, true);
		bus.register(this);
	}
	
	//State
	private CompressionPlan analysisResult = null;
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final CompressBO bo = new CompressBO(bus);
	@SuppressWarnings("unused")
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
	private RSyntaxTextArea taLog = new RSyntaxTextArea();
	private JScrollPane spLog = new JScrollPane(taLog);
	
	private JPanel pnlCompress = new JPanel(new MigLayout("","[][grow, fill][]20[]20[][]","[]"));
	private JPanel pnlDecompress = new JPanel(new MigLayout("","[][grow, fill][]20[]","[]"));
	
	private JTextField tfFolderToCompress = new JTextField();
	private JButton btnBrowse = new JButton("Browse...");
	private JButton btnAnalyze = new JButton("Analyze Folder");
	private JCheckBox chkDontRetainLogs = new JCheckBox("Don't Retain Logs", false);
	private JButton btnCompress = new JButton("Compress Folder");
	
	private JTextField tfFolderToDecompress = new JTextField();
	private JButton btnBrowseDe = new JButton("Browse...");
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
		pnlCompress.add(btnAnalyze);
		pnlCompress.add(chkDontRetainLogs);
		pnlCompress.add(btnCompress);
		
		pnlDecompress.add(new JLabel("Folder to De-compress"));
		pnlDecompress.add(tfFolderToDecompress);
		pnlDecompress.add(btnBrowseDe);
		pnlDecompress.add(btnDeCompress);

		tpMain.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tfFolderToCompress.setText(System.getProperty("user.home"));
		
		taLog.getDocument().addDocumentListener(
			    new LimitLinesDocumentListener(169) );
		taLog.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS);
		
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
	
		btnBrowseDe.addActionListener( (e)->{ 
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File( tfFolderToDecompress.getText() ));
			chooser.setDialogTitle("Choose Folder to De-compress...");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			// disable the "All files" option.
			chooser.setAcceptAllFileFilterUsed(false);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File result = chooser.getSelectedFile();
				if(!result.isDirectory()) {
					result = chooser.getCurrentDirectory();
				}
				tfFolderToDecompress.setText( result.toPath().toString() );
			}
		} );
		
		btnAnalyze.addActionListener( (e)->{ 
			new Thread( ()->{
				analysisResult = bo.analyze( new File( tfFolderToCompress.getText() ));
			} ).start();
		} );
		btnCompress.addActionListener( (e)->{ 
			new Thread( ()->{
				bo.compress( analysisResult, chkDontRetainLogs.isSelected());
			} ).start();
		} );
		btnDeCompress.addActionListener( (e)->{ 
			new Thread( ()->{
				bo.decompress( new File( tfFolderToDecompress.getText() ));
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