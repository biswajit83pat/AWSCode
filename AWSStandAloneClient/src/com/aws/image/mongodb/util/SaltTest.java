package com.aws.image.mongodb.util;

import org.bson.Document;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class SaltTest {

	public static void main(String[] args) {
		
		MongoDBConfigLoader mongoDBConfigLoader = new MongoDBConfigLoader();
		MongoDatabase database = MongoDBConnector.INSTANCE.getSyncDatabase();
		MongoCollection<Document> collection = database.getCollection("test");
		System.out.println(collection);
		
		Document doc = new Document("name", "MongoDB")
        .append("type", "database")
        .append("count", 1)
        .append("info", new Document("x", 203).append("y", 102));
		
		System.out.println("#1 DB Async: " + MongoDBConnector.INSTANCE.getAsyncDatabase().hashCode());
		System.out.println("#2 DB Async: " + MongoDBConnector.INSTANCE.getAsyncDatabase().hashCode());
		
		System.out.println("#1 DB Sync: " + MongoDBConnector.INSTANCE.getSyncDatabase().hashCode());
		System.out.println("#2 DB Sync: " + MongoDBConnector.INSTANCE.getSyncDatabase().hashCode());
		System.out.println("#3 DB Sync: " + MongoDBConnector.INSTANCE.getSyncDatabase().hashCode());
		System.out.println("#4 DB Sync: " + MongoDBConnector.INSTANCE.getSyncDatabase().hashCode());
		
		System.out.println("#1 Enum Instance: " + MongoDBConnector.INSTANCE.hashCode());
		System.out.println("#1 Enum Instance: " + MongoDBConnector.INSTANCE.hashCode());
		System.out.println("#1 Enum Instance: " + MongoDBConnector.INSTANCE.hashCode());
		
		collection.insertOne(doc);
		
		//Closing db connections for shutdown hook
		MongoDBConnector.INSTANCE.closeAsync();
		MongoDBConnector.INSTANCE.closeSync();
		
	}

}
