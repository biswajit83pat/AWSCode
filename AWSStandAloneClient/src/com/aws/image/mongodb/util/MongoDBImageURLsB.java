package com.aws.image.mongodb.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.bson.Document;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.UpdateOneModel;

public class MongoDBImageURLsB {
	
	
	private MongoCollection<Document> collection = MongoDBConnector.INSTANCE.getAsyncDatabase().getCollection(MongoDBConfigLoader.getMongoDBImageURLTableName());
	
	SingleResultCallback<BulkWriteResult> printBatchResult = new SingleResultCallback<BulkWriteResult>() {
	    @Override
	    public void onResult(final BulkWriteResult result, final Throwable t) {
	        System.out.println("Insert Many operations have been completed --> " + result);
	    }
	};
	
	public void insertMany(List<Document> documents) {
		
		collection.insertMany(documents, (result, t)-> {
			OptionalConsumer.of(Optional.ofNullable(t)).
			ifPresent(
					s -> {
						System.out.println("Insertion failed, cause " + s);
						//Do not trigger anything.
						//Do other rollback activities or simple log error and/or retry.
					}
					
					).
			ifNotPresent(
					
					() -> {
						System.out.println("Insertion Successful for records of size : " + documents.size());

						//Create trigger to insert into image URL b
						
						
						
					}
					
					
					);
		});
	}
	
	public void insertBatchUnordered(List<Document> documents) {
		
	 // 2. Unordered bulk operation - no guarantee of order of operation
	collection.bulkWrite(
	  Arrays.asList(new InsertOneModel<>(new Document("_id", 4)),
	                new InsertOneModel<>(new Document("_id", 5)),
	                new InsertOneModel<>(new Document("_id", 6)),
	                new UpdateOneModel<>(new Document("_id", 1),
	                                     new Document("$set", new Document("x", 2))),
	                new DeleteOneModel<>(new Document("_id", 2)),
	                new ReplaceOneModel<>(new Document("_id", 3),
	                                      new Document("_id", 3).append("x", 4))),
	  new BulkWriteOptions().ordered(false),
	  printBatchResult
	);
		
		
		
		
		
		collection.insertMany(documents, (result, t)-> {
			OptionalConsumer.of(Optional.ofNullable(t)).
			ifPresent(
					s -> {
						System.out.println("Insertion failed, cause " + s);
						//Do not trigger anything.
						//Do other rollback activities or simple log error and/or retry.
					}
					
					).
			ifNotPresent(
					
					() -> {
						System.out.println("Insertion Successful for records of size : " + documents.size());

						//Create trigger to insert into image URL b
						
						
						
					}
					
					
					);
		});
	}
	
}