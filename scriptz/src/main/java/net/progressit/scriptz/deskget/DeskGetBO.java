package net.progressit.scriptz.deskget;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeskGetBO {
	@Data
	public static class DeskGetConfig{
		@SerializedName("client_id")
		private String clientId;
		@SerializedName("client_secret")
		private String clientSecret;
		@SerializedName("refresh_token")
		private String refreshToken;
	}
	public static class DeskTicket{
		
	}
	public List<DeskTicket> getPortalTickets(){
		try {
			getAccessToken();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private OkHttpClient client = new OkHttpClient();
	private OkHttpClient getClient() {
		return client;
	}
	
	private String accessToken = null;
	@SuppressWarnings("unchecked")
	private String getAccessToken() throws IOException {
		if(accessToken!=null) return accessToken;
		
		DeskGetConfig config = getConfig();
		//refresh_token= &client_id= &client_secret= &grant_type=refresh_token"
		HttpUrl url = HttpUrl.parse("https://accounts.zoho.com/oauth/v2/token");
		HttpUrl fullUrl = url.newBuilder()//
								.addEncodedQueryParameter("refresh_token", config.refreshToken)//
								.addEncodedQueryParameter("client_id", config.clientId)//
								.addEncodedQueryParameter("client_secret", config.clientSecret)//
								.addEncodedQueryParameter("grant_type", "refresh_token").build();
		Request request = new Request.Builder().post(new FormBody.Builder().build()).url( fullUrl.url() ).build();
		Response response = getClient().newCall(request).execute();
		String sResponse = response.body().string();
		Map<String, Object> mapResponse = new Gson().fromJson(sResponse, Map.class);
		accessToken = (String) mapResponse.get("access_token");
		System.out.println(accessToken);
		return accessToken;
	}
	
	private DeskGetConfig config = null;
	private DeskGetConfig getConfig() {
		if(config!=null) return config;
		
		Gson g = new Gson();
		File configFile = new File(System.getProperty("user.home"), "scriptz-desk-get.config.json");
		try( FileReader fr = new FileReader(configFile) ){
			config = g.fromJson(fr, DeskGetConfig.class);
			return config;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
