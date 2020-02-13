package com.mirth.connect.connectors.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.mirth.connect.donkey.util.DonkeyElement;

public class FileDispatcherPropertiesTest {

    // @formatter:off
    static final String properties3_6 = "<properties class=\"com.mirth.connect.connectors.file.FileDispatcherProperties\" version=\"3.6.0\">\n" + 
            "    <pluginProperties/>\n" + 
            "    <destinationConnectorProperties version=\"3.6.0\">\n" + 
            "      <queueEnabled>false</queueEnabled>\n" + 
            "      <sendFirst>false</sendFirst>\n" + 
            "      <retryIntervalMillis>10000</retryIntervalMillis>\n" + 
            "      <regenerateTemplate>false</regenerateTemplate>\n" + 
            "      <retryCount>0</retryCount>\n" + 
            "      <rotate>false</rotate>\n" + 
            "      <includeFilterTransformer>false</includeFilterTransformer>\n" + 
            "      <threadCount>1</threadCount>\n" + 
            "      <threadAssignmentVariable></threadAssignmentVariable>\n" + 
            "      <validateResponse>false</validateResponse>\n" + 
            "      <resourceIds class=\"linked-hash-map\">\n" + 
            "        <entry>\n" + 
            "          <string>Default Resource</string>\n" + 
            "          <string>[Default Resource]</string>\n" + 
            "        </entry>\n" + 
            "      </resourceIds>\n" + 
            "      <queueBufferSize>1000</queueBufferSize>\n" + 
            "      <reattachAttachments>true</reattachAttachments>\n" + 
            "    </destinationConnectorProperties>\n" + 
            "    <scheme>FILE</scheme>\n" + 
            "    <host>my-dir</host>\n" + 
            "    <outputPattern>my-file</outputPattern>\n" + 
            "    <anonymous>true</anonymous>\n" + 
            "    <username>anonymous</username>\n" + 
            "    <password>anonymous</password>\n" + 
            "    <timeout>10000</timeout>\n" + 
            "    <secure>true</secure>\n" + 
            "    <passive>true</passive>\n" + 
            "    <validateConnection>true</validateConnection>\n" + 
            "    <outputAppend>true</outputAppend>\n" + 
            "    <errorOnExists>false</errorOnExists>\n" + 
            "    <temporary>false</temporary>\n" + 
            "    <binary>false</binary>\n" + 
            "    <charsetEncoding>DEFAULT_ENCODING</charsetEncoding>\n" + 
            "    <template>${message.encodedData}</template>\n" + 
            "  </properties>\n" +
            "";
    
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
    public void testMigrate3_7_0() throws Exception {
        DonkeyElement donkeyElement = new DonkeyElement(properties3_6);
        FileDispatcherProperties props = new FileDispatcherProperties();
        props.migrate3_7_0(donkeyElement);

        assertEquals("true", donkeyElement.getChildElement("keepConnectionOpen").getTextContent());
        assertEquals("0", donkeyElement.getChildElement("maxIdleTime").getTextContent());
    }

    @Test
    public void testMigrate3_9_0() throws Exception {
    	// Test schemeProperties is added for SMB
    	DonkeyElement donkey = new DonkeyElement(propertiesBeforeMigrate3_9Smb);
    	assertNull(donkey.getChildElement("schemeProperties"));
    	FileDispatcherProperties props = new FileDispatcherProperties();
    	props.migrate3_9_0(donkey);
    	
    	assertNotNull(donkey.getChildElement("schemeProperties"));
    	assertEquals("SMB1", donkey.getChildElement("schemeProperties").getChildElement("smbMinVersion").getTextContent());
    	assertEquals("SMB311", donkey.getChildElement("schemeProperties").getChildElement("smbMaxVersion").getTextContent());
    	
    	// Test schemeProperties is not added
    	donkey = new DonkeyElement(propertiesBeforeMigrate3_9File);
    	assertNull(donkey.getChildElement("schemeProperties"));
    	props = new FileDispatcherProperties();
    	props.migrate3_9_0(donkey);
    	
    	assertNull(donkey.getChildElement("schemeProperties"));
    	
    	// Test schemeProperties is not overridden
    	donkey = new DonkeyElement(propertiesBeforeMigrate3_9Ftp);
    	assertNotNull(donkey.getChildElement("schemeProperties"));
    	props = new FileDispatcherProperties();
    	props.migrate3_9_0(donkey);
    	
    	DonkeyElement schemeProperties = donkey.getChildElement("schemeProperties");
    	assertNotNull(schemeProperties);
    	assertEquals("com.mirth.connect.connectors.file.FTPSchemeProperties", schemeProperties.getAttribute("class"));
    	assertEquals("testInitialCommands", schemeProperties.getChildElement("initialCommands").getChildElement("string").getTextContent());
    	assertNull(schemeProperties.getChildElement("smbVersion"));
    }
}
