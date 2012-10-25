/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.uima;

public class UimaPipelineInfo {
    private String jmsUrl;
    private String pipeline;
    
    public UimaPipelineInfo(String jmsUrl, String pipeline) {
        this.jmsUrl = jmsUrl;
        this.pipeline = pipeline;
    }
    
    public String getJmsUrl() {
        return jmsUrl;
    }
    public void setJmsUrl(String jmsUrl) {
        this.jmsUrl = jmsUrl;
    }
    public String getPipeline() {
        return pipeline;
    }
    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }
    
    public String toString() {
        return this.getPipeline();
    }
}
