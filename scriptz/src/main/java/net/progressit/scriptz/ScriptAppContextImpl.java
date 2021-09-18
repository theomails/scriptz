package net.progressit.scriptz;

import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import lombok.RequiredArgsConstructor;
import net.progressit.scriptz.core.ScriptAppContext;
import net.progressit.scriptz.core.ScriptAppResourceDefinition;
import net.progressit.scriptz.core.ScriptInternalFrame;
import net.progressit.scriptz.core.ScriptLocalStateService;

@RequiredArgsConstructor
public class ScriptAppContextImpl implements ScriptAppContext {
	
	private final JDesktopPane dpMain;
	private final ScriptLocalStateService localStateService;
	private final ScriptAppResourceDefinition appDefn;
	@Override
	public void storeState(Object config) {
		localStateService.storeState(appDefn.getName(), config);
	}
	@Override
	public void storeConfig(Object config) {
		localStateService.storeConfig(appDefn.getName(), config);
	}
	@Override
	public void setStatusMessage(String message) {
		//TODO
		System.out.println(message);
	}
	
	@Override
	public <T> T loadState(T stateDefaults, Class<T> classOfState) {
		return localStateService.loadState(appDefn.getName(), stateDefaults, classOfState);
	}
	
	@Override
	public void loadFrame(ScriptInternalFrame sif) {
		dpMain.add( sif );
		sif.init();
		showFrame(sif, true);
	}
	
	@Override
	public <T> T loadConfig(T stateDefaults, Class<T> classOfState) {
		return localStateService.loadConfig(appDefn.getName(), stateDefaults, classOfState);
	}
	
	@Override
	public JDesktopPane getContainer() {
		return dpMain;
	}
	
	private void showFrame(JInternalFrame jif, boolean maximize) {
		position(jif);
		if(maximize) {
			try {
				jif.setMaximum(true);
			} catch (PropertyVetoException e1) {
				e1.printStackTrace();
			}
		}
		jif.setVisible(true);		
	}
	
	private void position(JInternalFrame jif) {
		int cnt = (dpMain.getComponentCount()-1) % 10 + 1; //-1 because this component has been already added, plus one after the modulo
		jif.setLocation( cnt*50 , cnt*50 );
	}
}