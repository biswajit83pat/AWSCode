package com.aws.changed.threads;

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

import org.apache.log4j.Logger;

import com.sony.picbee.image.util.PicBeeImageUtils;

public class ParallelWorker implements Runnable {
	
	Logger logger = Logger.getLogger("com.sony.picbee.newApproach"); 
	
	private final String imageUrl;
	private final String imageFolderPath;
	
	public ParallelWorker(String imageFolderPath, String imageUrl) {
		this.imageFolderPath = imageFolderPath;
		this.imageUrl = imageUrl;
	}

	public void downloadImagesIntoLocalFolder(String imageUrl) throws IOException {
		// String imageUrl =
		// "http://petsubjectsrescue.petethevet.com/wp-content/uploads/2013/11/Guide-Dogs_025-11.jpg";
		// String synset_code = "S0001";

		FilePermission permission = new FilePermission(imageFolderPath, "write");
		
		URL url = new URL(imageUrl);
		String fileName = url.getFile();

		File file = new File(imageFolderPath);
		if (!file.exists()) {
			if (file.mkdirs()) {
				logger.debug("Directory is created!");
			} else {
				logger.debug("Failed to create directory!");
			}
		} else {
			logger.debug("Directory already exists");
		}

		// String destName = "C:\\Users\\5013003472\\Desktop\\Image
		// Collection\\" + synset_code + "\\" + "image1" +
		// fileName.substring(fileName.lastIndexOf("/")+1);
		String destName = "";
		if(fileName.contains("mm.bing.net") || fileName.contains("th?id=OIP")) {//coming from bing
			destName = imageFolderPath + UUID.randomUUID() + "_bing.jpg";
		} else {
			destName = imageFolderPath + fileName.substring(fileName.lastIndexOf("/") + 1);
		}
		logger.debug(destName);

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
			logger.debug("Image downloaded");
			
		} catch (IOException ie) {
			// ie.printStackTrace();
			logger.error("Error in downloader : IO Exception");
		} finally {
			if (is != null)
				is.close();
			if (os != null)
				os.close();
		}
	}

	@Override
	public void run() {
		try {
		downloadImagesIntoLocalFolder(imageUrl);
		} catch (Exception ex) {
			//capture to avoid thread death
			System.out.println(ex.getMessage());
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
	}

}
