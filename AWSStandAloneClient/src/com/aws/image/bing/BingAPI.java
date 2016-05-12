package com.aws.image.bing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aws.image.mongodb.util.MongoDBImageURLsB;

public class BingAPI {
	private final static String SOURCE = "BING";
	private final static String CONTRIBUTOR = "NA";
	private final static String LICENSE = "4";//Use and share commercially

	private CountDownLatch countDownLatchForImageURLs;
	
	public BingAPI(CountDownLatch countDownLatchForImageURLs) {
		this.countDownLatchForImageURLs = countDownLatchForImageURLs;
	}

	/*public String callBingAPIForEachKeyword(String query, String synsetCode, String safeSearch, int urlsPerKeyword)
			throws IOException, JSONException {

		System.out.println("\n\t\t KEYWORD::" + query);
		System.out.println("\t No. of urls required::" + urlsPerKeyword);
		int totalPages;

		if(query == null || "".equals(query.trim())) {
			return null;
		}
		
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

					documentsInBatch.add(getDocumentPerCall(scrapedImageURL, CONTRIBUTOR, LICENSE, synsetCode, query));
					
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
	*/
	
	public void callBingAPIForEachKeyword(String query, String synsetCode, String safeSearch, int urlsPerKeyword) throws IOException
	{
		
		System.out.println("\t\tKEYWORD ::" + query);
		System.out.println("No of URLs required ::" + urlsPerKeyword);
		int itr = 0;
		
		List<Document> documentsInBatch = new ArrayList<>();
		int counter = 0;
		while(counter < urlsPerKeyword) {
			documentsInBatch = new ArrayList<>();
			//System.out.println("\t value of &first ==>" + counter);
			try {
				org.jsoup.nodes.Document doc = (org.jsoup.nodes.Document) Jsoup.connect("https://www.bing.com/images/async?q=" + query + "&first=" + counter + "&adlt=" + safeSearch + "&qft=+filterui:license-L2_L3_L4+filterui:imagesize-medium").get();
				Elements documents =  doc.getElementsByAttributeValueContaining("src2", "https");
				
				
				for (Element src : documents) {
					String scrapedImageURL = src.hasAttr("src2")? src.attr("src2") : null;
	
					itr++;
	
					documentsInBatch.add(getDocumentPerCall(scrapedImageURL, CONTRIBUTOR, LICENSE, synsetCode, query, itr));
					//System.out.println("URL::" + urll);
					counter++;
				}
				
				insertData(documentsInBatch);
				
			} catch (Exception ex) {
				System.out.println("Error while processing this url --> " + "https://www.bing.com/images/async?q=" + query + "&first=" + counter + "&adlt=" + safeSearch + "&qft=+filterui:license-L2_L3_L4+filterui:imagesize-medium");
			}
		}
		System.out.println("\t\tBING COUNT for keyword::" + query + " IS --->>" + counter);
		countDownLatchForImageURLs.countDown();
		
	}

	public Document getDocumentPerCall(String scrapedImageURL, String contributor, String license, String synsetCode, String query, int rank) {

		// Prepare batch records for batch insertion in "image_urls_b" table.
		Document datasetRecord = new Document();
		//datasetRecord.append("url", scrapedImageURL);
		
		Document primaryKeyId = new Document();
		primaryKeyId.append("url", scrapedImageURL).append("category", synsetCode);
		datasetRecord.append("_id", primaryKeyId);
		datasetRecord.append("contributor", contributor);
		datasetRecord.append("license", license);
		datasetRecord.append("source", SOURCE);
		datasetRecord.append("keyword", query);
		//datasetRecord.append("category", synsetCode);
		datasetRecord.append("pushToSamaHub", false);
		datasetRecord.append("taskId", null);
		datasetRecord.append("rank", rank);
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
