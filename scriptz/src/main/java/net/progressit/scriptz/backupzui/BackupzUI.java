package net.progressit.scriptz.backupzui;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;

import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.google.common.eventbus.EventBus;

import net.miginfocom.swing.MigLayout;
import net.progressit.backupzui.logic.BackupService;
import net.progressit.backupzui.ui.RunBackupPanel;
import net.progressit.scriptz.core.ScriptInternalFrame;

public class BackupzUI  extends  ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	
	@Inject
	public BackupzUI(BackupService backupService) {
		super("Backupz UI", true, true, true, true);
		
		this.runPanel = new RunBackupPanel(backupService, bus);
	}
	
	private JPanel mainPanel = new JPanel(new MigLayout("insets 10","[][600::,grow 3,fill]","[500::,grow,fill][]"));
	private JPanel closePanel = new JPanel(new MigLayout("insets 0","[grow,fill][]","[]"));
	
	private RunBackupPanel runPanel;
	private JButton btnClose = new JButton("Close");
	public void init() {
		bus.register(this);
		
		add(mainPanel, BorderLayout.CENTER);
		mainPanel.add(runPanel, "skip 1, wrap");
		mainPanel.add(closePanel, "spanx 2, grow");
		
		closePanel.add(btnClose, "skip 1");
		
		addHandlers();
		
		pack();
	}
	
	private void addHandlers() {
		btnClose.addActionListener( (e)->{ 
			try {
				setClosed(true);
			} catch (PropertyVetoException e1) {
				e1.printStackTrace();
			}
		});
	}
}