package com.perfectomobile.perfectomobilejenkins.connection.rest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.apache.http.HttpResponse;
import org.junit.Test;

import com.perfectomobile.perfectomobilejenkins.connection.http.HttpServices;

public class UploadFileServiceTest {

	@Test
	public void testuploadFile() throws FileNotFoundException, ParseException{
		
		HttpResponse perfectoResponse = null;
		File fileName = new File("src/test/resources/photo2.JPG");
		
		try {
			perfectoResponse = HttpServices.getInstance().uploadFile(
					"https://www.perfectomobile.com", 
					"jenkins@perfectomobile.com",
					"Perfecto1",
					"media", 
					"PRIVATE:/pictures/pic22.png",
					fileName);
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
}
