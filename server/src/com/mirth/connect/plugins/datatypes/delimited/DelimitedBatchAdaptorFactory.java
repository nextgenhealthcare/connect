/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.delimited;

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
import com.mirth.connect.server.MirthScopeProvider;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DelimitedBatchAdaptorFactory extends BatchAdaptorFactory {

    private ContextFactoryController contextFactoryController = getContextFactoryController();
    private DelimitedSerializationProperties serializationProperties;
    private DelimitedBatchProperties batchProperties;
    private boolean debug = false;
    protected MirthMain debugger;

	private MirthScopeProvider scopeProvider = new MirthScopeProvider();
    private String batchScriptId;

    public DelimitedBatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties serializerProperties) {
        super(sourceConnector);

        serializationProperties = (DelimitedSerializationProperties) serializerProperties.getSerializationProperties();
        batchProperties = (DelimitedBatchProperties) serializerProperties.getBatchProperties();
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        DelimitedBatchAdaptor batchAdaptor = new DelimitedBatchAdaptor(this, sourceConnector, batchRawMessage);

        batchAdaptor.setSerializationProperties(serializationProperties);
        batchAdaptor.setBatchProperties(batchProperties);
        batchAdaptor.setDelimitedReader(new DelimitedReader((DelimitedSerializationProperties) serializationProperties));

        return batchAdaptor;
    }
    
    protected ContextFactoryController getContextFactoryController() {
        return ControllerFactory.getFactory().createContextFactoryController();
    }
    
    @Override
    public MirthMain getDebugger() {
		return debugger;
	}

    protected MirthMain getDebugger(MirthContextFactory contextFactory, boolean showDebugger) {
        return MirthMain.mirthMainEmbedded(contextFactory, scopeProvider, sourceConnector.getChannel().getName() + "-" + sourceConnector.getChannelId(), batchScriptId, showDebugger);
    }
    
	public void setDebugger(MirthMain debugger) {
		this.debugger = debugger;
	}
	
	protected MirthContextFactory generateContextFactory(boolean debug, String script) throws ConnectorTaskException {
	    return JavaScriptUtil.generateContextFactory(debug, sourceConnector.getChannel().getResourceIds(), sourceConnector.getChannelId(), batchScriptId, script, ContextType.CHANNEL_BATCH);
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
        ignoreBreakpoints = false;
        if (debug && debugger != null) {
            debugger.enableDebugging();
        }
        super.start();
    }
    
    @Override
    public void stop() throws ConnectorTaskException, InterruptedException {
    	ignoreBreakpoints = true;
        if (debug && debugger != null) {
            debugger.finishScriptExecution();
        }
        super.stop();
    }
}
