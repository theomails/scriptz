package net.progressit.scriptz.chatclean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.progressit.scriptz.core.framework.AbstractScriptAppDefinition;
import net.progressit.scriptz.core.framework.ScriptAppContext;

public class ChatCleanDefinition extends AbstractScriptAppDefinition<Object, Object> {

	private ScriptAppContext context = null;
	
	@Override
	public void setAppContext(ScriptAppContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "Chat Clean";
	}

	@Override
	public Map<ScriptStandardMenu, JMenuItem> getStandardMenuItems() {
		return new HashMap<>();
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

	@Override
	public List<JButton> getToolButtons() {
		
		JButton btnFThisApp = new JButton( getName() );
		btnFThisApp.addActionListener( (e)->{
			ChatCleanUI sif = new ChatCleanUI();
			context.displayAndInitFrame(sif);
		} );
		
		return List.of(btnFThisApp);
	}

}
