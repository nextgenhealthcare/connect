/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v3;

import org.mozilla.javascript.tools.debugger.MirthMain;

import com.mirth.connect.donkey.model.message.BatchRawMessage;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.plugins.datatypes.DebuggableBatchAdaptorFactory;
import com.mirth.connect.server.MirthScopeProvider;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.util.javascript.MirthContextFactory;

public class HL7V3BatchAdaptorFactory extends DebuggableBatchAdaptorFactory {
    protected MirthMain debugger;
    private String batchScriptId;
	private MirthScopeProvider scopeProvider = new MirthScopeProvider();

    public HL7V3BatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties serializerProperties) {
        super(sourceConnector);

        batchProperties = (HL7V3BatchProperties) serializerProperties.getBatchProperties();
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        HL7V3BatchAdaptor batchAdaptor = new HL7V3BatchAdaptor(this, sourceConnector, batchRawMessage);

        batchAdaptor.setBatchProperties(batchProperties);

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
	
}
