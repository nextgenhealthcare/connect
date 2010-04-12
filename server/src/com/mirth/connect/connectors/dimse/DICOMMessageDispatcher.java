/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.connectors.dimse;

import java.io.File;

import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.tool.dcmsnd.DcmSnd;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.providers.TemplateValueReplacer;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.server.Constants;
import com.mirth.connect.server.controllers.AlertController;
import com.mirth.connect.server.controllers.ControllerFactory;
import com.mirth.connect.server.controllers.MessageObjectController;
import com.mirth.connect.server.controllers.MonitoringController;
import com.mirth.connect.server.controllers.MonitoringController.Event;
import com.mirth.connect.server.util.FileUtil;

public class DICOMMessageDispatcher extends AbstractMessageDispatcher {
    private MessageObjectController messageObjectController = ControllerFactory.getFactory().createMessageObjectController();
    private MonitoringController monitoringController = ControllerFactory.getFactory().createMonitoringController();
    private MonitoringController.ConnectorType connectorType = MonitoringController.ConnectorType.SENDER;
    private AlertController alertController = ControllerFactory.getFactory().createAlertController();
    private TemplateValueReplacer replacer = new TemplateValueReplacer();

    public DICOMMessageDispatcher(DICOMConnector connector) {
        super(connector);
        this.connector = connector;
        monitoringController.updateStatus(connector, connectorType, Event.INITIALIZED);
    }

    public UMOMessage doSend(UMOEvent event) throws Exception {
        monitoringController.updateStatus(connector, connectorType, Event.BUSY);
        MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);

        if (messageObject == null) {
            return null;
        }

        DICOMConnector dicomConnector = (DICOMConnector) connector;
        String template = replacer.replaceValues(dicomConnector.getTemplate(), messageObject);
        File tempFile = File.createTempFile("temp", "tmp");
        FileUtil.write(tempFile.getAbsolutePath(), false, FileUtil.decode(template));

        DcmSnd dcmSnd = new DcmSnd();
        dcmSnd.setCalledAET("DCMRCV");
        dcmSnd.setRemoteHost(event.getEndpoint().getEndpointURI().getHost());
        dcmSnd.setRemotePort(event.getEndpoint().getEndpointURI().getPort());

        if ((dicomConnector.getApplicationEntity() != null) && !dicomConnector.getApplicationEntity().equals("")) {
            dcmSnd.setCalledAET(dicomConnector.getApplicationEntity());
        }

        if ((dicomConnector.getLocalApplicationEntity() != null) && !dicomConnector.getLocalApplicationEntity().equals("")) {
            dcmSnd.setCalling(dicomConnector.getLocalApplicationEntity());
        }

        if ((dicomConnector.getLocalHost() != null) && !dicomConnector.getLocalHost().equals("")) {
            dcmSnd.setLocalHost(dicomConnector.getLocalHost());
            dcmSnd.setLocalPort(dicomConnector.getLocalPort());
        }

        dcmSnd.addFile(tempFile);

        if (dicomConnector.getAccecptto() != 5)
            dcmSnd.setAcceptTimeout(dicomConnector.getAccecptto());
        if (dicomConnector.getAsync() > 0)
            dcmSnd.setMaxOpsInvoked(dicomConnector.getAsync());
        if (dicomConnector.getBufsize() != 1)
            dcmSnd.setTranscoderBufferSize(dicomConnector.getBufsize());
        if (dicomConnector.getConnectto() > 0)
            dcmSnd.setConnectTimeout(dicomConnector.getConnectto());
        if (dicomConnector.getPriority().equals("med"))
            dcmSnd.setPriority(0);
        else if (dicomConnector.getPriority().equals("low"))
            dcmSnd.setPriority(1);
        else if (dicomConnector.getPriority().equals("high"))
            dcmSnd.setPriority(2);
        if (dicomConnector.getUsername() != null && !dicomConnector.getUsername().equals("")) {
            String username = dicomConnector.getUsername();
            UserIdentity userId;
            if (dicomConnector.getPasscode() != null && !dicomConnector.getPasscode().equals("")) {
                String passcode = dicomConnector.getPasscode();
                userId = new UserIdentity.UsernamePasscode(username, passcode.toCharArray());
            } else {
                userId = new UserIdentity.Username(username);
            }
            userId.setPositiveResponseRequested(dicomConnector.isUidnegrsp());
            dcmSnd.setUserIdentity(userId);
        }
        dcmSnd.setPackPDV(dicomConnector.isPdv1());
        if (dicomConnector.getRcvpdulen() != 16)
            dcmSnd.setMaxPDULengthReceive(dicomConnector.getRcvpdulen());
        if (dicomConnector.getReaper() != 10)
            dcmSnd.setAssociationReaperPeriod(dicomConnector.getReaper());
        if (dicomConnector.getReleaseto() != 5)
            dcmSnd.setReleaseTimeout(dicomConnector.getReleaseto());
        if (dicomConnector.getRspto() != 60)
            dcmSnd.setDimseRspTimeout(dicomConnector.getRspto());
        if (dicomConnector.getShutdowndelay() != 1000)
            dcmSnd.setShutdownDelay(dicomConnector.getShutdowndelay());
        if (dicomConnector.getSndpdulen() != 16)
            dcmSnd.setMaxPDULengthSend(dicomConnector.getSndpdulen());
        if (dicomConnector.getSoclosedelay() != 50)
            dcmSnd.setSocketCloseDelay(dicomConnector.getSoclosedelay());
        if (dicomConnector.getSorcvbuf() > 0)
            dcmSnd.setReceiveBufferSize(dicomConnector.getSorcvbuf());
        if (dicomConnector.getSosndbuf() > 0)
            dcmSnd.setSendBufferSize(dicomConnector.getSosndbuf());

        dcmSnd.setStorageCommitment(dicomConnector.isStgcmt());
        dcmSnd.setTcpNoDelay(!dicomConnector.isTcpdelay());

        if (dicomConnector.getTls() != null && !dicomConnector.getTls().equals("notls")) {
            if (dicomConnector.getTls().equals("without"))
                dcmSnd.setTlsWithoutEncyrption();
            if (dicomConnector.getTls().equals("3des"))
                dcmSnd.setTls3DES_EDE_CBC();
            if (dicomConnector.getTls().equals("aes"))
                dcmSnd.setTlsAES_128_CBC();
            if (dicomConnector.getTruststore() != null && !dicomConnector.getTruststore().equals(""))
                dcmSnd.setTrustStoreURL(dicomConnector.getTruststore());
            if (dicomConnector.getTruststorepw() != null && !dicomConnector.getTruststorepw().equals(""))
                dcmSnd.setTrustStorePassword(dicomConnector.getTruststorepw());
            if (dicomConnector.getKeypw() != null && !dicomConnector.getKeypw().equals(""))
                dcmSnd.setKeyPassword(dicomConnector.getKeypw());
            if (dicomConnector.getKeystore() != null && !dicomConnector.getKeystore().equals(""))
                dcmSnd.setKeyStoreURL(dicomConnector.getKeystore());
            if (dicomConnector.getKeystorepw() != null && !dicomConnector.getKeystorepw().equals(""))
                dcmSnd.setKeyStorePassword(dicomConnector.getKeystorepw());
            dcmSnd.setTlsNeedClientAuth(dicomConnector.isNoclientauth());
            if (!dicomConnector.isNossl2())
            	dcmSnd.setTlsProtocol(new String[] {"TLSv1","SSLv3"});
            dcmSnd.initTLS();
        }

        dcmSnd.setOfferDefaultTransferSyntaxInSeparatePresentationContext(dicomConnector.isTs1());
        dcmSnd.configureTransferCapability();
        dcmSnd.start();

        try {
            dcmSnd.open();
            dcmSnd.send();
            dcmSnd.close();
            messageObjectController.setSuccess(messageObject, "DICOM message successfully sent", null);
        } catch (AAssociateRJ e) {
            messageObjectController.setError(messageObject, Constants.ERROR_415, e.getMessage(), null, null);
            alertController.sendAlerts(((DICOMConnector) connector).getChannelId(), Constants.ERROR_415, e.getMessage(), null);
        } catch (Exception e) {
            messageObjectController.setError(messageObject, Constants.ERROR_415, "", null, null);
            alertController.sendAlerts(((DICOMConnector) connector).getChannelId(), Constants.ERROR_415, "", null);
        }

        dcmSnd.stop();
        tempFile.delete();
        monitoringController.updateStatus(connector, connectorType, MonitoringController.Event.DONE);
        return event.getMessage();
    }

    public void doDispatch(UMOEvent event) throws Exception {
        doSend(event);
    }

    public void doDispose() {

    }

    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        return null;
    }

    public Object getDelegateSession() throws UMOException {
        return null;
    }
}