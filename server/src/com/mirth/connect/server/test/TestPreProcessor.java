/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.server.test;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import com.mirth.connect.donkey.model.DonkeyException;
import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.donkey.model.message.ContentType;
import com.mirth.connect.donkey.model.message.Message;
import com.mirth.connect.donkey.model.message.MessageContent;
import com.mirth.connect.donkey.model.message.Response;
import com.mirth.connect.donkey.model.message.SerializerException;
import com.mirth.connect.donkey.model.message.Status;
import com.mirth.connect.donkey.model.message.XmlSerializer;
import com.mirth.connect.model.ServerEventContext;
import com.mirth.connect.model.converters.xml.DefaultXMLSerializer;
import com.mirth.connect.server.Mirth;
import com.mirth.connect.server.controllers.ChannelController;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.EngineController;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.transformers.JavaScriptFilterTransformer;
import com.mirth.connect.server.transformers.JavaScriptPostprocessor;
import com.mirth.connect.server.transformers.JavaScriptPreprocessor;
import com.mirth.connect.server.util.JavaScriptUtil;

public class TestPreProcessor {
    private final static String TEST_HL7_MESSAGE = "MSH|^~\\&|LABNET|Acme Labs|||20090601105700||ORU^R01|HMCDOOGAL-0088|D|2.2\rPID|1|8890088|8890088^^^72777||McDoogal^Hattie^||19350118|F||2106-3|100 Beach Drive^Apt. 5^Mission Viejo^CA^92691^US^H||(949) 555-0025|||||8890088^^^72|604422825\rPV1|1|R|C3E^C315^B||||2^HIBBARD^JULIUS^|5^ZIMMERMAN^JOE^|9^ZOIDBERG^JOHN^|CAR||||4|||2301^OBRIEN, KEVIN C|I|1783332658^1^1||||||||||||||||||||DISNEY CLINIC||N|||20090514205600\rORC|RE|928272608|056696716^LA||CM||||20090601105600||||  C3E|||^RESULT PERFORMED\rOBR|1|928272608|056696716^LA|1001520^K|||20090601101300|||MLH25|||HEMOLYZED/VP REDRAW|20090601102400||2301^OBRIEN, KEVIN C||||01123085310001100100152023509915823509915800000000101|0000915200932|20090601105600||LAB|F||^^^20090601084100^^ST~^^^^^ST\rOBX|1|NM|1001520^K||5.3|MMOL/L|3.5-5.5||||F|||20090601105600|IIM|IIM\r";
    private final static String CHANNEL_ID = "testchannel";
    
    private static Mirth server = new Mirth();
    
    @BeforeClass
    public static void beforeClass() throws Exception {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                server.run();
            }
        });
        
        while (ConfigurationController.getInstance().isEngineStarting()) {
            Thread.sleep(100);
        }
        
        ChannelController channelController = ChannelController.getInstance();
        EngineController engineController = ControllerFactory.getFactory().createEngineController();
        
        if (channelController.getCachedChannelById(CHANNEL_ID) == null) {
            channelController.updateChannel(TestUtils.getChannel(CHANNEL_ID), ServerEventContext.SYSTEM_USER_EVENT_CONTEXT, true);
    
            if (channelController.getCachedChannelById(CHANNEL_ID) == null) {
                throw new Exception("Failed to create channel");
            }
        }
        
        if (!engineController.isDeployed(CHANNEL_ID)) {
            engineController.deployChannel(CHANNEL_ID, ServerEventContext.SYSTEM_USER_EVENT_CONTEXT);
            Thread.sleep(200);
            
            if (!engineController.isDeployed(CHANNEL_ID)) {
                throw new Exception("failed to deploy channel");
            }
        }
    }
    
    private interface ScriptRunner {
        public Object runScript() throws Exception;
        public Object getExpectedResult();
    }
    
    @Test
    public final void testPreProcessor() throws Exception {
        final JavaScriptPreprocessor preprocessor = new JavaScriptPreprocessor();
        final String testResult = "test result";
        final int testSize = 100000;
        
        preprocessor.setChannelId(CHANNEL_ID);
        
        StringBuilder script = new StringBuilder();
        script.append("var result;");
        script.append("for (var i = 0; i < " + testSize + "; i++) {");
        script.append("result = Math.sqrt(i);");
        script.append("} return '" + testResult + "';");
        
        JavaScriptUtil.compileAndAddScript(ScriptController.getScriptId(ScriptController.PREPROCESSOR_SCRIPT_KEY, CHANNEL_ID), script.toString());

        ScriptRunner scriptRunner = new ScriptRunner() {
            @Override
            public Object runScript() throws DonkeyException, InterruptedException {
                return preprocessor.doPreProcess(createConnectorMessage(CHANNEL_ID, 1, 0));
            }

            @Override
            public Object getExpectedResult() {
                return testResult;
            }
        };
        
        testRun("Pre-processor", scriptRunner);
        testCancel(scriptRunner);
    }
    
    @Test
    public final void testFilterTransformer() throws Exception {
        final String scriptId = "testScriptId";
        final JavaScriptFilterTransformer filterTransformer = new JavaScriptFilterTransformer(CHANNEL_ID, "test connector", scriptId, null);
        final int testSize = 100000;
        
        StringBuilder script = new StringBuilder();
        script.append("var result;");
        script.append("for (var i = 0; i < " + testSize + "; i++) {");
        script.append("result = Math.sqrt(i);");
        script.append("} return false;");
        
        JavaScriptUtil.compileAndAddScript(scriptId, script.toString());

        ScriptRunner scriptRunner = new ScriptRunner() {
            @Override
            public Object runScript() throws DonkeyException, InterruptedException {
                return filterTransformer.doFilterTransform(createConnectorMessage(CHANNEL_ID, 1, 0));
            }

            @Override
            public Object getExpectedResult() {
                return false;
            }
        };
        
        testRun("Filter/Transformer", scriptRunner);
        testCancel(scriptRunner);
    }
    
    @Test
    public final void testPostProcessor() throws Exception {
        final JavaScriptPostprocessor postprocessor = new JavaScriptPostprocessor();
        final int testSize = 100000;
        final Response expectedResult = new Response(Status.SENT, "test");
        
        StringBuilder script = new StringBuilder();
        script.append("var result;");
        script.append("for (var i = 0; i < " + testSize + "; i++) {");
        script.append("result = Math.sqrt(i);");
        script.append("} return 'test';");
        
        JavaScriptUtil.compileAndAddScript(ScriptController.getScriptId(ScriptController.POSTPROCESSOR_SCRIPT_KEY, CHANNEL_ID), script.toString());

        ScriptRunner scriptRunner = new ScriptRunner() {
            @Override
            public Object runScript() throws Exception {
                Message message = new Message();
                message.setChannelId(CHANNEL_ID);
                message.setDateCreated(Calendar.getInstance());
                message.setMessageId(1l);
                message.setProcessed(false);
                message.getConnectorMessages().put(0, createConnectorMessage(CHANNEL_ID, 1l, 0));
                message.getConnectorMessages().put(1, createConnectorMessage(CHANNEL_ID, 1l, 1));
                
                return postprocessor.doPostProcess(message);
            }

            @Override
            public Object getExpectedResult() {
                return expectedResult;
            }
        };
        
        testRun("Post-processor", scriptRunner);
        testCancel(scriptRunner);
    }
    
    private void testRun(String name, ScriptRunner scriptRunner) throws Exception {
        long startTime = System.currentTimeMillis();
        Object result = scriptRunner.runScript();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        assertEquals(scriptRunner.getExpectedResult(), result);
        System.out.println("\n" + name + ": " + duration + "ms\n");
    }
    
    private void testCancel(final ScriptRunner scriptRunner) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Object> future = executor.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                return scriptRunner.runScript();
            }
        });
        
        executor.shutdownNow();
        Exception exception = null;
        
        try {
            future.get();
            throw new AssertionError("Failed to cancel task");
        } catch (ExecutionException e) {
            exception = e;
        } catch (InterruptedException e) {
            exception = e;
        }
        
        assertNotNull(exception);
        assertTrue(executor.awaitTermination(1000, TimeUnit.MILLISECONDS));
    }
    
    private ConnectorMessage createConnectorMessage(String channelId, long messageId, int metaDataId) throws SerializerException {
        ConnectorMessage connectorMessage = new ConnectorMessage();
        connectorMessage.setChannelId(channelId);
        connectorMessage.setMessageId(messageId);
        connectorMessage.setMetaDataId(metaDataId);
        connectorMessage.setContent(new MessageContent(channelId, messageId, metaDataId, ContentType.RAW, TEST_HL7_MESSAGE, false));
        
        XmlSerializer xmlSerializer = new DefaultXMLSerializer(DefaultXMLSerializer.getDefaultProperties());
        String serializedMessage = xmlSerializer.toXML(connectorMessage.getRaw().getContent());
        
        connectorMessage.setTransformed(new MessageContent(channelId, messageId, metaDataId, ContentType.TRANSFORMED, serializedMessage, false));
        return connectorMessage;
    }
}
