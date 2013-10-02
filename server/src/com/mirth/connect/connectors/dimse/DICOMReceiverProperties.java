/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import com.mirth.connect.donkey.model.channel.ConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorProperties;
import com.mirth.connect.donkey.model.channel.ListenerConnectorPropertiesInterface;
import com.mirth.connect.donkey.model.channel.ResponseConnectorProperties;
import com.mirth.connect.donkey.model.channel.ResponseConnectorPropertiesInterface;

public class DICOMReceiverProperties extends ConnectorProperties implements ListenerConnectorPropertiesInterface, ResponseConnectorPropertiesInterface {
    private ListenerConnectorProperties listenerConnectorProperties;
    private ResponseConnectorProperties responseConnectorProperties;

    private String applicationEntity;
    private String localHost;
    private String localPort;
    private String localApplicationEntity;
    private String soCloseDelay;
    private String releaseTo;
    private String requestTo;
    private String idleTo;
    private String reaper;
    private String rspDelay;
    private boolean pdv1;
    private String sndpdulen;
    private String rcvpdulen;
    private String async;
    private boolean bigEndian;
    private String bufSize;
    private boolean defts;
    private String dest;
    private boolean nativeData;
    private String sorcvbuf;
    private String sosndbuf;
    private boolean tcpDelay;

    private String keyPW;
    private String keyStore;
    private String keyStorePW;
    private boolean noClientAuth;
    private boolean nossl2;
    private String tls;
    private String trustStore;
    private String trustStorePW;

    public DICOMReceiverProperties() {
        listenerConnectorProperties = new ListenerConnectorProperties("104");
        responseConnectorProperties = new ResponseConnectorProperties();

        soCloseDelay = "50";
        releaseTo = "5";
        requestTo = "5";
        idleTo = "60";
        reaper = "10";
        rspDelay = "0";
        pdv1 = false;
        sndpdulen = "16";
        rcvpdulen = "16";
        async = "0";
        bigEndian = false;
        bufSize = "1";
        defts = false;
        dest = "";
        nativeData = false;
        sorcvbuf = "0";
        sosndbuf = "0";
        tcpDelay = true;

        keyPW = "";
        keyStore = "";
        keyStorePW = "";
        noClientAuth = true;
        nossl2 = true;
        tls = "notls";
        trustStore = "";
        trustStorePW = "";

        applicationEntity = "";
        localHost = "";
        localPort = "";
        localApplicationEntity = "";
    }

    @Override
    public String getProtocol() {
        return "DICOM";
    }

    @Override
    public String getName() {
        return "DICOM Listener";
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
    public ResponseConnectorProperties getResponseConnectorProperties() {
        return responseConnectorProperties;
    }

    public String getApplicationEntity() {
        return applicationEntity;
    }

    public void setApplicationEntity(String applicationEntity) {
        this.applicationEntity = applicationEntity;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public String getLocalPort() {
        return localPort;
    }

    public void setLocalPort(String localPort) {
        this.localPort = localPort;
    }

    public String getLocalApplicationEntity() {
        return localApplicationEntity;
    }

    public void setLocalApplicationEntity(String localApplicationEntity) {
        this.localApplicationEntity = localApplicationEntity;
    }

    public String getSoCloseDelay() {
        return soCloseDelay;
    }

    public void setSoCloseDelay(String soClosedDelay) {
        this.soCloseDelay = soClosedDelay;
    }

    public String getReleaseTo() {
        return releaseTo;
    }

    public void setReleaseTo(String releaseTo) {
        this.releaseTo = releaseTo;
    }

    public String getRequestTo() {
        return requestTo;
    }

    public void setRequestTo(String requestTo) {
        this.requestTo = requestTo;
    }

    public String getIdleTo() {
        return idleTo;
    }

    public void setIdleTo(String idleTo) {
        this.idleTo = idleTo;
    }

    public String getReaper() {
        return reaper;
    }

    public void setReaper(String reaper) {
        this.reaper = reaper;
    }

    public String getRspDelay() {
        return rspDelay;
    }

    public void setRspDelay(String rspDelay) {
        this.rspDelay = rspDelay;
    }

    public boolean isPdv1() {
        return pdv1;
    }

    public void setPdv1(boolean pdv1) {
        this.pdv1 = pdv1;
    }

    public String getSndpdulen() {
        return sndpdulen;
    }

    public void setSndpdulen(String sndpdulen) {
        this.sndpdulen = sndpdulen;
    }

    public String getRcvpdulen() {
        return rcvpdulen;
    }

    public void setRcvpdulen(String rcvpdulen) {
        this.rcvpdulen = rcvpdulen;
    }

    public String getAsync() {
        return async;
    }

    public void setAsync(String async) {
        this.async = async;
    }

    public boolean isBigEndian() {
        return bigEndian;
    }

    public void setBigEndian(boolean bigEndian) {
        this.bigEndian = bigEndian;
    }

    public String getBufSize() {
        return bufSize;
    }

    public void setBufSize(String bufSize) {
        this.bufSize = bufSize;
    }

    public boolean isDefts() {
        return defts;
    }

    public void setDefts(boolean defts) {
        this.defts = defts;
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public boolean isNativeData() {
        return nativeData;
    }

    public void setNativeData(boolean nativeData) {
        this.nativeData = nativeData;
    }

    public String getSorcvbuf() {
        return sorcvbuf;
    }

    public void setSorcvbuf(String sorcvbuf) {
        this.sorcvbuf = sorcvbuf;
    }

    public String getSosndbuf() {
        return sosndbuf;
    }

    public void setSosndbuf(String sosndbuf) {
        this.sosndbuf = sosndbuf;
    }

    public boolean isTcpDelay() {
        return tcpDelay;
    }

    public void setTcpDelay(boolean tcpDelay) {
        this.tcpDelay = tcpDelay;
    }

    public String getKeyPW() {
        return keyPW;
    }

    public void setKeyPW(String keyPW) {
        this.keyPW = keyPW;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getKeyStorePW() {
        return keyStorePW;
    }

    public void setKeyStorePW(String keyStorePW) {
        this.keyStorePW = keyStorePW;
    }

    public boolean isNoClientAuth() {
        return noClientAuth;
    }

    public void setNoClientAuth(boolean noClientAuth) {
        this.noClientAuth = noClientAuth;
    }

    public boolean isNossl2() {
        return nossl2;
    }

    public void setNossl2(boolean nossl2) {
        this.nossl2 = nossl2;
    }

    public String getTls() {
        return tls;
    }

    public void setTls(String tls) {
        this.tls = tls;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTrustStorePW() {
        return trustStorePW;
    }

    public void setTrustStorePW(String trustStorePW) {
        this.trustStorePW = trustStorePW;
    }

    public void setListenerConnectorProperties(ListenerConnectorProperties listenerConnectorProperties) {
        this.listenerConnectorProperties = listenerConnectorProperties;
    }

    public void setResponseConnectorProperties(ResponseConnectorProperties responseConnectorProperties) {
        this.responseConnectorProperties = responseConnectorProperties;
    }
}
