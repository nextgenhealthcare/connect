/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.smtp;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.Email;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.MirthSSLUtil;

public class DefaultSmtpConfiguration implements SmtpConfiguration {

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private String protocols;
    private String cipherSuites;

    @Override
    public void configureConnectorDeploy(Connector connector) {
        protocols = StringUtils.join(MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsClientProtocols()), ' ');
        cipherSuites = StringUtils.join(MirthSSLUtil.getEnabledHttpsCipherSuites(configurationController.getHttpsCipherSuites()), ' ');
    }

    @Override
    public void configureEncryption(ConnectorProperties connectorProperties, Email email) throws Exception {
        SmtpDispatcherProperties props = (SmtpDispatcherProperties) connectorProperties;

        if ("SSL".equalsIgnoreCase(props.getEncryption())) {
            email.setSSLOnConnect(true);
            email.setSslSmtpPort(props.getSmtpPort());
        } else if ("TLS".equalsIgnoreCase(props.getEncryption())) {
            email.setStartTLSEnabled(true);
        }
    }

    @Override
    public void configureMailProperties(Properties mailProperties) {
        mailProperties.setProperty("mail.smtp.ssl.protocols", protocols);
        mailProperties.setProperty("mail.smtp.ssl.ciphersuites", cipherSuites);
    }
}