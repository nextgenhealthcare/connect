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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.apache.ibatis.session.SqlSessionManager;

import com.mirth.connect.client.core.ControllerException;
import com.mirth.connect.model.DebugUsage;
import com.mirth.connect.server.tools.ScriptRunner;
import com.mirth.connect.server.util.SqlConfig;
import com.mirth.connect.util.PropertyLoader;

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
        Assert.assertEquals(testDebugUsage, null);
    }
    
    public void TestInsertDebugUsage() throws ControllerException {
    	DebugUsageController debugUsageController = mock(DebugUsageController.class);
    	DebugUsage dummyDebugUsage = new DebugUsage();
    	dummyDebugUsage.setAttachBatchCount(1);
    	dummyDebugUsage.setDestinationFilterTransCount(1);
    	dummyDebugUsage.setInvocationCount(1);
    	when(debugUsageController.getDebugUsage(anyString())).thenReturn(dummyDebugUsage);
        when(debugUsageController.upsertDebugUsage(any())).thenReturn(true);
        DebugUsage testDebugUsage = debugUsageController.getDebugUsage(uuid.toString());
        debugUsageController.upsertDebugUsage(testDebugUsage);

        Assert.assertEquals((int) testDebugUsage.getDuppCount(), 0);
        Assert.assertEquals((int) testDebugUsage.getAttachBatchCount(), 1);
        Assert.assertEquals((int) testDebugUsage.getSourceConnectorCount(), 0);
        Assert.assertEquals((int) testDebugUsage.getSourceFilterTransCount(), 0);
        Assert.assertEquals((int) testDebugUsage.getDestinationFilterTransCount(), 1);
        Assert.assertEquals((int) testDebugUsage.getDestinationConnectorCount(), 0);
        Assert.assertEquals((int) testDebugUsage.getResponseCount(), 0);
        Assert.assertEquals((int) testDebugUsage.getInvocationCount(), 1);
        Assert.assertEquals((Calendar) testDebugUsage.getLastSent(), null);
        
    }  

}
