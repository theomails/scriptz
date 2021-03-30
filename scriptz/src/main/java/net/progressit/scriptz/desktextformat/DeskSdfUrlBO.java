package net.progressit.scriptz.desktextformat;

import java.util.ArrayList;
import java.util.List;

public class DeskSdfUrlBO {

	public List<String> processAll(String sdfs) {
		
		List<String> results = new ArrayList<>();
		String[] inputs = sdfs.split("\\n");
		for(String sdf:inputs) {
			if(sdf.trim().length()==0) continue;
			sdf=sdf.trim();
			String link = "https://pitstop.manageengine.com/support/manageengine/ShowHomePage.do#Cases/search/CurDep/"+ sdf //
							+"/1/SDP%20OP%20Issue%20ID%20or%20Feature%20ID";
			results.add(link);
		}
		return results;
	}
	
}
