package net.progressit.scriptz.folderui;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;

import net.progressit.folderzui.ui.VisualizeFolderApp;
import net.progressit.progressive.PComponent;
import net.progressit.progressive.PComponent.PEventListener;
import net.progressit.progressive.PComponent.PPlacers;
import net.progressit.scriptz.core.ScriptInternalFrame;

public class FolderUI  extends  ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public FolderUI() {
		super("Visualize Folder", true, true, true, true);
	}
	
	private String home = System.getProperty("user.home");
	private PPlacers simplePlacers = new PPlacers( (component)->getContentPane().add(component, BorderLayout.CENTER), (component)->getContentPane().remove(component) );
	private VisualizeFolderApp app = new VisualizeFolderApp(simplePlacers, this);

	public void init() {
		PComponent.place(app, new PEventListener() {}, home);
		
		setSize(800, 600);
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		setTitle("Visualize Folder");
	}
}