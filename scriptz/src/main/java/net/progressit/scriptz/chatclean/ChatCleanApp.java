package net.progressit.scriptz.chatclean;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.eventbus.Subscribe;

import lombok.Builder;
import lombok.Data;
import net.miginfocom.swing.MigLayout;
import net.progressit.progressive.PComponent;
import net.progressit.progressive.PSimpleContainerPlacers;
import net.progressit.progressive.PSimpleTextArea;
import net.progressit.progressive.PSimpleTextArea.PSTAValueEvent;
import net.progressit.scriptz.chatclean.ChatCleanApp.ChatCleanAppData;

public class ChatCleanApp extends PComponent<ChatCleanAppData, Object>{
	
	@Data
	@Builder(toBuilder = true)
	public static class ChatCleanAppData{
		String chatCopiedText;
		String chatCleanedText;
	}
	
	public static class CCACloseEvent{}
	
	//Swing Context 
	
	//My swing
	private JPanel pnlMain = new JPanel(new BorderLayout()); //export
	private JPanel pnlResults = new JPanel(new MigLayout("insets 5","[300::,grow,fill]5[300::,grow,fill]","[grow, fill]"));
	
	private JPanel pnlButtons = new JPanel(new MigLayout("insets 5","[grow][][]15[]","[fill]"));
	private JButton btnPaste = new JButton("Paste");
	private JButton btnCopy = new JButton("Copy");
	private JButton btnClose = new JButton("Close");
	
	//Progressive support
	private PPlacers resultsSimplePlacementHandler = new PSimpleContainerPlacers(pnlResults);
	//Progressive
	private PSimpleTextArea copiedText = new PSimpleTextArea(resultsSimplePlacementHandler);
	private PSimpleTextArea cleanedText = new PSimpleTextArea(resultsSimplePlacementHandler);
	
	//STATE
	private boolean settingTextInProgress = false;
	private ChatCleanAppData defaultData = ChatCleanAppData.builder()
			.chatCopiedText("")
			.chatCleanedText("")
			.build();
	
	
	public ChatCleanApp(PPlacers placers) {
		super(placers);
	}

	@Override
	protected PDataPeekers<ChatCleanAppData> getDataPeekers() {
		return new PDataPeekers<ChatCleanAppData>( (data)->Set.of(), (data)->new HashSet<>(Arrays.asList(data.chatCopiedText, data.chatCleanedText)) );
	}

	@Override
	protected PRenderers<ChatCleanAppData> getRenderers() {
		return new PRenderers<ChatCleanAppData>( ()-> pnlMain, (data)->{}, (data)->{
			PChildrenPlan plans = new PChildrenPlan();
			
			PChildPlan plan = PChildPlan.builder().component(copiedText).props(data.chatCopiedText).listener( Optional.of( new PEventListener() {
				@Subscribe
				public void handle(PSTAValueEvent e) {
					if(settingTextInProgress) return;
					
					String copiedText = e.getValue();
					settingTextInProgress = true;
					String cleanedText = clean(copiedText);
					setData(getData().toBuilder().chatCopiedText(copiedText).chatCleanedText(cleanedText).build());
					settingTextInProgress = false;
					
				}

			} )).build();
			plans.addChildPlan(plan);

			
			plan = PChildPlan.builder().component(cleanedText).props(data.chatCleanedText).listener(Optional.empty()).build();
			plans.addChildPlan(plan);
			
			return plans;
		} );
	}
	

	private String clean(String copiedText) {
		String multilineOption = "(?m)";
		String res = copiedText.replaceAll(multilineOption + "(^\\s*$)|(^\\[.+?\\].*)", "").replaceAll(multilineOption+"^\\n", "");
		return res;
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
	
	@Override
	protected PLifecycleHandler getLifecycleHandler() {
		return new PSimpleLifecycleHandler() {
			@Override
			public void prePlacement() {
				//Cant put this in Render self?
				pnlMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				pnlMain.add(pnlResults, BorderLayout.CENTER);
				pnlMain.add(pnlButtons, BorderLayout.SOUTH);
				
				pnlButtons.add(new JLabel());
				pnlButtons.add(btnPaste);
				pnlButtons.add(btnCopy);
				pnlButtons.add(btnClose);
				
				btnClose.addActionListener( (e)->{ 
					post(new CCACloseEvent());
				});
				
				btnPaste.addActionListener( (e)->{
					String text = getClipboardText();
					setData(getData().toBuilder().chatCopiedText(text).build());
				} );
				btnCopy.addActionListener( (e)->{
					String text = getData().getChatCleanedText();
					setClipboardText(text);
				} );
			}
			

			@Override
			public void postProps() {
				ChatCleanAppData data = getData();
				data = data==null?defaultData:data;
				//Merge props into data, if any overlap.
				setData( data.toBuilder().build() );
			}
		};
	}

}