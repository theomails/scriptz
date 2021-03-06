package net.progressit.scriptz;

import com.google.inject.AbstractModule;

public class LiveModule extends AbstractModule{

	@Override
	protected void configure() {
		bind(ScriptzUI.class);
		
	}

}
