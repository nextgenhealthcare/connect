/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.model.util;

public class JavaScriptConstants {
    public static final String DEFAULT_CHANNEL_DEPLOY_SCRIPT = "// This script executes once when the channel is deployed\n// You only have access to the globalMap and globalChannelMap here to persist data\nreturn;";
    public static final String DEFAULT_CHANNEL_UNDEPLOY_SCRIPT = "// This script executes once when the channel is undeployed\n// You only have access to the globalMap and globalChannelMap here to persist data\nreturn;";
    public static final String DEFAULT_CHANNEL_PREPROCESSOR_SCRIPT = "// Modify the message variable below to pre process data\nreturn message;";
    public static final String DEFAULT_CHANNEL_POSTPROCESSOR_SCRIPT = "// This script executes once after a message has been processed\n// Responses returned from here will be stored as \"Postprocessor\" in the response map\nreturn;";
    public static final String DEFAULT_CHANNEL_ATTACHMENT_SCRIPT = "// Modify the message variable below to create attachments\nreturn message;";

    public static final String DEFAULT_GLOBAL_DEPLOY_SCRIPT = "// This script executes once for each deploy or redeploy task\n// You only have access to the globalMap here to persist data\nreturn;";
    public static final String DEFAULT_GLOBAL_UNDEPLOY_SCRIPT = "// This script executes once for each deploy, undeploy, or redeploy task\n// if at least one channel was undeployed\n// You only have access to the globalMap here to persist data\nreturn;";
    public static final String DEFAULT_GLOBAL_PREPROCESSOR_SCRIPT = "// Modify the message variable below to pre process data\n// This script applies across all channels\nreturn message;";
    public static final String DEFAULT_GLOBAL_POSTPROCESSOR_SCRIPT = "// This script executes once after a message has been processed\n// This script applies across all channels\n// Responses returned from here will be stored as \"Postprocessor\" in the response map\n// You have access to \"response\", if returned from the channel postprocessor\nreturn;";
}
