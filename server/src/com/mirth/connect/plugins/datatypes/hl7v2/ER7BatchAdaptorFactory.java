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

import org.apache.commons.lang3.StringUtils;

import com.mirth.connect.donkey.server.DeployException;
import com.mirth.connect.donkey.server.UndeployException;
import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.model.codetemplates.ContextType;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.server.controllers.ContextFactoryController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.ScriptController;
import com.mirth.connect.server.util.javascript.JavaScriptUtil;
import com.mirth.connect.server.util.javascript.MirthContextFactory;
import com.mirth.connect.util.StringUtil;

public class ER7BatchAdaptorFactory extends BatchAdaptorFactory {

    private ContextFactoryController contextFactoryController = ControllerFactory.getFactory().createContextFactoryController();
    private HL7v2BatchProperties batchProperties;
    private Pattern lineBreakPattern;
    private String segmentDelimiter;

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
    public BatchAdaptor createBatchAdaptor(BatchMessageSource batchMessageSource) {
        ER7BatchAdaptor batchAdaptor = new ER7BatchAdaptor(this, sourceConnector, batchMessageSource);

        batchAdaptor.setBatchProperties(batchProperties);
        batchAdaptor.setSegmentDelimiter(segmentDelimiter);
        batchAdaptor.setLineBreakPattern(lineBreakPattern);

        return batchAdaptor;
    }

    @Override
    public void onDeploy() throws DeployException {
        String batchScript = batchProperties.getBatchScript();

        if (StringUtils.isNotEmpty(batchScript)) {
            String batchScriptId = ScriptController.getScriptId(ScriptController.BATCH_SCRIPT_KEY, sourceConnector.getChannelId());

            try {
                MirthContextFactory contextFactory = contextFactoryController.getContextFactory(sourceConnector.getChannel().getResourceIds());
                setContextFactoryId(contextFactory.getId());
                JavaScriptUtil.compileAndAddScript(sourceConnector.getChannelId(), contextFactory, batchScriptId, batchScript.toString(), ContextType.CHANNEL_BATCH);
            } catch (Exception e) {
                throw new DeployException("Error compiling " + sourceConnector.getConnectorProperties().getName() + " script " + batchScriptId + ".", e);
            }
        }
    }

    @Override
    public void onUndeploy() throws UndeployException {}
}
