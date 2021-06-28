package net.progressit.scriptz.deskget;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;

import lombok.Data;
import okhttp3.Request;
import okhttp3.Response;

public class DeskGetBO {
	private static final String BASE_URL = "https://desk.zoho.com/api/v1";
	private static final SimpleDateFormat SDF_PARSE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
	private static final SimpleDateFormat SDF_FORMAT = new SimpleDateFormat("dd MMMM h:mm a");
	
	private EventBus bus;
	private DeskGetAccessBO accessBo = new DeskGetAccessBO();
	private String accessToken = null;
	public DeskGetBO(EventBus bus) {
		this.bus = bus;
	}
	
	@Data
	public static class DeskGetLogEventAsync{
		private final String msg;
	}
	
	@Data
	public static class DeskTicketList{
		private List<DeskTicket> data;
	}
	
	@Data
	public static class DeskTicket{
		private String id;
		private String ticketNumber;
		private String statusType;
		private String subject;
		private Map<String, Object> customFields; //SDP OP Issue ID or Feature ID
		private String dueDate;
		private String departmentId;
		private DeskPerson contact;
		private DeskPerson assignee;
		private List<String> historySummary;
	}
	
	@Data
	public static class DeskPerson{
		private String firstName;
		private String lastName;
		private String emailId;
		private String email;
	}
	
	@Data
	public static class DeskHistoryList{
		private List<DeskHistory> data;
	}
	
	@Data
	public static class DeskHistory{
		private String eventName;
		private String eventTime;
		private Map<String, Object> actor;
		private List<Map<String, Object>> eventInfo;
	}
	
	//"id":"24000242077147","name":"ServiceDesk Plus",
	//"id":"24000242746023","name":"ServiceDesk Plus - MSP",
	//"id":"24000242748275","name":"SupportCenter Plus",

	public DeskTicket getPortalTicket(String ticketNumber) throws IOException{
		if(accessToken==null) accessToken = accessBo.getAccessToken();
		
		bus.post(new DeskGetLogEventAsync("\nSearching ticket details: " + ticketNumber + " ..."));
		String url = BASE_URL + "/tickets/search?limit=1&ticketNumber="+ticketNumber;
		Request request = new Request.Builder().get().url( url ).addHeader("Authorization", "Zoho-oauthtoken "+accessToken).build();
		Response response = accessBo.getClient().newCall(request).execute();
		String sResponse = response.body().string();
		
		DeskTicketList list = new Gson().fromJson(sResponse, DeskTicketList.class);
		DeskTicket ticket = (list==null)?null:( list.getData().size()==0?null:list.getData().get(0) );
		//System.out.println( sResponse );
		//System.out.println( ticket );
		
		bus.post(new DeskGetLogEventAsync("\nLoading history: " + ticketNumber + " ..."));
		loadHistory(ticket);
		
		bus.post(new DeskGetLogEventAsync("\nDone: " + ticketNumber));
		return ticket;
	}
	
	private void loadHistory(DeskTicket ticket) throws IOException{
		if(accessToken==null) accessToken = accessBo.getAccessToken();
		
		String url = BASE_URL + "/tickets/"+ticket.id+"/History?limit=20";
		Request request = new Request.Builder().get().url( url ).addHeader("Authorization", "Zoho-oauthtoken "+accessToken).build();
		Response response = accessBo.getClient().newCall(request).execute();
		String sResponse = response.body().string();
		
		DeskHistoryList list = new Gson().fromJson(sResponse, DeskHistoryList.class);
		List<String> historySummary = historySummarize(list, ticket.getTicketNumber());
		ticket.setHistorySummary(historySummary);
		
		//DeskTicketList list = new Gson().fromJson(sResponse, DeskTicketList.class);
		//DeskTicket ticket = (list==null)?null:( list.getData().size()==0?null:list.getData().get(0) );
		System.out.println( sResponse );
		//System.out.println( ticket );
	}
	
	@SuppressWarnings("unchecked")
	private List<String> historySummarize(DeskHistoryList list, String ticketNumber){
		List<String> res = new ArrayList<>();
		if(list.getData()==null || list.getData().size()==0) return res;
		
		int histRow = 1;
		bus.post(new DeskGetLogEventAsync("\n"));
		for(DeskHistory hist:list.getData()){
			bus.post(new DeskGetLogEventAsync(" " + histRow + " : " + hist.getEventName()));
			System.out.println("#" + ticketNumber + " : " + histRow++);
			System.out.println(hist);
			String event = hist.getEventName().toLowerCase();
			if(event.contains("comment")){
				for(Map<String, Object> eventItem:hist.getEventInfo()){
					String propertyName = (String)eventItem.get("propertyName");
					if(propertyName.equals("Content")){
						String updatedContent = null;
						if(eventItem.get("propertyValue") instanceof String){
							updatedContent = (String) eventItem.get("propertyValue");
						}else{
							Map<String, Object> propertyValue = (Map<String, Object>)eventItem.get("propertyValue");
							updatedContent = (String) propertyValue.get("updatedValue");
						}
						String output = eventTitle(hist, "Comment") + updatedContent;
						addRes(res, output);
					}
				}
			}else if(event.contains("ticketupdated")){
				List<Map<String, Object>> eventInfo = hist.getEventInfo();
				for(Map<String, Object> eventItem:eventInfo){
					String propertyName = (String)eventItem.get("propertyName");
					if(propertyName.equals("Team")){
						Map<String, Object> propertyValue = (Map<String, Object>)eventItem.get("propertyValue");
						System.out.println(propertyValue);
						if(propertyValue==null) continue;
						Map<String, Object> updatedContent = (Map<String, Object>) propertyValue.get("updatedValue");
						if(updatedContent==null) continue;
						String output = eventTitle(hist, "Team") + updatedContent.get("name");
						addRes(res, output);
					}else if(propertyName.equals("Case Owner")){
						Map<String, Object> propertyValue = (Map<String, Object>)eventItem.get("propertyValue");
						if(propertyValue==null) continue;
						Map<String, Object> updatedContent = (Map<String, Object>) propertyValue.get("updatedValue");
						if(updatedContent==null) continue;
						String output = eventTitle(hist, "Case Owner") + updatedContent.get("name");
						addRes(res, output);
					}
				}
			}
		}
		
		return res;
	}
	
	private void addRes(List<String> res, String output) {
		bus.post(new DeskGetLogEventAsync(" ADDED"));
		res.add(output);
	}
	
	private String eventTitle(DeskHistory hist, String eventNiceName){
		return eventNiceName + " [By "+ safeAuthorName(hist) +", On "+ safeDateStr(hist.eventTime) +"]: ";
	}
	private String safeAuthorName(DeskHistory hist){
		Map<String, Object> author = hist.getActor();
		String name = author==null?"": ( (String)author.get("name") );
		name = name==null?"":name;
		return name;
	}
	private String safeDateStr(String sDate){
		if(sDate==null || sDate.isEmpty()) return "";
		try {
			return SDF_FORMAT.format(SDF_PARSE.parse(sDate));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}
}
