package com.webreach.mirth.connectors.dimse;

import org.mule.providers.AbstractMessageDispatcher;
import org.mule.umo.UMOMessage;
import org.mule.umo.UMOEvent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.dcm4che2.net.*;
import org.dcm4che2.net.service.StorageCommitmentService;
import org.dcm4che2.data.*;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.TranscoderInputHandler;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.UIDUtils;
import org.apache.commons.cli.*;
import com.webreach.mirth.server.controllers.MonitoringController;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.util.FileUtil;
import com.webreach.mirth.model.MessageObject;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.net.URL;
import java.net.MalformedURLException;

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

        DcmSnd2 dcmSnd = new DcmSnd2();
        dcmSnd.setCalledAET("DCMRCV");
        dcmSnd.setRemoteHost(uri.getHost());
        dcmSnd.setRemotePort(uri.getPort());
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
        dcmSnd.open();
        dcmSnd.send();
        dcmSnd.close();
        dcmSnd.stop();
        
        messageObjectController.setSuccess(messageObject, "Message successfully sent");
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
    
  
   
 
  
    
     public class DcmSnd2 extends StorageCommitmentService {
    private static final int KB = 1024;

    private static final int MB = KB * KB;

    private static final int PEEK_LEN = 1024;

    private static final String USAGE = 
        "dcmsnd [Options] <aet>[@<host>[:<port>]] <file>|<directory>...";

    private static final String DESCRIPTION = 
        "\nLoad composite DICOM Object(s) from specified DICOM file(s) and send it "
      + "to the specified remote Application Entity. If a directory is specified,"
      + "DICOM Object in files under that directory and further sub-directories "
      + "are sent. If <port> is not specified, DICOM default port 104 is assumed. "
      + "If also no <host> is specified, localhost is assumed. Optionally, a "
      + "Storage Commitment Request for successfully tranferred objects is sent "
      + "to the remote Application Entity after the storage. The Storage Commitment "
      + "result is accepted on the same association or - if a local port is "
      + "specified by option -L - in a separate association initiated by the "
      + "remote Application Entity\n"
      + "OPTIONS:";

    private static final String EXAMPLE = 
        "\nExample: dcmsnd -stgcmt -L DCMSND:11113 STORESCP@localhost:11112 image.dcm \n"
      + "=> Start listening on local port 11113 for receiving Storage Commitment "
      + "results, send DICOM object image.dcm to Application Entity STORESCP, "
      + "listening on local port 11112, and request Storage Commitment.";

    private  char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
    
    private  final String[] ONLY_IVLE_TS = { 
        UID.ImplicitVRLittleEndian
    };

    private  final String[] IVLE_TS = { 
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian, 
        UID.ExplicitVRBigEndian,
    };

    private  final String[] EVLE_TS = {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRBigEndian, 
    };

    private  final String[] EVBE_TS = { 
        UID.ExplicitVRBigEndian,
        UID.ExplicitVRLittleEndian, 
        UID.ImplicitVRLittleEndian, 
    };

    private  final int STG_CMT_ACTION_TYPE = 1;

    private Executor executor = new NewThreadExecutor("DCMSND");

    private NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();

    private NetworkConnection remoteConn = new NetworkConnection();

    private Device device = new Device("DCMSND");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection conn = new NetworkConnection();

    private HashMap as2ts = new HashMap();

    private ArrayList files = new ArrayList();

    private Association assoc;

    private int priority = 0;
    
    private int transcoderBufferSize = 1024;

    private int filesSent = 0;

    private long totalSize = 0L;
    
    private boolean stgcmt = false;
    
    private long shutdownDelay = 1000L;
    
    private DicomObject stgCmtResult;

    private String keyStoreURL = "resource:tls/test_sys_1.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET; 
    
    public DcmSnd2() {
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAssociationAcceptor(true);
        ae.register(this);
        ae.setAETitle("DCMSND");
    }

    public final void setLocalHost(String hostname) {
        conn.setHostname(hostname);
    }

    public final void setLocalPort(int port) {
        conn.setPort(port);
    }
    
    public final void setRemoteHost(String hostname) {
        remoteConn.setHostname(hostname);
    }

    public final void setRemotePort(int port) {
        remoteConn.setPort(port);
    }
    
    public final void setTlsWithoutEncyrption() {
        conn.setTlsWithoutEncyrption();
        remoteConn.setTlsWithoutEncyrption();
    }

    public final void setTls3DES_EDE_CBC() {
        conn.setTls3DES_EDE_CBC();
        remoteConn.setTls3DES_EDE_CBC();
    }

    public final void setTlsAES_128_CBC() {
        conn.setTlsAES_128_CBC();
        remoteConn.setTlsAES_128_CBC();
    }
    
    public final void disableSSLv2Hello() {
        conn.disableSSLv2Hello();
    }
    
    public final void setTlsNeedClientAuth(boolean needClientAuth) {
        conn.setTlsNeedClientAuth(needClientAuth);
    }  
    
    public final void setKeyStoreURL(String url) {
        keyStoreURL = url;
    }
    
    public final void setKeyStorePassword(String pw) {
        keyStorePassword = pw.toCharArray();
    }
    
    public final void setKeyPassword(String pw) {
        keyPassword = pw.toCharArray();
    }
    
    public final void setTrustStorePassword(String pw) {
        trustStorePassword = pw.toCharArray();
    }
    
    public final void setTrustStoreURL(String url) {
        trustStoreURL = url;
    }

    public final void setCalledAET(String called) {
        remoteAE.setAETitle(called);
    }

    public final void setCalling(String calling) {
        ae.setAETitle(calling);
    }
    
    public final void setUserIdentity(UserIdentity userIdentity) {
        ae.setUserIdentity(userIdentity);
    }
    
    public final void setOfferDefaultTransferSyntaxInSeparatePresentationContext(
            boolean enable) {
        ae.setOfferDefaultTransferSyntaxInSeparatePresentationContext(enable);
    }

    public final void setStorageCommitment(boolean stgcmt) {
        this.stgcmt = stgcmt;
    }

    public final boolean isStorageCommitment() {
        return stgcmt;
    }
    
    public final void setShutdownDelay(int shutdownDelay) {
        this.shutdownDelay = shutdownDelay;
    }
    

    public final void setConnectTimeout(int connectTimeout) {
        conn.setConnectTimeout(connectTimeout);
    }

    public final void setMaxPDULengthReceive(int maxPDULength) {
        ae.setMaxPDULengthReceive(maxPDULength);
    }

    public final void setMaxOpsInvoked(int maxOpsInvoked) {
        ae.setMaxOpsInvoked(maxOpsInvoked);
    }

    public final void setPackPDV(boolean packPDV) {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period) {
        device.setAssociationReaperPeriod(period);
    }

    public final void setDimseRspTimeout(int timeout) {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setPriority(int priority) {
        this.priority = priority;
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        conn.setTcpNoDelay(tcpNoDelay);
    }

    public final void setAcceptTimeout(int timeout) {
        conn.setAcceptTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout) {
        conn.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int timeout) {
        conn.setSocketCloseDelay(timeout);
    }

    public final void setMaxPDULengthSend(int maxPDULength) {
        ae.setMaxPDULengthSend(maxPDULength);
    }

    public final void setReceiveBufferSize(int bufferSize) {
        conn.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize) {
        conn.setSendBufferSize(bufferSize);
    }

    public final void setTranscoderBufferSize(int transcoderBufferSize) {
        this.transcoderBufferSize = transcoderBufferSize;
    }

    public final int getNumberOfFilesToSend() {
        return files.size();
    }

    public final int getNumberOfFilesSent() {
        return filesSent;
    }

    public final long getTotalSizeSent() {
        return totalSize;
    }


    private synchronized DicomObject waitForStgCmtResult() throws InterruptedException {
        while (stgCmtResult == null) wait();
        return stgCmtResult;
    }


    public void addFile(File f) {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            for (int i = 0; i < fs.length; i++)
                addFile(fs[i]);
            return;
        }
        FileInfo info = new FileInfo(f);
        DicomObject dcmObj = new BasicDicomObject();
        try {
            DicomInputStream in = new DicomInputStream(f);
            try {
                in.setHandler(new StopTagInputHandler(Tag.StudyDate));
                in.readDicomObject(dcmObj, PEEK_LEN);
                info.tsuid = in.getTransferSyntax().uid();
                info.fmiEndPos = in.getEndOfFileMetaInfoPosition();
            } finally {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("WARNING: Failed to parse " + f + " - skipped.");
            System.out.print('F');
            return;
        }
        info.cuid = dcmObj.getString(Tag.SOPClassUID);
        if (info.cuid == null) {
            System.err.println("WARNING: Missing SOP Class UID in " + f
                    + " - skipped.");
            System.out.print('F');
            return;
        }
        info.iuid = dcmObj.getString(Tag.SOPInstanceUID);
        if (info.iuid == null) {
            System.err.println("WARNING: Missing SOP Instance UID in " + f
                    + " - skipped.");
            System.out.print('F');
            return;
        }
        addTransferCapability(info.cuid, info.tsuid);
        files.add(info);
        System.out.print('.');
    }

    private void addTransferCapability(String cuid, String tsuid) {
        HashSet ts = (HashSet) as2ts.get(cuid);
        if (ts == null) {
            ts = new HashSet();
            ts.add(UID.ImplicitVRLittleEndian);
            as2ts.put(cuid, ts);
        }
        ts.add(tsuid);
    }

    private void configureTransferCapability() {
        int off = stgcmt ? 1 : 0;
        TransferCapability[] tc = new TransferCapability[off + as2ts.size()];
        if (stgcmt) {
            tc[0] = new TransferCapability(
                    UID.StorageCommitmentPushModelSOPClass,
                    ONLY_IVLE_TS, 
                    TransferCapability.SCU);
        }
        Iterator iter = as2ts.entrySet().iterator();
        for (int i = off; i < tc.length; i++) {
            Map.Entry e = (Map.Entry) iter.next();
            String cuid = (String) e.getKey();
            HashSet ts = (HashSet) e.getValue();
            tc[i] = new TransferCapability(cuid, 
                    (String[]) ts.toArray(new String[ts.size()]),
                    TransferCapability.SCU);
        }
        ae.setTransferCapability(tc);
    }

    public void start() throws IOException { 
        if (conn.isListening()) {
            conn.bind(executor );
            System.out.println("Start Server listening on port " + conn.getPort());
        }
    }

    public void stop() {
        if (conn.isListening()) {
            try {
                Thread.sleep(shutdownDelay);
            } catch (InterruptedException e) {
                // Should not happen
                e.printStackTrace();                
            }
            conn.unbind();
        }
    }
    
    public void open() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteAE, executor);
    }

    public void send() {
        for (int i = 0, n = files.size(); i < n; ++i) {
            FileInfo info = (FileInfo) files.get(i);
            TransferCapability tc = assoc.getTransferCapabilityAsSCU(info.cuid);
            if (tc == null) {
                System.out.println();
                System.out.println(UIDDictionary.getDictionary().prompt(
                        info.cuid)
                        + " not supported by " + remoteAE.getAETitle());
                System.out.println("skip file " + info.f);
                continue;
            }
            String tsuid = selectTransferSyntax(tc.getTransferSyntax(),
                    info.tsuid);
            if (tsuid == null) {
                System.out.println();
                System.out.println(UIDDictionary.getDictionary().prompt(
                        info.cuid)
                        + " with "
                        + UIDDictionary.getDictionary().prompt(info.tsuid)
                        + " not supported by" + remoteAE.getAETitle());
                System.out.println("skip file " + info.f);
                continue;
            }
            try {
                DimseRSPHandler rspHandler = new DimseRSPHandler() {
                    public void onDimseRSP(Association as, DicomObject cmd,
                            DicomObject data) {
                        DcmSnd2.this.onDimseRSP(as, cmd, data);
                    }
                };

                assoc.cstore(info.cuid, info.iuid, priority,
                        new DataWriter(info), tsuid, rspHandler);
            } catch (NoPresentationContextException e) {
                System.err.println("WARNING: " + e.getMessage()
                        + " - cannot send " + info.f);
                System.out.print('F');
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("ERROR: Failed to send - " + info.f + ": "
                        + e.getMessage());
                System.out.print('F');
            } catch (InterruptedException e) {
                // should not happen
                e.printStackTrace();
            }
        }
        try {
            assoc.waitForDimseRSP();
        } catch (InterruptedException e) {
            // should not happen
            e.printStackTrace();
        }
    }

    public boolean commit() {
        DicomObject actionInfo = new BasicDicomObject();
        actionInfo.putString(Tag.TransactionUID, VR.UI, UIDUtils.createUID());
        DicomElement refSOPSq = actionInfo.putSequence(Tag.ReferencedSOPSequence);
        for (int i = 0, n = files.size(); i < n; ++i) {
            FileInfo info = (FileInfo) files.get(i);
            if (info.transferred) {
                BasicDicomObject refSOP = new BasicDicomObject();
                refSOP.putString(Tag.ReferencedSOPClassUID, VR.UI, info.cuid);
                refSOP.putString(Tag.ReferencedSOPInstanceUID, VR.UI, info.iuid);
                refSOPSq.addDicomObject(refSOP);
            }
        }
        try {
            DimseRSP rsp = assoc.naction(UID.StorageCommitmentPushModelSOPClass,
                UID.StorageCommitmentPushModelSOPInstance, STG_CMT_ACTION_TYPE,
                actionInfo, UID.ImplicitVRLittleEndian);
            rsp.next();
            DicomObject cmd = rsp.getCommand();
            int status = cmd.getInt(Tag.Status);
            if (status == 0) {
                return true;
            }
            System.err.println(
                    "WARNING: Storage Commitment request failed with status: "
                    + StringUtils.shortToHex(status) + "H");
            System.err.println(cmd.toString());
        } catch (NoPresentationContextException e) {
            System.err.println("WARNING: " + e.getMessage()
                    + " - cannot request Storage Commitment");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(
                    "ERROR: Failed to send Storage Commitment request: "
                    + e.getMessage());
        } catch (InterruptedException e) {
            // should not happen
            e.printStackTrace();
        }
        return false;
    }
    
    private String selectTransferSyntax(String[] available, String tsuid) {
        if (tsuid.equals(UID.ImplicitVRLittleEndian))
            return selectTransferSyntax(available, IVLE_TS);
        if (tsuid.equals(UID.ExplicitVRLittleEndian))
            return selectTransferSyntax(available, EVLE_TS);
        if (tsuid.equals(UID.ExplicitVRBigEndian))
            return selectTransferSyntax(available, EVBE_TS);
        return tsuid;
    }

    private String selectTransferSyntax(String[] available, String[] tsuids) {
        for (int i = 0; i < tsuids.length; i++)
            for (int j = 0; j < available.length; j++)
                if (available[j].equals(tsuids[i]))
                    return available[j];
        return null;
    }

    public void close() {
        try {
            assoc.release(false);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final class FileInfo {
        File f;

        String cuid;

        String iuid;

        String tsuid;

        long fmiEndPos;

        long length;
        
        boolean transferred;

        public FileInfo(File f) {
            this.f = f;
            this.length = f.length();
        }
                
    }

    private class DataWriter implements org.dcm4che2.net.DataWriter {

        private FileInfo info;

        public DataWriter(FileInfo info) {
            this.info = info;
        }

        public void writeTo(PDVOutputStream out, String tsuid)
                throws IOException {
            if (tsuid.equals(info.tsuid)) {
                FileInputStream fis = new FileInputStream(info.f);
                try {
                    long skip = info.fmiEndPos;
                    while (skip > 0)
                        skip -= fis.skip(skip);
                    out.copyFrom(fis);
                } finally {
                    fis.close();
                }
            } else {
                DicomInputStream dis = new DicomInputStream(info.f);
                try {
                    DicomOutputStream dos = new DicomOutputStream(out);
                    dos.setTransferSyntax(tsuid);
                    TranscoderInputHandler h = new TranscoderInputHandler(dos,
                            transcoderBufferSize);
                    dis.setHandler(h);
                    dis.readDicomObject();
                } finally {
                    dis.close();
                }
            }
        }

    }

    private void promptErrRSP(String prefix, int status, FileInfo info,
            DicomObject cmd) {
        System.err.println(prefix + StringUtils.shortToHex(status) + "H for "
                + info.f + ", cuid=" + info.cuid + ", tsuid=" + info.tsuid);
        System.err.println(cmd.toString());
    }

    private void onDimseRSP(Association as, DicomObject cmd, DicomObject data) {
        int status = cmd.getInt(Tag.Status);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        FileInfo info = (FileInfo) files.get(msgId - 1);
        switch (status) {
        case 0:
            info.transferred = true;
            totalSize += info.length;
            ++filesSent;
            System.out.print('.');
            break;
        case 0xB000:
        case 0xB006:
        case 0xB007:
            info.transferred = true;
            totalSize += info.length;
            ++filesSent;
            promptErrRSP("WARNING: Received RSP with Status ", status, info,
                    cmd);
            System.out.print('W');
            break;
        default:
            promptErrRSP("ERROR: Received RSP with Status ", status, info, cmd);
            System.out.print('F');
        }
    }
    
    protected synchronized void onNEventReportRSP(Association as, int pcid,
            DicomObject rq, DicomObject info, DicomObject rsp) {
        stgCmtResult = info;
        notifyAll();
    }

    public void initTLS() throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword);
        KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword);
        device.initTLS(keyStore,
                keyPassword != null ? keyPassword : keyStorePassword,
                trustStore);
    }
    
    private KeyStore loadKeyStore(String url, char[] password)
            throws GeneralSecurityException, IOException {
        KeyStore key = KeyStore.getInstance(toKeyStoreType(url));
        InputStream in = openFileOrURL(url);
        try {
            key.load(in, password);
        } finally {
            in.close();
        }
        return key;
    }

    private InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return DcmSnd2.class.getClassLoader().getResourceAsStream(
                    url.substring(9));
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }
    }

    private String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12")
                 ? "PKCS12" : "JKS";
    }         
         
     }                
    
}
