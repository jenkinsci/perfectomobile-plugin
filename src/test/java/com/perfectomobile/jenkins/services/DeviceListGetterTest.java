package com.perfectomobile.jenkins.services;

//import junit.framework.Assert;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import org.junit.Test;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.miscel.StringListAccumulator;
import com.sun.jersey.api.client.ClientResponse;

/**
 * The tests currently check all error conditions... 
 * @author ronik
 *
 */
public class DeviceListGetterTest {

	static class DeviceList implements StringListAccumulator {
		private ArrayList<String> _items = new ArrayList<String>();
		
		public void addString(String string) { _items.add(string); }
		ArrayList<String> getItems() { return _items; }
	}
	static class MyDeviceListGetter extends DeviceListGetter {
			private boolean _setAccessDeniedTrueCalled = false;
			
			@Override protected boolean isAccessDenied() { return false; }
			@Override protected void setAccessDenied(boolean accessDenied) {
				_setAccessDeniedTrueCalled = accessDenied;
			}
			public boolean isSetAccessDeniedTrueCalled() { return _setAccessDeniedTrueCalled; }
		}
	
	@Test
	public void populateDevicesTestWhenAccessIsDenied() {
		DeviceListGetter getter = new DeviceListGetter() {
			@Override protected boolean isAccessDenied() { return true; }
		};
		DeviceList devices = new DeviceList();
		boolean ret = getter.populateDevices(devices, null, null, null);
		assertTrue("method should have return false", !ret);
		assertTrue("list should have been empty",devices.getItems().isEmpty());
	}
	@Test
	public void populateDevicesTestWhenAccessOKButExceptionThrown() {
		DeviceListGetter getter = new DeviceListGetter() {
			@Override protected boolean isAccessDenied() { return false; }
		};
		getter.setRestServices(new RestServices() {
			@Override public ClientResponse getHandsets(String url, String accessId, String secretKey) throws IOException, ServletException {
				throw new IOException();
			}
		});
		DeviceList devices = new DeviceList();
		boolean ret = getter.populateDevices(devices, null, null, null);
		assertTrue("method should have return true", ret);
		assertTrue("list should have been empty",devices.getItems().isEmpty());
	}
	@Test
	public void populateDevicesTestWhenAccessOKButHttpResponseIsNotSuccess() {
		MyDeviceListGetter getter = new MyDeviceListGetter();
		getter.setRestServices(new RestServices() {
			@Override public ClientResponse getHandsets(String url, String accessId, String secretKey) throws IOException, ServletException {
				return new ClientResponse(1 + Constants.PM_RESPONSE_STATUS_SUCCESS,null,null,null);
			}
		});
		DeviceList devices = new DeviceList();
		boolean ret = getter.populateDevices(devices, null, null, null);
		assertTrue("method should have set accessDenied to true but did not", getter.isSetAccessDeniedTrueCalled());
		assertTrue("method should have returned false", !ret);
		assertTrue("list should have been empty",devices.getItems().isEmpty());
	}
}
