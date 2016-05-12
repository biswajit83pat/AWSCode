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
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

public class MongoDBDownloaderUtilitySync {

	private MongoCollection<Document> collection = MongoDBConnector.INSTANCE.getSyncDatabase().getCollection(MongoDBConfigLoader.getMongoDBImageURLTableName());

	private final String filePath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Image Collection" + File.separator;

	private volatile String folderPath;
	
	List<Document> documentList = new ArrayList<>();
	
	Consumer<Document> consumer = (Document d) -> 
	{
		System.out.println(d.toJson());
		Document idObj = (Document) d.get("_id");
		System.out.println(idObj.get("url"));
		try{
			downloadImagesIntoLocalFolder(idObj.getString("url"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	};
	
	public void downloadAllImagesForSynsetCode(String synsetCode) {

		folderPath = filePath + synsetCode + File.separator;
		FilePermission permission = new FilePermission(folderPath, "write");


		//collection.find(eq("_id.category", synsetCode)).forEach(printDocumentBlock, callbackWhenFinished);
		//collection.find().filter(Filters.eq("_id.category", synsetCode)).forEach(printDocumentBlock, callbackWhenFinished);
		
		collection.find().filter(Filters.eq("_id.category", synsetCode)).forEach(consumer);
		
		// find documents
		/*collection.find().filter(Filters.eq("_id.category", synsetCode)).into(documentList, 
		    new SingleResultCallback<List<Document>>() {
		        @Override
		        public void onResult(final List<Document> result, final Throwable t) {
		            System.out.println("Found Documents: #" + result.size());
		            //result.forEach(consumer);
		            
		            for(Document d: result) {
		            	System.out.println(d.toJson());
		    			downloadAllImagesForSynsetCode(d.get("_id.url").toString());
		    			try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		            }
		            
		            
		            countDownLatch.countDown();
		        }
		    });*/
		
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
