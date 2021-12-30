package net.progressit.scriptz;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import net.progressit.scriptz.core.framework.ScriptLocalStateMigrationService;
import net.progressit.scriptz.core.framework.ScriptLocalStateService;
import net.progressit.scriptz.core.framework.ScriptzService;

public class LiveModule extends AbstractModule{

	@Override
	protected void configure() {
		//Just for debug. Comment as needed.
		binder().requireExplicitBindings();
		binder().requireAtInjectOnConstructors();
		
		bind(ScriptzService.class).in(Singleton.class);
		bind(ScriptLocalStateService.class).in(Singleton.class);
		bind(ScriptLocalStateMigrationService.class).in(Singleton.class);
		bind(ScriptzUI.class);
		
	}

}
