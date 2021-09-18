package net.progressit.scriptz.core;

import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

public interface ScriptAppResourceDefinition {
	
	public enum ScriptStandardMenus{ FILE }
	
	public void setAppContext(ScriptAppContext context);
	
	public String getName();
	
	public Map<ScriptStandardMenus, JMenuItem> getStandardMenuItems();
	
	public JMenu getMenu();
	
	public List<JButton> getToolButtons();
	
}
