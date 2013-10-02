/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

/*
 * TODO: Set "SerializationStrategy" in the UI?
 * TODO: Ability to add CAS metadata fields? (this would just be an ordered list of strings) (NOT KEY=VALUE)
 */
package com.mirth.connect.connectors.uima;

import java.util.HashMap;
import java.util.Map;

import org.apache.uima.aae.client.UimaAsynchronousEngine;
import org.apache.uima.adapter.jms.client.BaseUIMAAsynchronousEngine_impl;
import org.apache.uima.resource.ResourceInitializationException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.umo.UMOException;
import org.mule.umo.lifecycle.LifecycleException;

public class UimaConnector extends AbstractServiceEnabledConnector {
    private String channelId;
    
    private String template;
    private String pipeline;
    private int metaTimeout = 60000;
    private int casProcessTimeout = 0;
    private int cpcTimeout = 0;
    private String serializationStrategy = "xmi";
    private int casPoolSize = 2;
    private String jmsUrl = "tcp://localhost:61616";
    private Map<String, String> dispatcherParameters;
    
    private String successResponseChannelId = "sink";
    private String errorResponseChannelId = "sink";
    
    private UimaAsynchronousEngine engine = new BaseUIMAAsynchronousEngine_impl();
    

    public String getProtocol() {
        return "uima";
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getPipeline() {
        return pipeline;
    }

    public void setPipeline(String pipeline) {
        this.pipeline = pipeline;
    }

    public UimaAsynchronousEngine getEngine() {
        return this.engine;
    }
    
    
    public int getMetaTimeout() {
        return metaTimeout;
    }

    public void setMetaTimeout(int metaTimeout) {
        this.metaTimeout = metaTimeout;
    }

    public String getSerializationStrategy() {
        return serializationStrategy;
    }

    public void setSerializationStrategy(String serializationStrategy) {
        this.serializationStrategy = serializationStrategy;
    }

    public int getCasPoolSize() {
        return casPoolSize;
    }

    public void setCasPoolSize(int casPoolSize) {
        this.casPoolSize = casPoolSize;
    }

    public String getJmsUrl() {
        return jmsUrl;
    }

    public void setJmsUrl(String jmsUrl) {
        this.jmsUrl = jmsUrl;
    }

    public int getCasProcessTimeout() {
        return casProcessTimeout;
    }

    public void setCasProcessTimeout(int casProcessTimeout) {
        this.casProcessTimeout = casProcessTimeout;
    }

    public int getCpcTimeout() {
        return cpcTimeout;
    }

    public void setCpcTimeout(int cpcTimeout) {
        this.cpcTimeout = cpcTimeout;
    }

    public String getSuccessResponseChannelId() {
        return successResponseChannelId;
    }

    public void setSuccessResponseChannelId(String successResponseChannelId) {
        this.successResponseChannelId = successResponseChannelId;
    }

    public String getErrorResponseChannelId() {
        return errorResponseChannelId;
    }

    public void setErrorResponseChannelId(String errorResponseChannelId) {
        this.errorResponseChannelId = errorResponseChannelId;
    }

    public void setDispatcherParameters(Map<String,String> dispatcherParameters) {
        this.dispatcherParameters = dispatcherParameters;
    }

    public Map<String,String> getDispatcherParameters() {
        return dispatcherParameters;
    }

    @Override
    public void doDispose() {
        try {
            this.engine.stop();
        } catch (Exception e) {
        }
    }
    
    @Override
    public void doStart() throws UMOException {
        if (this.engine != null) {
            this.engine.addStatusCallbackListener(new MirthUimaCallbackListener(this));
        }
        try {
            Map<String, Object> config = new HashMap<String, Object>();
            config.put(UimaAsynchronousEngine.ServerUri, this.getJmsUrl());
            config.put(UimaAsynchronousEngine.Endpoint, this.getPipeline());
            config.put(UimaAsynchronousEngine.GetMetaTimeout, this.getMetaTimeout());
            config.put(UimaAsynchronousEngine.Timeout, this.getCasProcessTimeout());
            config.put(UimaAsynchronousEngine.CpcTimeout, this.getCpcTimeout());
            config.put(UimaAsynchronousEngine.SerializationStrategy, this.getSerializationStrategy());
            config.put(UimaAsynchronousEngine.CasPoolSize, this.getCasPoolSize());
            this.engine.initialize(config);
        } catch (ResourceInitializationException e) {
            throw new LifecycleException(new Message(Messages.FAILED_TO_START_X, "Uima Connection"), e);
        }
    }
    
}