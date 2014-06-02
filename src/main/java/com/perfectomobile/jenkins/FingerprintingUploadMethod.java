package com.perfectomobile.jenkins;

import hudson.Extension;
import hudson.FilePath;
import hudson.plugins.copyartifact.Copier;
import hudson.plugins.copyartifact.FingerprintingCopyMethod;

import java.io.IOException;

import com.perfectomobile.jenkins.miscel.MediaRepositoryArtifactUploader;

@Extension(ordinal=-101)
public class FingerprintingUploadMethod extends FingerprintingCopyMethod { 

	private MediaRepositoryArtifactUploader _mediaRepositoryArtifactUploader;

	public void setMediaRepositoryUploader(MediaRepositoryArtifactUploader mediaRepositoryArtifactUploader) {
		_mediaRepositoryArtifactUploader = mediaRepositoryArtifactUploader;
	}

	@Override
	public void copyOne(FilePath source, FilePath target,
			boolean fingerprintArtifacts) throws IOException,
			InterruptedException {
		super.copyOne(source, target, fingerprintArtifacts);
		
		_mediaRepositoryArtifactUploader.uploadOne(source, target, fingerprintArtifacts);		
 	}
	@Override
	public Copier clone() {
		return new FingerprintingUploadMethod();
	}
}
