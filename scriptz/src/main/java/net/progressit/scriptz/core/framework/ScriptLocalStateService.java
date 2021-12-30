package net.progressit.scriptz.core.framework;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

/**
 * This is one of the most important framework services provided - storage of Config / State to local file system.
 * 
 * <p>This also delegates to the State migration service automatically to upgrade versions. Version upgrades should
 * be supported (if version number changes from 1). Empty version numbers for default data will not be supported.
 * 
 * <p>If a script itself doesn't support Config or State data, then it need not call the related methods via the 
 * App Context. If called, then the related set of Class, upgradeVersion logic and Defaults need to be supported together (in the App Definition). 
 * 
 * @author theo
 *
 */
public class ScriptLocalStateService {
	
	public static class ScriptLocalStateServiceException extends RuntimeException{
		private static final long serialVersionUID = 1L;
		public ScriptLocalStateServiceException(String message) {
			super(message);
		}
		public ScriptLocalStateServiceException(String message, Throwable t) {
			super(message, t);
		}
	}
	
	private final ScriptLocalStateMigrationService migrationService;
	
	@Inject
	public ScriptLocalStateService(ScriptLocalStateMigrationService migrationService) {
		this.migrationService = migrationService;
	}
	
	public enum ScriptLocalStateType {userState, config}
	public void storeState(String script, Object state) {
		store(script, state, ScriptLocalStateType.userState);
	}
	@SuppressWarnings("unchecked")
	public <T,V> T loadState(ScriptAppDefinition<T,V> appDefinition) {
		return (T) load(appDefinition, ScriptLocalStateType.userState);
	}
	public void storeConfig(String script, Object config) {
		store(script, config, ScriptLocalStateType.config);
	}
	@SuppressWarnings("unchecked")
	public <T,V> V loadConfig(ScriptAppDefinition<T,V> appDefinition) {
		return (V) load(appDefinition, ScriptLocalStateType.config);
	}
	
	//PRIVATE
	
	private void store(String script, Object content, ScriptLocalStateType type) {
		try {
			File userHomeScritpzFolder = ensureAndGetScriptzFolder();
			String fileName = getFileName(script, type);
			File file = new File(userHomeScritpzFolder, fileName);
			Gson g = new GsonBuilder().setPrettyPrinting().create();
			try(FileWriter fw = new FileWriter(file)){
				g.toJson(content, fw);
			} 
		} catch (IOException e) {
			throw new ScriptLocalStateServiceException("Exception while storing " + type + " for " + script, e);
		}	
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object load(ScriptAppDefinition appDefinition, ScriptLocalStateType type) {
		try {
			File userHomeScritpzFolder = ensureAndGetScriptzFolder();
			String fileName = getFileName(appDefinition.getName(), type);
			File file = new File(userHomeScritpzFolder, fileName);
			Object loadedData = null;
			if(file.exists()) {
				Gson g = new Gson();
				try(FileReader fr = new FileReader(file)){
					Class dataClass = getClassForType(type, appDefinition);
					loadedData = g.fromJson(fr, dataClass);
					migrationService.migrateIfNeeded(loadedData, type, appDefinition);
				}
			} else {
				//Save the defaults as the data
				Object dataDefaults = getDefaultsForType(type, appDefinition);
				store(appDefinition.getName(), dataDefaults, type);
				loadedData = dataDefaults;
			}
			return loadedData;
		} catch (IOException e) {
			throw new ScriptLocalStateServiceException("Exception while loading " + type + " for " + appDefinition.getName(), e);
		}
	}
	
	@SuppressWarnings("rawtypes")
	private Object getDefaultsForType(ScriptLocalStateType type, ScriptAppDefinition appDefinition) {
		return type==ScriptLocalStateType.config?appDefinition.getConfigDefaults():appDefinition.getStateDefaults();
	}
	
	@SuppressWarnings("rawtypes")
	private Class getClassForType(ScriptLocalStateType type, ScriptAppDefinition appDefinition) {
		return type==ScriptLocalStateType.config?appDefinition.getConfigClass():appDefinition.getStateClass();
	}
	
	private File ensureAndGetScriptzFolder() throws IOException {
		String userHome = System.getProperty("user.home");
		File userHomeScritpzFolder = new File(userHome, ".scriptz");
		boolean foldersCreated = userHomeScritpzFolder.mkdirs();
		if(foldersCreated) {
			Path userHomeScriptzPath = userHomeScritpzFolder.toPath();
			try {
				Files.setAttribute(userHomeScriptzPath, "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
			}catch(Exception e) {
				//Ignore. Setting hidden via this method is not available for this OS. No issues.
			}
		}
		return userHomeScritpzFolder;
	}
	private String getFileName(String script, ScriptLocalStateType type) {
		String fileName = "scriptz-" + script + "." + type + ".json";
		return fileName;
	}
}
