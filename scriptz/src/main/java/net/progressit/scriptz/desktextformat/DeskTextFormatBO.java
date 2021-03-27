package net.progressit.scriptz.desktextformat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeskTextFormatBO {

	public String processAll(String sdpNow, String sdpYest, String msp, String scp) {
		
		StringBuilder result = new StringBuilder(100_000);
		appendLines( sdpNow, "", result);
		appendLines( sdpYest, "Yest", result);
		appendLines( msp, "MSP", result);
		appendLines( scp, "SCP", result);
		
		return result.toString();
	}
	
	private Pattern pMain = Pattern.compile("(.*?#)(\\d\\d\\d\\d\\d\\d\\d)(\\D.*)");//
	private Pattern pDetails1 = Pattern.compile("(.*?) \\. (.*?) \\. (.*?) \\. (.*?)");//
	private Pattern pDetails2 = Pattern.compile("(.*?) \\. (.*?) \\. (.*?)");//
	private Pattern pDetails3 = Pattern.compile("(.*?) \\. (.*?)");//
	
	private void appendLines(String lines, String type, StringBuilder result) {
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
				result.append(id) //
				.append("\t").append(type)//
				.append("\t").append(desc)//
				.append("\t").append(time)//
				.append("\n");				
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
			result.append(id) //
			.append("\t").append(type)//
			.append("\t").append(desc)//
			.append("\t").append(time)//
			.append("\n");		
		}
	}

}
