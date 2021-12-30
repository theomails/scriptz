package net.progressit.scriptz.core.app;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.progressit.scriptz.ScriptzUI.ScriptDetails;
import net.progressit.scriptz.ScriptzUI.ScriptzCoreConfig;
import net.progressit.scriptz.core.framework.AbstractScriptAppDefinition;
import net.progressit.scriptz.core.framework.ScriptAppContext;

/**
 * The UI core of the Scriptz system also uses some of the framework features like load/store of config files.
 * 
 * <p>Hence, the core app also provides a Script App Definition.
 *  
 * @author theo
 *
 */
public class ScriptCoreAppDefinition extends AbstractScriptAppDefinition<ScriptzCoreConfig, Object> {
	@Override
	public void setAppContext(ScriptAppContext context) {
	}

	@Override
	public String getName() {
		return "core";
	}

	@Override
	public Class<ScriptzCoreConfig> getConfigClass() {
		return ScriptzCoreConfig.class;
	}

	@Override
	public ScriptzCoreConfig upgradeConfigVersionToNext(ScriptzCoreConfig currentData) {
		Integer version = currentData.getVersion();
		
		if (version == null) {
			@SuppressWarnings("deprecation")
			Map<String, Boolean> scriptStatuses = currentData.getScriptStatus();
			Set<String> scriptNames = scriptStatuses.keySet();
			for (String scriptName : scriptNames) {
				boolean enabled = scriptStatuses.get(scriptName);

				ScriptDetails scriptDetails = ScriptDetails.builder().enabled(enabled).showInBar(true).build();
				ensureScriptDetailsCollection(currentData);
				currentData.getScriptDetails().put(scriptName, scriptDetails);
			}
			currentData.setScriptStatus(null);
			currentData.setVersion(1);
		}
		
		return currentData;
	}

	//PRIVATE
	
	private void ensureScriptDetailsCollection(ScriptzCoreConfig currentData) {
		Map<String, ScriptDetails> detailsMap = currentData.getScriptDetails();
		if (detailsMap == null) {
			currentData.setScriptDetails(new HashMap<>());
		}
	}

	@Override
	public ScriptzCoreConfig getConfigDefaults() {
		ScriptzCoreConfig defaults = new ScriptzCoreConfig();
		defaults.setVersion(1); //Latest version
		return defaults;
	}
}