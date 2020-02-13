package com.mirth.connect.connectors.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.mirth.connect.donkey.util.DonkeyElement;

public class FileReceiverPropertiesTest {
	
	// @formatter:off
	static final String propertiesBeforeMigrate3_9Smb = "<properties>\n" + 
    		"	<scheme>smb</scheme>\n" + 
    		"</properties>";
    
    static final String propertiesBeforeMigrate3_9File = "<properties>\n" + 
    		"	<scheme>file</scheme>\n" + 
    		"</properties>";
    
    static final String propertiesBeforeMigrate3_9Ftp = "<properties>\n" + 
    		"	<scheme>ftp</scheme>\n" + 
    		"	<schemeProperties class=\"com.mirth.connect.connectors.file.FTPSchemeProperties\">\n" + 
    		"        <initialCommands>\n" + 
    		"          <string>testInitialCommands</string>\n" + 
    		"        </initialCommands>\n" + 
    		"     </schemeProperties>\n" + 
    		"</properties>";
    // @formatter:on
	
	@Test
	public void testMigrate3_9_0() throws Exception {
		// Test schemeProperties is added for SMB
		DonkeyElement donkey = new DonkeyElement(propertiesBeforeMigrate3_9Smb);
		assertNull(donkey.getChildElement("schemeProperties"));
		FileReceiverProperties props = new FileReceiverProperties();
		props.migrate3_9_0(donkey);

		assertNotNull(donkey.getChildElement("schemeProperties"));
		assertEquals("SMB1", donkey.getChildElement("schemeProperties").getChildElement("smbMinVersion").getTextContent());
		assertEquals("SMB311", donkey.getChildElement("schemeProperties").getChildElement("smbMaxVersion").getTextContent());

		// Test schemeProperties is not added
		donkey = new DonkeyElement(propertiesBeforeMigrate3_9File);
		assertNull(donkey.getChildElement("schemeProperties"));
		props = new FileReceiverProperties();
		props.migrate3_9_0(donkey);

		assertNull(donkey.getChildElement("schemeProperties"));

		// Test schemeProperties is not overridden
		donkey = new DonkeyElement(propertiesBeforeMigrate3_9Ftp);
		assertNotNull(donkey.getChildElement("schemeProperties"));
		props = new FileReceiverProperties();
		props.migrate3_9_0(donkey);

		DonkeyElement schemeProperties = donkey.getChildElement("schemeProperties");
		assertNotNull(schemeProperties);
		assertEquals("com.mirth.connect.connectors.file.FTPSchemeProperties", schemeProperties.getAttribute("class"));
		assertEquals("testInitialCommands",
				schemeProperties.getChildElement("initialCommands").getChildElement("string").getTextContent());
		assertNull(schemeProperties.getChildElement("smbVersion"));
	}
}
