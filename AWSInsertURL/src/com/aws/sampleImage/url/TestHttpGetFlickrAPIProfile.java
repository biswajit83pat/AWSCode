package com.aws.sampleImage.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class TestHttpGetFlickrAPIProfile {

	
	public static void main(String[] args) throws IOException, JSONException {
		//String query = "church";
		//String query = "sukriti";
		String query = "people";
		System.out.println(callFlickrAPIForEachKeyword(query));
		
		String abc = "abcdef)";
		
		System.out.println(abc.substring(0,abc.lastIndexOf(")")));
		
	}
	
	
	private static String callFlickrAPIForEachKeyword(String query) throws IOException, JSONException {
    	String apiKey = "ad4f88ecfd53b17f93178e19703fe00d";
    	String apiSecret = "96cab0e9f89468d6";
    	
    	long httpCallTime = 0L;
    	long jsonParseTime = 0L;
    	
    	int totalPages = 4;
    	
		int total = 500;
		int perPage = 500;
		
		int counter = 0;
		
		int currentCount = 0; 
    	
    	for(int i = 1; i <= totalPages && currentCount <= total; i++, currentCount = currentCount + perPage) {
    	
    		long startHttpCall = System.currentTimeMillis();
    		
    		String url = "https://api.flickr.com/services/rest/?method=flickr.photos.search&text=" + query + "&extras=url_c,url_m,url_n,license,owner_name&per_page=500&page=" + i + "&format=json&api_key=" + apiKey + "&api_secret=" + apiSecret + "&license=4,5,6,7,8";
    		
    		System.out.println("URL FORMED --> " + url);
    		
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

			System.out.println("GET Response Status:: " + httpResponse.getStatusLine().getStatusCode());

			long endHttpCall = System.currentTimeMillis();
			
			httpCallTime = (long)(httpCallTime + (long)(endHttpCall - startHttpCall));
			
			long startJsonParse = System.currentTimeMillis();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			reader.close();

			String responseString = response.toString();
			
			responseString = responseString.replace("jsonFlickrApi(", "");
			
			int length = responseString.length();
			
			responseString = responseString.substring(0, length-1);
			
			
			// print result
			System.out.println("After making it a valid JSON --> " + responseString);
			httpClient.close();
    	
			JSONObject json = null;
			try {
				json = new JSONObject(responseString);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//System.out.println("Converted JSON String " + json);
			
			
			JSONObject photosObj = json.has("photos") ? json.getJSONObject("photos"): null;
			total = photosObj.has("total") ? (Integer.parseInt(photosObj.getString("total"))):0;
			perPage = photosObj.has("perpage") ? (Integer.parseInt(photosObj.getString("perpage"))):0;
			
			System.out.println(" perPage --> " + perPage + " total --> " + total);
			
			JSONArray photoArr = photosObj.getJSONArray("photo");
			System.out.println("Length of Array --> " + photoArr.length());
			String scrapedImageURL = "";
			
			for (int itr = 0; itr < photoArr.length(); itr++) {
				JSONObject tempObject = photoArr.getJSONObject(itr);
				scrapedImageURL = tempObject.has("url_c") ? tempObject.getString("url_c") : tempObject.has("url_m") ? tempObject.getString("url_m") :
					tempObject.has("url_n") ? tempObject.getString("url_n") : "";
							
				//System.out.println("Scraped Image URL --> " + scrapedImageURL);
				
				counter++;
			}
			
			long endJsonParse = System.currentTimeMillis();
			
			jsonParseTime = (long)(jsonParseTime + (long)(endJsonParse - startJsonParse));
			
    	}
    	
    	System.out.println("C O U N T E R -> " + counter);
    	
    	System.out.println("HTTP CALL TIME --> " + httpCallTime + " JSON PARSE TIME --> " + jsonParseTime);
    	
    	return null;
    }
	
	private static void insertScrapedImageURLsInDynamoDB(String synsetCode, String scrapedImageUrl) {
		String tableName = "image_urls_b";

		DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
		//TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
		//System.out.println("Table Description: " + tableDescription);
		
		

	}
}
