package net.progressit.scriptz.scripts;

import java.awt.BorderLayout;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.google.common.eventbus.EventBus;

import lombok.Data;
import net.miginfocom.swing.MigLayout;
import net.progressit.scriptz.deskget.DeskGetBO;

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
		public String toString() {
			return nodeDisplayName;
		}
	}
	
	//Interal bus for this script.
	private final EventBus bus = new EventBus();
	private final DeskGetBO bo = new DeskGetBO();

	private JPanel outer = new JPanel(new MigLayout("","[200:300:300, grow, fill][600:800:, grow, fill]","[][grow,fill][200::200, grow, fill]"));
	
	private JPanel pnlButtons = new JPanel(new MigLayout("insets 0","[grow][]","[]"));
	private DefaultMutableTreeNode tnNavRoot = new DefaultMutableTreeNode( new PortalNodeObject(PortalNodeId.ROOT, "Portals") );
	private JTree treeNav = new JTree(tnNavRoot);
	private JScrollPane spNav = new JScrollPane(treeNav);
	private JPanel pnlTickets = new JPanel(new MigLayout("insets 0","[grow,fill]","[]"));
	private JScrollPane spTickets = new JScrollPane(pnlTickets);
	
	private JButton btnRefresh = new JButton("Refresh");
	private JTextArea taLog = new JTextArea();
	private JScrollPane spLog = new JScrollPane(taLog);
	@Override
	public void init() {
		setBounds(50,50,300,300);
		
		add(outer, BorderLayout.CENTER);
		outer.add(pnlButtons, "spanx 2, wrap");
		outer.add(spNav, "");
		outer.add(spTickets, "wrap");
		outer.add(spLog, "spanx 2");
		
		pnlButtons.add(new JLabel(""));
		pnlButtons.add(btnRefresh);
		
		tnNavRoot.add( new DefaultMutableTreeNode( new PortalNodeObject(PortalNodeId.ALL_PORTALS, "All Portals") ) );
		treeNav.expandPath( new TreePath(tnNavRoot.getPath()) );
		setBetterLeafIcon();
		treeNav.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		
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
	
	private void addHandlers() {
		bus.register(this);
		treeNav.addTreeSelectionListener( (e)->{
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
			PortalNodeObject portalNode = (PortalNodeObject) node.getUserObject();
			if(portalNode.getNodeId()==PortalNodeId.ALL_PORTALS) {
				bo.getPortalTickets();
			}
		} );
	}
	
	private void setBetterLeafIcon() {
        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) treeNav.getCellRenderer();
        Icon leafIcon = UIManager.getIcon("FileView.fileIcon");
        renderer.setLeafIcon(leafIcon);		
	}
}