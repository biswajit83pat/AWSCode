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

public class ParallelDownloadFromCSV {
	
	Logger logger = Logger.getLogger("com.sony.picbee.newApproach"); 
	//private final static String filePath = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "Image Collection" + File.separator;
	
	private final String filePath = "E:" + File.separator + "SAMA" + File.separator + "TestImageCollection" + 
										File.separator + "TestImages" + File.separator;
	
	private volatile String folderPath;
	
	
	
	public void downloader(String synset_code) throws IOException
	{
		List<String> urls = new ArrayList<String>();
		String fileName = System.getProperty("user.dir") + File.separator + "src" + File.separator + "resources" + File.separator + synset_code + ".csv";
		//String fileName = "D://SAMA//TestImageCollection//TestCSVs//" + synset_code +".csv";
		List<String[]> rowsList = PicBeeImageUtils.read(fileName);
		String[] row = null;
		String groupId = "";
		String keywords = "";
		String safeSearch = "";
		String urlFromCsv = "";
		int limit = rowsList.size();
		//int limit = 500;
		
		downloadAllImagesForSynsetCode(synset_code);
		
		for (int i = 0; i < limit; i++) {
			
			row = rowsList.get(i);
			
			urlFromCsv = row[0].trim();
			//keywords = row[1].trim();
			//safeSearch = row[5].trim();
			logger.debug("URL read from CSV = " + urlFromCsv);
			
			urls.add(urlFromCsv);
			
			//downloadImagesIntoLocalFolder(urlFromCsv);			
			// Code to download image from the url obtained.
			
			
		}
		
		System.out.println("B4 calling parallel downloader, list size is --> " + urls.size());
		logger.debug("B4 calling parallel downloader, list size is --> " + urls.size());
		
		ParallelDownloader parallelDownloader = new ParallelDownloader();
		parallelDownloader.syncSubmit(folderPath, urls);
		
		System.out.println("After calling parallel downloader, ....");
		logger.debug("After calling parallel downloader, ....");

	}
	
	public void downloadAllImagesForSynsetCode(String synsetCode) {

		folderPath = filePath + synsetCode + File.separator;
		FilePermission permission = new FilePermission(folderPath, "write");

		
	}

	

}
