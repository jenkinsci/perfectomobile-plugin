package com.perfectomobile.jenkins.miscel;

public class RepositoryAccessFailureException extends Exception {
	private static final long serialVersionUID = 1L;
	private boolean accessDenied;

	public RepositoryAccessFailureException(boolean accessDenied) {
		super();
		this.accessDenied = accessDenied;
	}

	public boolean isAccessDenied() {
		return accessDenied;
	}
	
}
