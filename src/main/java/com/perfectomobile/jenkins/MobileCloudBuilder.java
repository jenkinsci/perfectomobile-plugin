package com.perfectomobile.jenkins;

import java.io.PrintStream;

import com.perfectomobile.jenkins.utils.GeneralUtils;

import hudson.model.BuildListener;
import hudson.tasks.Builder;

public abstract class MobileCloudBuilder extends Builder {
	//Note: the instance vars cannot have the '_' prefix as jenkins looks for the names by reflection
	
	private final String autoMedia;
	
	
	public MobileCloudBuilder( //String name, String perfectoCloud, 
			String autoMedia) {
		
		this.autoMedia = autoMedia;
	}
		
	
	public String getAutoMedia() {
		return autoMedia;
	}

	@Override
	public MobileCloudStepConfiguration getDescriptor() {	
		return (MobileCloudStepConfiguration) super.getDescriptor();
	}

	protected void logBeginingStep(BuildListener listener, String string) {
		listener.getLogger().println(Messages.LogInfo_StepBeginning(getDescriptor().getDisplayName(),string));
	}

	protected void debug(String msg, PrintStream logger) {
		GeneralUtils.debug(msg, logger);
	}

}
