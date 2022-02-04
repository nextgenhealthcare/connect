package com.mirth.connect.plugins.datatypes;

import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.ConnectorTaskException;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageException;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DebuggableBatchAdaptor extends BatchAdaptor {
    private BatchProperties batchProperties;
    protected DebuggableBatchAdaptorFactory factory;
    
    public DebuggableBatchAdaptor(BatchAdaptorFactory factory, SourceConnector sourceConnector, BatchRawMessage batchRawMessage) {
        super(factory, sourceConnector, batchRawMessage);
        // TODO Auto-generated constructor stub
    }
    
    public BatchProperties getBatchProperties() {
        return batchProperties;
    }

    public void setBatchProperties(BatchProperties batchProperties) {
        this.batchProperties = batchProperties;
    }

    @Override
    public void cleanup() throws BatchMessageException {
        // TODO Auto-generated method stub
    }
    
    @Override
    protected String getNextMessage(int batchSequenceId) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
    
    protected void triggerDebug(boolean debug) {
        if (debug) {
            MirthMain debugger = (MirthMain)factory.getDebugger();
            if (debugger != null && !factory.isIgnoreBreakpoints()) {
                debugger.doBreak();
                if (!debugger.isVisible()) {
                    debugger.setVisible(true);
                }
            }
        }
    }
    
    protected MirthContextFactory getContextFactoryAndRecompile(ContextFactoryController contextFactoryController, boolean debug, String batchScriptId, String batchScript) throws Exception {
        MirthContextFactory contextFactory = JavaScriptUtil.generateContextFactory(debug, sourceConnector.getChannel().getResourceIds(), sourceConnector.getChannelId(), batchScriptId, batchScript, ContextType.CHANNEL_BATCH);                
        if (!factory.getContextFactoryId().equals(contextFactory.getId())) {
            synchronized (factory) {
                contextFactory = contextFactoryController.getContextFactory(sourceConnector.getChannel().getResourceIds());
                if (!factory.getContextFactoryId().equals(contextFactory.getId())) {
                    JavaScriptUtil.recompileGeneratedScript(contextFactory, batchScriptId);
                    factory.setContextFactoryId(contextFactory.getId());
                }
            }
        }
        return contextFactory;
    }
}
