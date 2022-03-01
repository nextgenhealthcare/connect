/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.jdbc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.mirth.connect.donkey.util.DonkeyElement;

import junit.framework.Assert;

public class DatabaseReceiverTest {

    @Test
    public void testFixColumnName_Null() throws Exception {
        assertEquals("_", new DatabaseReceiver().fixColumnName(null));
    }

    @Test
    public void testFixColumnName_Empty() throws Exception {
        assertEquals("_", new DatabaseReceiver().fixColumnName(""));
    }

    @Test
    public void testFixColumnName_Blank() throws Exception {
        assertEquals("_", new DatabaseReceiver().fixColumnName("    "));
    }

    @Test
    public void testFixColumnName_InvalidStartChar() throws Exception {
        assertEquals("_abc", new DatabaseReceiver().fixColumnName(".abc"));
        assertEquals("_abc", new DatabaseReceiver().fixColumnName("-abc"));
        assertEquals("_abc", new DatabaseReceiver().fixColumnName("0abc"));
        assertEquals("_abc", new DatabaseReceiver().fixColumnName(" abc"));
    }

    @Test
    public void testFixColumnName_InvalidChar() throws Exception {
        assertEquals("ab_c", new DatabaseReceiver().fixColumnName("ab c"));
        assertEquals(":ab_c", new DatabaseReceiver().fixColumnName(":ab c"));
        assertEquals("ab_c", new DatabaseReceiver().fixColumnName("ab  c"));
        assertEquals("ab_c_", new DatabaseReceiver().fixColumnName("ab c "));
        assertEquals("_ab_c_", new DatabaseReceiver().fixColumnName("[ab c ]"));
    }

    @Test
    public void testFixColumnName_Valid() throws Exception {
        assertEquals("abc", new DatabaseReceiver().fixColumnName("abc"));
        assertEquals("a.bc", new DatabaseReceiver().fixColumnName("a.bc"));
        assertEquals("abc-", new DatabaseReceiver().fixColumnName("abc-"));
        assertEquals("azAZ09-_.", new DatabaseReceiver().fixColumnName("azAZ09-_."));
    }

    @Test
    public void testResultMapToXml_Valid() throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("abc", "test");
        resultMap.put("a.bc", "test");
        resultMap.put("abc-", "test");
        resultMap.put("azAZ09-_.", "test");

        DatabaseReceiver receiver = new DatabaseReceiver();
        receiver.connectorProperties = new DatabaseReceiverProperties();

        DonkeyElement result = new DonkeyElement(receiver.resultMapToXml(resultMap));
        List<String> tagNames = new ArrayList<String>();
        for (DonkeyElement child : result.getChildElements()) {
            tagNames.add(child.getTagName());
        }
        assertTrue(tagNames.contains("abc"));
        assertTrue(tagNames.contains("a.bc"));
        assertTrue(tagNames.contains("abc-"));
        assertTrue(tagNames.contains("azAZ09-_."));
    }

    @Test
    public void testResultMapToXml_Invalid() throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put(".abc", "test");
        resultMap.put("ab c", "test");
        resultMap.put("[ab c ]", "test");

        DatabaseReceiver receiver = new DatabaseReceiver();
        receiver.connectorProperties = new DatabaseReceiverProperties();

        DonkeyElement result = new DonkeyElement(receiver.resultMapToXml(resultMap));
        List<String> tagNames = new ArrayList<String>();
        for (DonkeyElement child : result.getChildElements()) {
            tagNames.add(child.getTagName());
        }
        assertTrue(tagNames.contains("_abc"));
        assertTrue(tagNames.contains("ab_c"));
        assertTrue(tagNames.contains("_ab_c_"));
    }

    @Test
    public void testCheckForDuplicateColumns() throws Exception {
        ResultSetMetaData metaData = mock(ResultSetMetaData.class);
        when(metaData.getColumnCount()).thenReturn(4);
        when(metaData.getColumnLabel(1)).thenReturn("a");
        when(metaData.getColumnLabel(2)).thenReturn("");
        when(metaData.getColumnName(2)).thenReturn("b");
        when(metaData.getColumnLabel(3)).thenReturn("c");
        when(metaData.getColumnLabel(4)).thenReturn(null);
        when(metaData.getColumnName(4)).thenReturn("d");

        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getMetaData()).thenReturn(metaData);

        DatabaseReceiver receiver = new DatabaseReceiver();

        // Should not cause an exception
        receiver.checkForDuplicateColumns(resultSet);

        when(metaData.getColumnName(4)).thenReturn("A");
        try {
            // Should cause an exception
            receiver.checkForDuplicateColumns(resultSet);
            fail("Exception should have been thrown");
        } catch (SQLException e) {
        }
    }

    @Test
    public void testProcessResultList() throws Exception {
        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("a", "test1");
        map.put("b", "test1");
        map.put("c", "test1");
        map.put("d", "test1");
        resultList.add(map);

        DatabaseReceiver receiver = spy(DatabaseReceiver.class);
        doNothing().when(receiver).processRecord(any());
        doReturn(false).when(receiver).isTerminated();

        // Should not cause an exception
        receiver.processResultList(resultList);

        map.put("A", "test1");
        try {
            // Should cause an exception
            receiver.processResultList(resultList);
            fail("Exception should have been thrown");
        } catch (DatabaseReceiverException e) {
        }
    }
    
    @Test
    public void testprocessResultList() throws Exception {
        
        TestDatabaseReceiver receiver = new TestDatabaseReceiver();
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        
        HashMap<String, Object> map1 = new HashMap<>();
        
        //put upper case key in map
        map1.put("KEY1", new Object());
        list.add(map1);
 
        receiver.processResultList(list);
    }
    
    
    public class TestDatabaseReceiver extends DatabaseReceiver {
        
        @Override
        protected void processRecord(Map<String, Object> resultMap) throws InterruptedException, DatabaseReceiverException {
            Set keySet = resultMap.keySet();
            
            //check to see if key is now lower case in map 
            Assert.assertTrue(keySet.contains("key1"));
        }
        
        public boolean isTerminated() {
            return false;
        }
    }
}
