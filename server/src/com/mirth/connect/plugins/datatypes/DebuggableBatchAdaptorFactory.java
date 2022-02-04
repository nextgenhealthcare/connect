package com.mirth.connect.plugins.datatypes;

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
import com.mirth.connect.model.datatype.BatchProperties;
import com.mirth.connect.server.MirthScopeProvider;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class DebuggableBatchAdaptorFactory extends BatchAdaptorFactory {
    protected ContextFactoryController contextFactoryController = getContextFactoryController();
    protected BatchProperties batchProperties;
    protected boolean debug = false;
    protected MirthMain debugger;
    protected MirthScopeProvider scopeProvider = new MirthScopeProvider();
    protected String batchScriptId;
    protected boolean ignoreBreakpoints = false;
    private volatile String contextFactoryId;

    public DebuggableBatchAdaptorFactory(SourceConnector sourceConnector) {
        super(sourceConnector);
        // TODO Auto-generated constructor stub
    }
    
    public MirthMain getDebugger() {
        return debugger;
    }
    
    public void setDebugger(MirthMain debugger) {
        this.debugger = debugger;
    }
    
    public boolean isIgnoreBreakpoints() {
        return ignoreBreakpoints;
    }
    
    public String getContextFactoryId() {
        return contextFactoryId;
    }

    public void setContextFactoryId(String contextFactoryId) {
        this.contextFactoryId = contextFactoryId;
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        // TODO Auto-generated method stub
        return null;
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

    protected ContextFactoryController getContextFactoryController() {
        return ControllerFactory.getFactory().createContextFactoryController();
    }
    
    protected MirthMain getDebugger(MirthContextFactory contextFactory, boolean showDebugger) {
        return MirthMain.mirthMainEmbedded(contextFactory, scopeProvider, sourceConnector.getChannel().getName() + "-" + sourceConnector.getChannelId(), batchScriptId, showDebugger);
    }
    
    protected MirthContextFactory generateContextFactory(boolean debug, String script) throws ConnectorTaskException {
        return JavaScriptUtil.generateContextFactory(debug, sourceConnector.getChannel().getResourceIds(), sourceConnector.getChannelId(), batchScriptId, script, ContextType.CHANNEL_BATCH);
    }
    
}
