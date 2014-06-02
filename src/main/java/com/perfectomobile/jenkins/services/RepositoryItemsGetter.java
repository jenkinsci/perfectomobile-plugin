package com.perfectomobile.jenkins.services;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.Messages;
import com.perfectomobile.jenkins.miscel.InfoGetter;
import com.perfectomobile.jenkins.miscel.RepositoryAccessFailureException;
import com.perfectomobile.jenkins.utils.GeneralUtils;
import com.perfectomobile.jenkins.utils.XmlParsingUtils;
import com.sun.jersey.api.client.ClientResponse;

public class RepositoryItemsGetter extends InfoGetter {
	private Map<String,String[]> repositoryItems = new HashMap<String,String[]>();
	
	/**
	 * obtain repository items of a certain repository type
	 * @param repositoryItemType
	 * @param url
	 * @param accessId
	 * @param secretKey
	 * @param curList 
	 * @return
	 */
	public String[] obtainRepositoryItemsOf(String repositoryItemType, String url, String accessId, String secretKey)//CurrentItemsAccessor curItemListAccessor) 
			throws RepositoryAccessFailureException {
		String[] ret = null;
		
		if (isAccessDenied())
			throw new RepositoryAccessFailureException(true); 
		
		if (!repositoryItems.containsKey(repositoryItemType)) { //no errors and no items - try to get the items 
			ClientResponse perfectoResponse = null;
			try {
				perfectoResponse = getRestServices().getFromRepository(
					url, accessId, secretKey, repositoryItemType);
				
				if (perfectoResponse.getStatus() == Constants.PM_RESPONSE_STATUS_SUCCESS) {
					File resultFile = perfectoResponse.getEntity(File.class);
		
					ret = XmlParsingUtils.getXmlElements(resultFile, XmlParsingUtils.ITEM_ELEMENT_NAME).toArray(new String[0]);
					repositoryItems.put(repositoryItemType,ret);
				} else {
					setAccessDenied(true);
					throw new RepositoryAccessFailureException(true);
				}
				
			} catch (IOException e) { //most likely io exception - we want caller to try again
				System.out.println("While trying to get repo items of type "+repositoryItemType+" got "+e.getClass().getName()+": "+e.getMessage());
				//not considered an access error so error is not set				
				repositoryItems.remove(repositoryItemType); //this is done so that the next time this method is called _scripts is still null so code will be attempted
				throw new RepositoryAccessFailureException(false); //so it will be called again
			} catch (ServletException e) { //most likely io exception - we want caller to try again
				System.out.println("While trying to get repo items of type "+repositoryItemType+" got "+e.getClass().getName()+": "+e.getMessage());
				//not considered an access error so error is not set				
				repositoryItems.remove(repositoryItemType); //this is done so that the next time this method is called _scripts is still null so code will be attempted
				throw new RepositoryAccessFailureException(false); //so it will be called again
			}
		} else //either _scripts is not null or access is denied or both
			ret = repositoryItems.get(repositoryItemType);
		return ret;
	}

	@Override
	public void doReset() {
		repositoryItems.clear();
		super.doReset();
	}
	public boolean isFoundInRepository(String name, String repository) {
		return repositoryItems.containsKey(repository) && 
			GeneralUtils.doesListContainString(name,repositoryItems.get(repository)); 
	}
	/**
	 * checks a potential repository key.
	 * @param folderAllowed true means the repositoryKey may specify a folder (ends with no extension) 
	 * @param repositoryKey  a potential target of an upload 
	 * @return error msg or null if no errors
	 */
	public String checkRepositoryItem( String repositoryKey, boolean folderAllowed) {
		String errMsg = null; //assume no errors
		if (repositoryKey.isEmpty()) 
			errMsg = Messages.Error_RepositoryKeyIsRequired();
		else if (!folderAllowed) { 
			if (!GeneralUtils.isWithFileExtension(repositoryKey))
				errMsg = Messages.Error_RepositoryKeyCannotBeFolder();
			
		}		
		return errMsg;
	}	
}
