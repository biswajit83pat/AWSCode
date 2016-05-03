package com.aws.sampleImage.url;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class LambdaFunctionImageURLCrawlerHandler implements RequestHandler<DynamodbEvent, String> {

	private final static String INSERT_EVENT = "INSERT";
	
	volatile static AmazonDynamoDBClient dynamoDB;

	private static void init() {
		AWSCredentials awsCredentials = new AWSCredentials() {
			
			@Override
			public String getAWSSecretKey() {
				return "M2HnD68DXBmeNYcuxO0PDVuRPjJONDLiRXTFwygN";//M2HnD68DXBmeNYcuxO0PDVuRPjJONDLiRXTFwygN
			}
			
			@Override
			public String getAWSAccessKeyId() {
				return "AKIAJ5LSYQCPXHSNYVYQ";//AKIAJ5LSYQCPXHSNYVYQ
			}
		};
		
        //dynamoDB = new AmazonDynamoDBClient(new EnvironmentVariableCredentialsProvider());
		dynamoDB = new AmazonDynamoDBClient(awsCredentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
        System.out.println("^^^^^  DynamoDB created successfully --> " + dynamoDB);
    }
	
    @Override
    public String handleRequest(DynamodbEvent input, Context context) {
        context.getLogger().log("Input: " + input);
        
        try {
			init();
		} catch (Exception e) {
			System.err.println("Could Not load credentials from environment");
			return "Unsuccessful because AWS Credentials from environment could not be obtained!";
		}
        
        List<DynamodbEvent.DynamodbStreamRecord> recordsList = input.getRecords();
        
        for(DynamodbStreamRecord record: recordsList) {
        	
        	
        	handleEachRecord(record.getEventName(), record.getDynamodb());
        	
        	//System.out.println("****EVENT ID ---> " + record.getEventID());
        	//System.out.println("&&&&& EVENT NAME *****  " + record.getEventName());
        	//System.out.println("^^^^^  END ^^^^^^");
        	
        }
        
        return new StringBuffer("Successfully processed ").append(recordsList.size()).append(" records").toString();
        
    }
    
    private void handleEachRecord(String eventName, StreamRecord streamRecord) {
    	try {
    		
    		//Only if Insert then start processing
        	if(INSERT_EVENT.equalsIgnoreCase(eventName)) {
        		
        	
				Map<String, AttributeValue> newImageMap = streamRecord.getNewImage();
				
				AttributeValue keywordsAttrVal = newImageMap.get("keywords");
				AttributeValue synsetCodeAttrVal = newImageMap.get("synset_code");
				
				String keywords = keywordsAttrVal.getS();
				String synsetCode = synsetCodeAttrVal.getS();
				
				/*System.out.println("Keywords : " + keywords);
				System.out.println("SynsetCode : " + synsetCode);*/
			
				String[] keywordFragments = keywords.split(",");
				
				for (int i = 0; i < keywordFragments.length; i++) {
					
					//System.out.println("Calling Flickr API for getting Image URL :: " + keywordFragments[i]);
					callFlickrAPIForEachKeyword(keywordFragments[i], synsetCode);
					
				}
        		
        	}
        	
    	} catch (AmazonServiceException ase) {
    		System.out.println(ase.getMessage());
    		//ace.printStackTrace();
    		
    	} catch (AmazonClientException ace) {
    		System.out.println(ace.getMessage());
    		//ace.printStackTrace();
    		
    	} catch (Exception ex) {
    		System.out.println(ex.getMessage());
    		//ex.printStackTrace();
    	}
    }
    
    private static String callFlickrAPIForEachKeyword(String query, String synsetCode) throws IOException, JSONException {
    	String apiKey = "ad4f88ecfd53b17f93178e19703fe00d";
    	String apiSecret = "96cab0e9f89468d6";
    	
    	int totalPages = 4;
    	
		int total = 500;
		int perPage = 500;
		
		int counter = 0;//For monitoring purposes only
		
		int currentCount = 0; 
    	
    	for(int i = 1; i <= totalPages && currentCount <= total; i++, currentCount = currentCount + perPage) {
    	
    		StringBuffer sb = new StringBuffer(512);
    		sb.append("https://api.flickr.com/services/rest/?method=flickr.photos.search&text=")
    			.append(query).append("&extras=url_c,url_m,url_n,license,owner_name&per_page=500&page=")
    				.append(i).append("&format=json&api_key=").append(apiKey).append("&api_secret=").append(apiSecret).append("&license=4,5,6,7,8");

    		String url = sb.toString();
    		
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
							tempObject.has("url_n") ? tempObject.getString("url_n") : "";
							
				//System.out.println("Scraped Image URL, need to insert this to Amazon DYnamo DB --> " + scrapedImageURL);
				
				counter++;
							
				insertScrapedImageURLsInDynamoDB(scrapedImageURL, synsetCode, query);
			}
			
			
			
    	}
    	
    	System.out.println("C O U N T E R -> " + counter);
    	return null;
    }
    
    private static void insertScrapedImageURLsInDynamoDB(String scrapedImageUrl, String synsetCode, String keyword) {
		String tableName = "image_urls_b";

		DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
		TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
		//System.out.println("For table name --> " + tableName + ", Table Description for : " + tableDescription);
		
		
		//Check if record with corresponding scraped image url exists
		//If it exists, don't do anything, else insert.
		//TODO - Do a DB check.
		
        // Add an item
        Map<String, AttributeValue> item = newItem(scrapedImageUrl, synsetCode, keyword);
        PutItemRequest putItemRequest = new PutItemRequest(tableName, item);
        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
        //System.out.println("Result: " + putItemResult);
        //System.out.println("$$$$$$ scrapedImageUrl -> " + scrapedImageUrl + " synsetCode --> " + synsetCode + " keyword --> " + keyword + " has been insrted succesfully!");
        
	}
    
    private static Map<String, AttributeValue> newItem(String scrapedImageUrl, String synsetCode, String keyword) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("url", new AttributeValue(scrapedImageUrl));
        item.put("synset_code", new AttributeValue(synsetCode));
        item.put("word", new AttributeValue(keyword));
        return item;
    }

}
