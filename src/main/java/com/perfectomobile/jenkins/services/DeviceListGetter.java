package com.perfectomobile.jenkins.services;

import java.io.File;
import java.util.List;

import com.sun.jersey.api.client.ClientResponse;
import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.miscel.InfoGetter;
import com.perfectomobile.jenkins.miscel.StringListAccumulator;
import com.perfectomobile.jenkins.utils.XmlParsingUtils;

public class DeviceListGetter extends InfoGetter {
	
	/**
	 * @param items
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @return true if successful otherwise returns false and getError could be called
	 */
	public boolean populateDevices(StringListAccumulator items, String url, String accessId, String secretKey) {
		boolean ret = false; //assume error
		ClientResponse cloudResponse = null;
		if (!isAccessDenied()) { 
			try {
				//next call may throw an exception that will be caught below
				cloudResponse = getRestServices().getHandsets(url, accessId, secretKey);
				
				if (cloudResponse.getStatus() == Constants.PM_RESPONSE_STATUS_SUCCESS) {
					
					File resultFile = cloudResponse.getEntity(File.class);
					List<String> devices = XmlParsingUtils.getXmlElements(
							resultFile, XmlParsingUtils.DEVICEID_ELEMENT_NAME);
					List<String> manufacturer = XmlParsingUtils.getXmlElements(
							resultFile, XmlParsingUtils.MANUFACTURER_ELEMENT_NAME);
					List<String> location = XmlParsingUtils.getXmlElements(
							resultFile, XmlParsingUtils.LOCATION_ELEMENT_NAME);
					List<String> model = XmlParsingUtils.getXmlElements(
							resultFile, XmlParsingUtils.MODEL_ELEMENT_NAME);
		
					for (int i = 0; i < devices.size(); i++) {
						StringBuffer itemDetails = new StringBuffer();
						itemDetails.append(manufacturer.get(i)).append(Constants.DEVICE_INFO_SEPARATOR)
								.append(model.get(i)).append(Constants.DEVICE_INFO_SEPARATOR)
								.append(location.get(i)).append(Constants.DEVICE_INFO_SEPARATOR)
								.append(devices.get(i));
						items.addString(itemDetails.toString());
					}
					ret = true;
				} else { //got bad credentials or something like that which results in 401 Unauthorized
					setAccessDenied(true);
					ret = false;
				}
			} catch (Exception e) { //technical error. probably io exception. return as if no error in order not to disable re-attempts
				System.out.println("ERROR:  While trying to get devices got "+e.getClass().getName()+": "+e.getMessage());
				ret = true; //acts as if no errors but the list will be / remain empty
			}
				
		}
		return ret;
	}
}
