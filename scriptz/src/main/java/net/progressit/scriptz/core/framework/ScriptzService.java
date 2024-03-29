package net.progressit.scriptz.core.framework;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.swing.JDesktopPane;

import net.progressit.scriptz.ScriptzUI.ScriptDetails;
import net.progressit.scriptz.ScriptzUI.ScriptzCoreConfig;
import net.progressit.scriptz.core.app.ScriptCoreAppDefinition;

/**
 * Provides access to all Scriptz definitons.
 * 
 * <p>On first demand for an app definition, all the app defenitions are loaded. However, the script app UI's or helpers are not initialized.
 * 
 * @return
 */
public class ScriptzService {
	private static final ScriptAppDefinition<ScriptzCoreConfig, Object> SCRIPT_CORE_APP_DEFINITION = new ScriptCoreAppDefinition();
	
	private final ScriptLocalStateService localStateService;
	private JDesktopPane dpMain;

	@Inject
	public ScriptzService(ScriptLocalStateService localStateService) {
		super();
		this.localStateService = localStateService;
	}

	@SuppressWarnings("rawtypes")
	private final Map<String, ScriptAppDefinition> loadedDefinitions = new LinkedHashMap<>();
	
	public void setContainer(JDesktopPane dpMain) {
		this.dpMain = dpMain;
	}

	/**
	 * Warning: Lazy load enabled. Can trigger the loading of entire scripts definition.
	 * @return
	 */
	public Set<String> getScriptNames(){
		ensureDefinitions();
		return loadedDefinitions.keySet();
	}
	
	/**
	 * Warning: Lazy load enabled. Can trigger the loading of entire scripts definition.
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public ScriptAppDefinition getDefinition(String scriptName) {
		ensureDefinitions();
		return loadedDefinitions.get(scriptName);
	}
	
	// PRIVATE
	
	private void ensureDefinitions(){
		if(loadedDefinitions.isEmpty()) {
			loadDefinitions();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void loadDefinitions() {
		ScriptzCoreConfig coreConfig = (ScriptzCoreConfig) localStateService.loadConfig(SCRIPT_CORE_APP_DEFINITION);
		Set<String> scriptClassNames = coreConfig.getScriptDetails().keySet();
		for(String scriptClassName: scriptClassNames) {
			ScriptDetails scriptDetails = coreConfig.getScriptDetails().get(scriptClassName);
			if(scriptDetails.isEnabled()) {
				ScriptAppDefinition appDefinition = null;
				Class<?> scriptClass;
				try {
					scriptClass = Class.forName(scriptClassName);
					Constructor<?> cons = scriptClass.getConstructor();
					appDefinition = (ScriptAppDefinition) cons.newInstance();
					appDefinition.setAppContext( new ScriptAppContextImpl(dpMain, localStateService, appDefinition) );
					loadedDefinitions.put(scriptClassName, appDefinition);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}

			}
		}
	}
	

}
