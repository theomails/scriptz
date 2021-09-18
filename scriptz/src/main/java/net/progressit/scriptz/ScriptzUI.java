package net.progressit.scriptz;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenuBar;

import com.google.inject.Inject;

import lombok.Data;
import net.progressit.scriptz.core.ScriptAppResourceDefinition;
import net.progressit.scriptz.core.ScriptLocalStateService;

public class ScriptzUI extends JFrame{
	private static final long serialVersionUID = 1L;
	
	@Data
	public static class ScriptzCoreConfig{
		private Map<String, Boolean> scriptStatus = new LinkedHashMap<>();
	}

	private ScriptzCoreConfig coreConfig;
	private final Map<String, ScriptAppResourceDefinition> loadedDefinitions = new LinkedHashMap<>(); 
	private ScriptLocalStateService localStateService;
	@Inject
	public ScriptzUI(ScriptLocalStateService localStateService) {
		this.localStateService = localStateService;
	}
	
	private JDesktopPane dpMain = new JDesktopPane();
	private JMenuBar mbMain = new JMenuBar();

	public void init() {
		setJMenuBar(mbMain);
		setContentPane(dpMain);
		
		loadDefinitions();
		Set<String> loadedClasses = loadedDefinitions.keySet();
		loadToolBar(loadedClasses);
		
		dpMain.setBackground(Color.gray);
	}
	
	private void loadDefinitions() {
		coreConfig = localStateService.loadConfig("core", new ScriptzCoreConfig(), ScriptzCoreConfig.class);
		Set<String> scriptClasses = coreConfig.scriptStatus.keySet();
		for(String scriptClass: scriptClasses) {
			boolean stateEnabled = coreConfig.getScriptStatus().get(scriptClass);
			if(stateEnabled) {
				ScriptAppResourceDefinition appDefinition = null;
				Class<?> c;
				try {
					c = Class.forName(scriptClass);
					Constructor<?> cons = c.getConstructor();
					Object object = cons.newInstance();
					appDefinition = (ScriptAppResourceDefinition) object;
					appDefinition.setAppContext( new ScriptAppContextImpl(dpMain, localStateService, appDefinition) );
					loadedDefinitions.put(scriptClass, appDefinition);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}

			}
		}
	}
	
	private void loadToolBar(Set<String> loadedClasses) {
		for(String loadedClass:loadedClasses) {
			ScriptAppResourceDefinition appDefn = loadedDefinitions.get(loadedClass);
			List<JButton> appToolButtons = appDefn.getToolButtons();
			for(JButton appBtn:appToolButtons) {
				//The button should be ready, with handler added by the App itself.
				mbMain.add( appBtn ); 
			}
		}
	}

}
