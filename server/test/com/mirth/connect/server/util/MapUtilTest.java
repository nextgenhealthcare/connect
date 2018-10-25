/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.util;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.client.core.Version;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.MapUtil;
import com.mirth.connect.model.converters.ObjectXMLSerializer;
import com.mirth.connect.server.userutil.DatabaseConnection;
import com.mirth.connect.server.userutil.DatabaseConnectionFactory;

public class MapUtilTest {

    @BeforeClass
    public static void setup() {
        try {
            ObjectXMLSerializer.getInstance().init(Version.getLatest().toString());
        } catch (Exception e) {
            // Ignore if it has already been initialized
        }
    }

    @Test
    public void testSerializableValue() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        MessageContent messageContent = new MessageContent(UUID.randomUUID().toString(), 1, 0, ContentType.RAW, "test", "RAW", false);
        map.put("test", messageContent);

        String result = MapUtil.serializeMap(ObjectXMLSerializer.getInstance(), map);

        DonkeyElement resultElement = new DonkeyElement(result);
        DonkeyElement entry = resultElement.getChildElement("entry");
        assertEquals("string", entry.getChildElements().get(0).getNodeName());
        assertEquals("test", entry.getChildElements().get(0).getTextContent());
        assertEquals(MessageContent.class.getName(), entry.getChildElements().get(1).getNodeName());
    }

    @Test
    public void testNonSerializableValue() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        Socket socket = new Socket();
        map.put("socket", socket);

        String result = MapUtil.serializeMap(ObjectXMLSerializer.getInstance(), map);

        DonkeyElement resultElement = new DonkeyElement(result);
        DonkeyElement entry = resultElement.getChildElement("entry");
        assertEquals("string", entry.getChildElements().get(0).getNodeName());
        assertEquals("socket", entry.getChildElements().get(0).getTextContent());
        assertEquals("string", entry.getChildElements().get(1).getNodeName());
        assertEquals(socket.toString(), entry.getChildElements().get(1).getTextContent());
    }

    @Test
    public void testNonSerializableValue2() throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();

        String database = "MapUtilTestDB";
        DatabaseConnection dbConn = new DatabaseConnectionFactory(null).createDatabaseConnection("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:" + database + ";create=true");
        try {
            map.put("dbConn", dbConn);

            String result = MapUtil.serializeMap(ObjectXMLSerializer.getInstance(), map);

            DonkeyElement resultElement = new DonkeyElement(result);
            DonkeyElement entry = resultElement.getChildElement("entry");
            assertEquals("string", entry.getChildElements().get(0).getNodeName());
            assertEquals("dbConn", entry.getChildElements().get(0).getTextContent());
            assertEquals("string", entry.getChildElements().get(1).getNodeName());
            assertEquals(dbConn.toString(), entry.getChildElements().get(1).getTextContent());
        } finally {
            try {
                FileUtils.deleteDirectory(new File(database));
            } catch (IOException e) {
                System.out.println("Unable to delete testing derby database MapUtilTestDB.");
            }
        }
    }
}
