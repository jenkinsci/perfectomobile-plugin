package com.perfectomobile.jenkins.miscel;

import com.perfectomobile.jenkins.utils.GeneralUtils;

public final class UploadFile {
	private String repository;
	private String filePath;
	private String repositoryItemKey;

	public UploadFile (String repository, String filePath, String repositoryItemKey){
		this.setRepository(repository);
		this.setFilePath(filePath);
		this.setRepositoryItemKey(repositoryItemKey);
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * if the repo key is a folder then it will combine with the filePath
	 * @return a repo item of a file not a folder
	 */
	public String getRepositoryItemKey() {
		if (!GeneralUtils.isWithFileExtension(repositoryItemKey)) 
			repositoryItemKey = GeneralUtils.combinePathAndFile(repositoryItemKey,filePath);
		
		return repositoryItemKey;
	}

	public void setRepositoryItemKey(String repositoryItemKey) {
		this.repositoryItemKey = repositoryItemKey;
	}

	public String getRepository() {
		return repository;
	}

	public void setRepository(String repository) {
		this.repository = repository;
	}

}
