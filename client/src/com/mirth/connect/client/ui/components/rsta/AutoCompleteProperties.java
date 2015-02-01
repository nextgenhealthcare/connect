/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components.rsta;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class AutoCompleteProperties {

    private boolean activateAfterLetters;
    private String activateAfterOthers;
    private int activationDelay;

    public AutoCompleteProperties() {
        this(null);
    }

    public AutoCompleteProperties(AutoCompleteProperties properties) {
        if (properties != null) {
            init(properties.isActivateAfterLetters(), properties.getActivateAfterOthers(), properties.getActivationDelay());
        } else {
            init(null, null, null);
        }
    }

    public AutoCompleteProperties(Boolean activateAfterLetters, String activateAfterOthers, Integer activationDelay) {
        init(activateAfterLetters, activateAfterOthers, activationDelay);
    }

    private void init(Boolean activateAfterLetters, String activateAfterOthers, Integer activationDelay) {
        this.activateAfterLetters = false;
        this.activateAfterOthers = ".";
        this.activationDelay = 300;

        if (activateAfterLetters != null) {
            this.activateAfterLetters = activateAfterLetters;
        }
        if (activateAfterOthers != null) {
            this.activateAfterOthers = activateAfterOthers;
        }
        if (activationDelay != null) {
            this.activationDelay = activationDelay;
        }
    }

    public boolean isActivateAfterLetters() {
        return activateAfterLetters;
    }

    public void setActivateAfterLetters(boolean activateAfterLetters) {
        this.activateAfterLetters = activateAfterLetters;
    }

    public String getActivateAfterOthers() {
        return activateAfterOthers;
    }

    public void setActivateAfterOthers(String activateAfterOthers) {
        this.activateAfterOthers = activateAfterOthers;
    }

    public int getActivationDelay() {
        return activationDelay;
    }

    public void setActivationDelay(int activationDelay) {
        this.activationDelay = activationDelay;
    }

    JsonNode toJsonNode() {
        ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
        rootNode.put("activateAfterLetters", activateAfterLetters);
        rootNode.put("activateAfterOthers", activateAfterOthers);
        rootNode.put("activationDelay", activationDelay);
        return rootNode;
    }

    static AutoCompleteProperties fromJSON(String autoCompleteJSON) {
        Boolean activateAfterLetters = null;
        String activateAfterOthers = null;
        Integer activationDelay = null;

        if (StringUtils.isNotBlank(autoCompleteJSON)) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ObjectNode rootNode = (ObjectNode) mapper.readTree(autoCompleteJSON);

                JsonNode node = rootNode.get("activateAfterLetters");
                if (node != null) {
                    activateAfterLetters = node.asBoolean();
                }

                node = rootNode.get("activateAfterOthers");
                if (node != null) {
                    activateAfterOthers = node.asText();
                }

                node = rootNode.get("activationDelay");
                if (node != null) {
                    activationDelay = node.asInt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new AutoCompleteProperties(activateAfterLetters, activateAfterOthers, activationDelay);
    }
}