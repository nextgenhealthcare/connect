package com.mirth.connect.donkey.model.channel;

public class DebugOptions {
    
    private boolean deployUndeployPreAndPostProcessorScripts = false;
    private boolean attachmentBatchScripts = false;
    private boolean sourceConnectorScripts = false;
    private boolean sourceFilterTransformer = false;
    private boolean destinationFilterTransformer = false;
    private boolean destinationConnectorScripts = false;
    private boolean destinationResponseTransformer = false;
    
    public DebugOptions(boolean deployUndeployPreAndPostProcessorScripts, boolean attachmentBatchScripts, boolean sourceConnectorScripts, boolean sourceFilterTransformer, boolean destinationFilterTransformer, boolean destinationConnectorScripts, boolean destinationResponseTransformer) {
        super();
        this.deployUndeployPreAndPostProcessorScripts = deployUndeployPreAndPostProcessorScripts;
        this.attachmentBatchScripts = attachmentBatchScripts;
        this.sourceConnectorScripts = sourceConnectorScripts;
        this.sourceFilterTransformer = sourceFilterTransformer;
        this.destinationFilterTransformer = destinationFilterTransformer;
        this.destinationConnectorScripts = destinationConnectorScripts;
        this.destinationResponseTransformer = destinationResponseTransformer;
    }
    
    public DebugOptions() {

    }
 
    public DebugOptions(String foo) {

    }
    
    public String toString() {
        return "DebugOptions [deployUndeployPreAndPostProcessorScripts=" + deployUndeployPreAndPostProcessorScripts + ", attachmentBatchScripts=" + attachmentBatchScripts + ", sourceConnectorScripts=" + sourceConnectorScripts + ", sourceFilterTransformer=" + sourceFilterTransformer + ", destinationFilterTransformer=" + destinationFilterTransformer + ", destinationConnectorScripts=" + destinationConnectorScripts + ", destinationResponseTransformer=" + destinationResponseTransformer + "]";
    }

    public boolean isDeployUndeployPreAndPostProcessorScripts() {
        return deployUndeployPreAndPostProcessorScripts;
    }
    public void setDeployUndeployPreAndPostProcessorScripts(boolean deployUndeployPreAndPostProcessorScripts) {
        this.deployUndeployPreAndPostProcessorScripts = deployUndeployPreAndPostProcessorScripts;
    }
    public boolean isAttachmentBatchScripts() {
        return attachmentBatchScripts;
    }
    public void setAttachmentBatchScripts(boolean attachmentBatchScripts) {
        this.attachmentBatchScripts = attachmentBatchScripts;
    }
    public boolean isSourceConnectorScripts() {
        return sourceConnectorScripts;
    }
    public void setSourceConnectorScripts(boolean sourceConnectorScripts) {
        this.sourceConnectorScripts = sourceConnectorScripts;
    }
    public boolean isSourceFilterTransformer() {
        return sourceFilterTransformer;
    }
    public void setSourceFilterTransformer(boolean sourceFilterTransformer) {
        this.sourceFilterTransformer = sourceFilterTransformer;
    }
    public boolean isDestinationFilterTransformer() {
        return destinationFilterTransformer;
    }
    public void setDestinationFilterTransformer(boolean destinationFilterTransformer) {
        this.destinationFilterTransformer = destinationFilterTransformer;
    }
    public boolean isDestinationConnectorScripts() {
        return destinationConnectorScripts;
    }
    public void setDestinationConnectorScripts(boolean destinationConnectorScripts) {
        this.destinationConnectorScripts = destinationConnectorScripts;
    }
    public boolean isDestinationResponseTransformer() {
        return destinationResponseTransformer;
    }
    public void setDestinationResponseTransformer(boolean destinationResponseTransformer) {
        this.destinationResponseTransformer = destinationResponseTransformer;
    }
    public boolean isEmpty() {
        if (!this.isAttachmentBatchScripts() && !this.isDeployUndeployPreAndPostProcessorScripts() &&
                !this.isDestinationConnectorScripts() && !this.isDestinationFilterTransformer() &&
                !this.isDestinationResponseTransformer() && !this.isSourceConnectorScripts() &&
                !this.isSourceFilterTransformer()) {
            return true;
        } else { 
            return false;
        }
    }
    
    
    

}
