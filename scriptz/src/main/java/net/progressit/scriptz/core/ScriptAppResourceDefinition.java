package net.progressit.scriptz.core;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public interface ScriptAppResourceDefinition {
	
	public enum ScriptStandardMenu{ FILE, SCRIPTS }
	
	//CORE
	
	public void setAppContext(ScriptAppContext context);

	public String getName();
	
	//DISPLAY
	
	public Map<ScriptStandardMenu, JMenuItem> getStandardMenuItems();
	
	public JMenu getMenu();
	
	public List<JButton> getToolButtons();
	
	//STATE and CONFIG
	
	public boolean hasConfigSupport();
	
	public <T> Class<T> getConfigClass();
	
	public void handleConfigMigration(Optional<Integer> savedVersion);
	
	public boolean hasStateSupport();
	
	public <T> Class<T> getStateClass();
	
	public void handleStateMigration(Optional<Integer> savedVersion);
}
