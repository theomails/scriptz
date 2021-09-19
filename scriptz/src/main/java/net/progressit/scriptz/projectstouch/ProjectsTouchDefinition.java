package net.progressit.scriptz.projectstouch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.progressit.scriptz.core.ScriptAppContext;
import net.progressit.scriptz.core.ScriptAppResourceDefinition;

public class ProjectsTouchDefinition implements ScriptAppResourceDefinition{

	private ScriptAppContext context = null;
	
	@Override
	public void setAppContext(ScriptAppContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "Projects Touch";
	}

	@Override
	public Map<ScriptStandardMenus, JMenuItem> getStandardMenuItems() {
		return new HashMap<>();
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

	@Override
	public List<JButton> getToolButtons() {
		
		JButton btnFolderUI = new JButton( getName() );
		btnFolderUI.addActionListener( (e)->{
			ProjectsTouch sif = new ProjectsTouch();
			context.loadFrame(sif);
		} );
		
		return List.of(btnFolderUI);
	}

}
