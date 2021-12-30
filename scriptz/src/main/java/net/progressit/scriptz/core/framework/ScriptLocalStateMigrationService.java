package net.progressit.scriptz.core.framework;

import com.google.inject.Inject;

import net.progressit.scriptz.core.framework.ScriptLocalStateService.ScriptLocalStateType;

public class ScriptLocalStateMigrationService {
	
	@Inject
	public ScriptLocalStateMigrationService() {}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void migrateIfNeeded(Object loadedData, ScriptLocalStateType type, ScriptAppDefinition appDefinition) {
		Object dataDefaults = getDefaultsForType(type, appDefinition);
		
		VersionedDataWrapper liveDataWrapped = new VersionedDataWrapper(loadedData);
		VersionedDataWrapper defaultDataWrapped = new VersionedDataWrapper(dataDefaults);
		
		Integer liveVersion = liveDataWrapped.getVersion();
		Integer latestVersion = defaultDataWrapped.getVersion();

		if(latestVersion==null) {
			throw new RuntimeException("Latest default data doesn't have a version mentioned. Script: " + appDefinition.getName());
		}
		
		int iterations = 0;
		while(liveVersion==null || liveVersion<latestVersion) {
			iterations++;
			if(iterations>100) {
				throw new RuntimeException("Too many file version upgrades. Probably an error. Abortring. Script: " + appDefinition.getName());
			}
			try {
				if(type==ScriptLocalStateType.config) {
					loadedData = appDefinition.upgradeConfigVersionToNext(loadedData);
				} else {
					loadedData = appDefinition.upgradeStateVersionToNext(loadedData);
				}
			}catch(RuntimeException e) {
				throw new RuntimeException("Exception while upgrading data of Script " + appDefinition.getName() 
					+ " from version " + liveVersion + " where latest version is " + latestVersion);
			}
			
			liveDataWrapped.set(loadedData);
			liveVersion = liveDataWrapped.getVersion();
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Object getDefaultsForType(ScriptLocalStateType type, ScriptAppDefinition appDefinition) {
		return type==ScriptLocalStateType.config?appDefinition.getConfigDefaults():appDefinition.getStateDefaults();
	}
}
