package com.aws.image.util;

import java.io.FileReader;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class AWSImageUtils {
	
	public static List<String[]> read(String fileName) {
		List<String[]> rowsList = null;
		try {
			CSVReader reader = new CSVReader(new FileReader(fileName), ',');
			rowsList = reader.readAll();
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rowsList;
	}
	
}