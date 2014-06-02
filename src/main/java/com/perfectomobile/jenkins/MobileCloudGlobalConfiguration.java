package com.perfectomobile.jenkins;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.IOException;

import javax.servlet.ServletException;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import com.perfectomobile.jenkins.services.MobileCloudServicesFactory;
import com.perfectomobile.jenkins.Messages;
import com.sun.jersey.api.client.ClientResponse;

/**
 * @author ronik
 * 
 * the base class of all descriptors of all builders in this plugin
 */
public abstract class MobileCloudGlobalConfiguration extends BuildStepDescriptor<Builder> implements MobileCloudGlobalConfigurationData {
	private static final String SECRET_KEY_ID = "secretKey";
	private static final String ACCESS_ID_ID = "accessId";
	private static final String URL_ID = "url";
	
	private static final String URL_FIELD_NAME = "Cloud name";
	
	/**
	 * To persist global configuration information, simply store it in a
	 * field and call save().
	 * 
	 * <p>
	 * If you don't want fields to be persisted, use <tt>transient</tt>.
	 */
	private String url;
	private String username;
	private String password;
	
	
	/**
	 * In order to load the persisted global configuration, you have to call
	 * load() in the constructor.
	 */
	public MobileCloudGlobalConfiguration() {
		load();
	}

	public FormValidation doCheckUrl(@QueryParameter String value)
			throws IOException, ServletException {
		FormValidation result = FormValidation.ok();
		if (!isSimpleCloudName(value))
			result = FormValidation.error(Messages.UiError_UrlShouldNotContainProtocol());
		return result;
	}
	boolean isSimpleCloudName(String value) {
		return !value.contains(Constants.PROTOCOL_SEPARATOR);
	}

	@SuppressWarnings("rawtypes")
	public boolean isApplicable(Class<? extends AbstractProject> aClass) { return true; }


	
	@Override
	public boolean configure(StaplerRequest req, JSONObject formData)
			throws FormException {
		// To persist global configuration information,
		// set that to properties and call save().
		
		//check if any of the important values entered has changed then we would need to refresh singletons data
		if (isPrimaryStep()) { 
			boolean changed = false;
			String newValue = formData.getString(URL_ID); 
			if (hasChanged(newValue,url)) {
				url = newValue;
				changed = true;
			}
			
			newValue = formData.getString(ACCESS_ID_ID);			
			if (!changed && hasChanged(newValue, username))
				changed = true;
			username = newValue;
			
			newValue = formData.getString(SECRET_KEY_ID);
			if (!changed && hasChanged(newValue, password))
				changed = true;
			password = newValue;

			//TODO: changed is true only if something relevant to the connection has changed
			//do we want to do something with this info?
		}			
		save();
		
		return super.configure(req, formData);
	}

	/**
	 * @return true is this is a primary step - one which has a global config that needs to be loaded and saved 
	 */
	protected boolean isPrimaryStep() { return true; }

	private boolean hasChanged(String newValue, String oldValue) {
		return oldValue == null && newValue != null || oldValue != null && !oldValue.equals(newValue);
	}

	public String getUrl() {
		return url;
	}

	/* (non-Javadoc)
	 * @see com.perfectomobile.jenkins.MobileCloudGlobalConfigurationData#getAccessId()
	 */
	public String getAccessId() {
		return username;
	}

	/* (non-Javadoc)
	 * @see com.perfectomobile.jenkins.MobileCloudGlobalConfigurationData#getSecretKey()
	 */
	public String getSecretKey() {
		return password;
	}
	public String getHostUrl() {
		return Constants.ACCESSING_PROTOCOL + url;
	}

	public FormValidation doTestConnection(
			@QueryParameter(URL_ID) final String url,
			@QueryParameter(ACCESS_ID_ID) final String accessId,
			@QueryParameter(SECRET_KEY_ID) final String secretKey)
			throws IOException, ServletException {

		// setup REST-Client
		ClientResponse perfectoResponse = null;

		FormValidation result = null;
		if (!isSimpleCloudName(url) )
			result = FormValidation.error(Messages.UiError_PleaseEnterValidFieldValue(URL_FIELD_NAME));
		else if (url.isEmpty() || accessId.isEmpty() || secretKey.isEmpty())
			result = FormValidation.error(Messages.UiError_PleaseEnterValidValues());
		else {
			try {
				perfectoResponse = MobileCloudServicesFactory.getInstance().testConnections(Constants.ACCESSING_PROTOCOL+url, accessId, secretKey);
				result = FormValidation.ok(Messages.UiInfo_DefaultConnectionSuccessful());
				if (perfectoResponse.getStatus() != Constants.PM_RESPONSE_STATUS_SUCCESS) {
					String msg = MobileCloudServicesFactory.getInstance().getScriptExecutionServices().getResponseErrorMsg(perfectoResponse);
					if (msg == null) //failed to parse successfully
						msg = Messages.UiError_DefaultConnectionRefused(perfectoResponse.getStatus(),perfectoResponse.getStatusInfo().getReasonPhrase());
					else if (msg.startsWith(Constants.MSG_ELEMENT_BEGINNING_FOR_FAILED_TO_GET_DEVICE_LIST))
						msg = msg.substring(Constants.MSG_ELEMENT_BEGINNING_FOR_FAILED_TO_GET_DEVICE_LIST.length(), msg.length()-1);
					result = FormValidation.error(msg);
				}
			} catch (Exception e) {
				result = FormValidation.error(Messages.UiError_ConnectionError(URL_FIELD_NAME,e.getMessage()));
			}
		}
		return result;
	}
	
}
