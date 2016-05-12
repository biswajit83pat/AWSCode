package com.aws.image.test;

import java.util.concurrent.CountDownLatch;

import com.aws.image.mongodb.util.MongoDBDownloaderUtilityAsync;

public class PicBeeDownloaderBootstrapAsync {

	public static void main(String[] args) throws InterruptedException {
		int NO_OF_IMAGES_REQUIRED_TO_BE_DOWNLOADED = 500;
		CountDownLatch countDownLatch = new CountDownLatch(NO_OF_IMAGES_REQUIRED_TO_BE_DOWNLOADED);
		
		String synsetCode = "n07942152";
		MongoDBDownloaderUtilityAsync mongoDBDownloaderUtility = new MongoDBDownloaderUtilityAsync(countDownLatch);
		mongoDBDownloaderUtility.downloadAllImagesForSynsetCode(synsetCode);
		System.out.println("MongoDB Downloader Utility end...");
		countDownLatch.await();
	}
}
