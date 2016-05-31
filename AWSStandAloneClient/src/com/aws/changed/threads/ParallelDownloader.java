package com.aws.changed.threads;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

public class ParallelDownloader {

	Logger logger = Logger.getLogger("com.sony.picbee.newApproach"); 

	public static final int PARALLEL_WORKERS = 5;
	private ExecutorService pool = Executors.newFixedThreadPool(PARALLEL_WORKERS);
	
	public void syncSubmit(String folderPath, List<String> urlsToBeDownloaded) {
		if(urlsToBeDownloaded == null || urlsToBeDownloaded.size() < 1) {
			return;
		}
		
		int listSize = urlsToBeDownloaded.size();
		
		ParallelWorker parallelWorker = null;

		for(int i = 0 ; i < listSize; i++) {
			parallelWorker = new ParallelWorker(folderPath, urlsToBeDownloaded.get(i));
			pool.submit(parallelWorker);
		}
		
		pool.shutdown();
		
		while(!pool.isTerminated()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Task OVER!!!");
		
		logger.debug("Task OVER!!!");
		
	}
	
}
