package net.progressit.scriptz.projectstouch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.eventbus.EventBus;

import lombok.Data;

public class ProjectTouchRunnable implements Runnable{

	public enum ProjectIdentifyEventCode{ START, FOUND, END }
	@Data
	public static class PTProjectIdentifiedAsyncEvent{
		private final ProjectIdentifyEventCode code;
		private final String projectId;
	}
	
	public enum ProjectTouchEventCode{ START, TOUCH, END }
	@Data
	public static class PTProjectTouchAsyncEvent{
		private final ProjectTouchEventCode code;
		private final String projectId;
	}
	
	private final EventBus bus;
	public ProjectTouchRunnable(EventBus bus) {
		this.bus = bus;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		try {
			List<String> projectIds = new ArrayList<>();
			
			List<Map<String, Object>> requests = new ProjectsTouchBO().getRequests();
			bus.post( new PTProjectIdentifiedAsyncEvent( ProjectIdentifyEventCode.START, null ) );
			for(Map<String,Object> request: requests) {
				Map<String,Object> udfFields = (Map<String,Object>) request.get("udf_fields");
				String projectId = (String) udfFields.get("udf_char21");
				if(projectId!=null && projectId.length()>0) {
					projectIds.add(projectId);
					bus.post( new PTProjectIdentifiedAsyncEvent( ProjectIdentifyEventCode.FOUND, projectId ) );
				}
			}
			bus.post( new PTProjectIdentifiedAsyncEvent( ProjectIdentifyEventCode.END, null ) );
			
			bus.post( new PTProjectTouchAsyncEvent( ProjectTouchEventCode.START, null ) );
			for(String projectId:projectIds) {
				Thread.sleep(3000);
				bus.post( new PTProjectTouchAsyncEvent( ProjectTouchEventCode.TOUCH, projectId ) );
				new ProjectsTouchBO().touchProject(projectId);
			}
			bus.post( new PTProjectTouchAsyncEvent( ProjectTouchEventCode.END, null ) );
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		
	}

}
