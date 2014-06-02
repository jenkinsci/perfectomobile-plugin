package com.perfectomobile.jenkins.miscel;

import com.perfectomobile.jenkins.services.RestServices;

public abstract class InfoGetter {
	private boolean _accessDenied;
	private RestServices _restServices;
	
	protected boolean isAccessDenied() { return _accessDenied; }
	protected void setAccessDenied(boolean accessDenied) { _accessDenied = accessDenied; }
	public void doReset() { _accessDenied = false; }

	public void setRestServices(RestServices restServices) {
		_restServices = restServices;
	}
	protected RestServices getRestServices() {
		return _restServices;
	}
}
