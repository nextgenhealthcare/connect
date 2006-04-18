package com.webreach.mirth.core.tests;

import com.webreach.mirth.core.Transformer;

import junit.framework.TestCase;

public class TransformerTest extends TestCase {

	private Transformer transformer;
	
	protected void setUp() throws Exception {
		super.setUp();
		transformer = new Transformer();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testAddScript() {
		transformer.getVariables().put("dob", "msg.PID['PID.5']['XPN.6']");
		assertEquals(1, transformer.getVariables().size());
	}
	
	public void testGetMappingScript() {
		transformer.setType(Transformer.Type.MAPPING);
		transformer.getVariables().put("firstName", "msg.PID['PID.5']['XPN.1']");
		transformer.getVariables().put("lastName", "msg.PID['PID.5']['XPN.2']");
		String script = "map.put('firstName', msg.PID['PID.5']['XPN.1']);" + "\n" + "map.put('lastName', msg.PID['PID.5']['XPN.2']);";

		assertEquals(script, transformer.getScript());
	}

	public void testGetXSLTScript() {
		transformer.setType(Transformer.Type.XSLT);
		transformer.getVariables().put("xslt", "");
		String script = "";
		
		assertEquals(script, transformer.getScript());
	}
	
	public void testGetScript() {
		transformer.setType(Transformer.Type.SCRIPT);
		transformer.getVariables().put("script", "msg.PID['PID.5']['XPN.1'] == 'test';");
		String script = "msg.PID['PID.5']['XPN.1'] == 'test';";
		
		assertEquals(script, transformer.getScript());
	}

}
