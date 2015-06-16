package WiFi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.Number;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class Conexao_http {
	private final int CODE_Created = 201;
	private final int CODE_Unauthorized  = 401;
	private final int CODE_Ok  = 200;
	private final String strApiKey = "ApiKey api:730cd04e8f4c05e81459ed8efd6bb326deed7efb";
	private final OkHttpClient client = new OkHttpClient();
	
	private List<String> tagList = new ArrayList<String>(); 
	
	private String URLBase;
	
	public Conexao_http(String URL) {
		URLBase = URL;
	}
	
	public boolean setCurrentFlight(String strNumVoo) throws IOException{
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "{\n\t\"current_flight\":\"" + strNumVoo + "\"\n}");
		Request request = new Request.Builder()
		  .url(URLBase + "api/v1/employee/")
		  .post(body)
		  .addHeader("content-type", "application/json")
		  .addHeader("authorization", strApiKey)
		  .build();

		Response response = client.newCall(request).execute();
		return (response.code() == CODE_Created);
	}
	
	public boolean flushCurrentFlight() throws IOException{
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, "{\n\t\"current_flight\":\"\"\n}");
		Request request = new Request.Builder()
		  .url(URLBase + "api/v1/employee/")
		  .post(body)
		  .addHeader("content-type", "application/json")
		  .addHeader("authorization", strApiKey)
		  .build();

		Response response = client.newCall(request).execute();
		return (response.code() == CODE_Created);
	}
	
	public boolean getPassengerList() throws IOException{
		Request request = new Request.Builder()
		  .url(URLBase + "api/v1/passenger/")
		  .get()
		  .addHeader("content-type", "application/json")
		  .addHeader("authorization", strApiKey)
		  .build();

		Response response = client.newCall(request).execute();
		return (response.code() == CODE_Ok);
	}
	
	public List<String> getLuggageList() throws IOException, ParseException{
		Request request = new Request.Builder()
		  .url("http://bagtrekkin.herokuapp.com/api/v1/luggage/")
		  .get()
		  .addHeader("content-type", "application/json")
		  .addHeader("authorization", strApiKey)
		  .build();
		
		Response response = client.newCall(request).execute();
		
		// Converte do json para o list
		JSONParser parser = new JSONParser();
		JSONObject obj = (JSONObject) parser.parse(response.body().string());
		// Pega o numero de malas
		JSONObject objMeta = (JSONObject) obj.get("meta");
		// Pega as tags e coloca na lista 
		JSONArray objList = (JSONArray) obj.get("objects");
		for(int i=0; i<objList.size(); i++){
			JSONObject elmo = (JSONObject) objList.get(i);
			tagList.add(elmo.get("material_number").toString());
		}
		
		return tagList;
	}
	
	public static final void main(String args[]) throws IOException, ParseException{
		//
		Conexao_http con = new Conexao_http("http://bagtrekkin.herokuapp.com/");
		
		//POST: atualiza o número do voo atual
		//System.out.println("Response: " + con.flushCurrentFlight("TP443"));
	
		//GET: obtem a lista de passageiros do voo
		con.getLuggageList();
		
	}
}
