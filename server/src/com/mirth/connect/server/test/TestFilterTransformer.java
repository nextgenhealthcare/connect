/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.donkey.server.channel.components.FilterTransformer;
import com.mirth.connect.model.converters.xml.DefaultXMLSerializer;
import com.mirth.connect.server.Mirth;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.transformers.JavaScriptFilterTransformer;
import com.mirth.connect.server.util.JavaScriptUtil;

public class TestFilterTransformer {
    private final static String TEST_HL7_MESSAGE = "MSH|^~\\&|LABNET|Acme Labs|||20090601105700||ORU^R01|HMCDOOGAL-0088|D|2.2\rPID|1|8890088|8890088^^^72777||McDoogal^Hattie^||19350118|F||2106-3|100 Beach Drive^Apt. 5^Mission Viejo^CA^92691^US^H||(949) 555-0025|||||8890088^^^72|604422825\rPV1|1|R|C3E^C315^B||||2^HIBBARD^JULIUS^|5^ZIMMERMAN^JOE^|9^ZOIDBERG^JOHN^|CAR||||4|||2301^OBRIEN, KEVIN C|I|1783332658^1^1||||||||||||||||||||DISNEY CLINIC||N|||20090514205600\rORC|RE|928272608|056696716^LA||CM||||20090601105600||||  C3E|||^RESULT PERFORMED\rOBR|1|928272608|056696716^LA|1001520^K|||20090601101300|||MLH25|||HEMOLYZED/VP REDRAW|20090601102400||2301^OBRIEN, KEVIN C||||01123085310001100100152023509915823509915800000000101|0000915200932|20090601105600||LAB|F||^^^20090601084100^^ST~^^^^^ST\rOBX|1|NM|1001520^K||5.3|MMOL/L|3.5-5.5||||F|||20090601105600|IIM|IIM\r";
    private final static String CHANNEL_ID = "testchannel";
    private final static int SCRIPT_SLEEP_MILLIS = 2000;
    private final static int PERFORMANCE_TEST_SIZE = 1000000;
    private final static String TEST_SCRIPT_ID = "testscript";
    private final static String PERFORMANCE_SCRIPT_ID = "performancescript";
    private final static String CONNECTOR_NAME = "testconnector";
    
    private static Mirth server = new Mirth();
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private static FilterTransformer filterTransformer;
    private static FilterTransformer filterTransformerPerformance;
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                server.run();
            }
        });
        
        while (ConfigurationController.getInstance().isEngineStarting()) {
            Thread.sleep(100);
        }
        
        StringBuilder script = new StringBuilder();
        script.append("var start = new Date().getTime();");
        script.append("for (var i = 0; i < 1e7; i++) {");
        script.append("if ((new Date().getTime() - start) > " + SCRIPT_SLEEP_MILLIS + ") break;");
        script.append("} return false;");
        
        JavaScriptUtil.compileAndAddScript(TEST_SCRIPT_ID, script.toString());
        
        script = new StringBuilder();
        script.append("var result;");
        script.append("for (var i = 0; i < " + PERFORMANCE_TEST_SIZE + "; i++) {");
        script.append("result = Math.sqrt(i);");
        script.append("} return false;");
        
        JavaScriptUtil.compileAndAddScript(PERFORMANCE_SCRIPT_ID, script.toString());

        initJavaScriptFilterTransformer();
    }
    
    private static void initJavaScriptFilterTransformer() throws Exception {
        filterTransformer = new JavaScriptFilterTransformer(CHANNEL_ID, CONNECTOR_NAME, TEST_SCRIPT_ID, null);
        filterTransformerPerformance = new JavaScriptFilterTransformer(CHANNEL_ID, CONNECTOR_NAME, PERFORMANCE_SCRIPT_ID, null);
    }
    
    @Test
    public final void testDoFilterTransform() throws Exception {
        ConnectorMessage connectorMessage = createConnectorMessage(CHANNEL_ID, 1, 0);
        long startTime = System.currentTimeMillis();
        boolean result = filterTransformer.doFilterTransform(connectorMessage);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertFalse(result);
        assertTrue(duration >= SCRIPT_SLEEP_MILLIS);
        System.out.println("\ntestDoFilterTransform: " + duration + "ms");
    }
    
    @Test
    public final void testCancel() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Void> future = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ConnectorMessage connectorMessage = createConnectorMessage(CHANNEL_ID, 1, 0);
                filterTransformer.doFilterTransform(connectorMessage);
                return null;
            }
        });

        long startTime = System.currentTimeMillis();
        executor.shutdownNow();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertFalse(future.isDone());
        assertTrue(duration < SCRIPT_SLEEP_MILLIS);
        System.out.println("\ntestCancel: " + duration + "ms");
    }
    
    @Test
    public final void testPerformance() throws Exception {
        ConnectorMessage connectorMessage = createConnectorMessage(CHANNEL_ID, 1, 0);
        long startTime = System.currentTimeMillis();
        boolean result = filterTransformerPerformance.doFilterTransform(connectorMessage);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertFalse(result);
        System.out.println("\nFilter/Transformer Performance: " + duration + "ms\n");
    }
    
    private ConnectorMessage createConnectorMessage(String channelId, long messageId, int metaDataId) throws SerializerException {
        ConnectorMessage connectorMessage = new ConnectorMessage();
        connectorMessage.setChannelId(channelId);
        connectorMessage.setMessageId(messageId);
        connectorMessage.setMetaDataId(metaDataId);
        connectorMessage.setContent(new MessageContent(channelId, messageId, metaDataId, ContentType.RAW, TEST_HL7_MESSAGE, null));
        
        XmlSerializer xmlSerializer = new DefaultXMLSerializer(DefaultXMLSerializer.getDefaultProperties());
        String serializedMessage = xmlSerializer.toXML(connectorMessage.getRaw().getContent());
        
        connectorMessage.setTransformed(new MessageContent(channelId, messageId, metaDataId, ContentType.TRANSFORMED, serializedMessage, null));
        return connectorMessage;
    }
}
