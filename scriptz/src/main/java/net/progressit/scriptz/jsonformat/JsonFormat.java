package net.progressit.scriptz.jsonformat;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.google.common.eventbus.EventBus;

import net.miginfocom.swing.MigLayout;
import net.progressit.scriptz.framework.ScriptInternalFrame;

public class JsonFormat  extends  ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public JsonFormat() {
		super("JSON Ordered Format", true, true, true, true);
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final JsonOrderedFormatBO bo = new JsonOrderedFormatBO();

	private JPanel outer = new JPanel(new MigLayout("","[400::,grow,fill][400::,grow,fill]","[grow, fill][]"));
	//private JTabbedPane tpInputs = new JTabbedPane();
	private RSyntaxTextArea taInput = new RSyntaxTextArea();
	private RTextScrollPane spInput = new RTextScrollPane(taInput);
	
	private JPanel pnlButtons = new JPanel(new MigLayout("insets 0","[grow][]10[]5[]5[]10[]","[]"));
	private JButton btnPaste = new JButton("Paste Input");
	private JCheckBox cbPrettyPrint = new JCheckBox("Pretty print", true);
	private JCheckBox cbSerializeNulls = new JCheckBox("Serialize Nulls", true);
	private JButton btnFormat = new JButton("Format");
	private JButton btnCopy = new JButton("Copy Result");
	
	private RSyntaxTextArea taResult = new RSyntaxTextArea();
	private RTextScrollPane spResult = new RTextScrollPane(taResult);
	@Override
	public void init() {
		setBounds(50,50,300,300);
		
		add(outer, BorderLayout.CENTER);
		outer.add(spInput, "");
		outer.add(spResult, "wrap");
		outer.add(pnlButtons, "spanx 2");
		
		pnlButtons.add(new JLabel(""));
		pnlButtons.add(btnPaste);
		pnlButtons.add(cbPrettyPrint);
		pnlButtons.add(cbSerializeNulls);
		pnlButtons.add(btnFormat);
		pnlButtons.add(btnCopy);
		
		spInput.setBorder(BorderFactory.createTitledBorder("Input JSON"));
		spResult.setBorder(BorderFactory.createTitledBorder("Ordered Formatted JSON"));
		
		taInput.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS);
		taInput.setCodeFoldingEnabled(true);
		taResult.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS);
		taResult.setCodeFoldingEnabled(true);

		
		//
		pack();

		addHandlers();
	}
	
	
	private void addHandlers() {
		bus.register(this);
		
		btnPaste.addActionListener( (e)->{
			String text = getClipboardText();
			taInput.setText(text);
		} );
		btnFormat.addActionListener( (e)->{
			String text = taInput.getText();
			String result = bo.orderAndFormatJson(text, cbPrettyPrint.isSelected(), cbSerializeNulls.isSelected());
			taResult.setText(result);
		} );
		btnCopy.addActionListener( (e)->{
			String text = taResult.getText();
			setClipboardText(text);
		} );
	}
	
	private String getClipboardText(){
	    Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
	    Transferable t = c.getContents(this);
	    if (t == null)
	        return "";
	    try {
	        return (String) t.getTransferData(DataFlavor.stringFlavor);
	    } catch (Exception e){
	        throw new RuntimeException(e);
	    }//try
	}
	private void setClipboardText(String text){
		StringSelection stringSelection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
}