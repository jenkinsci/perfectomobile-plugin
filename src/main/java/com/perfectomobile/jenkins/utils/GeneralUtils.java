package com.perfectomobile.jenkins.utils;

import java.io.PrintStream;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.Messages;


public class GeneralUtils {
	private static final boolean isDebug = Boolean.valueOf(System.getProperty(Constants.PM_DEBUG_MODE));
	
	/**
	 * checks that the fileName specification is of a file with extension
	 * @param fileName of file which may be prefixed by a folder and may just be a folder
	 * @return error msg if it's a folder without a file or null if it's a file with extension (no errors) 
	 */
	public static String checkNonEmptyFileToUpload( String fileName) {
		return isWithFileExtension(fileName) 
			? null : Messages.Error_LocalFileMustHaveExtension(); 
	}

	/**
	 * @param path is some path specification of a repository item or folder or local file or folder
	 * @return true if the path specified is like a file with extension (in other words the last path component contains a dot)
	 */
	public static boolean isWithFileExtension(String path) {
		String fl = removePath(path);
		return fl.contains(Constants.FILE_EXT_SEPARATOR);
	}


	/**
	 * @param repoFolder a repository folder but may contain slash at the end
	 * @param filePath a local file name which may be prefixed by some relative folder/path
	 * @return a repo item composed of the repo path followed by just the file name and extension
	 */
	public static String combinePathAndFile(String repoFolder, String filePath) {
		String ret = repoFolder.endsWith(Constants.PARAM_REPOSITORYKEY_FOLDER_SEPARATOR)
				? repoFolder
				: repoFolder + Constants.PARAM_REPOSITORYKEY_FOLDER_SEPARATOR;
		 
		return ret += removePath(filePath);
	}

	public static String removePath(String filePath) {
		int pos1 = filePath.lastIndexOf(Constants.WINDOWS_PATH_SEP);
		int pos2 = filePath.lastIndexOf(Constants.UNIX_PATH_SEP);
		if (pos2 > pos1)
			pos1 = pos2;
		
		return pos1 < 0 ? filePath : filePath.substring(pos1 + 1); //assumes the length of the slash is always 1
	}

	public static final void debug(String msg, PrintStream logger) {
		if (isDebug())
			logger.println(msg);
	}

	public static final boolean isDebug() {
		return isDebug;
	}

	public static boolean isEmpty(String str) {
		return str == null || str.isEmpty();
	}
	/**
	 * 
	 * @param string a string 
	 * @param list an array of strings
	 * @return true if string is contained in list
	 */
	public static boolean doesListContainString(String string, String[] list) {
		boolean ret = false;
		for (String s : list) {
			if (string.equals(s)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
}
