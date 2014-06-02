package com.perfectomobile.jenkins.services;

//import junit.framework.Assert;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import hudson.console.ConsoleNote;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.Cause;

import org.json.simple.parser.ParseException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.perfectomobile.jenkins.Constants;
import com.perfectomobile.jenkins.miscel.InvalidParametersSpecificationException;
import com.perfectomobile.jenkins.services.MobileCloudServicesFactory;
import com.perfectomobile.jenkins.services.ScriptExecutionServices;

public class ScriptExecutionServicesTest {
	
	//the next set passes the arguments that are passed when doing script execution (as opposed to obtaining the map for initializing the next script's parameters
	
	private static ScriptExecutionServices _scriptExecutionServices;
	private static BuildListener _myBuildListener;
	
	@SuppressWarnings("serial")
	static class MyBuildListener implements BuildListener {

		public PrintStream getLogger() {
			
			return new PrintStream(System.out) {
				@Override public void println(String x) { }
			};
		}

		@SuppressWarnings("rawtypes")
		public void annotate(ConsoleNote ann) throws IOException { }
		public void hyperlink(String url, String text) throws IOException { }
		public PrintWriter error(String msg) { return null; }
		public PrintWriter error(String format, Object... args) { return null; }
		public PrintWriter fatalError(String msg) {  return null; }
		public PrintWriter fatalError(String format, Object... args) { return null; }
		public void started(List<Cause> causes) { }
		public void finished(Result result) { }
	}


	@BeforeClass
	public static void initBeforeAllTests() {
		_scriptExecutionServices = MobileCloudServicesFactory.getInstance().getScriptExecutionServices();
		_myBuildListener = new MyBuildListener();
	}
	
	@Test
	public void testExecGetExecutionParametersWithoutParameters(){
		
		String params = "";
		
		MultivaluedMap<String, String> paramMap;
		try {
			paramMap = _scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			assertTrue("returned a non empty map", paramMap.isEmpty());
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
		
		
	}
	
	@Test
	public void testExecGetExecutionParametersOneParameter(){
		
		String paramName = "parameter1";
		String val = "dolphine";
		String params = paramName + "(String)="+val;
		
		try {
			MultivaluedMap <String, String> paramMap = _scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			
			String expectedKey = Constants.PM_EXEC_PARAMETER_PREFIX + paramName; 
			assertTrue("returned an empty map", !paramMap.isEmpty());
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));
			assertTrue("the map does not contain the expected value for key "+expectedKey, val.equals(paramMap.get(expectedKey).get(0)));
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
	}

/*
	@Test
	public void testExecGetExecutionParametersMultiParameter(){
		
		String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator") +
				"" + System.getProperty("line.separator") + //empty lines allowed
				"parameter2(" +Constants.PARAM_TYPE_MEDIA+")=Jonathan;file\\path.jpg" + System.getProperty("line.separator");
		
		try {
			MultivaluedMap <String, String> paramMap = _scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			
			String expectedKey = Constants.PM_EXEC_PARAMETER_PREFIX + "parameter1";
			assertTrue("returned an empty map", !paramMap.isEmpty() );
			assertTrue("returned a map with wrong number of entries "+paramMap.size() , paramMap.size() == 2);
			
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));		
			assertTrue("the map does not contain the expected value for key "+expectedKey, "Dolphine".equals(paramMap.get(expectedKey).get(0)));
			
			expectedKey = Constants.PM_EXEC_PARAMETER_PREFIX + "parameter2";
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));		
			assertTrue("the map does not contain the expected value for key "+expectedKey, "Jonathan".equals(paramMap.get(expectedKey).get(0)));
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
	}
*/
	
	@Test
	public void testExecValueIsBlankOnLine() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1(String)=   " + System.getProperty("line.separator");
		try {
			MultivaluedMap <String, String> paramMap = _scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();

			String expectedKey = Constants.PM_EXEC_PARAMETER_PREFIX + "parameter1"; 
			assertTrue("returned an empty map", !paramMap.isEmpty());
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));
			assertTrue("the map does not contain the expected value for key "+expectedKey, "   ".equals(paramMap.get(expectedKey).get(0)));
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
	}
	@Test
	public void testExecValueStartsWithBlankOnLine() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1(String)=  ab" + System.getProperty("line.separator");
		try {
			MultivaluedMap <String, String> paramMap = _scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();

			String expectedKey = Constants.PM_EXEC_PARAMETER_PREFIX + "parameter1"; 
			assertTrue("returned an empty map", !paramMap.isEmpty());
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));
			assertTrue("the map does not contain the expected value for key "+expectedKey, "  ab".equals(paramMap.get(expectedKey).get(0)));
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
	}
	@Test
	public void testExecValueEndsWithBlankOnLine() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1(String)=ab   " + System.getProperty("line.separator");
		try {
			MultivaluedMap <String, String> paramMap = _scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			String expectedKey = Constants.PM_EXEC_PARAMETER_PREFIX + "parameter1"; 
			assertTrue("returned an empty map", !paramMap.isEmpty());
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));
			assertTrue("the map does not contain the expected value for key "+expectedKey, "ab   ".equals(paramMap.get(expectedKey).get(0)));
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
	}
	@Test
	public void testExecValueStartsAndEndsWithBlankOnLine() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1(String)=  ab   " + System.getProperty("line.separator");
		try {
			MultivaluedMap <String, String> paramMap = _scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();

			String expectedKey = Constants.PM_EXEC_PARAMETER_PREFIX + "parameter1"; 
			assertTrue("returned an empty map", !paramMap.isEmpty());
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));
			assertTrue("the map does not contain the expected value for key "+expectedKey, "  ab   ".equals(paramMap.get(expectedKey).get(0)));
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
	}

/* all tests that do validation of the script params at configuration time are disabled
 * because we may decide to re-do the validation, in which case the tests would be enabled again	
 */

/*
	
	//error detection
	
	@Test
	public void testExecMissingEverythingOnLine() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "=" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing the parameter name detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}	
	
	
	@Test
	public void testExecMissingValueOnLine1() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1(String)=" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing the value detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	
	@Test
	public void testExecMissingParamName1OnLine() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "(String)=Dolphine" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing the parameter name detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingParamName1OnLine2() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "   (String)=Dolphine" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing the parameter name detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingParamTypeOnLine1() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1()=Dolphine" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing the parameter type detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingParamTypeOnLine2() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1(=Dolphine" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing the parameter type detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingParamTypeOnLine3() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1)=Dolphine" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing '(' after the param name before its type detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingParamTypeOnLine4() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1=Dolphine" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing '(' after the param name before its type detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingParamTypeOnLine5() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1(   )=Dolphine" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Missing the parameter type detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecWrongSyntaxParamNameTypeOnLine1() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1(String) and=Dolphine" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Invalid syntax after the parameter name and type... detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	
	@Test
	public void testExecMissingRepositoryKeyForMediaParam1() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1("+Constants.PARAM_TYPE_MEDIA+")=;somthing" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Syntax Error - The value should contain both a repository key and a local file path separated by a  ';' detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingRepositoryKeyForMediaParam2() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1("+Constants.PARAM_TYPE_MEDIA+")=   ;somthing" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Syntax Error - The value should contain both a repository key and a local file path separated by a  ';' detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingLocalfileForMediaParam1() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1("+Constants.PARAM_TYPE_MEDIA+")=somthing" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Syntax Error - The value should contain both a repository key and a local file path separated by a  ';' detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingLocalfileForMediaParam2() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1("+Constants.PARAM_TYPE_MEDIA+")=somthing;" + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Syntax Error - The value should contain both a repository key and a local file path separated by a  ';' detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	@Test
	public void testExecMissingLocalfileForMediaParam3() { 
		//String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator");
		String params = "parameter1("+Constants.PARAM_TYPE_MEDIA+")=somthing;   " + System.getProperty("line.separator");
		try {
			_scriptExecutionServices.parseScriptsParamsList(params,true).getScriptParams();
			fail("should have thrown exception");
		} catch( InvalidParametersSpecificationException ipe) {
			dbg(ipe.getMessage());
			assertTrue("wrong exception thrown "+ipe.getMessage(), "Syntax Error - The value should contain both a repository key and a local file path separated by a  ';' detected on line 1 of the Parameters List text".equals(ipe.getMessage()));
		}
	}
	

	//the next set of params is for when we parse the params list in order to obtain values for the next script
*/
	@Test
	public void testParseGetExecutionParametersMultiParameter(){
		
		String params = "parameter1(String)=Dolphine" + System.getProperty("line.separator") +
				"" + System.getProperty("line.separator") + //empty lines allowed
				"parameter2(" +Constants.PARAM_TYPE_MEDIA+")=Jonathan;file\\path.jpg" + System.getProperty("line.separator");
		
		try {
			MultivaluedMap <String, String> paramMap = _scriptExecutionServices.parseScriptsParamsList(params,false).getScriptParams();
			
			String expectedKey = "parameter1(String)";
			assertTrue("returned an empty map", !paramMap.isEmpty() );
			assertTrue("returned a map with wrong number of entries "+paramMap.size() , paramMap.size() == 2);
			
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));		
			assertTrue("the map does not contain the expected value for key "+expectedKey, "Dolphine".equals(paramMap.get(expectedKey).get(0)));
			
			expectedKey = "parameter2(" +Constants.PARAM_TYPE_MEDIA+")";
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));		
			assertTrue("the map does not contain the expected value for key "+expectedKey, "Jonathan;file\\path.jpg".equals(paramMap.get(expectedKey).get(0)));
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
	}

	//error detection for parsing - make sure no exception is thrown but the line is ignored if it has an error
	
	@Test
	public void testParseLineIgnoredWhenHasError() { 
		String params = "parameter1(string=Dolphine" + System.getProperty("line.separator") + //error
				"" + System.getProperty("line.separator") + //empty lines allowed  
				"parameter2(" +Constants.PARAM_TYPE_MEDIA+")=Jonathan;file\\path.jpg" + System.getProperty("line.separator") +
				"parameter3(" +Constants.PARAM_TYPE_MEDIA+")=Jonathan  file\\path.jpg" + System.getProperty("line.separator"); //error
		
		try {
			MultivaluedMap <String, String> paramMap = _scriptExecutionServices.parseScriptsParamsList(params,false).getScriptParams();
			
			String expectedKey = "parameter2(" +Constants.PARAM_TYPE_MEDIA+")";
			assertTrue("returned an empty map", !paramMap.isEmpty() );
			assertTrue("returned a map with wrong number of entries "+paramMap.size() , paramMap.size() == 2);
			
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));		
			assertTrue("the map does not contain the expected value for key "+expectedKey, "Jonathan;file\\path.jpg".equals(paramMap.get(expectedKey).get(0)));
			
			expectedKey = "parameter3(" +Constants.PARAM_TYPE_MEDIA+")";
			assertTrue("the returned map does not contain the expected key "+ expectedKey, paramMap.containsKey(expectedKey));		
			assertTrue("the map does not contain the expected value for key "+expectedKey, "Jonathan  file\\path.jpg".equals(paramMap.get(expectedKey).get(0)));
		} catch (InvalidParametersSpecificationException e) {
			fail("threw exception: "+e);
		}
	}
		
	//---- tests of the getJobStatus() method -----
	
	@Test
	public void testGetJobStatusWhenCant() {
		
		String response = "";
		try {
			_scriptExecutionServices.getJobStatus(response, _myBuildListener);
			fail("should have thrown exception");
		} catch (ParseException e) {
		}
	}
	
	@Test
	public void testGetJobStatusWhenStillRunning() {
		
		int expResult = ScriptExecutionServices.JOB_STATUS_RUNNING;
		String response = "{\"flowEndCode\":\"Failed\",\"status\":\"Running\"}"; 
		try {
			int result = _scriptExecutionServices.getJobStatus(response, _myBuildListener);
			assertTrue("Got wrong result: "+result, result == expResult);
		} catch (ParseException e) {
			fail("should not have thrown the exception: "+ e);
		}
	}
	@Test
	public void testGetJobStatusWhenFinishedAndSucceded() {
		
		int expResult = ScriptExecutionServices.JOB_STATUS_SUCCESS;
		String response = "{\"flowEndCode\":\"Success\",\"status\":\"Completed\"}"; 
		try {
			int result = _scriptExecutionServices.getJobStatus(response, _myBuildListener);
			assertTrue("Got wrong result: "+result, result == expResult);
		} catch (ParseException e) {
			fail("should not have thrown the exception: "+ e);
		}
	}
	@Test
	public void testGetJobStatusWhenFinishedButFailed() {
		
		int expResult = ScriptExecutionServices.JOB_STATUS_FAILED;
		String response = "{\"flowEndCode\":\"Failed\",\"status\":\"Completed\"}"; 
		try {
			int result = _scriptExecutionServices.getJobStatus(response, _myBuildListener);
			assertTrue("Got wrong result: "+result, result == expResult);
		} catch (ParseException e) {
			fail("should not have thrown the exception: "+ e);
		}
	}
}
