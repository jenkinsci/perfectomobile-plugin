package com.perfectomobile.jenkins.services;

import hudson.FilePath;
import hudson.ProxyConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import javax.servlet.ServletException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import jenkins.model.Jenkins;

import org.apache.commons.httpclient.auth.AuthScope;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.Messages;
import com.perfectomobile.jenkins.MobileCloudGlobalConfigurationData;
import com.perfectomobile.jenkins.miscel.UploadFile;
import com.perfectomobile.jenkins.utils.GeneralUtils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;

public class RestServices {
	
	/**
	 * Setup REST Client
	 * 
	 * @param url
	 * @param user
	 * @param password
	 * @return
	 */
	private WebResource getService(final String url, final String user, final String password) {
		Client client = createClient();
		client.addFilter(new HTTPBasicAuthFilter(user, password));
		WebResource service = client.resource(url);
		return service;
	}

	/**
	 * Get List of available devices.
	 * 
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse getHandsets(final String url, final String accessId,
			final String secretKey) throws IOException, ServletException {

		
		//setup the REST-Client
		WebResource service = createService(
				url, accessId, secretKey,
				new String[]{"handsets"}, 
				"list")
			.queryParam("availableTo", accessId)
			.queryParam("inUse", "false");
		
		ClientResponse perfectoResponse = service.get(ClientResponse.class);

		return perfectoResponse;
	}


	/**
	 * Get script variables Example:
	 * https://www.perfectomobile.com/services/repositories
	 * /scripts/PRIVATE:variables.xml?
	 * operation=download&user=jenkins@perfectomobile.com&password=Perfecto1
	 * 
	 * @param url
	 * @param accessId
	 * @param secretKeyprintRequest
	 * @param script
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse getRepoScriptsItems(final String url,
			final String accessId, final String secretKey, final String script)
			throws IOException, ServletException {

		//setup the REST-Client
		WebResource service = createService(
			url, accessId, secretKey,
			new String[]{"repositories", "scripts", script}, 
			"download");
		
		ClientResponse perfectoResponse = service.get(ClientResponse.class);
		
		return perfectoResponse;
	}
	
	private WebResource createService(
			MobileCloudGlobalConfigurationData mobileCloudGlobalConfigurationData,
			String[] pathComponents, String operation) {
		
		return createService(				
				mobileCloudGlobalConfigurationData.getHostUrl(), 	//host
				mobileCloudGlobalConfigurationData.getAccessId(), 	//user
				mobileCloudGlobalConfigurationData.getSecretKey(),	//password
				pathComponents,
				operation);
	}

	private WebResource createService( final String url, final String user, final String password, String[] pathComponents, String operation) {
		WebResource service = getService(url, user, password)
			.path("services");
		
		for(String comp : pathComponents)
			service = service.path(comp);
		
		service = service
			.queryParam("operation", operation) //add operation
			.queryParam("user", user).queryParam("password", password); //add credentials
		
		return service;
	}
	
	

	/**
	 * Starts a new asynchronous execution of the specified script and returns
	 * immediately with the response data. 
	 * 
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://mycloud.perfectomobile.com/services/executions?
	 * operation=execute&
	 * scriptKey=value&user=value&password=value[&optionalParameter=value]
	 * 
	 * -Response: {"executionId":
	 * "jenkins@perfectomobile.com_variables_13-12-23_12_59_54_8082",
	 * "reportKey":"PRIVATE:variables_13-12-23_12_59_54_8082.xml"}
	 * 
	 * @see http://help.perfectomobile.com/article/AA-00209/0
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param script
	 * @param list 
	 * @param optionalParameters
	 * @param uploadList
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse executeScript(MobileCloudGlobalConfigurationData mobileCloudGlobalConfigurationData, 
			final String script,
			MultivaluedMap<String, String> scriptParams, List<UploadFile> uploadList,
			PrintStream logger) throws IOException, ServletException {

		//setup the REST-Client
		WebResource service = createService(
				mobileCloudGlobalConfigurationData,
				new String[]{"executions"},
				"execute")
			.queryParam("scriptKey", script)
			.queryParams(scriptParams);
					
		debug(service, logger);
	
		ClientResponse perfectoResponse = service.get(ClientResponse.class);
		
		return perfectoResponse;
	}


	/**
	 * Gets the status of the running or recently completed execution
	 * 
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://mycloud.perfectomobile.com/services/executions/<executionId>?
	 * operation=status&user=value&password=value
	 * 
	 * -Response:
	 * {"failedValidations":"0","flowEndCode":"Failed","reason":"ParseError"
	 * ,"status":"Completed", "description":"Completed","failedActions":"0",
	 * "executionId"
	 * :"jenkins@perfectomobile.com_variables_13-12-23_12_59_54_8082",
	 * "reportKey":"PRIVATE:variables_13-12-23_12_59_54_8082.xml",
	 * "completionDescription":"variable/parameter DUT has no value",
	 * "progressPercentage"
	 * :"0.0","user":"jenkins@perfectomobile.com","completed":"true"}
	 * 
	 * 
	 * @see http 
	 *      ://help.perfectomobile.com/article/AA-00303/51/Guides-Documentation
	 *      /HTTP-API/Operations/Script-Operations/02.-Get-Execution-Status.html
	 * @param executionId
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */ 
	public ClientResponse getExecutionStatus(MobileCloudGlobalConfigurationData mobileCloudGlobalConfigurationData,
			final String executionId) throws IOException, ServletException {

		//setup the REST-Client
		WebResource service = createService(
				mobileCloudGlobalConfigurationData,
				new String[]{"executions", executionId},
				"status");
					
		ClientResponse perfectoResponse = service.get(ClientResponse.class);
		
		return perfectoResponse;
	}

	/**
	 * Gets the status of the running or recently completed execution
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://mycloud.perfectomobile.com/services/reports/<reportKey>?
	 * operation=download&user=value&password=value
	 * 
	 * @see http://help.perfectomobile.com/article/AA-00308/0
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param reportKey
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */
	public ClientResponse downloadExecutionReport(final String url,
			final String accessId, final String secretKey,
			final String reportKey) throws IOException, ServletException {

		
		//setup the REST-Client
		WebResource service = createService(
				url, accessId, secretKey,
				new String[]{"reports", reportKey},
				"download")
			.queryParam("format", Constants.REPORT_EXECUTION_DOWNLOAD_FORMAT);
					
		ClientResponse perfectoResponse = service.get(ClientResponse.class);

		return perfectoResponse;
	}
	private Client createClient() {

		Client client = null;

		ProxyConfiguration proxy = getProxyConfiguration();
		final DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
		//Create client with proxy settings
		if (proxy != null) {
			config.getProperties().put( DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI,
				"http://" + proxy.name + ":" + proxy.port);

			if (proxyHasCredentials(proxy)) 
				config.getState().setProxyCredentials( AuthScope.ANY_REALM,
					proxy.name, proxy.port, proxy.getUserName(), proxy.getPassword());
		} 
		client = ApacheHttpClient.create(config);
		return client;
	}
	private boolean proxyHasCredentials(ProxyConfiguration proxy) {
		String user, pswd;
		return (user=proxy.getUserName()) != null && !user.isEmpty() 
				&& (pswd=proxy.getPassword()) != null && !pswd.isEmpty();
	}

	public static ProxyConfiguration getProxyConfiguration() {
		Jenkins jenkins;
		return (jenkins = Jenkins.getInstance()) == null
			? null : jenkins.proxy;
	}

//	private Client createClient() {
//
//		Client client = null;
//
//		Proxy proxy = getProxy();
//
//		//Create client with proxy settings
//		if (proxy != null) {
//			final DefaultApacheHttpClientConfig config = new DefaultApacheHttpClientConfig();
//
//			config.getProperties().put( DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI,
//				"http://" + proxy.getProxyHost() + ":" + proxy.getProxyPort());
//
//			if (proxy.hasCredentials()) 
//				config.getState().setProxyCredentials( AuthScope.ANY_REALM,
//						proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
//
//			client = ApacheHttpClient.create(config);
//		} else {
//			ClientConfig config = new DefaultClientConfig();
//			client = Client.create(config);
//		}
//		return client;
//	}
//	public static Proxy getProxy() {
//		Proxy ret = null;
//		Jenkins jenkins;
//		if ((jenkins = Jenkins.getInstance()) != null) {
//			ProxyConfiguration proxy = jenkins.proxy;
//
//			if (proxy != null) 
//				ret = new Proxy(proxy);
//		}
//		return ret;
//	}

	private void debug(WebResource service, PrintStream logger){
		GeneralUtils.debug(service.toString(), logger);
	}

	/**
	 * Get list of available items Example:
	 * https://www.perfectomobile.com/services/repositories/scripts?
	 * operation=list
	 * &user=jenkins@perfectomobile.com&password=Perfecto1&responseFormat=xml
	 * 
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param repository "media", "scripts", or "datatables"
	 * @return
	 * @throws IOException
	 * @throws ServletException
	 */	
	public ClientResponse getFromRepository(final String url,
			final String accessId, final String secretKey, final String repository) throws IOException,
			ServletException {
		
		//setup the REST-Client
		WebResource service = createService(
				url, accessId, secretKey,
				new String[]{"repositories", repository},
				"list")
			.queryParam("responseFormat", "xml");
					
		ClientResponse perfectoResponse = service.get(ClientResponse.class);

		return perfectoResponse;
	}

	public ClientResponse uploadFile(final String url,
			final String accessId, 
			final String secretKey, 
			final String repository,
			final @PathParam("repositoryItemKey") String repositoryItemKey,
			final FilePath file,
			boolean binaryOtherwiseTextFile, 
			final PrintStream logger,
			final boolean displayUploadToLog
			) throws Exception { //URISyntaxException, IOException { throws IOException, ServletException
		
		if (displayUploadToLog)
			logger.println( Messages.LogInfo_UploadingFile(file.getRemote(), repository, repositoryItemKey));

		//setup the REST-Client
		WebResource service = createService(
				url, accessId, secretKey,
				new String[]{"repositories", repository, repositoryItemKey},
				"upload")
			.queryParam("overwrite", "true");
					
		File file2upload = new File(file.getRemote());		
		
		ClientResponse clientResponse = service
				.entity(file2upload,
				binaryOtherwiseTextFile 
					? MediaType.APPLICATION_OCTET_STREAM_TYPE 
					: MediaType.valueOf(Constants.TEXTUAL_CONTENT_TYPE))
				.post(ClientResponse.class);
		
		return clientResponse;
	}
}
