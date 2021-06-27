package net.progressit.scriptz.scripts;

import java.awt.BorderLayout;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.google.common.eventbus.EventBus;

import lombok.Data;
import net.miginfocom.swing.MigLayout;
import net.progressit.scriptz.deskget.DeskGetBO;
import net.progressit.scriptz.deskget.DeskGetBO.DeskPerson;
import net.progressit.scriptz.deskget.DeskGetBO.DeskTicket;

public class DeskGet  extends JInternalFrame implements ScriptInternalFrame{
	private static final long serialVersionUID = 1L;

	public DeskGet() {
		super("Desk Get", true, true, true, true);
	}
	
	public enum PortalNodeId{
		ROOT, ALL_PORTALS
	}
	@Data
	public static class PortalNodeObject{
		private final PortalNodeId nodeId;
		private final String nodeDisplayName;
		@Override
		public String toString() {
			return nodeDisplayName;
		}
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final DeskGetBO bo = new DeskGetBO();

	private JPanel outer = new JPanel(new MigLayout("","[200:200:200, grow, fill][600:800:, grow, fill]","[][grow,fill][200::200, grow, fill]"));
	
	private JPanel pnlButtons = new JPanel(new MigLayout("insets 0","[grow][]","[]"));
	private JTextArea txtTicketNumbers = new JTextArea();
	private JScrollPane spTicketNumbers = new JScrollPane(txtTicketNumbers);
	private JTextArea txtTicketDetails = new JTextArea();
	private JScrollPane spTicketDetails = new JScrollPane(txtTicketDetails);
	
	private JButton btnFetch = new JButton("Fetch");
	private JTextArea taLog = new JTextArea();
	private JScrollPane spLog = new JScrollPane(taLog);
	@Override
	public void init() {
		setBounds(50,50,300,300);
		
		add(outer, BorderLayout.CENTER);
		outer.add(pnlButtons, "spanx 2, wrap");
		outer.add(spTicketNumbers, "");
		outer.add(spTicketDetails, "wrap");
		outer.add(spLog, "spanx 2");
		
		pnlButtons.add(new JLabel(""));
		pnlButtons.add(btnFetch);
		
		taLog.setLineWrap(true);
		txtTicketDetails.setLineWrap(true);
		
		//
		pack();

		addHandlers();
	}
	
	private void log(String msg) {
		taLog.append(msg);
		taLog.getCaret().setDot(Integer.MAX_VALUE);
	}
	private void logLn(String msg) {
		log(msg + "\n");
	}
	private void result(String msg) {
		txtTicketDetails.append(msg);
		txtTicketDetails.getCaret().setDot(Integer.MAX_VALUE);
	}
	private void resultLn(String msg) {
		result(msg + "\n");
	}
	
	private void logLnAsync(String msg) {
		SwingUtilities.invokeLater( ()->{ logLn(msg); } );
	}
	private void resultLnAsync(String msg) {
		SwingUtilities.invokeLater( ()->{ resultLn(msg); } );
	}
	
	private void addHandlers() {
		bus.register(this);
		btnFetch.addActionListener( (e)->{
			processTicketsAsync();
		} );
	}
	
	private void processTicketsAsync() {
		new Thread( ()->{
			String ticketNumbers = txtTicketNumbers.getText();
			String[] arrTicketNos = ticketNumbers.split("\\n");
			addHeader();
			for(String ticketNo:arrTicketNos) {
				processTicket(ticketNo);
			}
			addFooter();
		} ).start();
	}
	private void addHeader() {
		resultLnAsync("<!doctype html>\n"
				+ "<html>\n"
				+ "<head>\n"
				+ "</head>\n"
				+ "<body>\n"
				+ "<table border='1'>");
	}
	private void addFooter() {
		resultLnAsync("\n"
				+ "</table>\n"
				+ "\n"
				+ "</body>\n"
				+ "</html>");
	}
	
	private void processTicket(String ticketNo) {
		DeskTicket ticket;
		try {
			ticket = bo.getPortalTicket(ticketNo);
			if(ticket!=null) {
				StringBuilder sb = new StringBuilder(500);
				sb.append("<tr><td>");
				
				sb.append(ticket.getTicketNumber());
				sb.append("\t").append(ticket.getSubject());
				sb.append("    ");
				
				sb.append("Client: ").append(asString(ticket.getContact()));
				sb.append("    ");
				
				sb.append("Assignee: ").append(asString(ticket.getAssignee()));
				sb.append("\n");
				
				sb.append("</td></tr>").append("<tr><td>");
				
				sb.append("History: ");
				sb.append("\n");	
				for(String historyRow:ticket.getHistorySummary()) {
					sb.append(historyRow);
					sb.append("\n");							
				}
				
				sb.append("</td></tr>").append("<tr><td>&nbsp;").append("</td></tr>");
				resultLnAsync(sb.toString());
			}else {
				resultLnAsync("No ticket details for: " + ticketNo);
			}
			
		} catch (IOException e1) {
			System.err.println(e1.toString());
			e1.printStackTrace();
			logLnAsync("Error while getting ticket details for: " + ticketNo + ": " + e1.toString());
		}
	}
	
	private String asString(DeskPerson person) {
		StringBuilder sb = new StringBuilder(100);
		if(person!=null) {
			sb.append(nvl(person.getFirstName())).append(" ").append(nvl(person.getLastName())).append(" (").append(nvl(person.getEmailId(),person.getEmail())).append(")");
		}
		return sb.toString();
	}
	private String nvl(Object o1) {
		return o1==null?"":o1.toString();
	}
	private String nvl(Object o1, Object o2) {
		return o1==null?(o2==null?"":o2.toString()):o1.toString();
	}
}