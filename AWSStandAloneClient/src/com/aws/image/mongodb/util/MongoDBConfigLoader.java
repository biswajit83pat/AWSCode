package com.aws.image.mongodb.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class MongoDBConfigLoader {

	private static Properties mongoDBProperties;
	
	static{
		mongoDBProperties = new Properties();
		String propFileName = "config/mongodb.properties";

		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream(propFileName);
		} catch (FileNotFoundException e1) {
			System.out.println("Could not find " + propFileName);
			e1.printStackTrace();
			System.exit(1);
		}

		if (inputStream != null) {
			try {
				mongoDBProperties.load(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
		} else {
			System.out.println("Could not load properties!!");
		}
	}
	
	public static String getMongoDBHost() {
		return mongoDBProperties.getProperty("mongodb.host");
	}
	
	public static int getMongoPort() {
		int port = 27017;
		try{ 
			port = Integer.parseInt(mongoDBProperties.getProperty("mongodb.port"));
		} catch (Exception e) {
			System.out.println("No valid port has been specified, defaulting to 27017!");
		}
		return port;
	}
	
	public static String getMongoDBDataSetTableName() {
		return mongoDBProperties.getProperty("mongodb.dataset.tableName");
	}
	
	public static String getMongoDBImageURLTableName() {
		return mongoDBProperties.getProperty("mongodb.imageUrls.tableName");
	}
	
	public static String getMongoDBUserName() {
		return mongoDBProperties.getProperty("mongodb.username");
	}
	
	public static String getMongoDBPassword() {//TODO change it to byte array. Also store encryoted password!
		return mongoDBProperties.getProperty("mongodb.password");
	}
}
