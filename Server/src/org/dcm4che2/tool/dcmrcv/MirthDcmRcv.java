package org.dcm4che2.tool.dcmrcv;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.pdu.PresentationContext;
import org.dcm4che2.net.pdu.UserIdentityRQ;

import com.mirth.connect.connectors.dimse.DICOMConfiguration;
import com.mirth.connect.donkey.model.message.RawMessage;
import com.mirth.connect.donkey.server.channel.ChannelException;
import com.mirth.connect.donkey.server.channel.DispatchResult;
import com.mirth.connect.donkey.server.channel.SourceConnector;

public class MirthDcmRcv extends DcmRcv {
    private Logger logger = Logger.getLogger(this.getClass());
    private SourceConnector sourceConnector;
    private DICOMConfiguration dicomConfiguration;

    public MirthDcmRcv(SourceConnector sourceConnector, DICOMConfiguration dicomConfiguration) {
        super("DCMRCV", false);
        this.sourceConnector = sourceConnector;
        this.dicomConfiguration = dicomConfiguration;
        init();
    }

    public Device getDevice() {
        return device;
    }

    public NetworkConnection getNetworkConnection() {
        return nc;
    }

    @Override
    protected NetworkConnection createNetworkConnection() {
        return dicomConfiguration.createNetworkConnection();
    }

    @Override
    void onCStoreRQ(Association as, int pcid, DicomObject rq, PDVInputStream dataStream, String tsuid, DicomObject rsp) throws IOException, DicomServiceException {
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bos = null;
        DicomOutputStream dos = null;

        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        BasicDicomObject fileMetaInformation = new BasicDicomObject();
        fileMetaInformation.initFileMetaInformation(cuid, iuid, tsuid);

        String originalThreadName = Thread.currentThread().getName();
        try {
            Thread.currentThread().setName("DICOM Receiver Thread on " + sourceConnector.getChannel().getName() + " (" + sourceConnector.getChannelId() + ") < " + originalThreadName);

            Map<String, Object> sourceMap = new HashMap<String, Object>();
            sourceMap.put("localApplicationEntityTitle", as.getLocalAET());
            sourceMap.put("remoteApplicationEntityTitle", as.getRemoteAET());

            if (as.getSocket() != null) {
                sourceMap.put("localAddress", as.getSocket().getLocalAddress().getHostAddress());
                sourceMap.put("localPort", as.getSocket().getLocalPort());
                if (as.getSocket().getRemoteSocketAddress() instanceof InetSocketAddress) {
                    sourceMap.put("remoteAddress", ((InetSocketAddress) as.getSocket().getRemoteSocketAddress()).getAddress().getHostAddress());
                    sourceMap.put("remotePort", ((InetSocketAddress) as.getSocket().getRemoteSocketAddress()).getPort());
                }
            }

            if (as.getAssociateAC() != null) {
                sourceMap.put("associateACProtocolVersion", as.getAssociateAC().getProtocolVersion());
                sourceMap.put("associateACImplClassUID", as.getAssociateAC().getImplClassUID());
                sourceMap.put("associateACImplVersionName", as.getAssociateAC().getImplVersionName());
                sourceMap.put("associateACApplicationContext", as.getAssociateAC().getApplicationContext());

                if (as.getAssociateAC().getNumberOfPresentationContexts() > 0) {
                    Map<Integer, String> pcMap = new LinkedHashMap<Integer, String>();
                    for (PresentationContext pc : as.getAssociateAC().getPresentationContexts()) {
                        pcMap.put(pc.getPCID(), pc.toString());
                    }
                    sourceMap.put("associateACPresentationContexts", MapUtils.unmodifiableMap(pcMap));
                }
            }

            if (as.getAssociateRQ() != null) {
                sourceMap.put("associateRQProtocolVersion", as.getAssociateRQ().getProtocolVersion());
                sourceMap.put("associateRQImplClassUID", as.getAssociateRQ().getImplClassUID());
                sourceMap.put("associateRQImplVersionName", as.getAssociateRQ().getImplVersionName());
                sourceMap.put("associateRQApplicationContext", as.getAssociateRQ().getApplicationContext());

                if (as.getAssociateRQ().getNumberOfPresentationContexts() > 0) {
                    Map<Integer, String> pcMap = new LinkedHashMap<Integer, String>();
                    for (PresentationContext pc : as.getAssociateRQ().getPresentationContexts()) {
                        pcMap.put(pc.getPCID(), pc.toString());
                    }
                    sourceMap.put("associateRQPresentationContexts", MapUtils.unmodifiableMap(pcMap));
                }

                if (as.getAssociateRQ().getUserIdentity() != null) {
                    sourceMap.put("username", as.getAssociateRQ().getUserIdentity().getUsername());
                    sourceMap.put("passcode", new String(as.getAssociateRQ().getUserIdentity().getPasscode()));

                    int type = as.getAssociateRQ().getUserIdentity().getUserIdentityType();
                    String typeString;
                    switch (type) {
                        case UserIdentityRQ.USERNAME:
                            typeString = "USERNAME";
                            break;
                        case UserIdentityRQ.USERNAME_PASSCODE:
                            typeString = "USERNAME_PASSCODE";
                            break;
                        case UserIdentityRQ.KERBEROS:
                            typeString = "KERBEROS";
                            break;
                        case UserIdentityRQ.SAML:
                            typeString = "SAML";
                            break;
                        default:
                            typeString = String.valueOf(type);
                    }
                    sourceMap.put("userIdentityType", typeString);
                }
            }

            sourceMap.putAll(dicomConfiguration.getCStoreRequestInformation(as));

            baos = new ByteArrayOutputStream();
            bos = new BufferedOutputStream(baos);
            dos = new DicomOutputStream(bos);
            dos.writeFileMetaInformation(fileMetaInformation);
            dataStream.copyTo(dos);
            // MIRTH-2072: This needs to be closed here
            dos.close();

            byte[] dicomMessage = baos.toByteArray();

            // Allow the stream buffers to be garbage collected before the message is processed.
            bos = null;
            baos = null;
            DispatchResult dispatchResult = null;

            try {
                dispatchResult = sourceConnector.dispatchRawMessage(new RawMessage(dicomMessage, null, sourceMap));
            } finally {
                sourceConnector.finishDispatch(dispatchResult);
            }
        } catch (ChannelException e) {
        } catch (Exception e) {
            logger.error(e);
        } finally {
            Thread.currentThread().setName(originalThreadName);
            // Let the dispose take care of closing the socket
            IOUtils.closeQuietly(baos);
            IOUtils.closeQuietly(bos);
        }
    }

    @Override
    public boolean isStoreFile() {
        return true;
    }
}
