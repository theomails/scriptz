package net.progressit.scriptz;

import com.google.inject.AbstractModule;

public class LiveModule extends AbstractModule{

	@Override
	protected void configure() {
		//Just for debug. Comment as needed.
		binder().requireExplicitBindings();
		binder().requireAtInjectOnConstructors();
		
		bind(ScriptzUI.class);
		
	}

}
