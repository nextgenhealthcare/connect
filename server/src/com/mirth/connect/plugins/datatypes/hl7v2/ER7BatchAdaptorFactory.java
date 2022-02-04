/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.datatypes.hl7v2;

import java.util.regex.Pattern;

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
import com.mirth.connect.util.StringUtil;

public class ER7BatchAdaptorFactory extends DebuggableBatchAdaptorFactory {

    private HL7v2BatchProperties batchProperties;
    private Pattern lineBreakPattern;
    private String segmentDelimiter;
    protected MirthMain debugger;
    private String batchScriptId;
	private MirthScopeProvider scopeProvider = new MirthScopeProvider();
	
    public ER7BatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties serializerProperties) {
        super(sourceConnector);

        HL7v2SerializationProperties serializationProperties = (HL7v2SerializationProperties) serializerProperties.getSerializationProperties();
        batchProperties = (HL7v2BatchProperties) serializerProperties.getBatchProperties();
        segmentDelimiter = StringUtil.unescape(serializationProperties.getSegmentDelimiter());

        String pattern;
        if (serializationProperties.isConvertLineBreaks()) {
            pattern = "\r\n|\r|\n";

            if (!(segmentDelimiter.equals("\r") || segmentDelimiter.equals("\n") || segmentDelimiter.equals("\r\n"))) {
                pattern += "|" + Pattern.quote(segmentDelimiter);
            }
        } else {
            pattern = Pattern.quote(segmentDelimiter);
        }

        lineBreakPattern = Pattern.compile(pattern);
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchRawMessage batchRawMessage) {
        ER7BatchAdaptor batchAdaptor = new ER7BatchAdaptor(this, sourceConnector, batchRawMessage);

        batchAdaptor.setBatchProperties(batchProperties);
        batchAdaptor.setSegmentDelimiter(segmentDelimiter);
        batchAdaptor.setLineBreakPattern(lineBreakPattern);

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
