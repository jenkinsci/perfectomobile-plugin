package com.perfectomobile.jenkins.utils;

//import junit.framework.Assert;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.Messages;

public class GeneralUtilsTest {

	private static final String TEST_FILE_NAME = "foo.jpg";
	private static final String REPO_FOLDER = "PRIVATE:my";
	
	@Test
	public void testRemovePathWhenPathContainsNoFolders() {
		String filePath = TEST_FILE_NAME;
		String result = GeneralUtils.removePath(filePath);
		assertTrue(TEST_FILE_NAME.equals(result));
	}
	
	@Test
	public void testRemovePathWhenPathIsWindowsStyle() {
		String filePath = "folder\\"+TEST_FILE_NAME;
		String result = GeneralUtils.removePath(filePath);
		assertTrue(TEST_FILE_NAME.equals(result));		
	}
	
	@Test
	public void testRemovePathWhenPathIsUnixStyle() {
		String filePath = "folder/"+TEST_FILE_NAME;
		String result = GeneralUtils.removePath(filePath);
		assertTrue(TEST_FILE_NAME.equals(result));		
	}
	@Test
	public void testIsEmptyWhenNull() {
		assertTrue(GeneralUtils.isEmpty(null));
	}
	@Test
	public void testIsEmptyWhenEmpty() {
		assertTrue(GeneralUtils.isEmpty(""));
	}
	@Test
	public void testIsEmptyWhenNotEmpty() {
		assertTrue(!GeneralUtils.isEmpty("x"));
	}
	
	@Test
	public void testCombinePathAndFileWhenFolderHasNoSlashAtEndAndFileIsWithoutFolder() {
		String repoFolder = REPO_FOLDER;
		String filePath = TEST_FILE_NAME;
		String expResult = REPO_FOLDER + Constants.PARAM_REPOSITORYKEY_FOLDER_SEPARATOR + TEST_FILE_NAME;		
		String result = GeneralUtils.combinePathAndFile(repoFolder, filePath);
		
		assertTrue(expResult.equals(result));
	}

	@Test
	public void testCombinePathAndFileWhenFolderHasNoSlashAtEndAndFileIsWithFolder() {
		String repoFolder = REPO_FOLDER;
		String filePath = "foo/" + TEST_FILE_NAME;
		String expResult = REPO_FOLDER + Constants.PARAM_REPOSITORYKEY_FOLDER_SEPARATOR + TEST_FILE_NAME;		
		String result = GeneralUtils.combinePathAndFile(repoFolder, filePath);
		
		assertTrue(expResult.equals(result));
	}

	@Test
	public void testCombinePathAndFileWhenFolderHasSlashAtEndAndFileIsWithoutFolder() {
		String repoFolder = REPO_FOLDER + Constants.PARAM_REPOSITORYKEY_FOLDER_SEPARATOR;
		String filePath = TEST_FILE_NAME;
		String expResult = REPO_FOLDER + Constants.PARAM_REPOSITORYKEY_FOLDER_SEPARATOR + TEST_FILE_NAME;		
		String result = GeneralUtils.combinePathAndFile(repoFolder, filePath);
		
		assertTrue(expResult.equals(result));
	}

	@Test
	public void testCombinePathAndFileWhenFolderHasSlashAtEndAndFileIsWithFolder() {
		String repoFolder = REPO_FOLDER + Constants.PARAM_REPOSITORYKEY_FOLDER_SEPARATOR;
		String filePath = "foo\\zoo\\" + TEST_FILE_NAME;
		String expResult = REPO_FOLDER + Constants.PARAM_REPOSITORYKEY_FOLDER_SEPARATOR + TEST_FILE_NAME;		
		String result = GeneralUtils.combinePathAndFile(repoFolder, filePath);
		
		assertTrue(expResult.equals(result));
	}
	@Test
	public void testIsWithFileExtensionWhenWithoutExt() {
		String path = "foo\\zoo";
		assertTrue(!GeneralUtils.isWithFileExtension(path));
	}
	@Test
	public void testIsWithFileExtensionWhenWithExt() {
		String path = "foo\\zoo\\" + TEST_FILE_NAME;
		assertTrue(GeneralUtils.isWithFileExtension(path));
	}
	@Test
	public void testCheckNonEmptyFileToUploadWhenEmptyFileSpecified1() {
		String path = "foo\\zoo"; 
		assertTrue(GeneralUtils.checkNonEmptyFileToUpload(path).equals(Messages.Error_LocalFileMustHaveExtension()));
	}
	@Test
	public void testCheckNonEmptyFileToUploadWhenEmptyFileSpecified2() {
		String path = "foo\\zoo\\"; 
		assertTrue(GeneralUtils.checkNonEmptyFileToUpload(path).equals(Messages.Error_LocalFileMustHaveExtension()));
	}
	@Test
	public void testCheckNonEmptyFileToUploadWhenNonEmptyFileSpecified1() {
		String path = "foo\\" + TEST_FILE_NAME; 
		assertTrue(GeneralUtils.checkNonEmptyFileToUpload(path) == null);
	}
	@Test
	public void testCheckNonEmptyFileToUploadWhenNonEmptyFileSpecified2() {
		String path = TEST_FILE_NAME; 
		assertTrue(GeneralUtils.checkNonEmptyFileToUpload(path) == null);
	}
	
	@Test
	public void testDoesListContainStringWhenItDoes() {
		String string = TEST_FILE_NAME;
		String[] list = new String[]{ "foo", TEST_FILE_NAME, "zoo"};
		assertTrue(GeneralUtils.doesListContainString(string,list));
	}
	@Test
	public void testDoesListContainStringWhenItDoesNot() {
		String string = TEST_FILE_NAME;
		String[] list = new String[]{ "foo",  "zoo"};
		assertTrue(!GeneralUtils.doesListContainString(string,list));
	}
	@Test
	public void testDoesListContainStringWhenListIsEmpty() {
		String string = TEST_FILE_NAME;
		String[] list = new String[]{};
		assertTrue(!GeneralUtils.doesListContainString(string,list));
	}
}
