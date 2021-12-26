package net.progressit.scriptz.folderui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.progressit.scriptz.core.ScriptAppContext;
import net.progressit.scriptz.core.ScriptAppResourceDefinition;

public class FolderUIDefinition implements ScriptAppResourceDefinition{

	private ScriptAppContext context = null;
	
	@Override
	public void setAppContext(ScriptAppContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "Folder UI";
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
			FolderUI sif = new FolderUI();
			context.loadFrame(sif);
		} );
		
		return List.of(btnFolderUI);
	}

	@Override
	public boolean hasConfigSupport() {
		return false;
	}

	@Override
	public <T> Class<T> getConfigClass() {
		return null;
	}

	@Override
	public void handleConfigMigration(Optional<Integer> savedVersion) {
		
	}

	@Override
	public boolean hasStateSupport() {
		return false;
	}

	@Override
	public <T> Class<T> getStateClass() {
		return null;
	}

	@Override
	public void handleStateMigration(Optional<Integer> savedVersion) {
		
	}
}
