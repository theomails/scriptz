package net.progressit.scriptz.compressui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.progressit.scriptz.core.AbstractScriptAppDefinition;
import net.progressit.scriptz.core.ScriptAppContext;

public class CompressUIDefinition extends AbstractScriptAppDefinition<Object, Object> {

	private ScriptAppContext context = null;
	
	@Override
	public void setAppContext(ScriptAppContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "Compress UI";
	}

	@Override
	public Map<ScriptStandardMenu, JMenuItem> getStandardMenuItems() {
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
			CompressUI sif = new CompressUI();
			context.displayAndInitFrame(sif);
		} );
		
		return List.of(btnFolderUI);
	}

}
