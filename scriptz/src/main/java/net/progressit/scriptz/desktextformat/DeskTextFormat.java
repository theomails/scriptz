package net.progressit.scriptz.desktextformat;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.google.common.eventbus.EventBus;

import net.miginfocom.swing.MigLayout;
import net.progressit.scriptz.core.framework.ScriptInternalFrame;

public class DeskTextFormat  extends  ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public DeskTextFormat() {
		super("Desk Text Format", true, true, true, true);
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final DeskTextFormatBO bo = new DeskTextFormatBO();

	private JPanel outer = new JPanel(new MigLayout("","[500::,grow,fill]","[250:250:300, grow, fill][][250:350:, grow, fill]"));
	private JPanel pnlInputs = new JPanel( new MigLayout("insets 0","[150::,grow,fill]","[grow, fill]") );
	//private JTabbedPane tpInputs = new JTabbedPane();
	private JTextArea taSdpYest = new JTextArea();
	private JTextArea taSdpNow = new JTextArea();
	private JTextArea taMsp = new JTextArea();
	private JTextArea taScp = new JTextArea();
	private JScrollPane spSdpYest = new JScrollPane(taSdpYest);
	private JScrollPane spSdpNow = new JScrollPane(taSdpNow);
	private JScrollPane spMsp = new JScrollPane(taMsp);
	private JScrollPane spScp = new JScrollPane(taScp);
	
	private JPanel pnlButtons = new JPanel(new MigLayout("insets 0","[grow][]","[]"));
	private JButton btnProcess = new JButton("Process");
	
	private JPanel pnlOutputs = new JPanel( new MigLayout("insets 0","[::150,grow,fill][grow,fill]","[grow, fill]") );
	private JTextArea taCovered = new JTextArea();
	private JScrollPane spCovered= new JScrollPane(taCovered);
	private JTextArea taResult = new JTextArea();
	private JScrollPane spResult = new JScrollPane(taResult);
	@Override
	public void init() {
		setBounds(50,50,300,300);
		
		add(outer, BorderLayout.CENTER);
		outer.add(pnlButtons, "spanx 2, wrap");
		outer.add(pnlInputs, "wrap");
		outer.add(pnlButtons, "wrap");
		outer.add(pnlOutputs, "");
		
		pnlButtons.add(new JLabel(""));
		pnlButtons.add(btnProcess);
		
		pnlInputs.add(spSdpNow);
		pnlInputs.add(spSdpYest);
		pnlInputs.add(spMsp);
		pnlInputs.add(spScp);
		
		pnlOutputs.add(spCovered);
		pnlOutputs.add(spResult);
		
		spSdpNow.setBorder(BorderFactory.createTitledBorder("SDP Now"));
		spSdpYest.setBorder(BorderFactory.createTitledBorder("SDP Yest"));
		spMsp.setBorder(BorderFactory.createTitledBorder("MSP"));
		spScp.setBorder(BorderFactory.createTitledBorder("SCP"));
		
		spCovered.setBorder(BorderFactory.createTitledBorder("Covered tickets"));
		spResult.setBorder(BorderFactory.createTitledBorder("PM Support addition"));
		
		//
		pack();

		addHandlers();
	}
	
	
	private void addHandlers() {
		bus.register(this);
		
		btnProcess.addActionListener( (e)->{
			String result = bo.processAll( taSdpNow.getText(), taSdpYest.getText(), taMsp.getText(), taScp.getText(), taCovered.getText() );
			taResult.setText(result);
		} );
	}
	
}