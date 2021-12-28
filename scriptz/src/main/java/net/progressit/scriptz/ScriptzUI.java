package net.progressit.scriptz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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

import lombok.Builder;
import lombok.Data;
import net.progressit.scriptz.core.ScriptAppDefinition;

public class ScriptzUI extends JFrame{
	private static final long serialVersionUID = 1L;
	
	@Data
	public static class ScriptzCoreConfig{
		private Integer version = 1;
		private Map<String, Boolean> scriptStatus = new LinkedHashMap<>();
		private Map<String, ScriptDetails> scriptDetails = new LinkedHashMap<>();
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
		loadToolbar();
		
		dpMain.setBackground(Color.gray);
	}

	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void loadToolbar() {
		Set<String> loadedClassNames = scriptzService.getScriptNames();
		for(String loadedClassName:loadedClassNames) {
			ScriptAppDefinition appDefn = scriptzService.getDefinition(loadedClassName);
			List<JButton> appToolButtons = appDefn.getToolButtons();
			for(JButton appBtn:appToolButtons) {
				//The button should be ready, with handler added by the App itself.
				toolbar.add( appBtn ); 
			}
		}
	}

}
