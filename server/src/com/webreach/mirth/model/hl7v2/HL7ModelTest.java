package com.webreach.mirth.model.hl7v2;

import junit.framework.TestCase;

import org.junit.Assert;

public class HL7ModelTest extends TestCase{
	
	
	public void testGetMessageDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getMessageDescription("25", "ADTA01");
		System.out.println(name);
		Assert.assertEquals(name, "Admit/Visit Notification");
	}
	public void testGetSegmentDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getSegmentDescription("25", "PID");
		System.out.println(name);
		Assert.assertEquals(name, "Patient Identification");
	}
	public void testGetSegmentFieldDescriptionIndexWithDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getSegmentFieldDescription("25", "PID", 5, true);
		System.out.println(name);
		Assert.assertEquals(name, "Patient Name [XPN]");
	}
	public void testGetSegmentFieldDescriptionIndexWithOutDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getSegmentFieldDescription("25", "SCH", 5, false);
		System.out.println(name);
		Assert.assertEquals(name, "Schedule ID");
	}
	public void testGetSegmentFieldDescriptionNoIndexWithDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getSegmentFieldDescription("25", "OBX.5", true);
		System.out.println(name);
		Assert.assertEquals(name, "Observation Value [ST]");
	}
	public void testGetSegmentFieldDescriptionNoIndexWithOutDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getSegmentFieldDescription("25", "PV1.5", false);
		System.out.println(name);
		Assert.assertEquals(name, "Preadmit Number");
	}
	public void testGetCompositeDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getCompositeDescription("25", "XPN");
		System.out.println(name);
		Assert.assertEquals(name, "Extended Person Name");
	}
	public void testGetCompositeFieldDescriptionWithIndexWithDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getCompositeFieldDescription("25", "XPN", 1, true);
		System.out.println(name);
		Assert.assertEquals(name, "Family Name [FN]");
	}
	public void testGetCompositeFieldDescriptionWithIndexWithNoDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getCompositeFieldDescription("25", "XPN", 1, false);
		System.out.println(name);
		Assert.assertEquals(name, "Family Name");
	}
	public void testGetCompositeFieldDescriptionWithNoIndexWithDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getCompositeFieldDescription("25", "XPN.2", true);
		System.out.println(name);
		Assert.assertEquals(name, "Given Name [ST]");
	}
	public void testGetCompositeFieldDescriptionWithNoIndexWithNoDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getCompositeFieldDescription("25", "XPN.3", false);
		System.out.println(name);
		Assert.assertEquals(name, "Second and Further Given Names or Initials Thereof");
	}

	public void testGetCompositeFieldDescriptionWithSegmentWithDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getCompositeFieldDescriptionWithSegment("231", "PID.5.1", true);
		System.out.println(name);
		Assert.assertEquals(name, "Family Last Name [FN]");
	}
	public void testGetCompositeFieldDescriptionWithSegmentWithNoDescription() throws ClassNotFoundException, InstantiationException, IllegalAccessException{
		String name = Component.getCompositeFieldDescriptionWithSegment("25", "OBX.3.1", false);
		System.out.println(name);
		Assert.assertEquals(name, "Identifier");
	}
		
}
