package WiFi;

import java.io.IOException;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
	
	private Set<String> tagList = new TreeSet<String>(); 
	
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
	
	public String formatStrObjectTag(Object obj){
		return "{\"material_number\":\"" + obj.toString() +"\"}";
	}
	
	public String formatStrObjectsList(Object[] list){
		String strList = "{\"objects\":[";
		for(int i=0; i<list.length; i++){
			strList = strList +  formatStrObjectTag(list[i].toString());
			if(i+1 < list.length){
				strList += ",";
			} else {
				strList += "";
			}
		}
		strList += "]}";
		
		return strList;
	}
	
	public boolean setCorrectLuggageList(Set<String> malas_corretas, Set<String> malas_erradas) throws IOException {
		// Gera uma lista única e converte para o formato adequado 
		Object[] list;
		String strList;
		malas_corretas.addAll(malas_erradas);
		list = malas_corretas.toArray();
		strList = formatStrObjectsList(list);
		System.out.println(strList);
		
		MediaType mediaType = MediaType.parse("application/json");
		RequestBody body = RequestBody.create(mediaType, strList);
		Request request = new Request.Builder()
		  .url(URLBase + "api/v1/luggage/")
		  .post(body)
		  .addHeader("content-type", "application/json")
		  .addHeader("authorization", strApiKey)
		  .build();
		
		Response response = client.newCall(request).execute();
		return (response.code() == CODE_Created);
	}
	
	public Set<String> getLuggageList() throws IOException, ParseException{
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
		//con.getLuggageList();
		
		//POST: lista de malas verificadas
		Set<String> s1 = new TreeSet();
		Set<String> s2 = new TreeSet();
		s1.add("E200 6296 9619 0229 0370 EC2B");
		//System.out.println(con.setCurrentFlight("TP443"));	//TP443
		System.out.println(con.setCorrectLuggageList(s1,s2));
		//System.out.println("{\n  \"objects\": [\n    {\n      // True Positive: Should be there and is\n      \"material_number\": \"E200 0000 9619 0229 0370 EC2B\"\n    },\n    {\n       // False Negative: Should not be there and is\n      \"material_number\": \"E200 1230 9619 0229 0370 EC2B\"\n    }\n  ]\n}");
		
	}

}
