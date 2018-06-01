/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.ws;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.SourceConnectorProperties;
import com.mirth.connect.donkey.model.channel.SourceConnectorPropertiesInterface;
import com.mirth.connect.donkey.util.DonkeyElement;

public class WebServiceReceiverProperties extends ConnectorProperties implements ListenerConnectorPropertiesInterface, SourceConnectorPropertiesInterface {
    private ListenerConnectorProperties listenerConnectorProperties;
    private SourceConnectorProperties sourceConnectorProperties;

    private String className;
    private String serviceName;
    private Binding soapBinding;

    public WebServiceReceiverProperties() {
        listenerConnectorProperties = new ListenerConnectorProperties("8081");
        sourceConnectorProperties = new SourceConnectorProperties();

        this.className = "com.mirth.connect.connectors.ws.DefaultAcceptMessage";
        this.serviceName = "Mirth";
        this.soapBinding = Binding.DEFAULT;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Binding getSoapBinding() {
        return soapBinding;
    }

    public void setSoapBinding(Binding soapBinding) {
        this.soapBinding = soapBinding;
    }

    @Override
    public String getProtocol() {
        return "WS";
    }

    @Override
    public String getName() {
        return "Web Service Listener";
    }

    @Override
    public String toFormattedString() {
        return null;
    }

    @Override
    public ListenerConnectorProperties getListenerConnectorProperties() {
        return listenerConnectorProperties;
    }

    @Override
    public SourceConnectorProperties getSourceConnectorProperties() {
        return sourceConnectorProperties;
    }

    @Override
    public boolean canBatch() {
        return true;
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
        super.migrate3_1_0(element);
    }

    @Override
    public void migrate3_2_0(DonkeyElement element) {}

    @Override
    public void migrate3_3_0(DonkeyElement element) {
        element.addChildElementIfNotExists("soapBinding", "DEFAULT");
    }

    @Override
    public void migrate3_4_0(DonkeyElement element) {
        DonkeyElement usernamesElement = element.removeChild("usernames");
        DonkeyElement passwordsElement = element.removeChild("passwords");

        if (usernamesElement != null && usernamesElement.hasChildNodes() && passwordsElement != null && passwordsElement.hasChildNodes()) {
            DonkeyElement authPropertiesElement = element.addChildElementIfNotExists("pluginProperties").addChildElement("com.mirth.connect.plugins.httpauth.basic.BasicHttpAuthProperties");
            authPropertiesElement.setAttribute("version", "3.4.0");
            authPropertiesElement.addChildElement("authType", "BASIC");

            DonkeyElement serviceNameElement = element.getChildElement("serviceName");
            String serviceName = serviceNameElement != null ? serviceNameElement.getTextContent() : "Mirth";
            authPropertiesElement.addChildElement("realm", "/services/" + serviceName);

            DonkeyElement credentialsElement = authPropertiesElement.addChildElement("credentials");
            credentialsElement.setAttribute("class", "linked-hash-map");
            List<DonkeyElement> usernameElements = usernamesElement.getChildElements();
            List<DonkeyElement> passwordElements = passwordsElement.getChildElements();

            for (int i = 0; i < usernameElements.size(); i++) {
                if (i < passwordElements.size()) {
                    DonkeyElement entry = credentialsElement.addChildElement("entry");
                    entry.addChildElement("string", usernameElements.get(i).getTextContent());
                    entry.addChildElement("string", passwordElements.get(i).getTextContent());
                }
            }
        }
    }

    @Override
    public void migrate3_5_0(DonkeyElement element) {}

    @Override
    public void migrate3_6_0(DonkeyElement element) {}
    
    @Override
    public Map<String, Object> getPurgedProperties() {
        Map<String, Object> purgedProperties = super.getPurgedProperties();
        purgedProperties.put("sourceConnectorProperties", sourceConnectorProperties.getPurgedProperties());
        purgedProperties.put("soapBinding", soapBinding);
        return purgedProperties;
    }
}
