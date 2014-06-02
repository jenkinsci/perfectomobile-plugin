package com.perfectomobile.jenkins.miscel;

import hudson.FilePath;

import java.io.IOException;
import java.io.PrintStream;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.Messages;
import com.perfectomobile.jenkins.MobileCloudGlobalConfigurationData;
import com.perfectomobile.jenkins.services.MobileCloudServicesFactory;
import com.perfectomobile.jenkins.services.ScriptExecutionServices;
import com.perfectomobile.jenkins.utils.GeneralUtils;

/**
 * @author ronik
 *
 */
public class MediaRepositoryArtifactUploader {

	private ArtifactUploadingInformation _artifactUploadingInformation;
	private FilePath _baseFileSystemTarget;
	private PrintStream _logger;
	
	
	private int _count = 0;


	public MediaRepositoryArtifactUploader(ArtifactUploadingInformation artifactUploadingInformation, 
			FilePath baseFileSystemTarget, PrintStream logger) {
		_artifactUploadingInformation = artifactUploadingInformation;
		_baseFileSystemTarget = baseFileSystemTarget;
		_logger = logger;
	}

	/**
	 * this method is only called for uploading artifact/s from another project 
	 * note that artifacts are first copied to the current project (in target) and uploaded from there
	 * 
	 * @param source the source file (file) or artifact source location in the archive folder of another project (artifact)  
	 * @param target  the location of the file to be uploaded (in both cases)  
	 * @param fingerprintArtifacts
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void uploadOne(FilePath source, FilePath target,
			boolean fingerprintArtifacts) throws IOException,
			InterruptedException {
		
		String archiveName = getArchiveName(source);
		
		String mediaDestination = _artifactUploadingInformation.getAutoMedia();
		if (GeneralUtils.isWithFileExtension(_artifactUploadingInformation.getAutoMedia())) { //file specified
			if (++_count >= 2)
				_logger.println(Messages.Warning_OverwritingTargetMediaFile(archiveName));
		} else //it's a path
			mediaDestination = makeRepoItem(target);
		
		
		//now do the upload
		_logger.println( Messages.LogInfo_UploadingArtifact(archiveName, _artifactUploadingInformation.getProjName(), mediaDestination));
		
		MobileCloudGlobalConfigurationData globalConfigData = getGlobalConfigurationData();
		try {
			
			getScriptExecutionServices().uploadFile(
				globalConfigData.getHostUrl(), globalConfigData.getAccessId(), globalConfigData.getSecretKey(), 
				Constants.MEDIA_REPOSITORY, mediaDestination, target, _logger,
				false); //false means do not display the uploading file msg as it has already been displayed above (more accurate message)
		} catch (Exception e) {
			_logger.println( Messages.LogError_Msg(e.getMessage()));
			throw new InterruptedException(e.getMessage());
		}
		
 	}

	/**
	 * this method is for convenience and to simplify junit testing
	 * @return the ScriptExecutionServices
	 */
	protected ScriptExecutionServices getScriptExecutionServices() {
		return MobileCloudServicesFactory.getInstance().getScriptExecutionServices();
	}

	/**
	 * this method is for convenience and to simplify junit testing
	 * @return the global data object
	 */
	protected MobileCloudGlobalConfigurationData getGlobalConfigurationData() {
		return MobileCloudServicesFactory.getInstance().getGlobalConfigurationData();
	}
	private String getArchiveName(FilePath source) {
		String fullArchiveName = source.getRemote().replace(Constants.WINDOWS_PATH_SEP, Constants.UNIX_PATH_SEP);
		String lookFor = Constants.ARCHIVE_FOLDER;
		int pos = fullArchiveName.lastIndexOf(lookFor);
		if (pos < 0) {
			lookFor = Constants.UNIX_PATH_SEP;
			pos = fullArchiveName.lastIndexOf(lookFor);
		} 
		pos += lookFor.length(); 
		return fullArchiveName.substring(pos);
	}

	private String makeRepoItem(FilePath fileSystemTarget) {
		String ret = _artifactUploadingInformation.getAutoMedia();
		//if the media destination (folder) doesn't end with a slash, add the slash to the media destination path being built up
		if (!(ret.endsWith(Constants.WINDOWS_PATH_SEP) || ret.endsWith(Constants.UNIX_PATH_SEP) ))
			ret += Constants.UNIX_PATH_SEP;
		
		String relativePc = fileSystemTarget.getRemote().substring(_baseFileSystemTarget.getRemote().length() + 1 + Constants.TEMP_FOLDER_FOR_ARTIFACT_UPLOAD.length());
		int pos = relativePc.startsWith(Constants.WINDOWS_PATH_SEP) || relativePc.startsWith(Constants.UNIX_PATH_SEP) ? 1 : 0;
		
		ret = ret + relativePc.substring(pos);
		return ret.replace(Constants.WINDOWS_PATH_SEP, Constants.UNIX_PATH_SEP);
	}


}
