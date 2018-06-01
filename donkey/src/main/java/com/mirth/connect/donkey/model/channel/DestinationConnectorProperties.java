/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.donkey.model.channel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.util.DonkeyElement;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.donkey.util.purge.Purgable;

public class DestinationConnectorProperties implements Serializable, Migratable, Purgable {
    private boolean queueEnabled;
    private boolean sendFirst;
    private int retryIntervalMillis;
    private boolean regenerateTemplate;
    private int retryCount;
    private boolean rotate;
    private boolean includeFilterTransformer;
    private int threadCount;
    private String threadAssignmentVariable;
    private boolean validateResponse;
    private Map<String, String> resourceIds;
    private int queueBufferSize;
    private boolean reattachAttachments;

    public DestinationConnectorProperties() {
        this(false);
    }

    public DestinationConnectorProperties(boolean validateResponse) {
        queueEnabled = false;
        sendFirst = false;
        retryIntervalMillis = 10000;
        regenerateTemplate = false;
        retryCount = 0;
        rotate = false;
        includeFilterTransformer = false;
        threadCount = 1;
        threadAssignmentVariable = "";
        this.validateResponse = validateResponse;
        this.resourceIds = new LinkedHashMap<String, String>();
        resourceIds.put("Default Resource", "[Default Resource]");
        this.queueBufferSize = 0;
        reattachAttachments = true;
    }

    public DestinationConnectorProperties(DestinationConnectorProperties props) {
        queueEnabled = props.isQueueEnabled();
        sendFirst = props.isSendFirst();
        retryIntervalMillis = props.getRetryIntervalMillis();
        regenerateTemplate = props.isRegenerateTemplate();
        retryCount = props.getRetryCount();
        rotate = props.isRotate();
        includeFilterTransformer = props.isIncludeFilterTransformer();
        threadCount = props.getThreadCount();
        threadAssignmentVariable = props.getThreadAssignmentVariable();
        validateResponse = props.isValidateResponse();
        resourceIds = new LinkedHashMap<String, String>(props.getResourceIds());
        queueBufferSize = props.getQueueBufferSize();
        reattachAttachments = props.isReattachAttachments();
    }

    public boolean isQueueEnabled() {
        return queueEnabled;
    }

    public void setQueueEnabled(boolean enabled) {
        this.queueEnabled = enabled;
    }

    public boolean isSendFirst() {
        return sendFirst;
    }

    public void setSendFirst(boolean sendFirst) {
        this.sendFirst = sendFirst;
    }

    public int getRetryIntervalMillis() {
        return retryIntervalMillis;
    }

    public void setRetryIntervalMillis(int retryIntervalMillis) {
        this.retryIntervalMillis = retryIntervalMillis;
    }

    public boolean isRegenerateTemplate() {
        return regenerateTemplate;
    }

    public void setRegenerateTemplate(boolean regenerateTemplate) {
        this.regenerateTemplate = regenerateTemplate;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public boolean isIncludeFilterTransformer() {
        return includeFilterTransformer;
    }

    public void setIncludeFilterTransformer(boolean includeFilterTransformer) {
        this.includeFilterTransformer = includeFilterTransformer;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    public String getThreadAssignmentVariable() {
        return threadAssignmentVariable;
    }

    public void setThreadAssignmentVariable(String threadAssignmentVariable) {
        this.threadAssignmentVariable = threadAssignmentVariable;
    }

    public boolean isValidateResponse() {
        return validateResponse;
    }

    public void setValidateResponse(boolean validateResponse) {
        this.validateResponse = validateResponse;
    }

    public Map<String, String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(Map<String, String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public int getQueueBufferSize() {
        return queueBufferSize;
    }

    public void setQueueBufferSize(int queueBufferSize) {
        this.queueBufferSize = queueBufferSize;
    }

    public boolean isReattachAttachments() {
        return reattachAttachments;
    }

    public void setReattachAttachments(boolean reattachAttachments) {
        this.reattachAttachments = reattachAttachments;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public void migrate3_0_1(DonkeyElement element) {}

    @Override
    public void migrate3_0_2(DonkeyElement element) {}

    @Override
    public void migrate3_1_0(DonkeyElement element) {
        element.addChildElementIfNotExists("validateResponse", "false");
    }

    @Override
    public void migrate3_2_0(DonkeyElement element) {
        DonkeyElement resourceIdsElement = element.addChildElement("resourceIds");
        resourceIdsElement.setAttribute("class", "linked-hash-set");
        resourceIdsElement.addChildElement("string", "Default Resource");
    }

    @Override
    public void migrate3_3_0(DonkeyElement element) {}

    @Override
    public void migrate3_4_0(DonkeyElement element) {
        DonkeyElement resourceIdsElement = element.getChildElement("resourceIds");
        List<DonkeyElement> resourceIdsList = resourceIdsElement.getChildElements();
        resourceIdsElement.removeChildren();
        resourceIdsElement.setAttribute("class", "linked-hash-map");

        for (DonkeyElement resourceId : resourceIdsList) {
            DonkeyElement entry = resourceIdsElement.addChildElement("entry");
            String resourceIdText = resourceId.getTextContent();
            entry.addChildElement("string", resourceIdText);
            if (resourceIdText.equals("Default Resource")) {
                entry.addChildElement("string", "[Default Resource]");
            } else {
                entry.addChildElement("string");
            }
        }
    }

    @Override
    public void migrate3_5_0(DonkeyElement element) {
        element.addChildElementIfNotExists("reattachAttachments", "true");
    }

    @Override
    public void migrate3_6_0(DonkeyElement element) {}
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = new HashMap<String, Object>();
        purgedProperties.put("queueEnabled", queueEnabled);
        purgedProperties.put("sendFirst", sendFirst);
        purgedProperties.put("retryIntervalMillis", retryIntervalMillis);
        purgedProperties.put("regenerateTemplate", regenerateTemplate);
        purgedProperties.put("retryCount", retryCount);
        purgedProperties.put("rotate", rotate);
        purgedProperties.put("includeFilterTransformer", includeFilterTransformer);
        purgedProperties.put("threadCount", threadCount);
        purgedProperties.put("validateResponse", validateResponse);
        purgedProperties.put("resourceIdsCount", resourceIds.size());
        purgedProperties.put("queueBufferSize", queueBufferSize);
        purgedProperties.put("reattachAttachments", reattachAttachments);
        return purgedProperties;
    }
}
