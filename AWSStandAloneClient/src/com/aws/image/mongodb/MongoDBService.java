package com.aws.image.mongodb;

import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.bson.Document;

import com.aws.image.mongodb.util.MongoDBDatasetA;
import com.aws.image.util.AWSImageUtils;

public class MongoDBService {
	public static final String fileName = System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources" + File.separator + "imagenet-keywords.csv";

	public static final int BATCH_SIZE = 5;

	private CountDownLatch countDownLatch;

	public MongoDBService(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}

	public void importFromCSVToDatasetTable() throws InterruptedException{
		List<String[]> rowsList = AWSImageUtils.read(fileName);
		String[] row = null;
		String groupId = "";
		String keywords = "";


		for (int i = 0; i < rowsList.size(); i++) {
			//if (i == 10) break;

			TimeUnit.SECONDS.sleep(30);

			if (i == BATCH_SIZE) break;

			/*if (i == 5) {
				try {
					TimeUnit.MINUTES.sleep(58);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}*/

			row = rowsList.get(i);
			groupId = row[0].trim();
			keywords = row[1].trim();
			System.out.println(groupId + "\t" + keywords);

			// Add an item to Mongodb "dataset_a" table
			boolean addItemResult = true;
			MongoDBDatasetA mongoDBDatasetA = new MongoDBDatasetA(countDownLatch);
			Document datasetRecord = new Document();
			datasetRecord.append("synset_code", groupId);
			datasetRecord.append("keyword", keywords);
			mongoDBDatasetA.insert(datasetRecord);
			System.out.println("Result: " + addItemResult);

		}

	}





}