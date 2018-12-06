package com.mirth.connect.connectors.file;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.apache.commons.io.FileUtils;
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
    
    static final String expectedProperties3_7 = "<properties class=\"com.mirth.connect.connectors.file.FileDispatcherProperties\" version=\"3.7.0\">\n" + 
            "    <pluginProperties/>\n" + 
            "    <destinationConnectorProperties version=\"3.7.0\">\n" + 
            "      <queueEnabled>false</queueEnabled>\n" + 
            "      <sendFirst>false</sendFirst>\n" + 
            "      <retryIntervalMillis>10000</retryIntervalMillis>\n" + 
            "      <regenerateTemplate>false</regenerateTemplate>\n" + 
            "      <retryCount>0</retryCount>\n" + 
            "      <rotate>false</rotate>\n" + 
            "      <includeFilterTransformer>false</includeFilterTransformer>\n" + 
            "      <threadCount>1</threadCount>\n" + 
            "      <threadAssignmentVariable/>\n" + 
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
            "    <keepConnectionOpen>true</keepConnectionOpen>\n" + 
            "    <maxIdleTime>0</maxIdleTime>\n" + 
            "  </properties>\n" + 
            "";
    // @formatter:on

    @Test
    public void testMigrate3_7_0() throws Exception {
        DonkeyElement oldDonkey = new DonkeyElement(properties3_6);
        FileDispatcherProperties props = new FileDispatcherProperties();
        props.migrate3_7_0(oldDonkey);
        DonkeyElement newDonkey = new DonkeyElement(expectedProperties3_7);

        assertEquals(expectedProperties3_7, newDonkey.toXml());
    }

}
