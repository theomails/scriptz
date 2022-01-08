package net.progressit.scriptz.deskget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.progressit.scriptz.core.framework.AbstractScriptAppDefinition;
import net.progressit.scriptz.core.framework.ScriptAppContext;

public class DeskGetDefinition extends AbstractScriptAppDefinition<Object, Object> {

	private ScriptAppContext context = null;
	
	@Override
	public void setAppContext(ScriptAppContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "Desk Get";
	}

	@Override
	public Map<ScriptStandardMenu, JMenuItem> getStandardMenuItems() {
		
		JMenuItem miThisScriptUi = new JMenuItem( getName() );
		miThisScriptUi.addActionListener( (e)->{
			DeskGet sif = new DeskGet();
			context.displayAndInitFrame(sif);
		} );
		
		Map<ScriptStandardMenu, JMenuItem> res = new HashMap<>();
		res.put(ScriptStandardMenu.SCRIPTS, miThisScriptUi);
		return res;
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

	@Override
	public List<JButton> getToolButtons() {
		return List.of();
	}

}
