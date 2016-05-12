package com.aws.image.bootstrap;

import com.aws.image.mongodb.util.MongoDBDownloaderUtilitySync;

public class PicBeeDownloaderBootstrapSync {

	public static void main(String[] args) throws InterruptedException {
		String synsetCode = "n07942152";
		MongoDBDownloaderUtilitySync mongoDBDownloaderUtility = new MongoDBDownloaderUtilitySync();
		mongoDBDownloaderUtility.downloadAllImagesForSynsetCode(synsetCode);
		System.out.println("MongoDB Downloader Utility end...");
	}
}
