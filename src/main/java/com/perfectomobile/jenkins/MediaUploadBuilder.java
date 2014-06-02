package com.perfectomobile.jenkins;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.Builder;

import java.util.LinkedList;
import java.util.List;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import com.perfectomobile.jenkins.miscel.InvalidParametersSpecificationException;
import com.perfectomobile.jenkins.miscel.UploadFile;
import com.perfectomobile.jenkins.services.MobileCloudServicesFactory;
import com.perfectomobile.jenkins.utils.GeneralUtils;


/**
 * Sample {@link Builder}.
 * 
 * <p>
 * When the user configures the project and enables this builder,
 * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new
 * {@link MediaUploadBuilder} is created. The created instance is persisted
 * to the project configuration XML by using XStream, so this allows you to use
 * instance fields (like {@link #name}) to remember the configuration.
 * 
 * <p>
 * When a build is performed, the
 * {@link #perform(AbstractBuild, Launcher, BuildListener)} method will be
 * invoked.
 * 
 */
public class MediaUploadBuilder extends MobileCloudBuilder {
	
	//Note: the instance vars cannot have the '_' prefix as jenkins looks for the names by reflection
	private final String file2upload;
	
	// Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
	@DataBoundConstructor
	public MediaUploadBuilder(
			String file2upload, 
			String autoMedia) {
		
		super( //name, perfectoCloud,
			autoMedia);
		this.file2upload = file2upload;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
		boolean success = false; //assume success
		String errMsg = null;

		logBeginingStep(listener, file2upload);
		
		MobileCloudServicesFactory mobileCloudServicesFactory = MobileCloudServicesFactory.getInstance();
		
		try {
			String fl2upload = null;
			String repoItem = getAutoMedia();
			repoItem = repoItem == null ? "" : repoItem.trim();
			if (file2upload == null || (fl2upload = file2upload.trim()).isEmpty()) 
				errMsg = Messages.LogError_TheLocalFileToUploadWasNotSpecified(); 
			else if (null == (errMsg = GeneralUtils.checkNonEmptyFileToUpload(fl2upload))
					&& null == (errMsg = mobileCloudServicesFactory.getRepositoryItemsGetter().checkRepositoryItem(repoItem,true))) { //repo item may be folder allowed  
				List<UploadFile> uploadFiles = new LinkedList<UploadFile>();
				uploadFiles.add(new UploadFile(Constants.MEDIA_REPOSITORY, fl2upload, repoItem));
				//Call PM to upload files into repository
				mobileCloudServicesFactory.getScriptExecutionServices().uploadFiles(getDescriptor(), build, listener, uploadFiles);
				success = true;
			}
		} catch(InvalidParametersSpecificationException ipse) {
			errMsg = ipse.getMessage(); //Build step aborted
		} catch (Exception e) {
			errMsg = e.getMessage(); //Build step aborted
		} 
		if (!success)
			listener.getLogger().println(Messages.LogError_Msg(errMsg));

		return success;
	}


	public static void dbg(String string) {
		System.out.println(string);
	}
	
	
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
	public static class DescriptorImpl extends MobileCloudNonPrimaryStepConfiguration { 	
		public String getDisplayName() { return Messages.MediaUploadBuilder_BuildStepName(); }
	}

	public String getFile2upload() { return file2upload; }
	
}