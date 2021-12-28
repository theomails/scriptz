package net.progressit.scriptz.core;

import javax.swing.JDesktopPane;

public interface ScriptAppContext {
	public void loadFrame(ScriptInternalFrame frame);

	public JDesktopPane getContainer();

	public <T> T loadState();

	public void storeState(Object config);
	
	public <T> T loadConfig();

	public void storeConfig(Object config);

	public void setStatusMessage(String message);
}