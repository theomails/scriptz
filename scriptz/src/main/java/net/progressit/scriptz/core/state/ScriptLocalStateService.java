package net.progressit.scriptz.core.state;

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
	
	@Inject
	public ScriptLocalStateService() {}
	
	public enum ScriptLocalStateType {userState, config}
	public void storeState(String script, Object state) {
		store(script, state, ScriptLocalStateType.userState);
	}
	public <T> T loadState(String script, T stateDefaults, Class<T> classOfState) {
		return load(script, stateDefaults, classOfState, ScriptLocalStateType.userState);
	}
	public void storeConfig(String script, Object config) {
		store(script, config, ScriptLocalStateType.config);
	}
	public <T> T loadConfig(String script, T configDefaults, Class<T> classOfConfig) {
		return load(script, configDefaults, classOfConfig, ScriptLocalStateType.config);
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
	
	private <T> T load(String script, T defaults, Class<T> classOfContent, ScriptLocalStateType type) {
		try {
			File userHomeScritpzFolder = ensureAndGetScriptzFolder();
			String fileName = getFileName(script, type);
			File stateFile = new File(userHomeScritpzFolder, fileName);
			T loadedData = null;
			if(stateFile.exists()) {
				Gson g = new Gson();
				try(FileReader fr = new FileReader(stateFile)){
					loadedData = g.fromJson(fr, classOfContent);
				}
			}else {
				//Save the defaults as the data
				store(script, defaults, type);
				loadedData = defaults;
			}
			migrateIfNeeded(loadedData);
			return loadedData;
		} catch (IOException e) {
			throw new ScriptLocalStateServiceException("Exception while loading " + type + " for " + script, e);
		}
	}
	
	private <T> void migrateIfNeeded(T loadedData) {
		// TODO Auto-generated method stub
		
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
