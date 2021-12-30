package net.progressit.scriptz.core.framework;

import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * This is where the Script App defines itself, and by these hooks it will be initialized.
 * 
 * <p>A reference to the environment will provided via the AppContext, which the Script app can keep a 
 * reference to in order to interact with the framework.
 * 
 * @author theo
 *
 * @param <T>
 * @param <V>
 */
public interface ScriptAppDefinition<T,V> {
	
	public enum ScriptStandardMenu{ FILE, SCRIPTS }
	
	//CORE
	
	public void setAppContext(ScriptAppContext context);

	public String getName();
	
	//DISPLAY
	
	public Map<ScriptStandardMenu, JMenuItem> getStandardMenuItems();
	
	public JMenu getMenu();
	
	public List<JButton> getToolButtons();
	
	//STATE and CONFIG
	
	/**
	 * Return null to indicate that the Config data is not applicable for this script.
	 * @return
	 */
	public Class<T> getConfigClass();
	
	public T upgradeConfigVersionToNext(T currentData);
	
	public T getConfigDefaults();
	
	/**
	 * Return null to indicate that the State data is not applicable for this script.
	 * @return
	 */
	public Class<V> getStateClass();
	
	public V upgradeStateVersionToNext(V currentData);
	
	public V getStateDefaults();
}
