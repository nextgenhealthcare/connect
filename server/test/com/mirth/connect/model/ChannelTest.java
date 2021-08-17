package com.mirth.connect.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.mirth.connect.donkey.util.DonkeyElement;

public class ChannelTest {

    // @formatter:off
    static final String properties3_12_0 = " <testXml>\n" +
            "<properties class=\"com.mirth.connect.connectors.file.FileDispatcherProperties\" version=\"3.6.0\">\n" + 
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
            "      <exportData>\n" + 
            "        <metadata>\n" + 
            "          <pruningSettings></pruningSettings>\n" + 
            "        </metadata>\n" + 
            "      </exportData>\n" + 
            "  </properties>\n" +
            "  </testXml>\n" +
            "";
    
    static final String properties3_12_0_negative = " <testXml>\n" +
            "<properties class=\"com.mirth.connect.connectors.file.FileDispatcherProperties\" version=\"3.6.0\">\n" + 
            "    <pluginProperties/>\n" + 
            "      <exportData>\n" + 
            "        <metadata>\n" + 
            "        </metadata>\n" + 
            "      </exportData>\n" + 
            "  </properties>\n" +
            "  </testXml>\n" +
            "";
    // @formatter:on 

    @Test
    public void testMigrate3_12_0() throws Exception {
        DonkeyElement donkey = new DonkeyElement(properties3_12_0);
        Channel props = new Channel();
        assertNotNull(donkey.getChildElement("properties"));
        props.migrate3_12_0(donkey);
        assertNotNull(donkey.getChildElement("properties").getChildElement("exportData"));
        assertNotNull(donkey.getChildElement("properties").getChildElement("exportData").getChildElement("metadata"));
        assertNotNull(donkey.getChildElement("properties").getChildElement("exportData").getChildElement("metadata").getChildElement("pruningSettings"));
        assertEquals("false", donkey.getChildElement("properties").getChildElement("exportData").getChildElement("metadata").getChildElement("pruningSettings").getChildElement("pruneErroredMessages").getTextContent());

        donkey = new DonkeyElement(properties3_12_0_negative);
        props.migrate3_12_0(donkey);
        assertNotNull(donkey.getChildElement("properties").getChildElement("exportData"));
        assertNotNull(donkey.getChildElement("properties").getChildElement("exportData").getChildElement("metadata"));
        assertNull(donkey.getChildElement("properties").getChildElement("exportData").getChildElement("metadata").getChildElement("pruningSettings"));

    }
}
