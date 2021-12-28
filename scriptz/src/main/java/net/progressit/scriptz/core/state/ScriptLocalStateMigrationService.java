package net.progressit.scriptz.core.state;

import org.apache.commons.jxpath.JXPathContext;

import com.google.inject.Inject;

import net.progressit.scriptz.core.ScriptAppDefinition;
import net.progressit.scriptz.core.state.ScriptLocalStateService.ScriptLocalStateType;

public class ScriptLocalStateMigrationService {
	
	@Inject
	public ScriptLocalStateMigrationService() {}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void migrateIfNeeded(Object loadedData, ScriptLocalStateType type, ScriptAppDefinition appDefinition) {
		Object dataDefaults = getDefaultsForType(type, appDefinition);
		
		JXPathContext sharedContext = JXPathContext.newContext(null);
		sharedContext.setLenient(true);
		
		JXPathContext fileContext = JXPathContext.newContext(sharedContext, loadedData);
		Integer fileVersion = (Integer) fileContext.getValue("version");
		JXPathContext latestContext = JXPathContext.newContext(sharedContext, dataDefaults);
		Integer latestVersion = (Integer) latestContext.getValue("version");
		if(latestVersion==null) {
			throw new RuntimeException("Default data doesn't have a version mentioned. Script: " + appDefinition.getName());
		}
		
		int iterations = 0;
		while(fileVersion==null || fileVersion<latestVersion) {
			iterations++;
			if(iterations>100) throw new RuntimeException("Too many file version upgrades. Probably an error. Abortring. Script: " + appDefinition.getName());
			try {
				if(type==ScriptLocalStateType.config) {
					loadedData = appDefinition.upgradeConfigVersionToNext(loadedData);
				} else {
					loadedData = appDefinition.upgradeStateVersionToNext(loadedData);
				}
			}catch(RuntimeException e) {
				throw new RuntimeException("Exception while upgrading data of Script " + appDefinition.getName() 
					+ " from version " + fileVersion + " where latest version is " + latestVersion);
			}
			fileContext = JXPathContext.newContext(sharedContext, loadedData);
			fileVersion = (Integer) fileContext.getValue("version");
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Object getDefaultsForType(ScriptLocalStateType type, ScriptAppDefinition appDefinition) {
		return type==ScriptLocalStateType.config?appDefinition.getConfigDefaults():appDefinition.getStateDefaults();
	}
}
