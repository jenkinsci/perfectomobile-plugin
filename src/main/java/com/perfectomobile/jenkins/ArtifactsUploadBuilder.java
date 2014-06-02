package com.perfectomobile.jenkins;

import hudson.EnvVars;
import hudson.Extension;
import hudson.ExtensionList;
import hudson.FilePath;
import hudson.Launcher;
import hudson.console.HyperlinkNote;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.plugins.copyartifact.BuildSelector;
import hudson.plugins.copyartifact.Copier;
import hudson.plugins.copyartifact.CopyArtifact;
import hudson.util.FormValidation;

import java.io.IOException;
import java.io.PrintStream;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.perfectomobile.jenkins.miscel.ArtifactUploadingInformation;
import com.perfectomobile.jenkins.miscel.MediaRepositoryArtifactUploader;
import com.perfectomobile.jenkins.utils.GeneralUtils;

public class ArtifactsUploadBuilder extends CopyArtifact implements ArtifactUploadingInformation {
	private FilePath _baseFileSystemTarget;	
	private String _projectName;
	@DataBoundConstructor
	public ArtifactsUploadBuilder(String projectName, String parameters, BuildSelector selector, String filter, String target,
			boolean flatten, boolean optional, boolean fingerprintArtifacts, String autoMedia) {
		super(projectName, parameters, selector, filter, Constants.TEMP_FOLDER_FOR_ARTIFACT_UPLOAD, flatten, optional, fingerprintArtifacts, autoMedia);
		_projectName = projectName;
	}
	/** the name is in order not to overload the base method getProjectName */
	public String getProjName() { return _projectName; }	
	
	@Override
	protected Copier createCopier(PrintStream logger) {
		Copier ret = null;
		ExtensionList<Copier> copiers = Jenkins.getInstance().getExtensionList(Copier.class);
		for(Copier copier : copiers) 
			if (copier instanceof FingerprintingUploadMethod) {
				ret = copier.clone();
				break;
			}
		assert(ret != null);
		((FingerprintingUploadMethod) ret).setMediaRepositoryUploader(new MediaRepositoryArtifactUploader(this,_baseFileSystemTarget, logger));
		return ret;
		
	}
	
	@Override
	protected FilePath calculateExpandedTargetDir( FilePath targetDir, EnvVars env) {
		_baseFileSystemTarget = targetDir; //before expansion 
		return super.calculateExpandedTargetDir(targetDir, env);
	}
	@SuppressWarnings("rawtypes")
	@Override
	protected void logSummary(Run src, PrintStream console, int cnt) {
		console.println(Messages.LogError_TotalUploaded(cnt, HyperlinkNote.encodeTo('/'+ src.getParent().getUrl(), src.getParent().getFullDisplayName()),
		        HyperlinkNote.encodeTo('/'+src.getUrl(), Integer.toString(src.getNumber()))));
	}

	
	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		boolean success = false; 
		
		logBeginingStep(listener, getFilter());
		
		if (GeneralUtils.isEmpty(getAutoMedia()))  
			listener.getLogger().println(Messages.LogError_Msg(Messages.UiError_TargetRepositoryMustBeSpecified()));
		else
			success = super.perform(build, launcher, listener);
		
		return success;
	}
	
	@Extension //This indicates to Jenkins that this is an implementation of an extension point.
	public static class DescriptorImpl extends CopyArtifact.DescriptorImpl {
		public String getDisplayName() {
            return Messages.ArtifactsUploadBuilder_BuildStepName();
        }
		@SuppressWarnings("rawtypes")
		public boolean isApplicable(Class<? extends AbstractProject> clazz) { return true; }
	}
}
