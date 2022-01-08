package net.progressit.scriptz.jsonformat;

import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.progressit.scriptz.core.framework.AbstractScriptAppDefinition;
import net.progressit.scriptz.core.framework.ScriptAppContext;

public class JsonFormatDefinition extends AbstractScriptAppDefinition<Object, Object> {

	private ScriptAppContext context = null;
	
	@Override
	public void setAppContext(ScriptAppContext context) {
		this.context = context;
	}

	@Override
	public String getName() {
		return "Json Format";
	}


	@Override
	public Map<ScriptStandardMenu, JMenuItem> getStandardMenuItems() {
		return null;
	}

	@Override
	public JMenu getMenu() {
		return null;
	}

	@Override
	public List<JButton> getToolButtons() {
		
		JButton btnThisScriptUi = new JButton( getName() );
		btnThisScriptUi.addActionListener( (e)->{
			JsonFormat sif = new JsonFormat();
			context.displayAndInitFrame(sif);
		} );
		
		return List.of(btnThisScriptUi);
	}
}
