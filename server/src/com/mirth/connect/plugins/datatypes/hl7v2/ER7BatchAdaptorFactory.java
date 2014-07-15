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

import com.mirth.connect.donkey.server.channel.SourceConnector;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptor;
import com.mirth.connect.donkey.server.message.batch.BatchAdaptorFactory;
import com.mirth.connect.donkey.server.message.batch.BatchMessageSource;
import com.mirth.connect.model.datatype.SerializerProperties;
import com.mirth.connect.util.StringUtil;

public class ER7BatchAdaptorFactory extends BatchAdaptorFactory {

    private Pattern lineBreakPattern;
    private String segmentDelimiter;
    private boolean lookAhead;

    public ER7BatchAdaptorFactory(SourceConnector sourceConnector, SerializerProperties serializerProperties) {
        super(sourceConnector);

        HL7v2SerializationProperties serializationProperties = (HL7v2SerializationProperties) serializerProperties.getSerializationProperties();
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

        lookAhead = serializerProperties.getBatchProperties().isLookAhead();
    }

    @Override
    public BatchAdaptor createBatchAdaptor(BatchMessageSource batchMessageSource) {
        ER7BatchAdaptor batchAdaptor = new ER7BatchAdaptor(sourceConnector, batchMessageSource, lookAhead);

        batchAdaptor.setSegmentDelimiter(segmentDelimiter);
        batchAdaptor.setLineBreakPattern(lineBreakPattern);

        return batchAdaptor;
    }

    @Override
    public void onDeploy() {}

    @Override
    public void onUndeploy() {}
}
