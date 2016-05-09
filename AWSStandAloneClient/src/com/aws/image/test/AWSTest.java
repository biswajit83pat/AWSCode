package com.aws.image.test;

import java.util.concurrent.CountDownLatch;

import com.aws.image.mongodb.MongoDBService;
import com.aws.image.mongodb.util.MongoDBConnector;
import com.aws.image.mongodb.util.MongoDBDatasetA;

public class AWSTest {

	public static void main(String[] args) throws InterruptedException {
		CountDownLatch countDownLatchForDataSet = new CountDownLatch(MongoDBService.BATCH_SIZE);
		//CountDownLatch countDownLatchForImageURLs = new CountDownLatch(MongoDBService.BATCH_SIZE * MongoDBDatasetA.IMAGES_LIMIT_PER_CATEGORY_FOR_FLICKR_AND_BING);
		
		System.out.println("TEST RUN");
		
		
		//MongoDBService mongoDBService = new MongoDBService(countDownLatchForDataSet, countDownLatchForImageURLs);
		MongoDBService mongoDBService = new MongoDBService(countDownLatchForDataSet);
		mongoDBService.importFromCSVToDatasetTable();
		
		//countDownLatchForImageURLs.await();
		countDownLatchForDataSet.await();
		MongoDBConnector.INSTANCE.closeAsync();
	}

}
