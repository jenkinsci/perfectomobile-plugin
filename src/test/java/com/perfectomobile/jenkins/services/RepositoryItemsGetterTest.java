package com.perfectomobile.jenkins.services;

//import junit.framework.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.Test;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.Messages;
import com.perfectomobile.jenkins.miscel.RepositoryAccessFailureException;
import com.sun.jersey.api.client.ClientResponse;

/**
 * The tests currently check all error conditions... 
 * @author ronik
 *
 */
public class RepositoryItemsGetterTest {

	private static final String TEST_FILE_NAME = "test.jpg";
	
	static class MyRepositoryItemsGetter extends RepositoryItemsGetter {
			private boolean _setAccessDeniedTrueCalled = false;
			
			@Override protected boolean isAccessDenied() { return false; }
			@Override protected void setAccessDenied(boolean accessDenied) {
				_setAccessDeniedTrueCalled = accessDenied;
			}
			public boolean isSetAccessDeniedTrueCalled() { return _setAccessDeniedTrueCalled; }
		}
	private static final String REPOSITORY = Constants.MEDIA_REPOSITORY;
	
	@Test
	public void obtainRepositoryItemsOfTestWhenAccessIsDenied() {
		RepositoryItemsGetter getter = new RepositoryItemsGetter() {
			@Override protected boolean isAccessDenied() { return true; }
		};
		try {
			getter.obtainRepositoryItemsOf(REPOSITORY, null, null, null);
			fail("should have throen exception");
		} catch (RepositoryAccessFailureException e) {
			assertTrue(e.isAccessDenied());
		}

	}
	@Test
	public void obtainRepositoryItemsOfTestWhenAccessOKButIOExceptionThrown() {
		RepositoryItemsGetter getter = new RepositoryItemsGetter() {
			@Override protected boolean isAccessDenied() { return false; }
		};
		getter.setRestServices(new RestServices() {
			@Override public ClientResponse getFromRepository(String url, 
					String accessId, String secretKey, String repository) throws IOException, ServletException {
				throw new IOException();
			}
		});
		try {
			getter.obtainRepositoryItemsOf(REPOSITORY, null, null, null);
			fail("should have thrown an exception");
		} catch (RepositoryAccessFailureException rafe) {
			assertTrue(!rafe.isAccessDenied());
		}
	}
	
	@Test
	public void obtainRepositoryItemsOfTestWhenAccessOKButServletExceptionThrown() {
		RepositoryItemsGetter getter = new RepositoryItemsGetter() {
			@Override protected boolean isAccessDenied() { return false; }
		};
		getter.setRestServices(new RestServices() {
			@Override public ClientResponse getFromRepository(String url, 
					String accessId, String secretKey, String repository) throws IOException, ServletException {
				throw new ServletException();
			}
		});
		try {
			getter.obtainRepositoryItemsOf(REPOSITORY, null, null, null);
			fail("should have thrown an exception");
		} catch (RepositoryAccessFailureException rafe) {
			assertTrue(!rafe.isAccessDenied());
		}
	}

	@Test
	public void obtainRepositoryItemsOfTestWhenAccessOKButHttpResponseIsNotSuccess() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		getter.setRestServices(new RestServices() {
			@Override public ClientResponse getFromRepository(String url, String accessId, String secretKey,
					String repositoryItemType) throws IOException, ServletException {
				return new ClientResponse(1 + Constants.PM_RESPONSE_STATUS_SUCCESS,null,null,null);
			}
		});
		
		try {
			getter.obtainRepositoryItemsOf(REPOSITORY, null, null, null);
			fail("should have thrown an exception");
		} catch (RepositoryAccessFailureException rafe) {
			assertTrue("isAccessDenied() in the thrown exception should have been true",rafe.isAccessDenied());
			assertTrue("method should have set accessDenied to true but did not",getter.isSetAccessDeniedTrueCalled());
		}
	}
	
	@Test
	public void checkRepositoryItemTestWhenRepoKeyIsEmptyAndFolderAllowed() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		String errMsg = getter.checkRepositoryItem("", true);
		assertTrue("the method returned the worng value",Messages.Error_RepositoryKeyIsRequired().equals(errMsg));
	}
	@Test
	public void checkRepositoryItemTestWhenRepoKeyIsEmptyAndFolderNotAllowed() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		String errMsg = getter.checkRepositoryItem("", false);
		assertTrue("the method returned the worng value",Messages.Error_RepositoryKeyIsRequired().equals(errMsg));
	}
	
	@Test
	public void checkRepositoryItemTestWhenRepoKeyIsFileAndFolderAllowed() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		String errMsg = getter.checkRepositoryItem(TEST_FILE_NAME, true);
		assertTrue("the method returned the worng value",null == errMsg);
	}
	@Test
	public void checkRepositoryItemTestWhenRepoKeyIsFileAndFolderNotAllowed() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		String errMsg = getter.checkRepositoryItem("foo\\"+TEST_FILE_NAME, false);
		assertTrue("the method returned the worng value",null == errMsg);
	}
	
	
	@Test
	public void checkRepositoryItemTestWhenRepoKeyIsFolderAndFolderAllowed1() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		String errMsg = getter.checkRepositoryItem("folder\\foo", true);
		assertTrue("the method returned the worng value",null == errMsg);
	}
	public void checkRepositoryItemTestWhenRepoKeyIsFolderAndFolderAllowed2() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		String errMsg = getter.checkRepositoryItem("folder/foo/", true);
		assertTrue("the method returned the worng value",null == errMsg);
	}
	@Test
	public void checkRepositoryItemTestWhenRepoKeyIsFolderAndFolderNotAllowed1() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		String errMsg = getter.checkRepositoryItem("folder\\foo", false);
		assertTrue("the method returned the worng value",Messages.Error_RepositoryKeyCannotBeFolder().equals( errMsg));
	}
	@Test
	public void checkRepositoryItemTestWhenRepoKeyIsFolderAndFolderNotAllowed2() {
		MyRepositoryItemsGetter getter = new MyRepositoryItemsGetter();
		String errMsg = getter.checkRepositoryItem("folder/foo/", false);
		assertTrue("the method returned the worng value",Messages.Error_RepositoryKeyCannotBeFolder().equals( errMsg));
	}
}
