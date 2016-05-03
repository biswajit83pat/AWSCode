package com.aws.sampleImage.url;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.StreamRecord;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent.DynamodbStreamRecord;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

public class LambdaFunctionHandler implements RequestHandler<DynamodbEvent, Object> {

private final static String INSERT_EVENT = "INSERT";
	
	volatile static AmazonDynamoDBClient dynamoDB;

	static AWSCredentials awsCredentials = new AWSCredentials() {
		
		@Override
		public String getAWSSecretKey() {
			return "M2HnD68DXBmeNYcuxO0PDVuRPjJONDLiRXTFwygN";//M2HnD68DXBmeNYcuxO0PDVuRPjJONDLiRXTFwygN
		}
		
		@Override
		public String getAWSAccessKeyId() {
			return "AKIAJ5LSYQCPXHSNYVYQ";//AKIAJ5LSYQCPXHSNYVYQ
		}
	};

	private static void init() {
		
        //dynamoDB = new AmazonDynamoDBClient(new EnvironmentVariableCredentialsProvider());
		dynamoDB = new AmazonDynamoDBClient(awsCredentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoDB.setRegion(usWest2);
        System.out.println("^^^^^  DynamoDB created successfully --> " + dynamoDB);
    }
	
    @Override
    public Object handleRequest(DynamodbEvent input, Context context) {
        context.getLogger().log("Input: " + input);

        try {
			init();
		} catch (Exception e) {
			System.err.println("Could Not load credentials from environment");
			return "Unsuccessful because AWS Credentials from environment could not be obtained!";
		}
        
        List<DynamodbEvent.DynamodbStreamRecord> recordsList = input.getRecords();
        
        for(DynamodbStreamRecord record: recordsList) {
        	
        	handleEachRecord(record.getEventName(), record.getDynamodb());
        }
        
        return new StringBuffer("Successfully downloaded ").append(recordsList.size()).append(" records").toString();        
    }
    
    private void handleEachRecord(String eventName, StreamRecord streamRecord) {
    	try {
    		
    		//Only if Insert then start processing
        	if(INSERT_EVENT.equalsIgnoreCase(eventName)) {
        		
        	
				Map<String, AttributeValue> newImageMap = streamRecord.getNewImage();
				
				AttributeValue urlAttrVal = newImageMap.get("url");
				AttributeValue synsetCodeAttrVal = newImageMap.get("synset_code");
				
				String url = urlAttrVal.getS();
				String synsetCode = synsetCodeAttrVal.getS();

				downloadImagesForEveryURL(url, synsetCode);
        		
        	}
        	
    	} catch (AmazonServiceException ase) {
    		System.out.println(ase.getMessage());
    		
    	} catch (AmazonClientException ace) {
    		System.out.println(ace.getMessage());
    		
    	} catch (Exception ex) {
    		System.out.println(ex.getMessage());
    	}
    }
    
   
    
    private static void downloadImagesForEveryURL(String url, String synsetCode) throws IOException {
        
    	URL url2 = new URL(url);
    	BufferedImage img = ImageIO.read(url2);
    	
    	File file = File.createTempFile("aws-java-sdk-", ".txt");
        file.deleteOnExit();
        
        //File output = new File(path, name);
        ImageIO.write(img, "jpg", file);

        AmazonS3 s3 = new AmazonS3Client(awsCredentials);
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);

        String bucketName = "images2016";
        
        //TODO create bucket code
        
        String tempUrl = url.replaceAll("https://", "");
        tempUrl = tempUrl.replaceAll("http://", "");
        
        String key = synsetCode + File.separator + tempUrl;
        //String extensionName = url.substring(url.lastIndexOf("."));
        
        key = key.replaceAll(File.separator,"_");
        //key = key + extensionName;
        
        System.out.println("Uploading a new object to S3 from a file\n");
        s3.putObject(new PutObjectRequest(bucketName, key, file));

      
    }


}
