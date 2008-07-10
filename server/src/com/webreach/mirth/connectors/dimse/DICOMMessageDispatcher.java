package com.webreach.mirth.connectors.dimse;

import java.io.File;
import java.util.HashMap;

import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.net.pdu.AAssociateRJ;
import org.dcm4che2.tool.dcmsnd.DcmSnd;
import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpointURI;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.util.FileUtil;

/**
 * Created by IntelliJ IDEA.
 * Date: Jun 11, 2008
 * Time: 10:22:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMMessageDispatcher extends AbstractMessageDispatcher {
    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();    
    private MonitoringController monitoringController = MonitoringController.getInstance();
	private MonitoringController.ConnectorType connectorType = MonitoringController.ConnectorType.SENDER;
	private AlertController alertController = AlertController.getInstance();
    
        private HashMap as2ts = new HashMap();
 
    public DICOMMessageDispatcher(DICOMConnector connector) {
		super(connector);
        System.out.println("DICOMMEssageDispatcher Constructor");
        this.connector = connector;
		monitoringController.updateStatus(connector, connectorType, MonitoringController.Event.INITIALIZED);
	}    
    @Override
    public UMOMessage doSend(UMOEvent event) throws Exception {
        // do sending logic
        System.out.println("DICOMMEssageDispatcher doSend");
        monitoringController.updateStatus(connector, connectorType, MonitoringController.Event.BUSY);        
        MessageObject messageObject = messageObjectController.getMessageObjectFromEvent(event);        
		String data = messageObject.getEncodedData();
        File tempFile = File.createTempFile("temp","tmp");
        FileUtil.write(tempFile.getAbsolutePath(),false,FileUtil.decode(data));
        if (messageObject == null) {
			return null;
		}
        DICOMConnector dicomConnector = (DICOMConnector) connector;
        UMOEndpointURI uri = event.getEndpoint().getEndpointURI();

        DcmSnd dcmSnd = new DcmSnd();
        dcmSnd.setCalledAET("DCMRCV");
        dcmSnd.setRemoteHost(uri.getHost());
        dcmSnd.setRemotePort(uri.getPort());
        if(dicomConnector.getApplicationEntity() != null && !dicomConnector.getApplicationEntity().equals(""))
            dcmSnd.setCalledAET(dicomConnector.getApplicationEntity());
        if(dicomConnector.getLocalApplicationEntity() != null && !dicomConnector.getLocalApplicationEntity().equals("")){
            dcmSnd.setCalling(dicomConnector.getLocalApplicationEntity());
        }
        if(dicomConnector.getLocalHost() != null && !dicomConnector.getLocalHost().equals(""))  {
            dcmSnd.setLocalHost(dicomConnector.getLocalHost());
            dcmSnd.setLocalPort(dicomConnector.getLocalPort());
        }
                
        dcmSnd.addFile(tempFile);
        // New Attributes/properties -----
        if(dicomConnector.getAccecptto() != 5)
            dcmSnd.setAcceptTimeout(dicomConnector.getAccecptto());
        if(dicomConnector.getAsync() > 0)
            dcmSnd.setMaxOpsInvoked(dicomConnector.getAsync());
        if(dicomConnector.getBufsize() !=  1)
            dcmSnd.setTranscoderBufferSize(dicomConnector.getBufsize());
        if(dicomConnector.getConnectto() > 0)
            dcmSnd.setConnectTimeout(dicomConnector.getConnectto());
        if(dicomConnector.getPriority().equals("med"))
            dcmSnd.setPriority(0);
        else if(dicomConnector.getPriority().equals("low"))
            dcmSnd.setPriority(1);    
        else if(dicomConnector.getPriority().equals("high"))
            dcmSnd.setPriority(2);
        if (dicomConnector.getUsername() != null && !dicomConnector.getUsername().equals("")) {
            String username = (String) dicomConnector.getUsername();
            UserIdentity userId;
            if (dicomConnector.getPasscode() != null && !dicomConnector.getPasscode().equals("")) {
                String passcode = dicomConnector.getPasscode();
                userId = new UserIdentity.UsernamePasscode(username,
                        passcode.toCharArray());
            } else {
                userId = new UserIdentity.Username(username);
            }                      
            userId.setPositiveResponseRequested(dicomConnector.isUidnegrsp());
            dcmSnd.setUserIdentity(userId);
        }
        dcmSnd.setPackPDV(dicomConnector.isPdv1());
        if(dicomConnector.getRcvpdulen() != 16)
            dcmSnd.setMaxPDULengthReceive(dicomConnector.getRcvpdulen());
        if(dicomConnector.getReaper() != 10)
            dcmSnd.setAssociationReaperPeriod(dicomConnector.getReaper());
        if(dicomConnector.getReleaseto() != 5)
            dcmSnd.setReleaseTimeout(dicomConnector.getReleaseto());
        if(dicomConnector.getRspto() != 60)
            dcmSnd.setDimseRspTimeout(dicomConnector.getRspto());
        if(dicomConnector.getShutdowndelay() != 1000)
            dcmSnd.setShutdownDelay(dicomConnector.getShutdowndelay());
        if(dicomConnector.getSndpdulen() != 16)
            dcmSnd.setMaxPDULengthSend(dicomConnector.getSndpdulen());
        if(dicomConnector.getSoclosedelay() != 50)
            dcmSnd.setSocketCloseDelay(dicomConnector.getSoclosedelay());
        if(dicomConnector.getSorcvbuf() > 0)
            dcmSnd.setReceiveBufferSize(dicomConnector.getSorcvbuf());
        if(dicomConnector.getSosndbuf() > 0)
            dcmSnd.setSendBufferSize(dicomConnector.getSosndbuf());
        dcmSnd.setStorageCommitment(dicomConnector.isStgcmt());
        dcmSnd.setTcpNoDelay(!dicomConnector.isTcpdelay());
       
        if(dicomConnector.getTls() != null && !dicomConnector.getTls().equals("notls")){
            if(dicomConnector.getTls().equals("without"))
                dcmSnd.setTlsWithoutEncyrption();
            if(dicomConnector.getTls().equals("3des"))
                dcmSnd.setTls3DES_EDE_CBC();
            if(dicomConnector.getTls().equals("aes"))
                dcmSnd.setTlsAES_128_CBC();
            if(dicomConnector.getTruststore() != null && !dicomConnector.getTruststore().equals(""))
                dcmSnd.setTrustStoreURL(dicomConnector.getTruststore());
            if(dicomConnector.getTruststorepw() != null && !dicomConnector.getTruststorepw().equals(""))
                dcmSnd.setTrustStorePassword(dicomConnector.getTruststorepw());
            if(dicomConnector.getKeypw() != null && !dicomConnector.getKeypw().equals(""))
                dcmSnd.setKeyPassword(dicomConnector.getKeypw());
            if(dicomConnector.getKeystore() != null && !dicomConnector.getKeystore().equals(""))
                dcmSnd.setKeyStoreURL(dicomConnector.getKeystore());
            if(dicomConnector.getKeystorepw() != null && !dicomConnector.getKeystorepw().equals(""))
                dcmSnd.setKeyStorePassword(dicomConnector.getKeystorepw());
            dcmSnd.setTlsNeedClientAuth(dicomConnector.isNoclientauth());
            if(!dicomConnector.isNossl2())
                dcmSnd.disableSSLv2Hello();
            dcmSnd.initTLS();
        }

        dcmSnd.setOfferDefaultTransferSyntaxInSeparatePresentationContext(dicomConnector.isTs1());
        
        dcmSnd.configureTransferCapability();
        dcmSnd.start();
        
        try {
            dcmSnd.open();
            dcmSnd.send();
            dcmSnd.close();            
            messageObjectController.setSuccess(messageObject, "Message successfully sent");
        }
        catch(AAssociateRJ e){
            e.getMessage();
            messageObjectController.setError(messageObject, Constants.ERROR_415, e.getMessage(), null);
			alertController.sendAlerts(((DICOMConnector) connector).getChannelId(), Constants.ERROR_415,e.getMessage(), null);
        }
        catch(Exception e){
            e.printStackTrace();
            messageObjectController.setError(messageObject, Constants.ERROR_415, "", null);
			alertController.sendAlerts(((DICOMConnector) connector).getChannelId(), Constants.ERROR_415,"", null);
        }
        dcmSnd.stop();
        
        tempFile.delete();
        monitoringController.updateStatus(connector, connectorType, MonitoringController.Event.DONE);
        return event.getMessage();    
    }


    @Override
	public void doDispatch(UMOEvent event) throws Exception {
        System.out.println("DICOMMEssageDispatcher doDispatch");
        doSend(event);
	}
    @Override
    public void doDispose() {
		
	}
    
    public UMOMessage receive(UMOEndpointURI endpointUri, long timeout) throws Exception {
        
        System.out.println("DICOMMEssageDispatcher receive");
        return null;    
    }
    
	
    public Object getDelegateSession() throws UMOException {
		return null;
	}  
       
    
}
