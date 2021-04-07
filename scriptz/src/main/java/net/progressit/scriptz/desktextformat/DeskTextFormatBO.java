package net.progressit.scriptz.desktextformat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;

public class DeskTextFormatBO {

	@Data
	public static class SdpSupportTicket{
		private final String id;
		private final String type;
		private final String desc;
		private final String time;
		@Override
		public String toString() {
			return new StringBuilder(500).append(id) //
					.append("\t").append(type)//
					.append("\t").append(desc)//
					.append("\t").append(time)//
					.toString();
		}
	}
	
	public String processAll(String sdpNow, String sdpYest, String msp, String scp, String covered) {
		List<SdpSupportTicket> allResults = new ArrayList<>();
		allResults.addAll( appendLines( sdpNow, "") );
		allResults.addAll( appendLines( sdpYest, "Yest") );
		allResults.addAll( appendLines( msp, "MSP") );
		allResults.addAll( appendLines( scp, "SCP") );
		
		remove(allResults, covered);

		StringBuilder sb = new StringBuilder(allResults.size() * 500);
		for(SdpSupportTicket ticket:allResults) {
			sb.append(ticket).append("\n");
		}
		return sb.toString();
	}
	
	private void remove(List<SdpSupportTicket> allResults, String covered) {
		String[] coveredLines = covered.split("\\n");
		for(String coveredLine:coveredLines) {
			String sdf = coveredLine.trim();
			if(sdf.length()==0) continue;
			Iterator<SdpSupportTicket> resIter = allResults.iterator();
			while(resIter.hasNext()) {
				SdpSupportTicket ticket = resIter.next();
				if(ticket.id.equals(sdf)) {
					resIter.remove();
				}
			}
		}
	}

	private Pattern pMain = Pattern.compile("(.*?#)(\\d\\d\\d\\d\\d\\d\\d)(\\D.*)");//
	private Pattern pDetails1 = Pattern.compile("(.*?) \\. (.*?) \\. (.*?) \\. (.*?)");//
	private Pattern pDetails2 = Pattern.compile("(.*?) \\. (.*?) \\. (.*?)");//
	private Pattern pDetails3 = Pattern.compile("(.*?) \\. (.*?)");//
	
	private List<SdpSupportTicket> appendLines(String lines, String type) {
		List<SdpSupportTicket> results = new ArrayList<>();
		lines = lines.replaceAll("\\n", "~").replaceAll("\\r", " ");
		System.out.println(lines);
		Matcher mMain = pMain.matcher(lines);
		String id = "";
		String rest = "";
		String details = "";
		while( mMain.matches() ) {
			id = mMain.group(2);
			rest = mMain.group(3);
			
			mMain = pMain.matcher(rest);
			String desc = "";
			String time = "";
			if(mMain.matches()) {
				details = mMain.group(1);
				System.out.println("id " + id);
				System.out.println("details " + details);
				
				
				Matcher mDetails = pDetails1.matcher(details);
				if(mDetails.matches()) {
					desc = mDetails.group(1);
					time = mDetails.group(3);
				}else {
					mDetails = pDetails2.matcher(details);
					if(mDetails.matches()) {
						desc = mDetails.group(1);
						time = mDetails.group(2);
					}else {
						mDetails = pDetails3.matcher(details);
						if(mDetails.matches()) {
							desc = mDetails.group(1);
							time = "";
						}
					}
				}
				desc = desc.replaceAll("~.*", "");
				results.add( new SdpSupportTicket(id, type, desc, time) );
			}
		}
		
		if(rest.trim().length()!=0) {
			Matcher mDetails = pDetails1.matcher(rest);
			String desc = "";
			String time = "";
			if(mDetails.matches()) {
				desc = mDetails.group(1);
				time = mDetails.group(3);
			}else {
				mDetails = pDetails2.matcher(rest);
				if(mDetails.matches()) {
					desc = mDetails.group(1);
					time = mDetails.group(2);
				}else {
					mDetails = pDetails3.matcher(rest);
					if(mDetails.matches()) {
						desc = mDetails.group(1);
						time = "";
					}
				}
			}
			desc = desc.replaceAll("~.*", "");
			results.add( new SdpSupportTicket(id, type, desc, time) );
		}
		
		return results;
	}

}
