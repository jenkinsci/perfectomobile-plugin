package com.perfectomobile.jenkins.services;

import hudson.FilePath;

import java.io.PrintStream;

public class MyScriptExecutionServices extends ScriptExecutionServices {
	private String _repositoryItemKey;
	private boolean _throwException;

	public void setThrowException(boolean throwException) {
		_throwException = throwException;
	}
	@Override
	public void uploadFile(String url, String accessId, String secretKey,
			String repository, String repositoryItemKey, FilePath file,
			PrintStream logger, boolean displayUploadToLog) throws Exception {
		_repositoryItemKey = repositoryItemKey;
		if (_throwException)
			throw new Exception("Blah");
	}

	public String getPassedRepositoryItemKey() {
		return _repositoryItemKey;
	}
	
}
