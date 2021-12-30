package net.progressit.scriptz.core.framework;

import org.apache.commons.jxpath.JXPathContext;

public class VersionedDataWrapper {
	private final JXPathContext sharedContext = JXPathContext.newContext(null);
	private Object data;
	private JXPathContext xpathContext;
	public VersionedDataWrapper(Object data) {
		super();
		this.sharedContext.setLenient(true);
		this.data = data;
		this.xpathContext = null;
	}
	
	public Object get() {
		return data;
	}
	public void set(Object data) {
		this.data = data;
		this.xpathContext = null;
	}
	public Integer getVersion() {
		ensureContext();
		return (Integer) xpathContext.getValue("version");
		
	}
	private void ensureContext() {
		if(xpathContext==null) {
			xpathContext = JXPathContext.newContext(sharedContext, data);
		}
	}
}
