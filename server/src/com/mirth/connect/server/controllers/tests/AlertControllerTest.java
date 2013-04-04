/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.controllers.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.mirth.connect.model.Alert;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerException;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.tools.ScriptRunner;
import com.mirth.connect.server.util.UUIDGenerator;

public class AlertControllerTest extends TestCase {
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private List<Alert> sampleAlertList;

    protected void setUp() throws Exception {
        super.setUp();

        // clear all database tables
        ScriptRunner.runScript(new File("conf/" + ControllerTestSuite.database + "/" + ControllerTestSuite.database + "-database.sql"));
        sampleAlertList = new ArrayList<Alert>();

        for (int i = 0; i < 10; i++) {
            Alert sampleAlert = new Alert();
            sampleAlert.setId(UUIDGenerator.getUUID());
            sampleAlert.setName("Sample Alert" + i);
            sampleAlert.setEnabled(true);
            sampleAlert.setExpression("exception");
            sampleAlert.setTemplate("template");

            for (int j = 0; j < 10; j++) {
                sampleAlert.getChannels().add("channel" + String.valueOf(j));
                sampleAlert.getEmails().add("test" + j + "@test.com");
            }

            sampleAlertList.add(sampleAlert);
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetAlert() throws ControllerException {
        insertSampleAlerts();

        for (Iterator<Alert> iter = sampleAlertList.iterator(); iter.hasNext();) {
            Alert sampleAlert = iter.next();
            List<Alert> testAlertList = alertController.getAlert(sampleAlert);

            Assert.assertFalse(testAlertList.isEmpty());
        }
    }

    public void testGetAlertByChannelId() throws ControllerException {
        insertSampleAlerts();

        for (Iterator<Alert> iter = sampleAlertList.iterator(); iter.hasNext();) {
            Alert sampleAlert = iter.next();
            List<Alert> testAlertList = alertController.getAlertByChannelId("channel0");

            Assert.assertFalse(testAlertList.isEmpty());
            Assert.assertEquals(10, testAlertList.size());
        }
    }

    public void testUpdateAlert() throws ControllerException {
        insertSampleAlerts();

        List<Alert> testAlertList = alertController.getAlert(null);

        Assert.assertEquals(10, testAlertList.size());
    }

    public void testRemoveAlert() throws ControllerException {
        insertSampleAlerts();

        Alert sampleAlert = sampleAlertList.get(0);
        alertController.removeAlert(sampleAlert);
        List<Alert> testAlertList = alertController.getAlert(null);

        Assert.assertFalse(testAlertList.contains(sampleAlert));
    }

    public void testRemoveAllAlerts() throws ControllerException {
        insertSampleAlerts();

        alertController.removeAlert(null);
        List<Alert> testAlertList = alertController.getAlert(null);

        Assert.assertTrue(testAlertList.isEmpty());
    }

    public void insertSampleAlerts() throws ControllerException {
        alertController.updateAlerts(sampleAlertList);
    }
}
