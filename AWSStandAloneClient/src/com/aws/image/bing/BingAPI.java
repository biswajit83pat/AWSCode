package com.aws.image.bing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aws.image.mongodb.util.MongoDBImageURLsB;

public class BingAPI {
	private final static String SOURCE = "BING";
	private final static String CONTRIBUTOR = "NA";
	private final static String LICENSE = "-1";//-1 means unknown

	static int counter = 0;
	
	private CountDownLatch countDownLatchForImageURLs;
	
	public BingAPI(CountDownLatch countDownLatchForImageURLs) {
		this.countDownLatchForImageURLs = countDownLatchForImageURLs;
	}

	public String callBingAPIForEachKeyword(String query, String synsetCode, String safeSearch, int urlsPerKeyword)
			throws IOException, JSONException {

		System.out.println("\n\t\t KEYWORD::" + query);
		System.out.println("\t No. of urls required::" + urlsPerKeyword);
		int totalPages;


		//int total = 500;
		int perPage = 50;
		
		List<Document> documentsInBatch = new ArrayList<>();

		if(urlsPerKeyword%perPage != 0)
			totalPages = (urlsPerKeyword/perPage) + 1;
		else
			totalPages = urlsPerKeyword/perPage;
		System.out.println( "\n\n\t total pages ::" + totalPages);

		for (int i = 0; i < totalPages; i++) {
			
			documentsInBatch = new ArrayList<>();
			
			StringBuffer sb = new StringBuffer(256);
			sb.append("https://api.datamarket.azure.com/Bing/Search/v1/Image?Query=\'").append(URLEncoder.encode(query, "UTF-8"))
					.append("\'&Adult=\'").append(safeSearch).append("\'&ImageFilters=\'Size:Medium\'&$skip=")
					.append((int)(i * 50)).append("&$format=json");

			String bingUrl = sb.toString();

			System.out.println("BING URL FORMED --> " + bingUrl);

			String accountKey = "CKdn6hztAstA3lxIdUwP+gtEoRoL0klIzvoH3QDi2iQ";
			String accountKeyEnc = Base64.getEncoder().encodeToString((accountKey + ":" + accountKey).getBytes());
			URL url = new URL(bingUrl);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("Authorization", "Basic " + accountKeyEnc);

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = reader.readLine()) != null) {
				response.append(inputLine);
			}
			reader.close();

			String responseString = response.toString();

			// print result
			////System.out.println("After making it a valid JSON --> " + responseString);

			JSONObject json = null;
			try {
				json = new JSONObject(responseString);
			} catch (JSONException e) {
				System.out.print("Error occurred while converting to JSON..");
				e.printStackTrace();
			}

			String scrapedImageURL = "";
			JSONObject d = json.has("d") ? json.getJSONObject("d") : null;
			if (d != null) {

				JSONArray results = d.has("results") ? d.getJSONArray("results") : null;
				for (int itr = 0; itr < results.length(); itr++) {
					JSONObject tempObject = results.getJSONObject(itr);
					scrapedImageURL = tempObject.has("MediaUrl") ? tempObject.getString("MediaUrl") : null;

					documentsInBatch.add(getDocumentPerCall(scrapedImageURL, CONTRIBUTOR, LICENSE, safeSearch));
					
					counter++;

				}
			}

			insertData(documentsInBatch);
		}

		
		System.out.println("B I N G    C O U N T E R -> " + counter);
		//insertData(documentsInBatch);
		countDownLatchForImageURLs.countDown();
		return null;
	}
	
	public Document getDocumentPerCall(String scrapedImageURL, String contributor, String license, String safeSearch) {

		// Prepare batch records for batch insertion in "image_urls_b" table.
		MongoDBImageURLsB mongoDBImageUrlsB = new MongoDBImageURLsB();
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
