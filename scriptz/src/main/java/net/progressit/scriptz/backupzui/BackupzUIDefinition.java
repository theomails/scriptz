package net.progressit.scriptz.backupzui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.google.inject.Injector;

import net.progressit.backupzui.Main;
import net.progressit.backupzui.logic.BackupService;
import net.progressit.scriptz.core.framework.AbstractScriptAppDefinition;
import net.progressit.scriptz.core.framework.ScriptAppContext;

public class BackupzUIDefinition extends AbstractScriptAppDefinition<Object, Object> {

	private ScriptAppContext context = null;
	
	@Override
	public void setAppContext(ScriptAppContext context) {
		this.context = context;
	}
	
	private Injector injector = null;


	@Override
	public String getName() {
		return "Backupz UI";
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
			if(injector==null) {
				injector = Main.getBackupzInjector();
			}
			
			BackupService backupService = injector.getInstance(BackupService.class);
			BackupzUI sif = new BackupzUI(backupService);
			
			context.displayAndInitFrame(sif);
		} );
		
		return List.of(btnFolderUI);
	}

}
