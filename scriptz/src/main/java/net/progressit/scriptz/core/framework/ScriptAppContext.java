package net.progressit.scriptz.core.framework;

import javax.swing.JDesktopPane;

/**
 * This is provided to the Script App Definition, so that the Script app can talk to the framework.
 * 
 * @author theo
 *
 */
public interface ScriptAppContext {
	public void displayAndInitFrame(ScriptInternalFrame frame);

	@Deprecated
	public JDesktopPane getContainer();

	public <T> T loadState();

	public void storeState(Object config);
	
	public <T> T loadConfig();

	public void storeConfig(Object config);

	public void setStatusMessage(String message);
}