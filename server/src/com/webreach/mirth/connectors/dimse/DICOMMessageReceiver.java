/*
 * $Header: /home/projects/mule/scm/mule/providers/dicom/src/java/org/mule/providers/dicom/DICOMMessageReceiver.java,v 1.23 2005/11/05 12:23:27 aperepel Exp $
 * $Revision: 1.23 $
 * $Date: 2005/11/05 12:23:27 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 */
package com.webreach.mirth.connectors.dimse;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.impl.ResponseOutputStream;
import org.mule.providers.AbstractMessageReceiver;
import org.mule.providers.ConnectException;
import org.mule.providers.TemplateValueReplacer;
import org.mule.providers.AbstractConnector;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.Disposable;
import org.mule.umo.lifecycle.DisposeException;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.dcm4che2.net.*;
import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.tool.dcmrcv.DcmRcv;

import sun.misc.BASE64Encoder;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Response;
import com.webreach.mirth.model.converters.DICOMSerializer;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MonitoringController.ConnectorType;
import com.webreach.mirth.server.controllers.MonitoringController.Event;
import com.webreach.mirth.server.mule.transformers.JavaScriptPostprocessor;


import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>DICOMMessageReceiver</code> acts like a DICOM server to receive socket
 * requests.
 * 
 * @author <a href="mailto:dans@webreachinc.com">Daniel Svanstedt</a>
 * 
 * @version $Revision: 1.23 $
 */
public class DICOMMessageReceiver extends AbstractMessageReceiver {
    // --- DICOM Specific Variables ---
    DcmRcv2 dcmrcv = new DcmRcv2(); 
    protected DICOMConnector connector;
	private AlertController alertController = AlertController.getInstance();
	private MonitoringController monitoringController = MonitoringController.getInstance();
	private JavaScriptPostprocessor postProcessor = new JavaScriptPostprocessor();
	private TemplateValueReplacer replacer = new TemplateValueReplacer();
	private ConnectorType connectorType = ConnectorType.LISTENER;
	public DICOMMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint) throws InitialisationException {
		super(connector, component, endpoint);
		DICOMConnector tcpConnector = (DICOMConnector) connector;
		this.connector = tcpConnector;
	}

    @Override
    public void doConnect() throws ConnectException {
        
        disposing.set(false);
		URI uri = endpoint.getEndpointURI().getUri();
		try {
            dcmrcv.setPort(uri.getPort());
            dcmrcv.setHostname(uri.getHost());
            dcmrcv.setAEtitle("DCMRCV");

            String[] only_def_ts = { UID.ImplicitVRLittleEndian };
            String[] native_le_ts = { UID.ImplicitVRLittleEndian };
            String[] native_ts = { UID.ImplicitVRLittleEndian };
            String[] non_retired_ts = { UID.ImplicitVRLittleEndian };
            if (connector.getDest() != null && !connector.getDest().equals(""))
                dcmrcv.setDestination(connector.getDest());            
            if (connector.isDefts())
                dcmrcv.setTransferSyntax(only_def_ts);            
            else if (connector.isNativeData()){
                if(connector.isBigendian()){
                    dcmrcv.setTransferSyntax(native_ts);
                }
                else {
                    dcmrcv.setTransferSyntax(native_le_ts);
                }
            }
            else if(connector.isBigendian()){
                dcmrcv.setTransferSyntax(non_retired_ts);
            }
            if(connector.getReaper() != 10)
                dcmrcv.setAssociationReaperPeriod(connector.getReaper());
            if(connector.getIdleto() != 60)
                dcmrcv.setIdleTimeout(connector.getIdleto());
            if(connector.getRequestto() != 5)
                dcmrcv.setRequestTimeout(connector.getRequestto());
            if(connector.getReleaseto() != 5)
                dcmrcv.setReleaseTimeout(connector.getReleaseto()); 
            if(connector.getSoclosedelay() != 50)
                dcmrcv.setSocketCloseDelay(connector.getSoclosedelay());            
            if(connector.getRspdelay() > 0)
                dcmrcv.setDimseRspDelay(connector.getRspdelay());            
            if(connector.getRcvpdulen() != 16)
                dcmrcv.setMaxPDULengthReceive(connector.getRcvpdulen());   
            if(connector.getSndpdulen() != 16)
                dcmrcv.setMaxPDULengthSend(connector.getSndpdulen());             
            if(connector.getSosndbuf() > 0)
                dcmrcv.setSendBufferSize(connector.getSosndbuf());              
            if(connector.getSorcvbuf() > 0)
                dcmrcv.setReceiveBufferSize(connector.getSorcvbuf());               
            if(connector.getBufsize() != 1)
                dcmrcv.setFileBufferSize(connector.getBufsize());              
            dcmrcv.setPackPDV(connector.isPdv1());
            dcmrcv.setTcpNoDelay(!connector.isTcpdelay());            
            if(connector.getAsync() > 0)
                dcmrcv.setMaxOpsPerformed(connector.getAsync());                
            dcmrcv.initTransferCapability();
            // tls settings
            if(connector.getTls() != null && !connector.getTls().equals("notls")){
                if(connector.getTls().  equals("without"))
                    dcmrcv.setTlsWithoutEncyrption();
                else if(connector.getTls().equals("3des"))
                    dcmrcv.setTls3DES_EDE_CBC();
                else if(connector.getTls().equals("aes"))
                    dcmrcv.setTlsAES_128_CBC();
                if(connector.getTruststore() != null && !connector.getTruststore().equals(""))
                    dcmrcv.setTrustStoreURL(connector.getTruststore());
                if(connector.getTruststorepw() != null && !connector.getTruststorepw().equals(""))
                    dcmrcv.setTrustStorePassword(connector.getTruststorepw());
                if(connector.getKeypw() != null && !connector.getKeypw().equals(""))
                    dcmrcv.setKeyPassword(connector.getKeypw());
                if(connector.getKeystore() != null && !connector.getKeystore().equals(""))
                    dcmrcv.setKeyStoreURL(connector.getKeystore());
                if(connector.getKeystorepw() != null && !connector.getKeystorepw().equals(""))
                    dcmrcv.setKeyStorePassword(connector.getKeystorepw());
                dcmrcv.setTlsNeedClientAuth(connector.isNoclientauth());
                if(!connector.isNossl2())
                    dcmrcv.disableSSLv2Hello();
                dcmrcv.initTLS();
            }

            try {
                dcmrcv.start();
            } catch (IOException e) {
                e.printStackTrace();
            }                
            //System.out.println("Start Server listening on port " + connector.getNc().getPort());
            monitoringController.updateStatus(this.connector, connectorType, Event.INITIALIZED);
		} catch (Exception e) {
			throw new ConnectException(new Message("DICOM", 1, uri), e, this);
		}
	}

    @Override
    public void doDisconnect() throws ConnectException {
		// this will cause the server thread to quit
	}

    @Override
    public void doDispose() {
        //device.stopListening();
		disposing.set(true);
        try {
            dcmrcv.stop();
		}
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
			monitoringController.updateStatus(connector, connectorType, Event.DISCONNECTED);
		}     
        logger.info("Closed DICOM port");
	}
        
    public class DcmRcv2 extends DcmRcv {

        @Override
        protected void onCStoreRQ(Association as, int pcid, DicomObject rq,
                PDVInputStream dataStream, String tsuid, DicomObject rsp)
                throws IOException, DicomServiceException {
                UMOMessage returnMessage = null;
                try {
                    String cuid = rq.getString(Tag.AffectedSOPClassUID);
                    String iuid = rq.getString(Tag.AffectedSOPInstanceUID);                    
                    byte[] dicomObj = DICOMSerializer.readDicomObj(rq);
                    BasicDicomObject fmi = new BasicDicomObject();
                    fmi.initFileMetaInformation(cuid, iuid, tsuid);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    BufferedOutputStream bos = new BufferedOutputStream(baos);
                    DicomOutputStream dos = new DicomOutputStream(bos);
                    dos.writeFileMetaInformation(fmi);
                    dataStream.copyTo(dos);
                    dos.close();        
                    BASE64Encoder encoder = new BASE64Encoder();
                    String dcmString = encoder.encode(baos.toByteArray());
                    returnMessage = routeMessage(new MuleMessage(dcmString), endpoint.isSynchronous());
                    // We need to check the message status
                    if (returnMessage != null && returnMessage instanceof MuleMessage) {
                        Object payload = returnMessage.getPayload();
                        if (payload instanceof MessageObject) {
                            MessageObject messageObjectResponse = (MessageObject) payload;
                            postProcessor.doPostProcess(messageObjectResponse);
                            Map responseMap = messageObjectResponse.getResponseMap();
                            String errorString = "";
                        }
                    }
                } catch (Exception e) {
                } finally {
                    // Let the dispose take care of closing the socket
                }
        }
        @Override
        public void cstore(final Association as, final int pcid, DicomObject rq, 
                PDVInputStream dataStream, String tsuid) 
                throws DicomServiceException, IOException {
            final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
            onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
            as.writeDimseRSP(pcid, rsp);
            onCStoreRSP(as, pcid, rq, dataStream, tsuid, rsp);
        }      
           
    }             
}