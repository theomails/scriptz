package net.progressit.scriptz.framework;

import javax.swing.JDesktopPane;

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