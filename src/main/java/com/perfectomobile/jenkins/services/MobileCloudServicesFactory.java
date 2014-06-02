package com.perfectomobile.jenkins.services;

import java.io.IOException;

import javax.servlet.ServletException;

import com.perfectomobile.jenkins.MobileCloudGlobalConfigurationData;
import com.perfectomobile.jenkins.utils.JsonParsingUtils;
import com.sun.jersey.api.client.ClientResponse;

public class MobileCloudServicesFactory {
	private DeviceListGetter _deviceListGetter;
	private RepositoryItemsGetter _repositoryItemsGetter;
	private ScriptExecutionServices _scriptExecutionServices;
	private RestServices _restServices;	
	private JsonParsingUtils _jsonParsingUtils;
	private MobileCloudGlobalConfigurationData _globalConfigurationData;
	
	private static MobileCloudServicesFactory _instance = new MobileCloudServicesFactory();
	private MobileCloudServicesFactory() {
		_jsonParsingUtils = new JsonParsingUtils();
		_restServices = new RestServices();
		
		_deviceListGetter = new DeviceListGetter();
		_deviceListGetter.setRestServices(_restServices);
		
		_repositoryItemsGetter = new RepositoryItemsGetter();
		_repositoryItemsGetter.setRestServices(_restServices);
		
		_scriptExecutionServices = new ScriptExecutionServices();
		_scriptExecutionServices.setRestServices(_restServices);
		_scriptExecutionServices.setJsonParsingUtils(_jsonParsingUtils);
	}
	public static MobileCloudServicesFactory getInstance() { return _instance; }
	
	
	public DeviceListGetter getDeviceListGetter() {
		return _deviceListGetter;
	}
	
	public RepositoryItemsGetter getRepositoryItemsGetter() {
		return _repositoryItemsGetter;
	}
	
	public ScriptExecutionServices getScriptExecutionServices() {
		return _scriptExecutionServices;
	}
	public void resetGetters() {
		_repositoryItemsGetter.doReset();
		_deviceListGetter.doReset();		
	}
	public ClientResponse testConnections(String hostUrl, String accessId,
			String secretKey) throws IOException, ServletException {
		return _restServices.getHandsets(hostUrl, accessId, secretKey);
	}
	public JsonParsingUtils getJsonParsingUtils() {
		return _jsonParsingUtils;
	}
	public void setGlobalConfigurationData(MobileCloudGlobalConfigurationData globalConfigurationData) {
		if (_globalConfigurationData == null)
			_globalConfigurationData = globalConfigurationData;
	}
	public MobileCloudGlobalConfigurationData getGlobalConfigurationData() {
		return _globalConfigurationData;
	}
	
	
}
