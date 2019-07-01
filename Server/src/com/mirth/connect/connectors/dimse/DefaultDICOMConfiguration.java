/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import java.util.HashMap;
import java.util.Map;

import org.dcm4che2.net.Association;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.tool.dcmrcv.MirthDcmRcv;
import org.dcm4che2.tool.dcmsnd.MirthDcmSnd;

import com.mirth.connect.donkey.server.channel.Connector;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.util.MirthSSLUtil;

public class DefaultDICOMConfiguration implements DICOMConfiguration {

    private ConfigurationController configurationController = ControllerFactory.getFactory().createConfigurationController();
    private String[] protocols;

    @Override
    public void configureConnectorDeploy(Connector connector) throws Exception {
        if (connector instanceof DICOMReceiver) {
            protocols = MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsServerProtocols());
        } else {
            protocols = MirthSSLUtil.getEnabledHttpsProtocols(configurationController.getHttpsClientProtocols());
        }
    }

    @Override
    public NetworkConnection createNetworkConnection() {
        return new NetworkConnection();
    }

    @Override
    public void configureDcmRcv(MirthDcmRcv dcmrcv, DICOMReceiver connector, DICOMReceiverProperties connectorProperties) throws Exception {
        DICOMConfigurationUtil.configureDcmRcv(dcmrcv, connector, connectorProperties, protocols);
    }

    @Override
    public void configureDcmSnd(MirthDcmSnd dcmsnd, DICOMDispatcher connector, DICOMDispatcherProperties connectorProperties) throws Exception {
        DICOMConfigurationUtil.configureDcmSnd(dcmsnd, connector, connectorProperties, protocols);
    }

    @Override
    public Map<String, Object> getCStoreRequestInformation(Association as) {
        return new HashMap<String, Object>();
    }
}