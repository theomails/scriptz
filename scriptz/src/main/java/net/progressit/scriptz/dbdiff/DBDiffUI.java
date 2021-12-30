package net.progressit.scriptz.dbdiff;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.miginfocom.swing.MigLayout;
import net.progressit.backupzui.ui.LimitLinesDocumentListener;
import net.progressit.scriptz.core.framework.ScriptInternalFrame;
import net.progressit.scriptz.dbdiff.DBDiffBO.DbDiffLogEvent;

public class DBDiffUI  extends  ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public DBDiffUI() {
		super("DB Diff UI", true, true, true, true);
		bus.register(this);
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final DBDiffBO bo = new DBDiffBO(bus);
	
	private JPanel pnlOuter = new JPanel(new MigLayout("","[grow, fill]","[][grow, fill]"));
	private RSyntaxTextArea taLog = new RSyntaxTextArea();
	private JScrollPane spLog = new JScrollPane(taLog);
	
	private JPanel pnlConnect = new JPanel(new MigLayout("","[][grow, fill]20[][grow, fill]20[][grow, fill][]20[]10[][]","[]"));
	
	private JTextField txtConnection = new JTextField("jdbc:postgresql://localhost:65432/servicedesk");
	private JTextField txtUname = new JTextField("");
	private JPasswordField txtPass = new JPasswordField("");
	private JButton btnConnect = new JButton("Connect");
	private JButton btnGrabSchema = new JButton("Grab Schema");
	private JButton btnGrabDiff = new JButton("Grab Diff");
	private JButton btnShowDiff = new JButton("Show Diff");
	
	public void init() {
		add(pnlOuter, BorderLayout.CENTER);
		
		pnlOuter.add(pnlConnect, "wrap");
		pnlOuter.add(spLog, "");
		
		pnlConnect.add(new JLabel("Connection"));
		pnlConnect.add(txtConnection);
		pnlConnect.add(new JLabel("User name"));
		pnlConnect.add(txtUname);
		pnlConnect.add(new JLabel("Password"));
		pnlConnect.add(txtPass);
		pnlConnect.add(btnConnect);
		pnlConnect.add(btnGrabSchema);
		pnlConnect.add(btnGrabDiff);
		pnlConnect.add(btnShowDiff);
		
		taLog.getDocument().addDocumentListener(
			    new LimitLinesDocumentListener(2999) );
		
		taLog.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS);
		taLog.setCodeFoldingEnabled(true);
		
		addHandlers();
	}

	private void addHandlers() {
		btnConnect.addActionListener( (e)->{
			bo.connect( txtConnection.getText(), txtUname.getText(), txtPass.getPassword() );
		} );
		btnGrabSchema.addActionListener( (e)->{ 
			new Thread( ()->{ bo.grabSchema(); } ).start();
		} );
		btnGrabDiff.addActionListener( (e)->{ 
			new Thread( ()->{ bo.grabDiff(); } ).start();
		} );
		btnShowDiff.addActionListener( (e)->{ 
			new Thread( ()->{ bo.printDiff(); } ).start();
		} );
	}
	
	@Subscribe
	private void handle(DbDiffLogEvent event) {
		System.out.println(event.getMessage());
		SwingUtilities.invokeLater( ()->{ 
			logLn(event.getMessage());
		} );
	}
	
	private void log(String msg) {
		try {
			taLog.append(msg);
			taLog.getCaret().setDot( taLog.getText().length() );
		} catch(Throwable e) {
			System.err.println( e.toString() );
		}
	}
	private void logLn(String msg) {
		log(msg + "\n");
	}
}