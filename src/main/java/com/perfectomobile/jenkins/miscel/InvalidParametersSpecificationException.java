package com.perfectomobile.jenkins.miscel;

@SuppressWarnings("serial")
public class InvalidParametersSpecificationException extends Exception {

	private String _fileName;

	public InvalidParametersSpecificationException(String string) {
		super(string);
	}

	public void setFileName(String fileName) {
		_fileName = fileName;
	}
	@Override
	public String getMessage() {
		String ret = super.getMessage();
		if (_fileName != null)
			ret += " for file "+_fileName;
		return ret;
	}

}
