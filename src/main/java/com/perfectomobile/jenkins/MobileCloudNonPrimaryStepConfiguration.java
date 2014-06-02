package com.perfectomobile.jenkins;

import com.perfectomobile.jenkins.services.MobileCloudServicesFactory;

/**
 * all non-primary steps (in this plugin there should only be one primary step) should extend from this class
 * global data is accessed via this class and this accessing is different then the way the global data is accessed
 * by the primary step
 * @author ronik
 *
 */
public abstract class MobileCloudNonPrimaryStepConfiguration extends MobileCloudStepConfiguration {
	private MobileCloudGlobalConfigurationData getGlobalData() {
		return MobileCloudServicesFactory.getInstance().getGlobalConfigurationData();
	}
	
	/* (non-Javadoc)
	 * @see com.perfectomobile.jenkins.MobileCloudGlobalConfiguration#isPrimaryStep()
	 * @return false since this is not a primary step - it has no global config that needs to be loaded and saved
	 */
	@Override
	protected boolean isPrimaryStep() { return false; }

	@Override
	public String getHostUrl() {
		return getGlobalData().getHostUrl();
	}
	
	@Override
	public String getAccessId() {
		return getGlobalData().getAccessId();
	}
	
	@Override
	public String getSecretKey() {
		return getGlobalData().getSecretKey();
	}

}
