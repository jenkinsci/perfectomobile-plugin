package com.perfectomobile.jenkins;

/**
 * the global data needed by every builder in this plugin  
 * 
 * @author ronik
 */
public interface MobileCloudGlobalConfigurationData {

	public String getHostUrl(); //a derivation of the url 

	public String getAccessId();

	public String getSecretKey();

}