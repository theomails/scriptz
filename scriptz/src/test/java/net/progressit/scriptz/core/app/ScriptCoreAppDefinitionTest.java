package net.progressit.scriptz.core.app;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;

import net.progressit.scriptz.ScriptzUI.ScriptzCoreConfig;

public class ScriptCoreAppDefinitionTest{
	
	private ScriptCoreAppDefinition scriptCoreAppDefinition = new ScriptCoreAppDefinition();
	
	@Before
	public void setup() {
		
	}
	
	@Test
	public void shouldUpgradeConfigVersionToNext() throws IOException {
		String inputConfigVnullFileStr = getClass().getResource("/scriptz-core-vnull-sample.config.json").getFile();
		File inputConfigVnullFile = new File(inputConfigVnullFileStr);
		
		String expectedConfigV1FileStr = getClass().getResource("/scriptz-core-v1-sample.config.json").getFile();
		File expectedConfigV1File = new File(expectedConfigV1FileStr);
		
		try(FileReader frIn = new FileReader(inputConfigVnullFile);
				FileReader frExp = new FileReader(expectedConfigV1File);){
			ScriptzCoreConfig inputConfigVnull = new Gson().fromJson(frIn, ScriptzCoreConfig.class);
			ScriptzCoreConfig expectedConfigV1 = new Gson().fromJson(frExp, ScriptzCoreConfig.class);
			
			ScriptzCoreConfig outputConfigV1 = scriptCoreAppDefinition.upgradeConfigVersionToNext(inputConfigVnull);
			
			assertThat(outputConfigV1, is(expectedConfigV1));
			System.out.println(outputConfigV1);
			System.out.println(expectedConfigV1);
		}
	}
	
}