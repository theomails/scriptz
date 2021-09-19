package net.progressit.scriptz.desktextformat;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;

import com.google.common.eventbus.EventBus;

import net.miginfocom.swing.MigLayout;
import net.progressit.scriptz.core.ScriptInternalFrame;

public class DeskSdfUrl  extends  ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public DeskSdfUrl() {
		super("Desk SDF URL", true, true, true, true);
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final DeskSdfUrlBO bo = new DeskSdfUrlBO();

	private JPanel outer = new JPanel(new MigLayout("","[500::,grow,fill]","[250:250:300, grow, fill][][250:350:, grow, fill][]"));
	private JTextArea taInputs = new JTextArea();
	private JScrollPane spInputs = new JScrollPane(taInputs);
	
	private JPanel pnlButtons = new JPanel(new MigLayout("insets 0","[grow][]","[]"));
	private JButton btnProcess = new JButton("Process");
	
	private JList<String> lstResult = new JList<>();
	private JScrollPane spResult = new JScrollPane(lstResult);
	
	private JPanel pnlButtons2 = new JPanel(new MigLayout("insets 0","[grow][]","[]"));
	private JButton btnOpen = new JButton("Open");
	
	@Override
	public void init() {
		setBounds(50,50,300,300);
		
		add(outer, BorderLayout.CENTER);
		outer.add(spInputs, "wrap");
		outer.add(pnlButtons, "wrap");
		outer.add(spResult, "wrap");
		outer.add(pnlButtons2, "");
		
		pnlButtons.add(new JLabel(""));
		pnlButtons.add(btnProcess);
		pnlButtons2.add(new JLabel(""));
		pnlButtons2.add(btnOpen);
		
		lstResult.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		//
		pack();

		addHandlers();
	}
	
	
	private void addHandlers() {
		bus.register(this);
		
		btnProcess.addActionListener( (e)->{
			List<String> result = bo.processAll( taInputs.getText() );
			lstResult.setListData( result.toArray( new String[] {} ) );
		} );
		
		btnOpen.addActionListener( (e)->{
			final int[] indices = lstResult.getSelectedIndices();
			new Thread( ()->{
				try {
					for(int index:indices) {
						String url = lstResult.getModel().getElementAt(index);
						Desktop.getDesktop().browse(new URI(url));
						Thread.sleep(2000);
					}
				} catch (IOException | URISyntaxException | InterruptedException e1) {
					e1.printStackTrace();
				}				
			} ).start();
		} );
		
	}
	
}