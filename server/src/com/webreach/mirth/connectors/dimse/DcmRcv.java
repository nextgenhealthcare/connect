package com.webreach.mirth.connectors.dimse;

import org.dcm4che2.net.service.StorageService;
import org.dcm4che2.net.service.VerificationService;
import org.dcm4che2.net.*;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.io.DicomOutputStream;
import org.mule.umo.UMOMessage;
import org.mule.impl.MuleMessage;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;

import com.webreach.mirth.model.converters.DICOMSerializer;
import com.webreach.mirth.model.MessageObject;
import sun.misc.BASE64Encoder;

/**
 * Created by IntelliJ IDEA.
 * Date: Jun 16, 2008
 * Time: 2:15:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class DcmRcv extends StorageService {

//-------------------------------------------------------------------------        
    private static final int KB = 1024;

    private static final String USAGE = "dcmrcv [Options] [<aet>[@<ip>]:]<port>";

    private static final String DESCRIPTION = "DICOM Server listening on specified <port> for incoming association "
            + "requests. If no local IP address of the network interface is specified "
            + "connections on any/all local addresses are accepted. If <aet> is "
            + "specified, only requests with matching called AE title will be "
            + "accepted.\n" + "Options:";

    private static final String EXAMPLE = "\nExample: dcmrcv DCMRCV:11112 -dest /tmp \n"
            + "=> Starts server listening on port 11112, accepting association "
            + "requests with DCMRCV as called AE title. Received objects "
            + "are stored to /tmp.";

    private  char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
    
    private  final String[] ONLY_DEF_TS = { UID.ImplicitVRLittleEndian };

    private  final String[] NATIVE_TS = { UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian };

    private  final String[] NATIVE_LE_TS = { UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian };

    private  final String[] NON_RETIRED_TS = { UID.JPEGLSLossless,
            UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14,
            UID.JPEG2000LosslessOnly, UID.DeflatedExplicitVRLittleEndian,
            UID.RLELossless, UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian,
            UID.JPEGBaseline1, UID.JPEGExtended24, UID.JPEGLSLossyNearLossless,
            UID.JPEG2000, UID.MPEG2, };

    private  final String[] NON_RETIRED_LE_TS = { UID.JPEGLSLossless,
            UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14,
            UID.JPEG2000LosslessOnly, UID.DeflatedExplicitVRLittleEndian,
            UID.RLELossless, UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian, UID.JPEGBaseline1, UID.JPEGExtended24,
            UID.JPEGLSLossyNearLossless, UID.JPEG2000, UID.MPEG2, };

    private final String[] CUIDS = {
            UID.BasicStudyContentNotificationSOPClassRetired,
            UID.StoredPrintStorageSOPClassRetired,
            UID.HardcopyGrayscaleImageStorageSOPClassRetired,
            UID.HardcopyColorImageStorageSOPClassRetired,
            UID.ComputedRadiographyImageStorage,
            UID.DigitalXRayImageStorageForPresentation,
            UID.DigitalXRayImageStorageForProcessing,
            UID.DigitalMammographyXRayImageStorageForPresentation,
            UID.DigitalMammographyXRayImageStorageForProcessing,
            UID.DigitalIntraoralXRayImageStorageForPresentation,
            UID.DigitalIntraoralXRayImageStorageForProcessing,
            UID.StandaloneModalityLUTStorageRetired,
            UID.EncapsulatedPDFStorage, UID.StandaloneVOILUTStorageRetired,
            UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
            UID.ColorSoftcopyPresentationStateStorageSOPClass,
            UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
            UID.BlendingSoftcopyPresentationStateStorageSOPClass,
            UID.XRayAngiographicImageStorage, UID.EnhancedXAImageStorage,
            UID.XRayRadiofluoroscopicImageStorage, UID.EnhancedXRFImageStorage,
            UID.XRayAngiographicBiPlaneImageStorageRetired,
            UID.PositronEmissionTomographyImageStorage,
            UID.StandalonePETCurveStorageRetired, UID.CTImageStorage,
            UID.EnhancedCTImageStorage, UID.NuclearMedicineImageStorage,
            UID.UltrasoundMultiframeImageStorageRetired,
            UID.UltrasoundMultiframeImageStorage, UID.MRImageStorage,
            UID.EnhancedMRImageStorage, UID.MRSpectroscopyStorage,
            UID.RTImageStorage, UID.RTDoseStorage, UID.RTStructureSetStorage,
            UID.RTBeamsTreatmentRecordStorage, UID.RTPlanStorage,
            UID.RTBrachyTreatmentRecordStorage,
            UID.RTTreatmentSummaryRecordStorage,
            UID.NuclearMedicineImageStorageRetired,
            UID.UltrasoundImageStorageRetired, UID.UltrasoundImageStorage,
            UID.RawDataStorage, UID.SpatialRegistrationStorage,
            UID.SpatialFiducialsStorage, UID.RealWorldValueMappingStorage,
            UID.SecondaryCaptureImageStorage,
            UID.MultiframeSingleBitSecondaryCaptureImageStorage,
            UID.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
            UID.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
            UID.MultiframeTrueColorSecondaryCaptureImageStorage,
            UID.VLImageStorageRetired, UID.VLEndoscopicImageStorage,
            UID.VideoEndoscopicImageStorage, UID.VLMicroscopicImageStorage,
            UID.VideoMicroscopicImageStorage,
            UID.VLSlideCoordinatesMicroscopicImageStorage,
            UID.VLPhotographicImageStorage, UID.VideoPhotographicImageStorage,
            UID.OphthalmicPhotography8BitImageStorage,
            UID.OphthalmicPhotography16BitImageStorage,
            UID.StereometricRelationshipStorage,
            UID.VLMultiframeImageStorageRetired,
            UID.StandaloneOverlayStorageRetired, UID.BasicTextSR,
            UID.EnhancedSR, UID.ComprehensiveSR, UID.ProcedureLogStorage,
            UID.MammographyCADSR, UID.KeyObjectSelectionDocument,
            UID.ChestCADSR, UID.StandaloneCurveStorageRetired,
            UID._12leadECGWaveformStorage, UID.GeneralECGWaveformStorage,
            UID.AmbulatoryECGWaveformStorage, UID.HemodynamicWaveformStorage,
            UID.CardiacElectrophysiologyWaveformStorage,
            UID.BasicVoiceAudioWaveformStorage, UID.HangingProtocolStorage,
            UID.SiemensCSANonImageStorage };

    private Executor executor = new NewThreadExecutor("DCMRCV");

    private Device device = new Device("DCMRCV");

    private NetworkApplicationEntity ae = new NetworkApplicationEntity();

    private NetworkConnection nc = new NetworkConnection();

    private String[] tsuids = NON_RETIRED_LE_TS;

    private File destination;

    private boolean devnull;

    private int fileBufferSize = 256;

    private int rspdelay = 0;

    private String keyStoreURL = "resource:tls/test_sys_2.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET; 
    
        public DcmRcv() {
            super(new String[]{UID.BasicStudyContentNotificationSOPClassRetired,
            UID.StoredPrintStorageSOPClassRetired,
            UID.HardcopyGrayscaleImageStorageSOPClassRetired,
            UID.HardcopyColorImageStorageSOPClassRetired,
            UID.ComputedRadiographyImageStorage,
            UID.DigitalXRayImageStorageForPresentation,
            UID.DigitalXRayImageStorageForProcessing,
            UID.DigitalMammographyXRayImageStorageForPresentation,
            UID.DigitalMammographyXRayImageStorageForProcessing,
            UID.DigitalIntraoralXRayImageStorageForPresentation,
            UID.DigitalIntraoralXRayImageStorageForProcessing,
            UID.StandaloneModalityLUTStorageRetired,
            UID.EncapsulatedPDFStorage, UID.StandaloneVOILUTStorageRetired,
            UID.GrayscaleSoftcopyPresentationStateStorageSOPClass,
            UID.ColorSoftcopyPresentationStateStorageSOPClass,
            UID.PseudoColorSoftcopyPresentationStateStorageSOPClass,
            UID.BlendingSoftcopyPresentationStateStorageSOPClass,
            UID.XRayAngiographicImageStorage, UID.EnhancedXAImageStorage,
            UID.XRayRadiofluoroscopicImageStorage, UID.EnhancedXRFImageStorage,
            UID.XRayAngiographicBiPlaneImageStorageRetired,
            UID.PositronEmissionTomographyImageStorage,
            UID.StandalonePETCurveStorageRetired, UID.CTImageStorage,
            UID.EnhancedCTImageStorage, UID.NuclearMedicineImageStorage,
            UID.UltrasoundMultiframeImageStorageRetired,
            UID.UltrasoundMultiframeImageStorage, UID.MRImageStorage,
            UID.EnhancedMRImageStorage, UID.MRSpectroscopyStorage,
            UID.RTImageStorage, UID.RTDoseStorage, UID.RTStructureSetStorage,
            UID.RTBeamsTreatmentRecordStorage, UID.RTPlanStorage,
            UID.RTBrachyTreatmentRecordStorage,
            UID.RTTreatmentSummaryRecordStorage,
            UID.NuclearMedicineImageStorageRetired,
            UID.UltrasoundImageStorageRetired, UID.UltrasoundImageStorage,
            UID.RawDataStorage, UID.SpatialRegistrationStorage,
            UID.SpatialFiducialsStorage, UID.RealWorldValueMappingStorage,
            UID.SecondaryCaptureImageStorage,
            UID.MultiframeSingleBitSecondaryCaptureImageStorage,
            UID.MultiframeGrayscaleByteSecondaryCaptureImageStorage,
            UID.MultiframeGrayscaleWordSecondaryCaptureImageStorage,
            UID.MultiframeTrueColorSecondaryCaptureImageStorage,
            UID.VLImageStorageRetired, UID.VLEndoscopicImageStorage,
            UID.VideoEndoscopicImageStorage, UID.VLMicroscopicImageStorage,
            UID.VideoMicroscopicImageStorage,
            UID.VLSlideCoordinatesMicroscopicImageStorage,
            UID.VLPhotographicImageStorage, UID.VideoPhotographicImageStorage,
            UID.OphthalmicPhotography8BitImageStorage,
            UID.OphthalmicPhotography16BitImageStorage,
            UID.StereometricRelationshipStorage,
            UID.VLMultiframeImageStorageRetired,
            UID.StandaloneOverlayStorageRetired, UID.BasicTextSR,
            UID.EnhancedSR, UID.ComprehensiveSR, UID.ProcedureLogStorage,
            UID.MammographyCADSR, UID.KeyObjectSelectionDocument,
            UID.ChestCADSR, UID.StandaloneCurveStorageRetired,
            UID._12leadECGWaveformStorage, UID.GeneralECGWaveformStorage,
            UID.AmbulatoryECGWaveformStorage, UID.HemodynamicWaveformStorage,
            UID.CardiacElectrophysiologyWaveformStorage,
            UID.BasicVoiceAudioWaveformStorage, UID.HangingProtocolStorage,
            UID.SiemensCSANonImageStorage});
            device.setNetworkApplicationEntity(ae);
            device.setNetworkConnection(nc);
            ae.setNetworkConnection(nc);
            ae.setAssociationAcceptor(true);
            ae.register(new VerificationService());
            ae.register(this);
        }

    public final void setAEtitle(String aet) {
        ae.setAETitle(aet);
    }

    public final void setHostname(String hostname) {
        nc.setHostname(hostname);
    }

    public final void setPort(int port) {
        nc.setPort(port);
    }

    public final void setTlsWithoutEncyrption() {
        nc.setTlsWithoutEncyrption();
    }

    public final void setTls3DES_EDE_CBC() {
        nc.setTls3DES_EDE_CBC();
    }

    public final void setTlsAES_128_CBC() {
        nc.setTlsAES_128_CBC();
    }
    
    public final void disableSSLv2Hello() {
        nc.disableSSLv2Hello();
    }
    
    public final void setTlsNeedClientAuth(boolean needClientAuth) {
        nc.setTlsNeedClientAuth(needClientAuth);
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
        
    public final void setPackPDV(boolean packPDV) {
        ae.setPackPDV(packPDV);
    }

    public final void setAssociationReaperPeriod(int period) {
        device.setAssociationReaperPeriod(period);
    }

    public final void setTcpNoDelay(boolean tcpNoDelay) {
        nc.setTcpNoDelay(tcpNoDelay);
    }

    public final void setRequestTimeout(int timeout) {
        nc.setRequestTimeout(timeout);
    }

    public final void setReleaseTimeout(int timeout) {
        nc.setReleaseTimeout(timeout);
    }

    public final void setSocketCloseDelay(int delay) {
        nc.setSocketCloseDelay(delay);
    }

    public final void setIdleTimeout(int timeout) {
        ae.setIdleTimeout(timeout);
    }

    public final void setDimseRspTimeout(int timeout) {
        ae.setDimseRspTimeout(timeout);
    }

    public final void setMaxPDULengthSend(int maxLength) {
        ae.setMaxPDULengthSend(maxLength);
    }

    public void setMaxPDULengthReceive(int maxLength) {
        ae.setMaxPDULengthReceive(maxLength);
    }

    public final void setReceiveBufferSize(int bufferSize) {
        nc.setReceiveBufferSize(bufferSize);
    }

    public final void setSendBufferSize(int bufferSize) {
        nc.setSendBufferSize(bufferSize);
    }

    private void setDimseRspDelay(int delay) {
        rspdelay = delay;
    }

    private void setTransferSyntax(String[] tsuids) {
        this.tsuids = tsuids;
    }

    private void initTransferCapability() {
        TransferCapability[] tc = new TransferCapability[CUIDS.length + 1];
        tc[0] = new TransferCapability(UID.VerificationSOPClass, ONLY_DEF_TS,
                TransferCapability.SCP);
        for (int i = 0; i < CUIDS.length; i++)
            tc[i + 1] = new TransferCapability(CUIDS[i], tsuids,
                    TransferCapability.SCP);
        ae.setTransferCapability(tc);
    }

    private void setFileBufferSize(int size) {
        fileBufferSize = size;
    }

    private void setMaxOpsPerformed(int maxOps) {
        ae.setMaxOpsPerformed(maxOps);
    }

    private void setDestination(String filePath) {
        this.destination = new File(filePath);
        this.devnull = "/dev/null".equals(filePath);
        if (!devnull)
            destination.mkdir();
    }

    public void initTLS() throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword);
        KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword);
        device.initTLS(keyStore,
                keyPassword != null ? keyPassword : keyStorePassword,
                trustStore);
    }

    private  KeyStore loadKeyStore(String url, char[] password)
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

    private  InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return org.dcm4che2.tool.dcmrcv.DcmRcv.class.getClassLoader().getResourceAsStream(
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
    
    public void start() throws IOException {
        device.startListening(executor);
        System.out.println("Start Server listening on port " + nc.getPort());
    }
    public void stop() throws IOException {
        device.stopListening();
        System.out.println("Stop Server listening on port " + nc.getPort());
    }        
//-----------------------------------------------------------------------        
        
//        
//        @Override
//        protected void onCStoreRQ(Association as, int pcid, DicomObject rq,
//                PDVInputStream dataStream, String tsuid, DicomObject rsp)
//                throws IOException, DicomServiceException {
//                UMOMessage returnMessage = null;
//                try {
//                    String cuid = rq.getString(Tag.AffectedSOPClassUID);
//                    String iuid = rq.getString(Tag.AffectedSOPInstanceUID);                    
//                    byte[] dicomObj = DICOMSerializer.readDicomObj(rq);
//                    BasicDicomObject fmi = new BasicDicomObject();
//                    fmi.initFileMetaInformation(cuid, iuid, tsuid);
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    BufferedOutputStream bos = new BufferedOutputStream(baos);
//                    DicomOutputStream dos = new DicomOutputStream(bos);
//                    dos.writeFileMetaInformation(fmi);
//                    dataStream.copyTo(dos);
//                    dos.close();        
//                    BASE64Encoder encoder = new BASE64Encoder();
//                    String dcmString = encoder.encode(baos.toByteArray());
//                    returnMessage = routeMessage(new MuleMessage(dcmString), endpoint.isSynchronous());
//                    // We need to check the message status
//                    if (returnMessage != null && returnMessage instanceof MuleMessage) {
//                        Object payload = returnMessage.getPayload();
//                        if (payload instanceof MessageObject) {
//                            MessageObject messageObjectResponse = (MessageObject) payload;
//                            postProcessor.doPostProcess(messageObjectResponse);
//                            Map responseMap = messageObjectResponse.getResponseMap();
//                            String errorString = "";
//                        }
//                    }
//                } catch (Exception e) {
//                } finally {
//                    // Let the dispose take care of closing the socket
//                }
//        }
//        @Override
//        public void cstore(final Association as, final int pcid, DicomObject rq, 
//                PDVInputStream dataStream, String tsuid) 
//                throws DicomServiceException, IOException {
//            final DicomObject rsp = CommandUtils.mkRSP(rq, CommandUtils.SUCCESS);
//            onCStoreRQ(as, pcid, rq, dataStream, tsuid, rsp);
//            as.writeDimseRSP(pcid, rsp);
//            onCStoreRSP(as, pcid, rq, dataStream, tsuid, rsp);
//        }  
}
