/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import java.util.Map;

import org.dcm4che2.net.Association;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.tool.dcmrcv.MirthDcmRcv;
import org.dcm4che2.tool.dcmsnd.MirthDcmSnd;

import com.mirth.connect.donkey.server.channel.Connector;

public interface DICOMConfiguration {

    public void configureConnectorDeploy(Connector connector) throws Exception;

    public NetworkConnection createNetworkConnection();

    public void configureDcmRcv(MirthDcmRcv dcmrcv, DICOMReceiver connector, DICOMReceiverProperties connectorProperties) throws Exception;

    public void configureDcmSnd(MirthDcmSnd dcmsnd, DICOMDispatcher connector, DICOMDispatcherProperties connectorProperties) throws Exception;

    public Map<String, Object> getCStoreRequestInformation(Association as);
}