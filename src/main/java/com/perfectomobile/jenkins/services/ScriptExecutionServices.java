package com.perfectomobile.jenkins.services;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.console.HyperlinkNote;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;

import org.json.simple.parser.ParseException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.Messages;
import com.perfectomobile.jenkins.MobileCloudGlobalConfigurationData;
import com.perfectomobile.jenkins.miscel.InvalidParametersSpecificationException;
import com.perfectomobile.jenkins.miscel.ScriptParamsParsedResults;
import com.perfectomobile.jenkins.miscel.UploadFile;
import com.perfectomobile.jenkins.utils.GeneralUtils;
import com.perfectomobile.jenkins.utils.JsonParsingUtils;
import com.perfectomobile.jenkins.utils.XmlParsingUtils;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * PM services during the execution of the script.
 * 
 * @author Guy Michaelis
 * 
 */
public class ScriptExecutionServices {
	private static final String SECURED_VALUE = "true";
	public static final int JOB_STATUS_RUNNING = 1;
	public static final int JOB_STATUS_FAILED = 2;
	public static final int JOB_STATUS_SUCCESS = 3;

	public static final Map<String,String> internal2externalType = initMapOfDisplayableTypes();
	private static final String STRING_PARAM_INTERNAL_TYPE = "string";
	private static final String SECURED_STRING_PARAM_INTERNAL_TYPE = "sec-" + STRING_PARAM_INTERNAL_TYPE; //sec - could be anythign - it doesn't matter = internal use for this method 

	@SuppressWarnings("serial")
	public class TechnicalException extends Exception {
		public TechnicalException(Exception e) { super(e); }
		@Override public String getMessage() {
			return Messages.LogError_FailedTechnicalException(getCause().getMessage()); 
		}
	}
	@SuppressWarnings("serial")
	public class ApplicationException extends Exception {
		private String _error;
		public ApplicationException(ClientResponse clientResponse) {
			_error = getResponseErrorMsg(clientResponse);
		}
		@Override public String getMessage() {
			return getError(); 
		}
		/** for clients that want to produce their own message rather than the default getMessage() */
		public String getError() { return _error; }
	}
	
	private RestServices _restServices;

	private JsonParsingUtils _jsonParsingUtils;
	
	ScriptExecutionServices() { }
	/**
	 * this method creates a map where in each entry the key is an internal parameter type 
	 * and the value is how it's going to be displayed in the params text area 
	 * @return
	 */
	private static Map<String,String> initMapOfDisplayableTypes() {
		Map<String,String> ret = new HashMap<String,String> ();
		ret.put(STRING_PARAM_INTERNAL_TYPE, "String");
		ret.put(SECURED_STRING_PARAM_INTERNAL_TYPE, "Secured String");
		ret.put("handset", Constants.PARAM_TYPE_HANDSET);
		ret.put("integer", "Number");
		ret.put("media", Constants.PARAM_TYPE_MEDIA);
		ret.put("boolean", "Boolean");
		ret.put("table", Constants.PARAM_TYPE_DATATABLES);
		ret.put("url", "URL");

		return ret;		
	}
		
	/**
	 * Get job status according to manipulation on PM get execution status
	 * response.
	 * 
	 * @param perfectoResponse
	 * @param listener
	 * @return JOB_STATUS_RUNNING, JOB_STATUS_SUCCESS, or JOB_STATUS_FAILED
	 * @throws ParseException 
	 */
	public int getJobStatus(String perfectoResponse, BuildListener listener) throws ParseException {

		String executionStatus = null;
		String flowEndCode = null;
		int returnStatus = JOB_STATUS_RUNNING;

		//the next two lines may throw an exception
		executionStatus = _jsonParsingUtils.getElement( perfectoResponse, Constants.PM_RESPONSE_NODE_STATUS);
		debug(Constants.PM_RESPONSE_NODE_STATUS + "=" + executionStatus, listener.getLogger());
		
		if (Constants.PM_EXEC_STATUS_COMPLETED.equals(executionStatus)) {
			flowEndCode = _jsonParsingUtils.getElement(perfectoResponse, Constants.PM_RESPONSE_NODE_FLOW_END_CODE);
			debug( Constants.PM_RESPONSE_NODE_FLOW_END_CODE + "=" + flowEndCode, listener.getLogger());
			
			if (Constants.PM_EXEC_FLOW_END_CODE_SUCCESS.equals(flowEndCode)) {
				returnStatus = JOB_STATUS_SUCCESS;
			} else {
				returnStatus = JOB_STATUS_FAILED;
			}
		}

		return returnStatus;

	}

	/**
	 * Get script parameters from the file returned by PM cloud
	 * 
	 * @param inputFile
	 * @return Map of parameter name and parameter type.
	 */
	public Map<String, String> getScriptParameters(File inputFile) {

		Map<String, String> scriptParams = new LinkedHashMap<String, String>();

		String paramName, paramType, internalTypeName;

		NodeList nodeList = XmlParsingUtils.getNodeList(inputFile,
				XmlParsingUtils.PARAMETER_ELEMENT_NAME);

		// do this the old way, because nodeList is not iterable
		for (int itr = 0; itr < nodeList.getLength(); itr++) {

			// Get parameter element
			Node parameterNode = nodeList.item(itr);
			Element parameterElement = (Element) parameterNode;

			// Get data element which holds the parameter type
			NodeList dataList = parameterElement
					.getElementsByTagName(XmlParsingUtils.DATA_ELEMENT_NAME);
			Node dataNode = dataList.item(0);
			Element dataElement = (Element) dataNode;
			internalTypeName = dataElement.getAttribute(XmlParsingUtils.CLASS_ATT_NAME);
			internalTypeName = internalTypeName.substring(0, internalTypeName.length()-Constants.PARAM_TYPE_DATA_LENGTH);
			if (STRING_PARAM_INTERNAL_TYPE.equals(internalTypeName)) { //may be secuerd
				String secured = dataElement.getAttribute(XmlParsingUtils.SECURED_ATT_NAME);
				boolean boolSecured = secured != null && SECURED_VALUE.equals(secured);
				if (boolSecured)
					internalTypeName = SECURED_STRING_PARAM_INTERNAL_TYPE;
			}
			
			paramType = internal2externalType.get(internalTypeName);
			

			// Get name element which holds the parameter name
			NodeList nameList = dataElement
					.getElementsByTagName(XmlParsingUtils.NAME_ELEMENT_NAME);
			Node nameNode = nameList.item(0);
			Element nameElement = (Element) nameNode;
			paramName = nameElement.getTextContent();

			scriptParams.put(paramName, paramType);
		}

		return scriptParams;
	}
	

	/**
	 * Call PM to retrieve the execution report.
	 * 
	 * @param globalConfiguration
	 * @param build
	 * @param listener
	 * @param jsonExecutionResult
	 *            PM response in json format
	 * @return the execution report
	 * @throws InterruptedException
	 * @throws IOException
	 * @return true if the report was obtained successfully
	 */
	@SuppressWarnings("rawtypes")
	public boolean getExecutionReport(MobileCloudGlobalConfigurationData globalConfiguration,
			AbstractBuild build, BuildListener listener,
			String jsonExecutionResult) throws Exception {

		boolean success = false;
		String reportKey = null;
		FilePath report = null;
		ClientResponse perfectoResponse = null;
		PrintStream logger = listener.getLogger();

		reportKey = _jsonParsingUtils.getElement( jsonExecutionResult, Constants.PM_RESPONSE_NODE_REPORT_KEY);
		debug( Constants.PM_RESPONSE_NODE_REPORT_KEY + "=" + reportKey, logger);
		logger.println( Messages.LogInfo_GettingExecutionReport());

		try {
			perfectoResponse = _restServices.downloadExecutionReport(
				globalConfiguration.getHostUrl(),
				globalConfiguration.getAccessId(),
				globalConfiguration.getSecretKey(), 
				reportKey);		
		} catch(Exception e) {
			throw new TechnicalException(e);
		}
		//request completed w/o technical exceptions


		if (perfectoResponse.getStatus() == Constants.PM_RESPONSE_STATUS_SUCCESS) {
			//downloaded ok
			logger.println( Messages.LogInfo_GotExecutionReport());
			
			try {
				EnvVars envVars = new EnvVars();
				try {
					envVars = build.getEnvironment(listener); //amy throw the exception caught
				} catch (Exception e) {
					logger.println( Messages.Error_FailedToGetEnvVars(e.toString()));
					return false;
				}
				
				
				report = new FilePath(perfectoResponse.getEntity(File.class));
				reportKey = reportKey.replace(":", "_");
				FilePath reportPath = build.getWorkspace().child( reportKey + "."+Constants.REPORT_EXECUTION_DOWNLOAD_FORMAT);
					
				logger.println( HyperlinkNote.encodeTo(
					envVars.get("JOB_URL") + "/ws/" + reportKey + "."+Constants.REPORT_EXECUTION_DOWNLOAD_FORMAT, Messages.ScriptExecution_ShowReportLink()));
				reportPath.copyFrom(report);
				success = true;
			} catch (Exception e) {
				logger.println( Messages.LogError_FailedGotException( e.toString()));
			}

		} else {
			String error = getResponseErrorMsg(perfectoResponse);
			logger.println( Messages.LogError_FailedToGetExecutionReport(error));
		}

		return success;

	}

	/**
	 * stops on the first file that fails to upload and returns that name
	 * Call PM to upload files to the repository.
	 * 
	 * @param globalConfiguration
	 * @param build
	 * @param listener
	 * @param uploadFiles
	 *            files to upload
	 * @return null if all uploads completes successfully otherwise the name of the first file that failed to upload
	 */
	@SuppressWarnings("rawtypes")
	public void uploadFiles(MobileCloudGlobalConfigurationData globalConfiguration,
			AbstractBuild build, BuildListener listener,
			List<UploadFile> uploadFiles) throws Exception {

		// Print upload details
		for (UploadFile uploadFile : uploadFiles) {

			String repoItem = uploadFile.getRepositoryItemKey();
				
			FilePath filePath = build.getWorkspace().child(uploadFile.getFilePath());
			if (!filePath.exists())
				throw new InvalidParametersSpecificationException(
					Messages.Error_FileToUploadMissing(uploadFile.getFilePath()));
			
			
			// Call PM to upload the files
			//the call below may throw an exception that will cause the method to abort
			uploadFile(
					globalConfiguration.getHostUrl(),
					globalConfiguration.getAccessId(),
					globalConfiguration.getSecretKey(),
					uploadFile.getRepository(),
					repoItem,
					filePath,
					listener.getLogger(),
					true); //true means display a log msg for each upload			
		}

	}

	
	
	/**
	 * Uploads the item specified by repositoryItemKey to the repository area specified by repository. 
	 * 
	 * Request/Response Example: 
	 * 
	 * -Request:
	 * https://www.perfectomobile.com/services/repositories/<media or datatables>/<PRIVATE:/myapps/TestApp.apk>?
	 * operation=upload&user=value&password=value&overwrite=true
	 * 
	 * -Response: {"executionId":
	 * {"status":"success"}
	 * @param binaryOtherwiseTextFile 
	 * 
	 * @see http://help.perfectomobile.com/article/AA-00311/53/Guides-Documentation/HTTP-API/Operations/Repository-Operations/02.-Upload-Item-to-Repository.html
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param repository
	 * @param repositoryItemKey
	 * @throws Exception
	 */
	public void uploadFile(final String url,
			final String accessId, 
			final String secretKey, 
			final String repository,
			final @PathParam("repositoryItemKey") String repositoryItemKey,
			final FilePath file,
			final PrintStream logger,
			final boolean displayUploadToLog
			) throws Exception { //URISyntaxException, IOException {
		
		ClientResponse clientResponse = null; 
		try {
			clientResponse = _restServices.uploadFile(url, accessId, secretKey, repository, repositoryItemKey, file, 
				Constants.MEDIA_REPOSITORY.equals(repository), logger, displayUploadToLog);
		} catch(Exception e) {
			throw new TechnicalException(e);
		}
		//request completed w/o technical exceptions
		int statusCode = clientResponse.getStatus();
		if (statusCode != Constants.PM_RESPONSE_STATUS_SUCCESS) {
			throw new ApplicationException(clientResponse);
		}
	}

	/**
	 * This method gets parameters as they appears in Jenkins textarea field and
	 * returns an object containing a Map object that holds parameter name and parameter value, and potentially an uploadFilesInfo object
	 * it is called either before performing script execution, or to obtain the Map of key-values for initializing the parameters list as a result of
	 * the user pressing the Refresh Parameter List button in the step configuration GUI. this is specified by the boolean parameter forScriptExecution
	 * @param optionalParameters
	 * @param forScriptExecution true means called for script execution, 
	 * 			and for checking the text area (in the later case we are just interested exceptions or in the warnings in the returned object, 
	 * 		false indicates just for refreshing the param list text
	 * 
	 * this is what the method will do depending on forScriptExecution:
	 *  	forScriptExecution = true										forScriptExecution = false
	 *      ----------------------------------------------------			----------------------------------------------------
	 * 1) the key in the map will be the param name prefixed by "param."  	the key will be the param name but will also have the type
	 * 																		so each keys would be of the form <paramName>(<param type>)
	 * 2) throw an exception if syntax error is detected 					ignore the line if it contains syntax error
	 * 3) check the value of media and data tables for errors				don't check the value of these types
	 * 4) param for media and datatable the value in the map would 			the value would be everything typed by the user even if it contains errors
	 * 		just be the repository key 
	 * 5) return uploadInfo	list												return empty  uploadInfo list
	 */
	public ScriptParamsParsedResults parseScriptsParamsList( String optionalParameters, boolean forScriptExecution) 
			throws InvalidParametersSpecificationException {

		StringBuffer paramsContainsHandsetParam = new StringBuffer();
		//defaults for when forScriptExecution == true
		String paramNamePrefix = Constants.PM_EXEC_PARAMETER_PREFIX;
		boolean getKeyWithType = false, 
				skipIfErrorsOtherwiseThrowException = false; 
		if (!forScriptExecution) {
			paramNamePrefix = "";
			getKeyWithType = skipIfErrorsOtherwiseThrowException = true;
		}

		MultivaluedMap<String, String> scriptParams = new MultivaluedMapImpl();
		List<UploadFile> uploadInfo = new ArrayList<UploadFile>();
		

		String paramValue;
		String errorMsg = null;
		int lineNum=0;
		String warnings = "";
		int warnNum = 0;

		if (!optionalParameters.trim().isEmpty()) {
			// Split to lines
			StringTokenizer stParameters = new StringTokenizer(optionalParameters, System.getProperty("line.separator"));
			while( (errorMsg == null || skipIfErrorsOtherwiseThrowException) && stParameters.hasMoreTokens()) {
				++lineNum;
				String parameterKeyValueLine = stParameters.nextToken(); //parameter holds the whole line
				if (!parameterKeyValueLine.trim().isEmpty()) { //skip empty lines
					// Split a line. get the name and the value.
					int nameValSepPos = parameterKeyValueLine.indexOf(Constants.PARAM_NAME_VALUE_SEPARATOR);
					if (nameValSepPos < 0) { 
						errorMsg = Messages.Error_ParameterNameValueSeparatorMissing(Constants.PARAM_NAME_VALUE_SEPARATOR);
						continue;
					}
					if (nameValSepPos == 0) { 
						errorMsg = Messages.Error_ParameterNameMissing();
						continue;
					}
					if (nameValSepPos == parameterKeyValueLine.length() - Constants.PARAM_NAME_VALUE_SEPARATOR.length()) { 
						errorMsg = Messages.Error_ParameterValueMissing( parameterKeyValueLine.substring(0,nameValSepPos));
						continue;
					}
					
					String paramWithType = parameterKeyValueLine.substring(0,nameValSepPos).trim();
					
					//now parse the param name and type 
					int paramTypeBeginPos = paramWithType.indexOf(Constants.PARAM_TYPE_START_TAG);
					if (paramTypeBeginPos < 0) { 
						errorMsg = Messages.Error_ParamterLeftParenMissing(Constants.PARAM_TYPE_START_TAG, paramWithType);
						continue;
					}
					if (paramTypeBeginPos == 0) { 
						errorMsg = Messages.Error_ParamterNameMissing(paramWithType);
						continue;
					}
					if (paramTypeBeginPos == paramWithType.length() - Constants.PARAM_TYPE_START_TAG.length()) { 
						errorMsg = Messages.Error_ParamterTypeMissing(paramWithType);
						continue;
					}
					
					String paramName = paramNamePrefix + paramWithType.substring(0,paramTypeBeginPos).trim(); //add the desired prefix

					int paramTypeEndPos = paramWithType.indexOf(Constants.PARAM_TYPE_END_TAG);
					if (paramTypeEndPos < 0) { 
						errorMsg = Messages.Error_ParamterRightParenMissing(Constants.PARAM_TYPE_END_TAG,paramWithType);
						continue;
					}
					if (paramTypeEndPos == 0) { 
						errorMsg = Messages.Error_ParamterNameAndTypeMissing();
						continue;
					}
					if (paramTypeEndPos < paramTypeBeginPos) { 
						errorMsg = Messages.Error_ParamterParenthesisWrongOrder(paramWithType);
						continue;
					}
										
					String parameType = paramWithType.substring(paramTypeBeginPos + Constants.PARAM_TYPE_START_TAG.length(), paramTypeEndPos).trim();
					if (parameType.isEmpty()) {
						errorMsg = Messages.Error_ParamterTypeMissing(paramWithType);
						continue;
					}
					if (Constants.PARAM_TYPE_HANDSET.equals(parameType)) {
						if (paramsContainsHandsetParam.length() >= 1)
							paramsContainsHandsetParam.append(", ");
						paramsContainsHandsetParam.append( Messages.Info_DeviceOnLine(paramWithType,lineNum));
					}
					
					if (paramWithType.substring(paramTypeEndPos+Constants.PARAM_TYPE_END_TAG.length()).length() > 0) { //shouldn't have any more token just the name and type to the left of the equal sign
						errorMsg = Messages.Error_ParamterInvalidSyntaxAfterNameAndType(paramWithType);
						continue;
					}
					
					
					paramValue = parameterKeyValueLine.substring(nameValSepPos + Constants.PARAM_NAME_VALUE_SEPARATOR.length());
					if (!parameType.equals(Constants.PARAM_TYPE_STRING))
						paramValue = paramValue.trim();
					//In case it is a file. need to separate the repositorykey from the filepath.
					if (parameType.equals(Constants.PARAM_TYPE_MEDIA) || parameType.equals(Constants.PARAM_TYPE_DATATABLES)) {
						RepositoryItemsGetter repositoryItemsGetter = MobileCloudServicesFactory.getInstance().getRepositoryItemsGetter();
						int sepPos = paramValue.indexOf(Constants.PARAM_REPOSITORYKEY_FILEPATH_SEPARATOR);
						if (forScriptExecution) {
							boolean localFileSpecified = true; //assume
						
							if (sepPos < 0) {
								localFileSpecified = false;
								sepPos = paramValue.length();	
							} else if (sepPos == paramValue.length()-Constants.PARAM_REPOSITORYKEY_FILEPATH_SEPARATOR.length()) 
								localFileSpecified = false;
						
							String repositoryKey = paramValue.substring(0, sepPos).trim();
							if ( null != (errorMsg = repositoryItemsGetter.checkRepositoryItem(repositoryKey,localFileSpecified))) { //2nd param is supposed to specify whether the repo key may be a folder
								errorMsg = Messages.Error_ParamterErrorFor(errorMsg, paramWithType);
								continue;
							}
							
							if (localFileSpecified) {
								String localFile = paramValue.substring(sepPos + Constants.PARAM_REPOSITORYKEY_FILEPATH_SEPARATOR.length()).trim();
								if (null != (errorMsg = GeneralUtils.checkNonEmptyFileToUpload(localFile))) //local file must have extension - the check returns null if ok
									continue;
								
								UploadFile fileUpload = new UploadFile( 
									Constants.PARAM_TYPE_DATATABLES.equals(parameType) 
										? Constants.DATATABLES_REPOSITORY 
										: Constants.MEDIA_REPOSITORY,
									localFile,repositoryKey);
								uploadInfo.add( fileUpload);
								paramValue = fileUpload.getRepositoryItemKey(); //note that the getter will return a combined folder + file name if the repo item was a folder
							} else //local file not specified
								paramValue = repositoryKey;
							
							boolean repositoryKeyExistsAlready = repositoryItemsGetter.isFoundInRepository(
									repositoryKey,
									parameType.equals(Constants.PARAM_TYPE_MEDIA) ? Constants.MEDIA_REPOSITORY : Constants.DATATABLES_REPOSITORY);
							if (repositoryKeyExistsAlready) {
								if (localFileSpecified)
									warnings += Messages.Warning_RepositoryFileWillBeOverwritten( ++warnNum, paramWithType, lineNum) + " ";
							} else if (!localFileSpecified)
								warnings += Messages.Warning_NothingInRepositoryMatchesParameter( ++warnNum, paramWithType,lineNum) + " ";									
						}
							
					} 
					
					if (getKeyWithType) 
						paramName = paramWithType;
					scriptParams.add(paramName, paramValue);
				} 
			} //end of for
		}
		if (errorMsg != null && !skipIfErrorsOtherwiseThrowException) {
			throw new InvalidParametersSpecificationException(
				Messages.Error_ErrorDetectedOnLine( errorMsg,lineNum));
		}
	
		if (paramsContainsHandsetParam.length() >= 1)
			warnings += Messages.Warning_FinalRecommendation(++warnNum,paramsContainsHandsetParam.toString());
		
		if (warnings.length() >= 1)
			warnings = Messages.Warning_Warnings(warnings);

		return new ScriptParamsParsedResults(scriptParams,uploadInfo, warnNum >= 1 ? warnings : null);
	}

	private void debug(String string, PrintStream logger){
		GeneralUtils.debug(string, logger);
	}
	public void setRestServices(RestServices restServices) {
		_restServices = restServices;
	}
	
	public ClientResponse executeScript(MobileCloudGlobalConfigurationData mobileCloudglobalConfigurationData, String scriptName,
			MultivaluedMap<String, String> scriptParams,
			List<UploadFile> uploadList, PrintStream logger) throws TechnicalException {
		
		try {
			return _restServices.executeScript(mobileCloudglobalConfigurationData, scriptName, scriptParams, uploadList, logger);
		} catch (Exception e) {
			throw new TechnicalException(e);
		}
	}
	public ClientResponse getExecutionStatus(MobileCloudGlobalConfigurationData mobileCloudglobalConfigurationData, 
			String executionId) throws TechnicalException {
		try {
			return _restServices.getExecutionStatus(mobileCloudglobalConfigurationData, executionId);
		} catch (Exception e) {
			throw new TechnicalException(e);
		}
	}
	public ClientResponse getScriptItems(String hostUrl, String accessId,
			String secretKey, String script) throws IOException, ServletException {
		return _restServices.getRepoScriptsItems(hostUrl, accessId, secretKey, script);
	}
	public void setJsonParsingUtils(JsonParsingUtils jsonParsingUtils) {
		_jsonParsingUtils = jsonParsingUtils;
	}
	public String extractExecutionResult(ClientResponse perfectoResponse) {
		return perfectoResponse.getEntity(String.class);
	}
	
	public String extractExecutionIdFromResult(String result) throws ParseException {
		return _jsonParsingUtils.getElement(result, Constants.PM_RESPONSE_NODE_EXEC_ID);
	}
	public String getResponseErrorMsg(ClientResponse perfectoResponse) {
		String ret = null;
		if (perfectoResponse.getStatus() == Constants.SERVICE_UNAVAILABLE)
			ret = Messages.LogError_ServiceUnavailable();
		else 
			try {
				String responseStr = perfectoResponse.getEntity(String.class);
				ret = getErrorFromResp(responseStr);
			} catch (Exception e) {
				e.printStackTrace();
				ret = Messages.LogError_ServiceUnavailable();
			} 
		return ret;
	}
	private String getErrorFromResp(String responseStr) throws ParseException {
		return "\"" + _jsonParsingUtils.getElement( responseStr, Constants.PM_RESPONSE_NODE_ERRONEOUS_MSG_KEY) + "\"";
	}
}
