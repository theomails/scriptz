package net.progressit.scriptz;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;

import net.progressit.scriptz.scripts.ProjectsTouch;

public class ScriptzUI extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private JDesktopPane dpMain = new JDesktopPane();
	private JMenuBar mbMain = new JMenuBar();
	private JButton btnProjectsTouch = new JButton("Touch Projects");
	public void init() {
		setJMenuBar(mbMain);
		setContentPane(dpMain);
		
		mbMain.add(btnProjectsTouch);
		
		
		dpMain.setBackground(Color.gray);
		addHandlers();
	}
	
	private void addHandlers() {
		btnProjectsTouch.addActionListener( (e)->{
			ProjectsTouch sif = new ProjectsTouch();
			
			dpMain.add( sif );
			
			sif.init();
			position(sif);
			sif.setVisible(true);
		} );
	}

	private void position(JInternalFrame jif) {
		int cnt = (dpMain.getComponentCount()-1) % 10 + 1; //-1 because this component has been already added, plus one after the modulo
		jif.setLocation( cnt*50 , cnt*50 );
	}
}
