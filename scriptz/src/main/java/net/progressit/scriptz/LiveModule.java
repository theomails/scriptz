package net.progressit.scriptz;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import net.progressit.scriptz.core.framework.ScriptLocalStateService;

public class LiveModule extends AbstractModule{

	@Override
	protected void configure() {
		//Just for debug. Comment as needed.
		binder().requireExplicitBindings();
		binder().requireAtInjectOnConstructors();
		
		bind(ScriptLocalStateService.class).in(Singleton.class);
		bind(ScriptzUI.class);
		
	}

}
