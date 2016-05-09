package com.aws.image.flickr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aws.image.mongodb.util.MongoDBImageURLsB;


public class FlickrAPI {

	private final static String SOURCE = "FLICKR";
	
	static int counter = 0;
	
	private CountDownLatch countDownLatchForImageURLs;
	
	public FlickrAPI(CountDownLatch countDownLatchForImageURLs) {
		this.countDownLatchForImageURLs = countDownLatchForImageURLs;
	}
	
    public String callFlickrAPIForEachKeyword(String query, String synsetCode, String safeSearch, int urlsPerKeyword) throws IOException, JSONException {
    	String apiKey = "ad4f88ecfd53b17f93178e19703fe00d";
    	String apiSecret = "96cab0e9f89468d6";
    	
		int total = 500;
		int perPage = 500;
		
		System.out.println("\n\t\t KEYWORD::" + query);
    	System.out.println("\t No. of urls required::" + urlsPerKeyword);
    	int totalPages;
    	if(urlsPerKeyword%perPage != 0)
    		totalPages = (urlsPerKeyword/perPage) + 1;
    	else
    		totalPages = urlsPerKeyword/perPage;
    	
		System.out.println( "\n\n\t total pages ::" + totalPages);
		
		int currentCount = 0;
		
		int eachPage;
		List<Document> documentsInBatch = new ArrayList<>();
    	
    	for(int i = 1; i <= totalPages && currentCount <= total; i++, currentCount = currentCount + perPage) {
    		documentsInBatch = new ArrayList<>();
    		
    		eachPage = urlsPerKeyword < perPage ? urlsPerKeyword : perPage;
    		
    		StringBuffer sb = new StringBuffer(512);
    		sb.append("https://api.flickr.com/services/rest/?method=flickr.photos.search&text=")
    			.append(URLEncoder.encode(query, "UTF-8")).append("&safe_search=").append(safeSearch).append("&extras=url_c,url_m,url_n,license,owner_name&per_page=")
    			.append(eachPage).append("&page=").append(i).append("&format=json&api_key=").append(apiKey).append("&api_secret=").append(apiSecret).append("&license=4,5,6,7,8");

    		String url = sb.toString();
    		
    		System.out.println("URL FORMED --> " + url);
    		
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(url);
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

			//System.out.println("GET Response Status:: " + httpResponse.getStatusLine().getStatusCode());

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
			
			//System.out.println(" perPage --> " + perPage + " total --> " + total);
			
			JSONArray photoArr = photosObj.getJSONArray("photo");
			//System.out.println("Length of Array --> " + photoArr.length());
			String scrapedImageURL = "";
			
			
			for (int itr = 0; itr < photoArr.length(); itr++) {
				JSONObject tempObject = photoArr.getJSONObject(itr);
				scrapedImageURL = tempObject.has("url_c") ? tempObject.getString("url_c") : tempObject.has("url_m") ? tempObject.getString("url_m") :
							tempObject.has("url_n") ? tempObject.getString("url_n") : null;
							
				if(scrapedImageURL == null) {
					continue;
				}
				String contributor = tempObject.getString("ownername");
				String license = tempObject.getString("license");
							
				//System.out.println("Scraped Image URL, need to insert this to Mongo DB --> " + scrapedImageURL);
				
				documentsInBatch.add(getDocumentPerCall(scrapedImageURL, contributor, license, safeSearch));
				
				
				counter++;
							
			}
			
			insertData(documentsInBatch);
    	}
    	
    	System.out.println("F L I C K R      C O U N T E R -> " + counter);
    	//insertData(documentsInBatch);
    	
    	countDownLatchForImageURLs.countDown();
    	return null;
    }

	public Document getDocumentPerCall(String scrapedImageURL, String contributor, String license, String safeSearch) {

		// Prepare batch records for batch insertion in "image_urls_b" table.
		Document datasetRecord = new Document();
		datasetRecord.append("url", scrapedImageURL);
		datasetRecord.append("contributor", contributor);
		datasetRecord.append("license", license);
		datasetRecord.append("safeSearch", safeSearch);
		datasetRecord.append("source", SOURCE);
		return datasetRecord;

	}
	
	public void insertData(List<Document> documents) {

		// Add an item to Mongodb "image_urls_b" table
		boolean addItemResult = true;
		MongoDBImageURLsB mongoDBImageUrlsB = new MongoDBImageURLsB();
		if(documents != null && documents.size() > 0) {
			mongoDBImageUrlsB.insertMany(documents);
			System.out.println("Result: " + addItemResult);
		} else {
			System.out.println("Document list was empty!!");
		}

	}

}
