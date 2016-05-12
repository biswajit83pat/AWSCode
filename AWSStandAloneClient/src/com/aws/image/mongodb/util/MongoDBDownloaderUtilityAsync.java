package com.aws.image.mongodb.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.async.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class MongoDBDownloaderUtilityAsync {

	private MongoCollection<Document> collection = MongoDBConnector.INSTANCE.getAsyncDatabase().getCollection(MongoDBConfigLoader.getMongoDBImageURLTableName());

	private final String filePath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Image Collection" + File.separator;

	private volatile String folderPath;

	private CountDownLatch countDownLatch;
	
	public MongoDBDownloaderUtilityAsync(CountDownLatch countDownLatch) {
		this.countDownLatch = countDownLatch;
	}
	
	List<Document> documentList = new ArrayList<>();
	
	Consumer<Document> consumer = (Document d) -> 
	{
		//System.out.println(d.toJson());
		Document idObj = (Document) d.get("_id");
		System.out.println(idObj.get("url"));
		try{
			downloadImagesIntoLocalFolder(idObj.getString("url"));
		} catch (Exception e) {
			//System.out.println("Download failed , cause --> " + e.getMessage());
		}
		countDownLatch.countDown();
	};
	
	/*Block<Document> printDocumentBlock = new Block<Document>() {
		@Override
		public void apply(final Document document) {
			System.out.println(document.toJson());
			Document idObj = (Document) document.get("_id");
			System.out.println(idObj.get("url"));
			downloadAllImagesForSynsetCode(document.get("_id.url").toString());
			countDownLatch.countDown();
		}
	};

	SingleResultCallback<Void> callbackWhenFinished = new SingleResultCallback<Void>() {
		@Override
		public void onResult(final Void result, final Throwable t) {
			System.out.println("Operation Finished!");
		}
	};*/

	
	public void downloadAllImagesForSynsetCode(String synsetCode) {

		folderPath = filePath + synsetCode + File.separator;
		FilePermission permission = new FilePermission(folderPath, "write");

		//Consumer<Student> style = (Student s) -> System.out.println("Name:"+s.name +" and Age:"+s.age);
		
		
		//collection.find(eq("_id.category", synsetCode)).forEach(printDocumentBlock, callbackWhenFinished);
		//collection.find().filter(Filters.eq("_id.category", synsetCode)).forEach(printDocumentBlock, callbackWhenFinished);
		
		
		// find documents
		collection.find().filter(Filters.eq("_id.category", synsetCode)).into(documentList, 
		    new SingleResultCallback<List<Document>>() {
		        @Override
		        public void onResult(final List<Document> result, final Throwable t) {
		            System.out.println("Found Documents: #" + result.size());
		            result.forEach(consumer);
		        }
		    });
		
		System.out.println("Document size is --> " + documentList.size());
	}

	public void downloadImagesIntoLocalFolder(String imageUrl) throws IOException {
		// String imageUrl =
		// "http://petsubjectsrescue.petethevet.com/wp-content/uploads/2013/11/Guide-Dogs_025-11.jpg";
		// String synset_code = "S0001";

		URL url = new URL(imageUrl);
		String fileName = url.getFile();

		File file = new File(folderPath);
		if (!file.exists()) {
			if (file.mkdirs()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		} else {
			System.out.println("Directory already exists");
		}

		// String destName = "C:\\Users\\5013003472\\Desktop\\Image
		// Collection\\" + synset_code + "\\" + "image1" +
		// fileName.substring(fileName.lastIndexOf("/")+1);
		String destName = "";
		if(fileName.contains("mm.bing.net") || fileName.contains("th?id=OIP")) {//coming from bing
			destName = folderPath + UUID.randomUUID() + "_bing.jpg";
		} else {
			destName = folderPath + fileName.substring(fileName.lastIndexOf("/") + 1);
		}
		System.out.println(destName);

		// FilePermission permission = new
		// FilePermission("C:\\Users\\5013003472\\Desktop\\Image Collection\\" +
		// synset_code, "write");
		InputStream is = null;
		OutputStream os = null;

		try {
			is = url.openStream();
			os = new FileOutputStream(destName);

			byte[] b = new byte[2048];
			int length;

			while ((length = is.read(b)) != -1) {
				os.write(b, 0, length);
			}

		} catch (IOException ie) {
			// ie.printStackTrace();
		} finally {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
		}
	}
}
