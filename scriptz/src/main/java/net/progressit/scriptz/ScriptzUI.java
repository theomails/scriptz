package net.progressit.scriptz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import com.google.inject.Inject;

import lombok.Data;
import net.progressit.scriptz.core.ScriptAppResourceDefinition;
import net.progressit.scriptz.core.state.ScriptLocalStateService;

public class ScriptzUI extends JFrame{
	private static final long serialVersionUID = 1L;
	
	@Data
	public static class ScriptzCoreConfig{
		private Map<String, Boolean> scriptStatus = new LinkedHashMap<>();
		private Map<String, ScriptDetails> scriptDetails = new LinkedHashMap<>();
	}
	@Data
	public static class ScriptDetails{
		private Integer version = 1;
		private boolean enabled = true;
		private boolean showInBar = true;
	}

	private ScriptzCoreConfig coreConfig;
	private final Map<String, ScriptAppResourceDefinition> loadedDefinitions = new LinkedHashMap<>(); 
	private ScriptLocalStateService localStateService;
	@Inject
	public ScriptzUI(ScriptLocalStateService localStateService) {
		this.localStateService = localStateService;
	}
	
	private JPanel pnlWrapper = new JPanel(new BorderLayout());
	private JDesktopPane dpMain = new JDesktopPane();
	private JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
	private JMenuBar menubar = new JMenuBar();
	private JMenu mnuFile = new JMenu();
	private JMenu mnuScripts = new JMenu();

	public void init() {
		setJMenuBar(menubar);
		setContentPane(pnlWrapper);
		
		menubar.add(mnuFile);
		menubar.add(mnuScripts);
		
		pnlWrapper.add(toolbar, BorderLayout.NORTH);
		pnlWrapper.add(dpMain, BorderLayout.CENTER);
		
		toolbar.setAlignmentX(LEFT_ALIGNMENT);
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
				toolbar.add( appBtn ); 
			}
		}
	}

}
