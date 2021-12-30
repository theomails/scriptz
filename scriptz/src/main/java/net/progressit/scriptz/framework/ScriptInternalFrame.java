package net.progressit.scriptz.framework;

import javax.swing.JInternalFrame;

public abstract class ScriptInternalFrame extends JInternalFrame{
	private static final long serialVersionUID = 1L;

	public ScriptInternalFrame(String title, boolean resizable, boolean closable,
            boolean maximizable, boolean iconifiable) {
		super(title, resizable, closable, maximizable, iconifiable);
	}
	
	public abstract void init();	
}
