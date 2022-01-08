package net.progressit.scriptz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.google.inject.Inject;

import lombok.Builder;
import lombok.Data;
import net.progressit.scriptz.core.framework.ScriptAppDefinition;
import net.progressit.scriptz.core.framework.ScriptAppDefinition.ScriptStandardMenu;
import net.progressit.scriptz.core.framework.ScriptzService;

public class ScriptzUI extends JFrame{
	private static final long serialVersionUID = 1L;
	
	@Data
	public static class ScriptzCoreConfig{
		private Integer version;
		private Map<String, Boolean> scriptStatus = null;
		private Map<String, ScriptDetails> scriptDetails = new LinkedHashMap<>();
		@Deprecated
		public Map<String, Boolean> getScriptStatus(){
			return scriptStatus;
		}
	}
	@Data
	@Builder
	public static class ScriptDetails{
		@Builder.Default
		private boolean enabled = true;
		@Builder.Default
		private boolean showInBar = true;
	}
	
	private final ScriptzService scriptzService;

	@Inject
	public ScriptzUI(ScriptzService scriptzService) {
		this.scriptzService = scriptzService;
	}
	
	private JPanel pnlWrapper = new JPanel(new BorderLayout());
	private JDesktopPane dpMain = new JDesktopPane();
	//private JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 2));
	private JToolBar toolbar = new JToolBar();
	private JMenuBar menubar = new JMenuBar();
	private JMenu mnuFile = new JMenu("File");
	private JMenu mnuScripts = new JMenu("Scripts");
	private JMenuItem miExit = new JMenuItem("Exit");

	public void init() {
		setJMenuBar(menubar);
		setContentPane(pnlWrapper);
		
		menubar.add(mnuFile);
		menubar.add(mnuScripts);
		
		mnuFile.add(miExit);
		
		pnlWrapper.add(toolbar, BorderLayout.NORTH);
		pnlWrapper.add(dpMain, BorderLayout.CENTER);
		
		toolbar.setAlignmentX(LEFT_ALIGNMENT);
		
		dpMain.setBackground(Color.gray);
		
		//Process definitions
		scriptzService.setContainer(dpMain);
		loadToolbar();
		
		addActionHandlers();
	}

	private void addActionHandlers() {
		miExit.addActionListener( (e)->{
			setVisible(false); //you can't see me!
			dispose(); //Destroy the JFrame object
		} );
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadToolbar() {
		Set<String> loadedClassNames = scriptzService.getScriptNames();
		for(String loadedClassName:loadedClassNames) {
			ScriptAppDefinition appDefn = scriptzService.getDefinition(loadedClassName);
			
			List<JButton> appToolButtons = appDefn.getToolButtons();
			if(appToolButtons!=null) {
				for(JButton appBtn:appToolButtons) {
					//The button should be ready, with handler added by the App itself.
					toolbar.add( appBtn ); 
				}
			}
			
			Map<ScriptStandardMenu, JMenuItem> appStandardMenuItems = appDefn.getStandardMenuItems();
			if(appStandardMenuItems!=null) {
				Set<Entry<ScriptStandardMenu, JMenuItem>> itemEntries = appStandardMenuItems.entrySet();
				for(Entry<ScriptStandardMenu, JMenuItem> itemEntry:itemEntries) {
					if(itemEntry.getKey()==ScriptStandardMenu.FILE) {
						mnuFile.add(itemEntry.getValue());
					}else if(itemEntry.getKey()==ScriptStandardMenu.SCRIPTS) {
						mnuScripts.add(itemEntry.getValue());
					}else {
						throw new RuntimeException("Unknown standard menu.");
					}
				}
			}
			
		}
	}

}
