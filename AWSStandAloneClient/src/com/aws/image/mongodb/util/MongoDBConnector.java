package com.aws.image.mongodb.util;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.ServerAddress;
import com.mongodb.async.client.MongoClientSettings;
import com.mongodb.async.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ConnectionPoolSettings;
import com.mongodb.connection.SocketSettings;

public enum MongoDBConnector {
	INSTANCE;

	private com.mongodb.async.client.MongoClient mongoAsyncClient;// Async client
	private MongoClient mongoSyncClient;// Sync client
	private volatile String host;
	private volatile int port;
	private final static String databaseName = "PicBeeDBName";
	
	private MongoDBConnector() {
		try {
			
			host = MongoDBConfigLoader.getMongoDBHost();
			port = MongoDBConfigLoader.getMongoPort();
			
			if (mongoSyncClient == null)
				mongoSyncClient = getSyncClient();
			if (mongoAsyncClient == null)
				mongoAsyncClient = getASyncClient();
		} catch (Exception ex) {
			System.out
					.println("Error occurred while trying to get MongoClient, cause : "
							+ ex.getMessage());
			ex.printStackTrace();
		}
	}

	private MongoClient getSyncClient() {
		Builder mongoClientOptionsBuilder = new MongoClientOptions.Builder().socketKeepAlive(true).connectTimeout(4000).socketTimeout(0);//4 secs
		MongoClientOptions mongoClientOptions = mongoClientOptionsBuilder.build();
		mongoSyncClient = new MongoClient(Arrays.asList(new ServerAddress(host, port)), mongoClientOptions);
		return mongoSyncClient;
	}
	
	private com.mongodb.async.client.MongoClient getASyncClient() {
		//return new MongoClient(MongoDBConfigLoader.getMongoDBHost(), MongoDBConfigLoader.getMongoPort());
		ClusterSettings clusterSettings = ClusterSettings.builder().hosts(Arrays.asList(new ServerAddress(host))).build();
		MongoClientSettings settings = MongoClientSettings.builder().clusterSettings(clusterSettings).socketSettings(SocketSettings.builder().keepAlive(true).connectTimeout(3, TimeUnit.MINUTES).build()).connectionPoolSettings(ConnectionPoolSettings.builder().maxConnectionLifeTime(3, TimeUnit.MINUTES).build()).build();
		mongoAsyncClient =  MongoClients.create(settings);
		return mongoAsyncClient;
	}
	
	public com.mongodb.async.client.MongoDatabase getAsyncDatabase() {
		return mongoAsyncClient.getDatabase(databaseName);
	}
	
	public MongoDatabase getSyncDatabase() {
		return mongoSyncClient.getDatabase(databaseName);
	}

	//call it on any shutdown hook for example 
    public void closeSync(){
    	mongoSyncClient.close();
    }
    
    //call it on any shutdown hook for example 
    public void closeAsync(){
    	mongoAsyncClient.close();
    }
}
