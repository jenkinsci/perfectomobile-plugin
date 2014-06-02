package com.perfectomobile.jenkins.miscel;

//import junit.framework.Assert;
import static org.junit.Assert.assertTrue;
import hudson.FilePath;

import java.io.File;
import java.io.PrintStream;

import org.junit.Test;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.Messages;
import com.perfectomobile.jenkins.MobileCloudGlobalConfigurationData;
import com.perfectomobile.jenkins.services.MyScriptExecutionServices;
import com.perfectomobile.jenkins.services.ScriptExecutionServices;

/**
 * The tests currently check all error conditions... 
 * @author ronik
 *
 */
public class MediaRepositoryArtifactUploaderTest {

	private static final String DEFAULT_BASE_FILE = "c:/jenkins/blah blah/workblah";

	private static final String LOCAL_MEDIA_FOLDER = "localFolder/";
	private static final String LOCAL_MEDIA_FILE = "item.jpg";
	private static final String FOLDER_REPO_MEDIA_ITEM = "PRIVATE:folder";
	private static final String FILE_REPO_MEDIA_ITEM = FOLDER_REPO_MEDIA_ITEM + "/" + LOCAL_MEDIA_FILE;;
	public static final String DEFAULT_PROJ_NAME = "proj1";
	public static final String BEGINNING_OF_UPLOADING_ARTIFACT_MSG = "Uploading artifact \"";

	private static final boolean IMMATERIAL = false;
	
	static class MyPrintStream extends PrintStream {
		String _archiveName;
		int warnCount = 0;

		MyPrintStream() { super(System.out); }
		
		@Override
		public void println(String str) {
			if (str.startsWith(Messages.Warning_OverwritingTargetMediaFile("").substring(0,60))) //first 20 chars are same - probably the msg we're looking for 
				++warnCount;
			else
				parseUploadingArtifactMessage(str);
			
		}
		private void parseUploadingArtifactMessage(String str) {
			if (str.startsWith(BEGINNING_OF_UPLOADING_ARTIFACT_MSG)) {
				int start;
				int pos = str.indexOf("\"", start=BEGINNING_OF_UPLOADING_ARTIFACT_MSG.length());
				if (pos > 0)
					_archiveName = str.substring(start,pos);
			}
		}
		int getWarnCount() { return warnCount; }
		protected String getArchiveName() { return _archiveName; }
	};
	static class DefaultArtifactUploadingInformation implements ArtifactUploadingInformation {
		public String getAutoMedia() { return null; }
		public String getProjName() { return DEFAULT_PROJ_NAME; }
	}
	static class MyGlobalConfigurationData implements MobileCloudGlobalConfigurationData {
		MyGlobalConfigurationData() { }
		public String getHostUrl() { return null; }
		public String getAccessId() { return null; }
		public String getSecretKey() { return null; }
	}
	static class MyMediaRepositoryArtifactUploader extends MediaRepositoryArtifactUploader {
		private MyScriptExecutionServices _scriptExecutionServices;
		public MyMediaRepositoryArtifactUploader(
				ArtifactUploadingInformation artifactUploadingInformation,
				FilePath baseFileSystemTarget, PrintStream logger) {
			super(artifactUploadingInformation, baseFileSystemTarget, logger);
			_scriptExecutionServices = new MyScriptExecutionServices();
		}
		@Override protected MobileCloudGlobalConfigurationData getGlobalConfigurationData() {
			return new MyGlobalConfigurationData();
		}
		@Override protected ScriptExecutionServices getScriptExecutionServices() {
			return _scriptExecutionServices; }

		void setThrowException(boolean throwException) { _scriptExecutionServices.setThrowException(throwException); }
	}




	private MyPrintStream myPrintStream;

	private MyMediaRepositoryArtifactUploader uploader;
	
	/*
	 * these tests test uplodOne() as well as getArchiveName() and makeRepoItem() 
	 * Each test of uploadOne checks that the media key and the archive file that is uploaded is correct.
	 * the variability is the following:
	 * 1) mediaSpecifiedFileOtherwiseFolder : may be file or folder - this affects media destination and may affect the warning count
	 *    if it's a folder it may or may not end with a forward slash 
	 * all the rest of the variables affect the name of the file/s that is/are supposedly uploaded
	 * 2) the test case may upload one or more files/artifacts, one at a time. may affect the warning count
	 * 3) archive : true means the file was in archive folder of another project, false means not in archive folder (this should not occur normally)
	 * 4) unixOtherwiseWindows : source/TARGET may specified in unix style or windows 
	 * this also affects the media destination file
	 * 4) throwException : if true the operation will throws an exception 
	 * 
	 */
	
	
	@Test
	public void uploadOneTestWhenMediaSpecifiedIsFileAndUploadingSingleArtifactOnUnix() {
		final boolean mediaSpecifiedFileOtherwiseFolder = true;
		final boolean folderEndsWithSlash = false;
		boolean archive = true;
		boolean unixOtherwiseWindows = true;
		boolean throwException = false;

		int expectedWarnings = 0;
		String expectedMediaKey = FILE_REPO_MEDIA_ITEM;
		String expectedArchiveName = LOCAL_MEDIA_FILE;

		
		invokeAndCheckUploadOne(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey,
				expectedArchiveName); 
	}
	@Test
	public void uploadOneTestWhenMediaSpecifiedIsFileAndUploadingSingleArtifactOnUnixButNotFromArchive() {
		final boolean mediaSpecifiedFileOtherwiseFolder = true;
		final boolean folderEndsWithSlash = false;
		boolean archive = false;
		boolean unixOtherwiseWindows = true;
		boolean throwException = false;

		int expectedWarnings = 0;
		String expectedMediaKey = FILE_REPO_MEDIA_ITEM;
		String expectedArchiveName = LOCAL_MEDIA_FILE;

		
		invokeAndCheckUploadOne(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey,
				expectedArchiveName); 
	}

	@Test
	public void uploadOneTestWhenExceptionThrown() {
		final boolean mediaSpecifiedFileOtherwiseFolder = true;
		final boolean folderEndsWithSlash = false;
		boolean archive = true;
		boolean unixOtherwiseWindows = true;
		boolean throwException = true;

		int expectedWarnings = 0;
		String expectedMediaKey = FILE_REPO_MEDIA_ITEM;
		String expectedArchiveName = LOCAL_MEDIA_FILE;

		
		invokeAndCheckUploadOne(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey,
				expectedArchiveName); 
	}
	@Test
	public void uploadOneTestWhenMediaSpecifiedIsFileAndUploadingSingleArtifactOnWindows() {
		final boolean mediaSpecifiedFileOtherwiseFolder = true;
		final boolean folderEndsWithSlash = false;
		boolean archive = true;
		boolean unixOtherwiseWindows = false;
		boolean throwException = false;

		int expectedWarnings = 0;
		String expectedMediaKey = FILE_REPO_MEDIA_ITEM;
		String expectedArchiveName = LOCAL_MEDIA_FILE;

		
		invokeAndCheckUploadOne(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey,
				expectedArchiveName); 
	}

	@Test
	public void uploadOneTestWhenMediaSpecifiedIsFolderWithoutSlashAndUploadingSingleArtifactOnUnix() {
		final boolean mediaSpecifiedFileOtherwiseFolder = false;
		final boolean folderEndsWithSlash = false;
		boolean archive = true;
		boolean unixOtherwiseWindows = true;
		boolean throwException = false;

		int expectedWarnings = 0;
		String expectedMediaKey = FILE_REPO_MEDIA_ITEM;
		String expectedArchiveName = LOCAL_MEDIA_FILE;

		
		invokeAndCheckUploadOne(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey,
				expectedArchiveName); 
	}
	@Test
	public void uploadOneTestWhenMediaSpecifiedIsFolderWithSlashAndUploadingSingleArtifactOnUnix() {
		final boolean mediaSpecifiedFileOtherwiseFolder = false;
		final boolean folderEndsWithSlash = true;
		boolean archive = true;
		boolean unixOtherwiseWindows = true;
		boolean throwException = false;

		int expectedWarnings = 0;
		String expectedMediaKey = FILE_REPO_MEDIA_ITEM;
		String expectedArchiveName = LOCAL_MEDIA_FILE;

		
		invokeAndCheckUploadOne(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey,
				expectedArchiveName); 
	}

	@Test
	public void uploadOneTestWhenMediaSpecifiedIsFolderAndUploadingTwoArtifactsOnWindows() {
		final boolean mediaSpecifiedFileOtherwiseFolder = false;
		final boolean folderEndsWithSlash = false;
		boolean archive = true;
		boolean unixOtherwiseWindows = false;
		boolean throwException = false;
		

		int expectedWarnings = 0;
		String expectedArchiveName = LOCAL_MEDIA_FILE;
		String expectedMediaKey = FILE_REPO_MEDIA_ITEM;

		
		invokeAndCheckUploadOne(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey,
				expectedArchiveName);
		
		//invoke it again to see if the warnings will work

		
		expectedWarnings = 0;		
		expectedArchiveName = LOCAL_MEDIA_FOLDER + LOCAL_MEDIA_FILE;
		expectedMediaKey = FOLDER_REPO_MEDIA_ITEM + "/" +  expectedArchiveName;
		
		invokeAndCheckUploadOneMoreThanOnce(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey, expectedArchiveName,
				createSource(archive, unixOtherwiseWindows, expectedArchiveName),
				createTarget(unixOtherwiseWindows, expectedArchiveName));
		
	}
	
	@Test
	public void uploadOneTestWhenMediaSpecifiedIsFileAndUploadingTwoArtifactsOnWindows() {
		final boolean mediaSpecifiedFileOtherwiseFolder = true;
		final boolean folderEndsWithSlash = IMMATERIAL;
		boolean archive = true;
		boolean unixOtherwiseWindows = false;
		boolean throwException = false;

		int expectedWarnings = 0;
		String expectedArchiveName = LOCAL_MEDIA_FILE;
		String expectedMediaKey = FILE_REPO_MEDIA_ITEM;

		
		invokeAndCheckUploadOne(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey,
				expectedArchiveName);
		
		//invoke it again to see if the warnings will work

		
		expectedWarnings = 1;		
		expectedArchiveName = LOCAL_MEDIA_FOLDER + LOCAL_MEDIA_FILE;
		
		invokeAndCheckUploadOneMoreThanOnce(mediaSpecifiedFileOtherwiseFolder,
				folderEndsWithSlash, archive, unixOtherwiseWindows,
				throwException, expectedWarnings, expectedMediaKey, expectedArchiveName,
				createSource(archive, unixOtherwiseWindows, expectedArchiveName),
				createTarget(unixOtherwiseWindows, expectedArchiveName));
		
	}

	private void invokeAndCheckUploadOne(
			final boolean mediaSpecifiedFileOtherwiseFolder,
			final boolean folderEndsWithSlash, boolean archive,
			boolean unixOtherwiseWindows, boolean throwException,
			int expectedWarnings, String expectedMediaKey,
			String expectedArchiveName) {
		
		invokeAndCheckUploadOneMoreThanOnce(mediaSpecifiedFileOtherwiseFolder, folderEndsWithSlash, archive, 
				unixOtherwiseWindows, throwException, expectedWarnings, expectedMediaKey, expectedArchiveName,
				createSource(archive, unixOtherwiseWindows, LOCAL_MEDIA_FILE),
				createTarget(unixOtherwiseWindows, LOCAL_MEDIA_FILE)); 
	}

	private void invokeAndCheckUploadOneMoreThanOnce(
			final boolean mediaSpecifiedFileOtherwiseFolder,
			final boolean folderEndsWithSlash, boolean archive,
			boolean unixOtherwiseWindows, boolean throwException,
			int expectedWarnings, String expectedMediaKey,
			String expectedArchiveName, FilePath source, FilePath target) {

		FilePath baseFileSystemTarget = createFilePath(DEFAULT_BASE_FILE,unixOtherwiseWindows);
		
		DefaultArtifactUploadingInformation uploadingInformation = new DefaultArtifactUploadingInformation() {
			public String getAutoMedia() { return mediaSpecifiedFileOtherwiseFolder 
					? FILE_REPO_MEDIA_ITEM  //media destination is a file
							: FOLDER_REPO_MEDIA_ITEM + (folderEndsWithSlash ? "/" : ""); }  //media destination is a folder
		};
		

		
		if (myPrintStream == null)
			myPrintStream = new MyPrintStream();
		
		if (uploader == null)
			uploader = new MyMediaRepositoryArtifactUploader(
				uploadingInformation, baseFileSystemTarget, myPrintStream);
		
		if (throwException)
			uploader.setThrowException(true);
		
		
		try {
			uploader.uploadOne(source, target, IMMATERIAL);
			assert(!throwException);
			
			assertTrue( "num of warnings issued is incorrect", myPrintStream.getWarnCount() == expectedWarnings); //no warning should be issued
			assertTrue("did not get the expected media key", expectedMediaKey.equals(((MyScriptExecutionServices) uploader.getScriptExecutionServices()).getPassedRepositoryItemKey()));
			assertTrue( "did not get the expected archive Name", expectedArchiveName.equals(myPrintStream.getArchiveName()));
		} catch (Exception e) {
			assert(throwException);
		}
	}

	
	
	
	private FilePath createTarget( boolean unixOtherwiseWindows, String fileName) {
		String ret = DEFAULT_BASE_FILE + "/" +  Constants.TEMP_FOLDER_FOR_ARTIFACT_UPLOAD + "/" + fileName;
		return createFilePath(ret,unixOtherwiseWindows);
	}




	private FilePath createSource(boolean artifactOtherwiseLocalFile, boolean unixOtherwiseWindows, String fileName) {		
		String ret = artifactOtherwiseLocalFile 
				? "c:/blahBlah/whatever" + Constants.ARCHIVE_FOLDER + fileName
				: "c:/blahBlah/whatever/" + fileName;
		return createFilePath(ret,unixOtherwiseWindows);
	}




	private FilePath createFilePath(String fileName, boolean unixOtherwiseWindows) {
		fileName = unixOtherwiseWindows ? fileName.replace("\\", "/") : fileName.replace("/", "\\");
		return new FilePath(new File(fileName));
	}
}

