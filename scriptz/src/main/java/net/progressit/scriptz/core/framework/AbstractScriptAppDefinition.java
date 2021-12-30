package net.progressit.scriptz.core.framework;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import net.progressit.scriptz.framework.ScriptAppDefinition;
import net.progressit.scriptz.framework.ScriptAppDefinition.ScriptStandardMenu;

public abstract class AbstractScriptAppDefinition<T,V> implements ScriptAppDefinition<T,V>{

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
		return new ArrayList<>();
	}

	@Override
	public Class<T> getConfigClass() {
		return null;
	}

	@Override
	public T upgradeConfigVersionToNext(T currentData) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public T getConfigDefaults() {
		throw new RuntimeException("Not supported");
	}

	@Override
	public Class<V> getStateClass() {
		return null;
	}

	@Override
	public V upgradeStateVersionToNext(V currentData) {
		throw new RuntimeException("Not supported");
	}

	@Override
	public V getStateDefaults() {
		throw new RuntimeException("Not supported");
	}

}
