package net.progressit.scriptz.projectstouch;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProjectsTouchBO {
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<Map<String,Object>> getRequests() throws IOException {
		//	http://sdp-issues:8080/OPODFeatures/rest/requests?filterBy={}&showDiff=false
		
		HttpUrl url = HttpUrl.parse("http://sdp-issues:8080/OPODFeatures/rest/requests");
		
		Request request = new Request.Builder().url( url.url() ).build();
		Response response = getClient().newCall(request).execute();
		String sResponse = response.body().string();
		System.out.println( sResponse );
		
		Gson g = new Gson();
		Map requestsWrap = g.fromJson(sResponse, Map.class);
		List requests = (List) requestsWrap.get("requests");
		return requests;
	}
	
	private OkHttpClient client = new OkHttpClient();
	private OkHttpClient getClient() {
		return client;
	}
	public void touchProject(String projectId) throws IOException {
		HttpUrl url = HttpUrl.parse("http://sdp-issues:8080/OPODFeatures/rest/actions/projects/" + projectId + "/touch");
		
		Request request = new Request.Builder().post(new FormBody.Builder().build()).url( url.url() ).build();
		Response response = getClient().newCall(request).execute();
		String sResponse = response.body().string();
		System.out.println( sResponse );
	}
}
