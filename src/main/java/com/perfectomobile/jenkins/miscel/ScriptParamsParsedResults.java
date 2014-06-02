package com.perfectomobile.jenkins.miscel;

import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

public class ScriptParamsParsedResults {

	private MultivaluedMap<String, String> _scriptParams;
	private List<UploadFile> _uploadInfo;
	private String _warnings;

	public ScriptParamsParsedResults(
			MultivaluedMap<String, String> scriptParams,
			List<UploadFile> uploadInfo, String warnings) {
		_scriptParams = scriptParams;
		_uploadInfo = uploadInfo;
		_warnings = warnings;
	}

	public MultivaluedMap<String, String> getScriptParams() {
		return _scriptParams;
	}

	/**
	 * @return map where keys are local file name and value is the repository key
	 */
	public List<UploadFile> getUploadInfo() {
		return _uploadInfo;
	}

	public String getWarnings() {
		return _warnings;
	}
}
