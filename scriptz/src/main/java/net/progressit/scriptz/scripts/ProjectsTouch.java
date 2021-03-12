package net.progressit.scriptz.scripts;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import net.miginfocom.swing.MigLayout;
import net.progressit.scriptz.projectstouch.ProjectTouchRunnable;
import net.progressit.scriptz.projectstouch.ProjectTouchRunnable.PTProjectIdentifiedAsyncEvent;
import net.progressit.scriptz.projectstouch.ProjectTouchRunnable.PTProjectTouchAsyncEvent;
import net.progressit.scriptz.projectstouch.ProjectTouchRunnable.ProjectIdentifyEventCode;
import net.progressit.scriptz.projectstouch.ProjectTouchRunnable.ProjectTouchEventCode;
import net.progressit.scriptz.swingsupport.LimitLinesDocumentListener;

public class ProjectsTouch extends JInternalFrame implements ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public ProjectsTouch() {
		super("Projects Touch", true, true, true, true);
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();

	private JPanel outer = new JPanel(new MigLayout("","[800::, grow, fill]","[][600::,grow,fill]"));
	private JButton btnRun = new JButton("Run");
	private JTextArea taLog = new JTextArea();
	private JScrollPane spLog = new JScrollPane(taLog);
	@Override
	public void init() {
		setBounds(50,50,300,300);
		
		add(outer, BorderLayout.CENTER);
		outer.add(btnRun, "wrap");
		outer.add(spLog, "");
		
		pack();
		
		taLog.setLineWrap(true);
		taLog.setWrapStyleWord(true);
		taLog.getDocument().addDocumentListener(
			    new LimitLinesDocumentListener(499) );
		
		addHandlers();
	}
	
	private void log(String msg) {
		taLog.append(msg);
		taLog.getCaret().setDot(Integer.MAX_VALUE);
	}
	private void logLn(String msg) {
		log(msg + "\n");
	}
	
	private void addHandlers() {
		bus.register(this);
		
		btnRun.addActionListener( (e)->{
			new Thread( new ProjectTouchRunnable(bus) ).start();
		} );
	}
	
	@Subscribe
	private void handle(PTProjectIdentifiedAsyncEvent e) {
		SwingUtilities.invokeLater( ()->{ 
			if(e.getCode()==ProjectIdentifyEventCode.START) {
				log("Projects ");
			}else if(e.getCode() == ProjectIdentifyEventCode.FOUND) {
				log(e.getProjectId() + " "); 
			}else {
				logLn("");
				logLn("Finished finding projects. ");
			}
		}  );
	}
	
	@Subscribe
	private void handle(PTProjectTouchAsyncEvent e) {
		SwingUtilities.invokeLater( ()->{ 
			if(e.getCode()==ProjectTouchEventCode.START) {
				logLn("Continuing to touch projects.. ");
			}else if(e.getCode() == ProjectTouchEventCode.TOUCH) {
				logLn("Touching.. " + e.getProjectId() ); 
			}else {
				logLn("Finished touching projects.");
			}
		}  );
	}
}
