package net.progressit.scriptz;

import java.awt.Color;
import java.beans.PropertyVetoException;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenuBar;

import net.progressit.scriptz.scripts.DeskGet;
import net.progressit.scriptz.scripts.DeskTextFormat;
import net.progressit.scriptz.scripts.ProjectsTouch;

public class ScriptzUI extends JFrame{
	private static final long serialVersionUID = 1L;
	
	private JDesktopPane dpMain = new JDesktopPane();
	private JMenuBar mbMain = new JMenuBar();
	//One button per Script as of now.
	private JButton btnDeskGet = new JButton("Desk Get");
	private JButton btnDeskTextFormat = new JButton("Desk Text Format");
	private JButton btnProjectsTouch = new JButton("Touch Projects");
	public void init() {
		setJMenuBar(mbMain);
		setContentPane(dpMain);
		
		mbMain.add(btnDeskTextFormat);
		mbMain.add(btnDeskGet);
		mbMain.add(btnProjectsTouch);
		
		
		dpMain.setBackground(Color.gray);
		addHandlers();
	}
	
	private void addHandlers() {
		btnDeskTextFormat.addActionListener( (e)->{ 
			DeskTextFormat sif = new DeskTextFormat();
			dpMain.add( sif );
			sif.init();
			showFrame(sif, true);			
		} );
		btnDeskGet.addActionListener( (e)->{
			DeskGet sif = new DeskGet();
			dpMain.add( sif );
			sif.init();
			showFrame(sif, true);
		} );
		btnProjectsTouch.addActionListener( (e)->{
			ProjectsTouch sif = new ProjectsTouch();
			dpMain.add( sif );
			sif.init();
			showFrame(sif, false);
		} );
	}
	private void showFrame(JInternalFrame jif, boolean maximize) {
		position(jif);
		if(maximize) {
			try {
				jif.setMaximum(true);
			} catch (PropertyVetoException e1) {
				e1.printStackTrace();
			}
		}
		jif.setVisible(true);		
	}
	private void position(JInternalFrame jif) {
		int cnt = (dpMain.getComponentCount()-1) % 10 + 1; //-1 because this component has been already added, plus one after the modulo
		jif.setLocation( cnt*50 , cnt*50 );
	}
}
