package com.aws.changed.threads;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;

public class DownloaderThrottle {

	public static void main(String[] args) throws InterruptedException, IOException, JSONException {
		Logger log = Logger.getLogger("com.sony.picbee.newApproach");//testNewApproach.class);
		// TODO Auto-generated method stub
		//InsertBatchId insertObj = new InsertBatchId();
		//insertObj.addBatchIdInCsv();
		
		//SearchImageURLs obj = new SearchImageURLs();
		//obj.readCSVAndFetchURLs();
		
		ParallelDownloadFromCSV downloadObj = new ParallelDownloadFromCSV();
		//downloadObj.downloader("n04335209");
		downloadObj.downloader("n07942152");
		
		//logger.debug("Test new file ... ");
		log.error("test error");
	}

}
