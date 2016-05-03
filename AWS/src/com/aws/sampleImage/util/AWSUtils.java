package com.aws.sampleImage.util;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import au.com.bytecode.opencsv.CSVReader;

public class AWSUtils {

	public static List<String[]> read(String fileName) {
		List<String[]> rowsList = null;
		try {
			CSVReader reader = new CSVReader(new FileReader(fileName), ',');
			rowsList = reader.readAll();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rowsList;
	}

	public static AWSCredentials getAWSCredentials() throws Exception {
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file.", e);
		}
		return credentials;
	}
	
	public static Map<String, AttributeValue> newItem(String synset_code, String keywords) {
        Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("synset_code", new AttributeValue(synset_code));
        item.put("keywords", new AttributeValue(keywords));
        return item;
    }
}
