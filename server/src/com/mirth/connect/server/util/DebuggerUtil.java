package com.mirth.connect.server.util;

import com.mirth.connect.donkey.model.channel.DebugOptions;

public class DebuggerUtil {

    public static DebugOptions parseDebugOptions(String debugOptionsString) {
        String[] options = debugOptionsString.split(",");
        Object[] params = new Object[options.length];
        for (int i = 0; i < options.length; i++) {
            if (options[i].equals("t")) {
                params[i] = true;
            } else {
                params[i] = false;
            }
        }
        
        DebugOptions debugOptions = new DebugOptions((Boolean) params[0],
                                            (Boolean) params[1], 
                                            (Boolean) params[2], 
                                            (Boolean) params[3], 
                                            (Boolean) params[4], 
                                            (Boolean) params[5], 
                                            (Boolean) params[6] );

        return (DebugOptions) debugOptions;
    }
    
    public static String parseDebugOptions(DebugOptions debugOptions) {
        
        String isAttachmentBatchScripts = debugOptions.isAttachmentBatchScripts() ? "t": "f";
        String isDeployUndeployPreAndPostProcessorScripts = debugOptions.isDeployUndeployPreAndPostProcessorScripts() ? "t": "f";
        String isDestinationConnectorScripts = debugOptions.isDestinationConnectorScripts() ? "t": "f";
        String isDestinationFilterTransformer = debugOptions.isDestinationFilterTransformer() ? "t": "f";
        String isDestinationResponseTransformer = debugOptions.isDestinationResponseTransformer() ? "t": "f";
        String isSourceConnectorScripts = debugOptions.isSourceConnectorScripts() ? "t": "f";
        String isSourceFilterTransformer = debugOptions.isSourceFilterTransformer() ? "t": "f";
        
        String debug = new String();
        debug += isDeployUndeployPreAndPostProcessorScripts + ",";
        debug += isAttachmentBatchScripts + ",";
        debug += isSourceConnectorScripts + ",";
        debug += isDestinationFilterTransformer + ",";
        debug += isSourceFilterTransformer+ ",";
        debug += isDestinationConnectorScripts + ",";
        debug += isDestinationResponseTransformer;
        
        return debug;
    }

}
