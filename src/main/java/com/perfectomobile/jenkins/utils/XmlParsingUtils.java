package com.perfectomobile.jenkins.utils;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XmlParsingUtils {


	public static final String ITEM_ELEMENT_NAME = "item";
	public static final String DEVICEID_ELEMENT_NAME = "deviceId";
	public static final String MANUFACTURER_ELEMENT_NAME = "manufacturer";
	public static final String MODEL_ELEMENT_NAME = "model";
	public static final String LOCATION_ELEMENT_NAME = "location";
	public static final String HANDSETS_ELEMENT_NAME = "handsets";
	public static final String PARAMETERS_ELEMENT_NAME = "parameters";
	public static final String PARAMETER_ELEMENT_NAME = "parameter";
	public static final String DATA_ELEMENT_NAME = "data";
	public static final String CLASS_ATT_NAME = "class";
	public static final String SECURED_ATT_NAME = "secured"; //relevant to string params only
	public static final String NAME_ELEMENT_NAME = "name";

	public static ArrayList<String> getXmlElements(File inputFile, String elementNametoSearch) {

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		ArrayList<String> elements = new ArrayList<String>();

		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document document = docBuilder.parse(inputFile);

			NodeList itemEelements = document.getElementsByTagName(elementNametoSearch);
			for (int i = 0; i < itemEelements.getLength(); i++) 
				elements.add(itemEelements.item(i).getTextContent());
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		return elements;
	}

	public static String getXmlFirstElement(File inputFile, String elementNametoSearch) {
		return getXmlElements(inputFile, elementNametoSearch).get(0);
	}

	public static NodeList getNodeList(File inputFile, String elementName) {

		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		Document doc = null;
		try {
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			doc = docBuilder.parse(inputFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

		NodeList nodeList = doc.getElementsByTagName(elementName);
		
		return nodeList;

	}
}
