package net.progressit.scriptz;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class Main {
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		//WebLookAndFeel.install();
		
		Injector injector = Guice.createInjector(new LiveModule());
		ScriptzUI ui = injector.getInstance( ScriptzUI.class );
		ui.init();
		
		ui.pack();
		ui.setLocationRelativeTo(null);
		ui.setVisible(true);
		ui.setExtendedState(JFrame.MAXIMIZED_BOTH);
		ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ui.setTitle("Scriptz");
	}
	
}
