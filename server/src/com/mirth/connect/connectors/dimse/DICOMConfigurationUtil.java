/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dcm4che2.tool.dcmrcv.MirthDcmRcv;
import org.dcm4che2.tool.dcmsnd.MirthDcmSnd;

import com.mirth.connect.util.MirthSSLUtil;

public class DICOMConfigurationUtil {

    public static void configureDcmRcv(MirthDcmRcv dcmrcv, DICOMReceiver connector, DICOMReceiverProperties connectorProperties, String[] protocols) throws Exception {
        if (!StringUtils.equals(connectorProperties.getTls(), "notls")) {
            if (connectorProperties.getTls().equals("without")) {
                dcmrcv.setTlsWithoutEncyrption();
            } else if (connectorProperties.getTls().equals("3des")) {
                dcmrcv.setTls3DES_EDE_CBC();
            } else if (connectorProperties.getTls().equals("aes")) {
                dcmrcv.setTlsAES_128_CBC();
            }

            String trustStore = connector.getReplacer().replaceValues(connectorProperties.getTrustStore(), connector.getChannelId(), connector.getChannel().getName());
            if (StringUtils.isNotBlank(trustStore)) {
                dcmrcv.setTrustStoreURL(trustStore);
            }

            String trustStorePW = connector.getReplacer().replaceValues(connectorProperties.getTrustStorePW(), connector.getChannelId(), connector.getChannel().getName());
            if (StringUtils.isNotBlank(trustStorePW)) {
                dcmrcv.setTrustStorePassword(trustStorePW);
            }

            String keyPW = connector.getReplacer().replaceValues(connectorProperties.getKeyPW(), connector.getChannelId(), connector.getChannel().getName());
            if (StringUtils.isNotBlank(keyPW)) {
                dcmrcv.setKeyPassword(keyPW);
            }

            String keyStore = connector.getReplacer().replaceValues(connectorProperties.getKeyStore(), connector.getChannelId(), connector.getChannel().getName());
            if (StringUtils.isNotBlank(keyStore)) {
                dcmrcv.setKeyStoreURL(keyStore);
            }

            String keyStorePW = connector.getReplacer().replaceValues(connectorProperties.getKeyStorePW(), connector.getChannelId(), connector.getChannel().getName());
            if (StringUtils.isNotBlank(keyStorePW)) {
                dcmrcv.setKeyStorePassword(keyStorePW);
            }

            dcmrcv.setTlsNeedClientAuth(connectorProperties.isNoClientAuth());

            protocols = ArrayUtils.clone(protocols);

            if (connectorProperties.isNossl2()) {
                if (ArrayUtils.contains(protocols, "SSLv2Hello")) {
                    List<String> protocolsList = new ArrayList<String>(Arrays.asList(protocols));
                    protocolsList.remove("SSLv2Hello");
                    protocols = protocolsList.toArray(new String[protocolsList.size()]);
                }
            } else if (!ArrayUtils.contains(protocols, "SSLv2Hello")) {
                List<String> protocolsList = new ArrayList<String>(Arrays.asList(protocols));
                protocolsList.add("SSLv2Hello");
                protocols = protocolsList.toArray(new String[protocolsList.size()]);
            }

            dcmrcv.setTlsProtocol(MirthSSLUtil.getEnabledHttpsProtocols(protocols));

            dcmrcv.initTLS();
        }
    }

    public static void configureDcmSnd(MirthDcmSnd dcmsnd, DICOMDispatcher connector, DICOMDispatcherProperties connectorProperties, String[] protocols) throws Exception {
        if (connectorProperties.getTls() != null && !connectorProperties.getTls().equals("notls")) {
            if (connectorProperties.getTls().equals("without"))
                dcmsnd.setTlsWithoutEncyrption();
            if (connectorProperties.getTls().equals("3des"))
                dcmsnd.setTls3DES_EDE_CBC();
            if (connectorProperties.getTls().equals("aes"))
                dcmsnd.setTlsAES_128_CBC();
            if (connectorProperties.getTrustStore() != null && !connectorProperties.getTrustStore().equals(""))
                dcmsnd.setTrustStoreURL(connectorProperties.getTrustStore());
            if (connectorProperties.getTrustStorePW() != null && !connectorProperties.getTrustStorePW().equals(""))
                dcmsnd.setTrustStorePassword(connectorProperties.getTrustStorePW());
            if (connectorProperties.getKeyPW() != null && !connectorProperties.getKeyPW().equals(""))
                dcmsnd.setKeyPassword(connectorProperties.getKeyPW());
            if (connectorProperties.getKeyStore() != null && !connectorProperties.getKeyStore().equals(""))
                dcmsnd.setKeyStoreURL(connectorProperties.getKeyStore());
            if (connectorProperties.getKeyStorePW() != null && !connectorProperties.getKeyStorePW().equals(""))
                dcmsnd.setKeyStorePassword(connectorProperties.getKeyStorePW());
            dcmsnd.setTlsNeedClientAuth(connectorProperties.isNoClientAuth());

            protocols = ArrayUtils.clone(protocols);

            if (connectorProperties.isNossl2()) {
                if (ArrayUtils.contains(protocols, "SSLv2Hello")) {
                    List<String> protocolsList = new ArrayList<String>(Arrays.asList(protocols));
                    protocolsList.remove("SSLv2Hello");
                    protocols = protocolsList.toArray(new String[protocolsList.size()]);
                }
            } else if (!ArrayUtils.contains(protocols, "SSLv2Hello")) {
                List<String> protocolsList = new ArrayList<String>(Arrays.asList(protocols));
                protocolsList.add("SSLv2Hello");
                protocols = protocolsList.toArray(new String[protocolsList.size()]);
            }

            protocols = MirthSSLUtil.getEnabledHttpsProtocols(protocols);

            dcmsnd.setTlsProtocol(protocols);

            dcmsnd.initTLS();
        }
    }
}