/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.edi;

import org.apache.commons.lang3.StringUtils;
import org.mozilla.javascript.tools.debugger.MirthMain;
import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.MirthScopeProvider;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class EDIBatchAdaptorFactory extends BatchAdaptorFactory {

    private ContextFactoryController contextFactoryController = getContextFactoryController();
    private EDIBatchProperties batchProperties;
    private MirthMain debugger;
    private boolean debug = false;
    private MirthScopeProvider scopeProvider = new MirthScopeProvider();
    private String batchScriptId;


    @Override
    public MirthMain getDebugger() {
		return debugger;
	}

	public void setDebugger(MirthMain debugger) {
		this.debugger = debugger;
	}

	public EDIBatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties serializerProperties) {
        super(sourceConnector);

        batchProperties = (EDIBatchProperties) serializerProperties.getBatchProperties();
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        EDIBatchAdaptor batchAdaptor = new EDIBatchAdaptor(this, sourceConnector, batchRawMessage);

        batchAdaptor.setBatchProperties(batchProperties);

        return batchAdaptor;
    }
    
    protected ContextFactoryController getContextFactoryController() {
        return ControllerFactory.getFactory().createContextFactoryController();
    }
	
    @Override
    public void onDeploy() throws DeployException {
        String batchScript = batchProperties.getBatchScript();
        debug = sourceConnector.getChannel().getDebugOptions() != null && sourceConnector.getChannel().getDebugOptions().isAttachmentBatchScripts() == true;
        
        if (StringUtils.isNotEmpty(batchScript)) {
            batchScriptId = ScriptController.getScriptId(ScriptController.BATCH_SCRIPT_KEY, sourceConnector.getChannelId());

            try {
                MirthContextFactory contextFactory = generateContextFactory(debug, batchScript);
                setContextFactoryId(contextFactory.getId());
                if (debug) {
                     setDebugger(getDebugger(contextFactory, false));
                }

            } catch (Exception e) {
                throw new DeployException("Error compiling " + sourceConnector.getConnectorProperties().getName() + " script " + batchScriptId + ".", e);
            }
        }
    }

    protected MirthMain getDebugger(MirthContextFactory contextFactory, boolean showDebugger) {
        return MirthMain.mirthMainEmbedded(contextFactory, scopeProvider, sourceConnector.getChannel().getName() + "-" + sourceConnector.getChannelId(), batchScriptId, showDebugger);
    }
    
    @Override
    public void onUndeploy() throws UndeployException {
        if (debug && debugger != null) {
            contextFactoryController.removeDebugContextFactory(sourceConnector.getChannel().getResourceIds(), sourceConnector.getChannelId(), batchScriptId);
            debugger.dispose();
            debugger = null;
        }
    }
    
    @Override
    public void start() throws ConnectorTaskException, InterruptedException {
        super.start();
        ignoreBreakpoints=false;
        if (debug && debugger != null) {
            debugger.enableDebugging();
        }
    }
    
    @Override
    public void stop() throws ConnectorTaskException, InterruptedException {
        super.stop();
        ignoreBreakpoints=true;
        if (debug && debugger != null) {
            debugger.finishScriptExecution();
        }
    }

    protected MirthContextFactory generateContextFactory(boolean debug, String script) throws ConnectorTaskException {
        return JavaScriptUtil.generateContextFactory(debug, sourceConnector.getChannel().getResourceIds(), sourceConnector.getChannelId(), batchScriptId, script, ContextType.CHANNEL_BATCH);
    }


}
