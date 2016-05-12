package com.aws.image.bootstrap;

import java.util.concurrent.CountDownLatch;

import com.aws.image.mongodb.MongoDBService;
import com.aws.image.mongodb.util.MongoDBConnector;

public class PicBeeBootStrapDataImporterFromCSV {

	public static void main(String[] args) throws InterruptedException {
		CountDownLatch countDownLatchForDataSet = new CountDownLatch(MongoDBService.BATCH_SIZE);
		
		System.out.println("TEST RUN");
		
		
		MongoDBService mongoDBService = new MongoDBService(countDownLatchForDataSet);
		mongoDBService.importFromCSVToDatasetTable();
		
		countDownLatchForDataSet.await();
		MongoDBConnector.INSTANCE.closeAsync();
	}

}
