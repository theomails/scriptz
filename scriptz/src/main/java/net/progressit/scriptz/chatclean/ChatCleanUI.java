package net.progressit.scriptz.chatclean;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

import com.google.common.eventbus.Subscribe;

import net.progressit.progressive.PComponent;
import net.progressit.progressive.PComponent.PEventListener;
import net.progressit.progressive.PComponent.PPlacers;
import net.progressit.scriptz.chatclean.ChatCleanApp.CCACloseEvent;
import net.progressit.scriptz.core.framework.ScriptInternalFrame;

public class ChatCleanUI extends  ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public ChatCleanUI() {
		super("Chat Clean", true, true, true, true);
	}
	
	private Object placeholder = new Object();
	private PPlacers simplePlacers = new PPlacers( (component)->getContentPane().add(component, BorderLayout.CENTER), (component)->getContentPane().remove(component) );
	private ChatCleanApp app = new ChatCleanApp(simplePlacers);

	public void init() {
		PComponent.place(app, new PEventListener() {
			@Subscribe
			public void handle(CCACloseEvent event) {
				closeMe();
			}
		}, placeholder);
		
		setSize(800, 600);
		setDefaultCloseOperation(JInternalFrame.DISPOSE_ON_CLOSE);
		setTitle("Chat Clean");
	}
	
	private void closeMe() {
		try {
			setClosed(true);
		} catch (PropertyVetoException e1) {
			e1.printStackTrace();
		}
	}
}
