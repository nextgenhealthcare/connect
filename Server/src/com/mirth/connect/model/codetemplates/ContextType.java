/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.codetemplates;

import org.apache.commons.lang3.text.WordUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("contextType")
public enum ContextType {
    GLOBAL_DEPLOY, GLOBAL_UNDEPLOY, GLOBAL_PREPROCESSOR, GLOBAL_POSTPROCESSOR, CHANNEL_DEPLOY, CHANNEL_UNDEPLOY, CHANNEL_PREPROCESSOR, CHANNEL_POSTPROCESSOR, CHANNEL_ATTACHMENT, CHANNEL_BATCH, SOURCE_RECEIVER, SOURCE_FILTER_TRANSFORMER, DESTINATION_FILTER_TRANSFORMER, DESTINATION_DISPATCHER, DESTINATION_RESPONSE_TRANSFORMER;

    public String getDisplayName() {
        switch (this) {
            case GLOBAL_DEPLOY:
            case CHANNEL_DEPLOY:
                return "Deploy Script";
            case GLOBAL_UNDEPLOY:
            case CHANNEL_UNDEPLOY:
                return "Undeploy Script";
            case GLOBAL_PREPROCESSOR:
            case CHANNEL_PREPROCESSOR:
                return "Preprocessor Script";
            case GLOBAL_POSTPROCESSOR:
            case CHANNEL_POSTPROCESSOR:
                return "Postprocessor Script";
            case CHANNEL_ATTACHMENT:
                return "Attachment Script";
            case CHANNEL_BATCH:
                return "Batch Script";
            case SOURCE_RECEIVER:
                return "Receiver Script(s)";
            case SOURCE_FILTER_TRANSFORMER:
            case DESTINATION_FILTER_TRANSFORMER:
                return "Filter / Transformer Script";
            case DESTINATION_DISPATCHER:
                return "Dispatcher Script";
            case DESTINATION_RESPONSE_TRANSFORMER:
                return "Response Transformer Script";
            default:
                return null;
        }
    }

    @Override
    public String toString() {
        return WordUtils.capitalizeFully(super.toString().replace('_', ' '));
    }
}