/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.UUID;

import org.apache.ibatis.session.SqlSessionManager;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DebugUsage;

import junit.framework.Assert;
import junit.framework.TestCase;

public class DebugUsageControllerTest extends TestCase {

    UUID uuid = UUID.randomUUID();
    
    protected void setUp() throws Exception {
    	
    }

    protected SqlSessionManager getReadOnlySqlSessionManager() {
    	return mock(SqlSessionManager.class);
    }
        
    public void testGetDebugUsageIsNull() throws ControllerException {
    	DebugUsageController debugUsageController = mock(DebugUsageController.class);
    	when(debugUsageController.getDebugUsage(anyString())).thenReturn(null);
        DebugUsage testDebugUsage = debugUsageController.getDebugUsage(uuid.toString());
        Assert.assertEquals(null, testDebugUsage);
    }
    
    public void TestInsertDebugUsage() throws ControllerException {
    	DebugUsageController debugUsageController = mock(DebugUsageController.class);
    	DebugUsage dummyDebugUsage = new DebugUsage();
    	dummyDebugUsage.setAttachBatchCount(1);
    	dummyDebugUsage.setDestinationFilterTransCount(1);
    	dummyDebugUsage.setInvocationCount(1);
        when(debugUsageController.upsertDebugUsage(any())).thenReturn(true);
    	when(debugUsageController.getDebugUsage(anyString())).thenReturn(dummyDebugUsage);
        DebugUsage testDebugUsage = debugUsageController.getDebugUsage(uuid.toString());
        debugUsageController.upsertDebugUsage(testDebugUsage);

        Assert.assertEquals(0, (int) testDebugUsage.getDuppCount());
        Assert.assertEquals(1, (int) testDebugUsage.getAttachBatchCount());
        Assert.assertEquals(0, (int) testDebugUsage.getSourceConnectorCount());
        Assert.assertEquals(0, (int) testDebugUsage.getSourceFilterTransCount());
        Assert.assertEquals(1, (int) testDebugUsage.getDestinationFilterTransCount());
        Assert.assertEquals(0, (int) testDebugUsage.getDestinationConnectorCount());
        Assert.assertEquals(0, (int) testDebugUsage.getResponseCount());
        Assert.assertEquals(1, (int) testDebugUsage.getInvocationCount());
        
    }  

}
