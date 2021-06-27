package net.progressit.scriptz.deskget;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import lombok.Data;
import net.progressit.zauth.ZAuthV2;
import net.progressit.zauth.ZAuthV2.AccessType;
import net.progressit.zauth.ZAuthV2.GrantType;
import net.progressit.zauth.ZAuthV2.Prompt;
import net.progressit.zauth.ZAuthV2.ResponseType;
import net.progressit.zauth.ZAuthV2.TokenResponseField;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeskGetAccessBO {
	@Data
	public static class DeskGetConfig{
		@SerializedName("client_id")
		private String clientId;
		@SerializedName("client_secret")
		private String clientSecret;
		@SerializedName("refresh_token")
		private String refreshToken;
	}
	
	private OkHttpClient client = new OkHttpClient();
	public OkHttpClient getClient() {
		return client;
	}
	
	public static void main(String[] args) throws IOException {
		DeskGetAccessBO bo = new DeskGetAccessBO();
		
		//Code
		bo.getAuthCode();
		
		//Refresh token
		bo.getRefreshToken("1000.31db0eeff6b5ed077d19e0132ff03f61.70e9c19e62c173cd86ae7c7753424066");
	}
	
	private void getAuthCode() {
		DeskGetConfig config = getConfig();
		ZAuthV2.AuthUrl authUrl = ZAuthV2.AuthUrl.builder() //
				.clientId(config.clientId)
				.responseType(ResponseType.code)
				.scopesCsv("Desk.tickets.READ,Desk.basic.READ,Desk.search.READ")
				.redirectUri("https://www.google.com")
				.prompt(Prompt.Consent)
				.accessType(AccessType.offline)
				.build();

		System.out.println("Please use the below URL in a browser to get the Auth code");
		System.out.println(authUrl.makeAuthUrl());
	}
	private void getRefreshToken(String authCode) throws IOException {
		DeskGetConfig config = getConfig();
		ZAuthV2.TokenUrl tokenUrl = ZAuthV2.TokenUrl.builder() //
				.clientId(config.clientId)
				.clientSecret(config.clientSecret)
				.grantType(GrantType.authorization_code)
				.code(authCode)
				.redirectUri("https://www.google.com")
				.build();

		System.out.println(tokenUrl.makeTokenUrl());
		Request request = new Request.Builder().post(new FormBody.Builder().build()).url( tokenUrl.makeTokenUrl() ).build();
		Response response = getClient().newCall(request).execute();
		String sResponse = response.body().string();
		System.out.println( sResponse );
		
		ZAuthV2.TokenResponse tokenResponse = new ZAuthV2.TokenResponse( sResponse );
		String refreshToken = tokenResponse.get(TokenResponseField.refresh_token);
		System.out.println("Refresh Token: " + refreshToken);
	}
	
	private String accessToken = null;
	public String getAccessToken() throws IOException {
		if(accessToken!=null) return accessToken;
		
		DeskGetConfig config = getConfig();
		ZAuthV2.TokenUrl tokenUrl = ZAuthV2.TokenUrl.builder() //
				.clientId(config.clientId)
				.clientSecret(config.clientSecret)
				.grantType(GrantType.refresh_token)
				.refreshToken(config.refreshToken)
				.build();
		Request request = new Request.Builder().post(new FormBody.Builder().build()).url( tokenUrl.makeTokenUrl() ).build();
		Response response = getClient().newCall(request).execute();
		String sResponse = response.body().string();
		
		ZAuthV2.TokenResponse tokenResponse = new ZAuthV2.TokenResponse( sResponse );
		accessToken = tokenResponse.get(TokenResponseField.access_token);
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
