package com.aws.sampleImage;

import java.util.List;
import java.util.Map;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.aws.sampleImage.util.AWSUtils;

public class CreateDataset {

	public static void main(String[] args) throws Exception {
		String fileName = System.getProperty("user.dir") + "\\src\\resources\\aws-keywords.csv";
		List<String[]> rowsList = AWSUtils.read(fileName);
		String[] row = null;
		String groupId = "";
		String keywords = "";

		AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(AWSUtils.getAWSCredentials());
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		dynamoDB.setRegion(usWest2);
		String tableName = "dataset_a";

		DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
		TableDescription tableDescription = dynamoDB.describeTable(describeTableRequest).getTable();
		System.out.println("Table Description: " + tableDescription);

		Map<String, AttributeValue> item = null;
		PutItemRequest putItemRequest = null;
		PutItemResult putItemResult = null;

		for (int i = 0; i < rowsList.size(); i++) {
			//if (i == 10) break;
			if (i == 1) break;
			
			row = rowsList.get(i);
			groupId = row[0].trim();
			keywords = row[1].trim();
			System.out.println(groupId + "\t" + keywords);

			// Add an item
			item = AWSUtils.newItem(groupId, keywords);
			putItemRequest = new PutItemRequest(tableName, item);
			putItemResult = dynamoDB.putItem(putItemRequest);
			System.out.println("Result: " + putItemResult);

		}
	}

}
