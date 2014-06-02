package com.perfectomobile.jenkins;

import hudson.Extension; 
import hudson.Launcher;
import hudson.model.AutoCompletionCandidates;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.ServletException;
import javax.ws.rs.core.MultivaluedMap;

import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import com.perfectomobile.jenkins.miscel.InvalidParametersSpecificationException;
import com.perfectomobile.jenkins.miscel.ScriptParamsParsedResults;
import com.perfectomobile.jenkins.miscel.StringListAccumulator;
import com.perfectomobile.jenkins.services.MobileCloudServicesFactory;
import com.perfectomobile.jenkins.services.ScriptExecutionServices;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Sample {@link Builder}.
 * 
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link ScriptExecutionBuilder} is created. The created instance is persisted
 * to the project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #name}) to remember the configuration.
 * 
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
 * 
 * @author Guy Michaelis
 */
public class ScriptExecutionBuilder extends MobileCloudBuilder {
	
	
	//Note: the instance vars cannot have the '_' prefix as jenkins looks for the names by reflection
	
	private final String id;
	private final String autoScript;
	private final String scriptParams;
	
	// Fields in config.jelly must match the parameter names in the
	// "DataBoundConstructor"
	@DataBoundConstructor
	public ScriptExecutionBuilder(
			//String name, //this is not needed for anything 
			//String perfectoCloud, 
			String autoScript, String scriptParams, 
			//String autoDatatable, String autoLocalFile, //removed per NP-15359
			String id, String autoMedia) {
		super(	//name, perfectoCloud 
				autoMedia);
		this.id = id;
		this.autoScript = autoScript;
		this.scriptParams = scriptParams;
		//this.autoMedia = autoMedia;
		/* //removed per NP-15359
		this.autoDatatable = autoDatatable;
		this.autoLocalFile = autoLocalFile;
		*/				
	}
	
	public String getId() {
		return id;
	}

	public String getAutoScript() {
		return autoScript;
	}

	public String getScriptParams() { return scriptParams; } 

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		boolean success = false; //assume failure

		logBeginingStep(listener, autoScript);
		
		ClientResponse perfectoResponse = null;
		String jsonExecutionStatusResult = null;
		int jobStatus = ScriptExecutionServices.JOB_STATUS_RUNNING;
		
		MobileCloudServicesFactory mobileCloudServicesFactory = MobileCloudServicesFactory.getInstance(); 
		ScriptExecutionServices scriptExecutionServices = mobileCloudServicesFactory.getScriptExecutionServices(); 
		
		try {
			PrintStream logger = listener.getLogger();
			debug(Messages.LogInfo_Parsing(),logger);
			ScriptParamsParsedResults paramsListParsedInfo = scriptExecutionServices.parseScriptsParamsList(scriptParams,true);
			
			//Call PM to upload files into repository
			scriptExecutionServices.uploadFiles(getDescriptor(), build, listener, paramsListParsedInfo.getUploadInfo());
			
			listener.getLogger().println(Messages.LogInfo_AttemptingToExecute());

			//Call PM to execute the script
			perfectoResponse = scriptExecutionServices.executeScript(getDescriptor(), 
					autoScript + Constants.SCRIPT_FILE_EXT, paramsListParsedInfo.getScriptParams(),
					paramsListParsedInfo.getUploadInfo(), logger);
			
			
			int statusCode = perfectoResponse.getStatus();
			if (statusCode == Constants.PM_RESPONSE_STATUS_SUCCESS) {
				listener.getLogger().println(Messages.LogInfo_ScriptExecuting());
				
				String jsonExecutionResult = scriptExecutionServices.extractExecutionResult(perfectoResponse);
				debug(jsonExecutionResult, listener.getLogger());

				String executionId = scriptExecutionServices.extractExecutionIdFromResult(jsonExecutionResult);

				debug("executionId=" + executionId, logger);

				// Check execution status
				listener.getLogger().println(Messages.LogInfo_GettingCurrentScriptExecutionStatus());
				success = true;
				while (jobStatus == ScriptExecutionServices.JOB_STATUS_RUNNING && success) {

					debug("Getting current script execution status", listener.getLogger());

					//Call PM to get status
					perfectoResponse = scriptExecutionServices.getExecutionStatus(
							getDescriptor(), executionId);
					if (perfectoResponse.getStatus() == Constants.PM_RESPONSE_STATUS_SUCCESS) {
						jsonExecutionStatusResult = perfectoResponse.getEntity(String.class);
						
						try {
							//Get Job status according to PM logic.
							jobStatus = scriptExecutionServices.getJobStatus( jsonExecutionStatusResult, listener);
						} catch( Exception pe) {
							String str = pe.getMessage();
							throw new Exception(Messages.LogError_CouldNotGetValidJobStatus() + (str==null ? "" : ": " + str));
						}

						// Wait if status is not completed
						if (jobStatus == ScriptExecutionServices.JOB_STATUS_RUNNING) {
							debug("Script is not done executing...", listener.getLogger());
							Thread.sleep(Constants.EXEC_STATUS_WAIT_TIME_IN_SECONDS * 1000);
						}
					} else {
						processErrorResponse(listener, perfectoResponse, scriptExecutionServices, Messages.LogError_CouldNotGetValidJobStatus()); //logs the error
						success = false;
					}
				} //end of while
				//potentially can display a msg if the execution finished, whether the script executed fine or not (failed if jobStatus == ScriptExecutionServices.JOB_STATUS_FAILED					
				
			} else {
				processErrorResponse(listener, perfectoResponse, scriptExecutionServices,Messages.LogError_FailedToLaunchScriptExecution()); //logs the error
			}
		} catch( Exception e) {
			listener.getLogger().println( e.getMessage()); //Build step aborted
		} 

		if (success) { //not failed already
			listener.getLogger().println(Messages.LogInfo_ScriptExecuted());
			//Call PM to get report even if job fail
			try {
				success = scriptExecutionServices.getExecutionReport(getDescriptor(), build, listener, jsonExecutionStatusResult);
			} catch (Exception e) {
				listener.getLogger().println( Messages.Error_FailedToGetExecutionReport(e.toString()));
				success = false;
			}
		}
		if (jobStatus == ScriptExecutionServices.JOB_STATUS_FAILED)
			success = false;
		return success;
	}

	private void processErrorResponse(BuildListener listener, ClientResponse perfectoResponse,
			ScriptExecutionServices scriptExecutionServices, String attemptMsg) {
		
		String error = scriptExecutionServices.getResponseErrorMsg(perfectoResponse);
		listener.getLogger().println( Messages.LogError_Msg(attemptMsg + (error == null ? " " : ": "+ error)));
	}
	
	//------------------------------------------------------------------------------------

	/**
	 * Descriptor for {@link ScriptExecutionBuilder}. Used as a singleton. The
	 * class is marked as public so that it can be accessed from views.
	 * 
	 * <p>
	 * See
	 * <tt>src/main/resources/com/perfectomobile/perfectomobilejenkins/PerfectoMobileBuilder/*.jelly</tt>
	 * for the actual HTML fragment for the configuration screen.
	 */
	@Extension //This indicates to Jenkins that this is an implementation of an extension point.
	public static class DescriptorImpl extends MobileCloudStepConfiguration {
		private static final String DEVICES = "devices";
		
		
		public DescriptorImpl() {
			System.out.println("ScriptExecutionBuilder.DescriptorImpl()");
			MobileCloudServicesFactory.getInstance().setGlobalConfigurationData(this);
		}
		
		/**
		 * This human readable name is used in the configuration screen.getName
		 */
		public String getDisplayName() { return Messages.ScriptExecutionBuilder_BuildStepName(); }
		
		/**
		 * Performs on-the-fly validation of the form field 'name'.
		 * 
		 * @param value
		 *            This parameter receives the value that the user has typed.
		 * @return Indicates the outcome of the validation. This is sent to the
		 *         browser.
		 */
		public FormValidation doCheckScriptParams(@QueryParameter String value)  //NP-15359
				throws IOException, ServletException {
			
			return FormValidation.ok();
		}
		public FormValidation doCheckScriptParamsPrevious(@QueryParameter String value)  //NP-15359
				throws IOException, ServletException {
			String err = null;
			
			String warnings = null;
			try {
				ScriptParamsParsedResults results = null;	
				if (!value.isEmpty()) {
					results = MobileCloudServicesFactory.getInstance().getScriptExecutionServices().parseScriptsParamsList(value,true);
					warnings = results.getWarnings();
				}
			} catch (InvalidParametersSpecificationException e) {
				err = e.getMessage();
			}
			return err == null 
				? ( warnings == null ? FormValidation.ok() : FormValidation.warning(warnings)) 
				:  FormValidation.error(err);
		}		
		
		public ListBoxModel doFillDeviceIdItems() {
			@SuppressWarnings("serial")
			final class MyListBoxModel extends ListBoxModel implements StringListAccumulator {
				public void addString(String string) { add(string); }
			}
			MyListBoxModel items = new MyListBoxModel();
			boolean result = MobileCloudServicesFactory.getInstance().getDeviceListGetter().populateDevices(items, getHostUrl(), getAccessId(), getSecretKey());
			if (!result) //failed
				items.add(Messages.UiError_CantGetItems(DEVICES));
			else if (items.isEmpty())
				items.add(Messages.UiInfo_NoDevicesAvailable());
			return items;
		}

		/**
		 * This method is called when the user press on "Parameter list" button.
		 * @param req
		 * @param rsp
		 * @throws ServletException
		 * @throws IOException
		 */
		public void doGetParameters(StaplerRequest req, StaplerResponse rsp)
				throws ServletException, IOException {

			String targetClass = getId();
            String retVal = null;

            JSONObject builder = obtainBuilderForThisStep(req, targetClass);
            
            String autoScriptJson =  builder.getString("autoScript");
            String curParams = builder.getString("scriptParams");
			MultivaluedMap<String, String> mapOfCurVals = null; 
			try {
				//the call below creates a map where each key is <param name>(<displayed type>) and the value is the current value in the text area
				ScriptParamsParsedResults parsedParamsListInfo = MobileCloudServicesFactory.getInstance().getScriptExecutionServices().
					parseScriptsParamsList(curParams,false);
				mapOfCurVals = parsedParamsListInfo.getScriptParams();
			} catch (InvalidParametersSpecificationException e) {
				//exception should not thrown because the invocation above specifies not to throw exception but to just ignore errors
				e.printStackTrace();
			}
				
			retVal = getParameters(autoScriptJson.toString(),mapOfCurVals);
			
			rsp.getWriter().append(retVal);	
		}

		/**
		 * @param autoScript
		 * @param mapOfCurVals contains a map of the current values, key is <param name>(<param type>)
		 * @return
		 */
		public String getParameters(String autoScript, MultivaluedMap<String, String> mapOfCurVals) {

			ClientResponse perfectoResponse = null;
			StringBuffer returnParameters = new StringBuffer();
			
				try {
					ScriptExecutionServices scriptExecutionServices = MobileCloudServicesFactory.getInstance().getScriptExecutionServices();
					perfectoResponse = scriptExecutionServices.getScriptItems(
						getHostUrl(), getAccessId(), getSecretKey(), autoScript + Constants.SCRIPT_FILE_EXT);

					if (perfectoResponse.getStatus() == Constants.PM_RESPONSE_STATUS_SUCCESS) {
						File responseXml = perfectoResponse.getEntity(File.class);

						Map <String, String> parametersMap = scriptExecutionServices.getScriptParameters(responseXml);

						if (!parametersMap.isEmpty()) {
							Set<Entry<String, String>> parameters = parametersMap.entrySet();
							Iterator<Entry<String, String>> iterator = parameters.iterator();
							while (iterator.hasNext()) {
								Entry<String, String> nextParam = iterator.next();
								String paramName;
								String paramType;
								returnParameters
										.append(paramName=nextParam.getKey())
										.append(Constants.PARAM_TYPE_START_TAG)
										.append(paramType = nextParam.getValue())
										.append(Constants.PARAM_TYPE_END_TAG)
										.append(Constants.PARAM_NAME_VALUE_SEPARATOR)
										.append(getNonEmptyCurValue(paramName,paramType,mapOfCurVals)) //previous value or empty string if none 
										.append(System
												.getProperty("line.separator"));
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			return returnParameters.toString();
		}
		private String getNonEmptyCurValue(String paramName, String paramType,
				MultivaluedMap<String, String> mapOfCurVals) {
			String mapKey = paramName + Constants.PARAM_TYPE_START_TAG + paramType + Constants.PARAM_TYPE_END_TAG;
			List<String> valList = mapOfCurVals.get(mapKey);
			String ret = valList == null || valList.isEmpty() 
					? (paramType.equals(Constants.PARAM_TYPE_MEDIA) || paramType.equals(Constants.PARAM_TYPE_DATATABLES)
							? Constants.PARAM_REPOSITORYKEY_FILEPATH_SEPARATOR
							: "") 
					: valList.get(0);
			return ret;
		}
		
		public AutoCompletionCandidates doAutoCompleteAutoScript( @QueryParameter String value) {
			return doAutoCompleteRepositoryItems(value,Constants.SCRIPTS_REPOSITORY);
		}
		
	}
	
}