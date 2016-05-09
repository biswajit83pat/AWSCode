package com.aws.image.mongodb.util;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

import org.bson.Document;
import org.json.JSONException;

import com.aws.image.bing.BingAPI;
import com.aws.image.flickr.FlickrAPI;
import com.aws.image.mongodb.MongoDBService;
import com.mongodb.async.client.MongoCollection;

public class MongoDBDatasetA {
	
	public static final int IMAGES_LIMIT_PER_CATEGORY_FOR_FLICKR_AND_BING = 1000;
	
	private MongoCollection<Document> collection = MongoDBConnector.INSTANCE.getAsyncDatabase().getCollection(MongoDBConfigLoader.getMongoDBDataSetTableName());
	
	private CountDownLatch countDownLatch;
	private CountDownLatch countDownLatchForImageURLs = new CountDownLatch(2);
	
	public MongoDBDatasetA(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}
	
	public void insert(Document document) {
		collection.insertOne(document, (result, t)-> {
			OptionalConsumer.of(Optional.ofNullable(t)).
			ifPresent(
					s -> {
						System.out.println("Insertion failed, cause " + s);
						//Do not trigger anything.
						//Do other rollback activities or simple log error and/or retry.
					}
					
					).
			ifNotPresent(
					
					() -> {
						System.out.println("Insertion Successful!");

						//Create trigger to insert into image URL b
						String synset_code = (String) document.get("synset_code");
						String keywords = (String) document.get("keyword");
						try {
							callingAPIs(synset_code, keywords);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						try {
							countDownLatchForImageURLs.await();
						} catch (Exception e) {
							e.printStackTrace();
						}
						
						countDownLatch.countDown();
					}
					
					
					);
		});
		
	}
	
	public void callingAPIs(String synset_code, String keywords) throws IOException, JSONException
	{
		
		BingAPI bingObj = new BingAPI(countDownLatchForImageURLs);
		FlickrAPI flickrObj = new FlickrAPI(countDownLatchForImageURLs);
		
		//String keywords = "people,person,image";
		//String keywords = "people";

		// Only if Insert then start processing

		String safeSearchDefault = "Strict";// TODO need to get value from
											// safeSearch variable
		
		String[] keywordFragments = keywords.split(",");

		double ceilingvalue = Math.ceil(IMAGES_LIMIT_PER_CATEGORY_FOR_FLICKR_AND_BING/keywordFragments.length);
		int urlsPerKeyword = (int)ceilingvalue ;


		for (int i = 0; i < keywordFragments.length; i++) {

			bingObj.callBingAPIForEachKeyword(keywordFragments[i], synset_code, safeSearchDefault, urlsPerKeyword);
			flickrObj.callFlickrAPIForEachKeyword(keywordFragments[i], synset_code, safeSearchDefault, urlsPerKeyword);
		}

		
	}
	
}