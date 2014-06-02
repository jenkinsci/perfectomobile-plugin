package com.perfectomobile.jenkins;



public class Constants {
	
	
	public static final int EXEC_STATUS_WAIT_TIME_IN_SECONDS=10;
	public static final int PM_RESPONSE_STATUS_SUCCESS=200;
	
	public static final String PM_EXEC_STATUS_COMPLETED="Completed";
	public static final String PM_EXEC_FLOW_END_CODE_SUCCESS="Success";
	
	//PM Response nodes
	public static final String PM_RESPONSE_NODE_STATUS="status";
	public static final String PM_RESPONSE_NODE_FLOW_END_CODE="flowEndCode";
	public static final String PM_RESPONSE_NODE_EXEC_ID="executionId";
	public static final String PM_RESPONSE_NODE_REPORT_KEY="reportKey";
	public static final String PM_RESPONSE_NODE_ERRONEOUS_MSG_KEY="errorMessage";
	
	public static final String PM_EXEC_PARAMETER_PREFIX = "param.";
	
	
	
	public static final String PARAM_TYPE_START_TAG = "(";
	public static final String PARAM_TYPE_END_TAG = ")";
	public static final String PARAM_NAME_VALUE_SEPARATOR = "=";
	public static final String PARAM_REPOSITORYKEY_FILEPATH_SEPARATOR = ";";
	public static final String PARAM_REPOSITORYKEY_FOLDER_SEPARATOR = "/";
		
	public static final String PARAM_TYPE_STRING = "String";
	public static final String PARAM_TYPE_HANDSET = "Device";
	public static final String PARAM_TYPE_MEDIA = "Media";
	public static final String PARAM_TYPE_DATATABLES = "DataTable";
	
	public static final int PARAM_TYPE_DATA_LENGTH = 5;
	
	public static final String SCRIPT_FILE_EXT = ".xml";
	public static final String HTML_EXT = ".html";
	
	public static final String PM_DEBUG_MODE="pmDebug";
	public static final String ACCESSING_PROTOCOL = "https://";

	public static final String DEVICE_INFO_SEPARATOR = "-";
	public static final String PROTOCOL_SEPARATOR = ":";
	public static final String WINDOWS_PATH_SEP = "\\";
	public static final String UNIX_PATH_SEP = "/";
	public static final String FILE_EXT_SEPARATOR = ".";
	public static final String TEXTUAL_CONTENT_TYPE = "text/xml;charset=UTF-8";
	public static final String TEMP_FOLDER_FOR_ARTIFACT_UPLOAD = "_4upload";
	public static final String ARCHIVE_FOLDER = "/archive/"; 
	public static final String MSG_ELEMENT_BEGINNING_FOR_FAILED_TO_GET_DEVICE_LIST = "\"Failed to list device items - ";
	public static final String SCRIPTS_REPOSITORY = "scripts";
	public static final String MEDIA_REPOSITORY = "media";
	public static final String DATATABLES_REPOSITORY = "datatables";
	public static final String REPORT_EXECUTION_DOWNLOAD_FORMAT = "pdf";
	public static final int SERVICE_UNAVAILABLE = 503; //status code 
}
