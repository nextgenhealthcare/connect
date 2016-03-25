/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che2.tool.dcmrcv;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.concurrent.Executor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che2.data.BasicDicomObject;
import org.dcm4che2.data.DicomElement;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.UID;
import org.dcm4che2.data.VR;
import org.dcm4che2.filecache.FileCache;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DicomServiceException;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.PDVInputStream;
import org.dcm4che2.net.Status;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.service.VerificationService;
import org.dcm4che2.util.CloseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 15806 $ $Date: 2011-08-12 18:19:11 +0200 (Fri, 12 Aug 2011) $
 * @since Oct 13, 2005
 */
public class DcmRcv {

    private static final int NO_SUCH_OBJECT_INSTANCE = 0x0112;

    static Logger LOG = LoggerFactory.getLogger(DcmRcv.class);

    private static final int KB = 1024;

    private static final String USAGE = "dcmrcv [Options] [<aet>[@<ip>]:]<port>";

    private static final String DESCRIPTION = "DICOM Server listening on specified <port> for incoming association "
            + "requests. If no local IP address of the network interface is specified "
            + "connections on any/all local addresses are accepted. If <aet> is "
            + "specified, only requests with matching called AE title will be "
            + "accepted. If <aet> and a storage directory is specified by option "
            + "-dest <dir>, also Storage Commitment requests will be accepted and "
            + "processed.\n Options:";

    private static final String EXAMPLE = "\nExample: dcmrcv DCMRCV:11112 -dest /tmp \n"
            + "=> Starts server listening on port 11112, accepting association "
            + "requests with DCMRCV as called AE title. Received objects "
            + "are stored to /tmp.";

    private static String[] TLS1 = { "TLSv1" };

    private static String[] SSL3 = { "SSLv3" };

    private static String[] NO_TLS1 = { "SSLv3", "SSLv2Hello" };

    private static String[] NO_SSL2 = { "TLSv1", "SSLv3" };

    private static String[] NO_SSL3 = { "TLSv1", "SSLv2Hello" };

    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };

    private static final String[] ONLY_DEF_TS = { UID.ImplicitVRLittleEndian };

    private static final String[] NATIVE_TS = { UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian };

    private static final String[] NATIVE_LE_TS = { UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian };

    private static final String[] NON_RETIRED_TS = { UID.JPEGLSLossless,
            UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14,
            UID.JPEG2000LosslessOnly, UID.DeflatedExplicitVRLittleEndian,
            UID.RLELossless, UID.ExplicitVRLittleEndian,
            UID.ExplicitVRBigEndian, UID.ImplicitVRLittleEndian,
            UID.JPEGBaseline1, UID.JPEGExtended24, UID.JPEGLSLossyNearLossless,
            UID.JPEG2000, UID.MPEG2, };

    private static final String[] NON_RETIRED_LE_TS = { UID.JPEGLSLossless,
            UID.JPEGLossless, UID.JPEGLosslessNonHierarchical14,
            UID.JPEG2000LosslessOnly, UID.DeflatedExplicitVRLittleEndian,
            UID.RLELossless, UID.ExplicitVRLittleEndian,
            UID.ImplicitVRLittleEndian, UID.JPEGBaseline1, UID.JPEGExtended24,
            UID.JPEGLSLossyNearLossless, UID.JPEG2000, UID.MPEG2, };

    private static final String[] CUIDS = {
            UID.BasicStudyContentNotificationSOPClassRetired,
            UID.StoredPrintStorageSOPClassRetired,
            UID.HardcopyGrayscaleImageStorageSOPClassRetired,
            UID.HardcopyColorImageStorageSOPClassRetired,
            UID.ComputedRadiographyImageStorage,
            UID.DigitalXRayImageStorageForPresentation,
            UID.DigitalXRayImageStorageForProcessing,
            UID.DigitalMammographyXRayImageStorageForPresentation,
            UID.DigitalMammographyXRayImageStorageForProcessing,
            UID.DigitalIntraOralXRayImageStorageForPresentation,
            UID.DigitalIntraOralXRayImageStorageForProcessing,
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
            UID.UltrasoundMultiFrameImageStorageRetired,
            UID.UltrasoundMultiFrameImageStorage, UID.MRImageStorage,
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
            UID.MultiFrameSingleBitSecondaryCaptureImageStorage,
            UID.MultiFrameGrayscaleByteSecondaryCaptureImageStorage,
            UID.MultiFrameGrayscaleWordSecondaryCaptureImageStorage,
            UID.MultiFrameTrueColorSecondaryCaptureImageStorage,
            UID.VLImageStorageTrialRetired, UID.VLEndoscopicImageStorage,
            UID.VideoEndoscopicImageStorage, UID.VLMicroscopicImageStorage,
            UID.VideoMicroscopicImageStorage,
            UID.VLSlideCoordinatesMicroscopicImageStorage,
            UID.VLPhotographicImageStorage, UID.VideoPhotographicImageStorage,
            UID.OphthalmicPhotography8BitImageStorage,
            UID.OphthalmicPhotography16BitImageStorage,
            UID.StereometricRelationshipStorage,
            UID.VLMultiFrameImageStorageTrialRetired,
            UID.StandaloneOverlayStorageRetired, UID.BasicTextSRStorage,
            UID.EnhancedSRStorage, UID.ComprehensiveSRStorage,
            UID.ProcedureLogStorage, UID.MammographyCADSRStorage,
            UID.KeyObjectSelectionDocumentStorage,
            UID.ChestCADSRStorage, UID.XRayRadiationDoseSRStorage,
            UID.EncapsulatedPDFStorage, UID.EncapsulatedCDAStorage,
            UID.StandaloneCurveStorageRetired,
            UID.TwelveLeadECGWaveformStorage, UID.GeneralECGWaveformStorage,
            UID.AmbulatoryECGWaveformStorage, UID.HemodynamicWaveformStorage,
            UID.CardiacElectrophysiologyWaveformStorage,
            UID.BasicVoiceAudioWaveformStorage, UID.HangingProtocolStorage,
            UID.SiemensCSANonImageStorage,
            UID.Dcm4cheAttributesModificationNotificationSOPClass };

    private final String name;

    private Executor executor;

    protected Device device;

    private final NetworkApplicationEntity ae = new NetworkApplicationEntity();

    protected NetworkConnection nc;

    private final StorageSCP storageSCP = new StorageSCP(this, CUIDS);

    private final StgCmtSCP stgcmtSCP =
            new StgCmtSCP(this);

    private String[] tsuids = NON_RETIRED_LE_TS;

    private FileCache cache = new FileCache();

    private File devnull;

    private Properties calling2dir;

    private Properties called2dir;

    private String callingdefdir = "OTHER";

    private String calleddefdir = "OTHER";

    private int fileBufferSize = 1024;

    private int rspdelay = 0;

    private String keyStoreURL = "resource:tls/test_sys_2.p12";

    private char[] keyStorePassword = SECRET;

    private char[] keyPassword;

    private String trustStoreURL = "resource:tls/mesa_certs.jks";

    private char[] trustStorePassword = SECRET;

    private Timer stgcmtTimer;

    private boolean stgcmtReuseFrom = false;

    private boolean stgcmtReuseTo = false;

    private int stgcmtPort = 104;

    private long stgcmtDelay = 1000;

    private int stgcmtRetry = 0;

    private long stgcmtRetryPeriod = 60000;

    private String stgcmtRetrieveAET;

    private String stgcmtRetrieveAETs;

    private final DimseRSPHandler nEventReportRspHandler = 
        new DimseRSPHandler();

    public DcmRcv() {
        this("DCMRCV");
    }

    public DcmRcv(String name) {
        this(name, true);
    }

    public DcmRcv(String name, boolean init) {
        this.name = name;
        if (init) {
            init();
        }
    }

    protected void init() {
        nc = createNetworkConnection();
        device = new Device(name);
        executor = new NewThreadExecutor(name);
        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(nc);
        ae.setNetworkConnection(nc);
        ae.setAssociationAcceptor(true);
        ae.register(new VerificationService());
        ae.register(storageSCP);
        ae.register(stgcmtSCP);
    }

    protected NetworkConnection createNetworkConnection() {
        return new NetworkConnection();
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

    public final void setTlsProtocol(String[] tlsProtocol) {
        nc.setTlsProtocol(tlsProtocol);
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

    public final void setConnectTimeout(int connectTimeout) {
        nc.setConnectTimeout(connectTimeout);
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

    public final void setAcceptTimeout(int timeout) {
        nc.setAcceptTimeout(timeout);
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

    public final void setDimseRspDelay(int delay) {
        rspdelay = delay;
    }

    public final int getDimseRspDelay() {
        return rspdelay;
    }

    public final void setStgCmtReuseFrom(boolean stgcmtReuseFrom) {
        this.stgcmtReuseFrom = stgcmtReuseFrom;
    }

    public final boolean isStgCmtReuseFrom() {
        return stgcmtReuseFrom;
    }

    public final void setStgCmtReuseTo(boolean stgcmtReuseTo) {
        this.stgcmtReuseTo = stgcmtReuseTo;
    }

    public final boolean isStgCmtReuseTo() {
        return stgcmtReuseTo;
    }

    public final int getStgCmtPort() {
        return stgcmtPort;
    }

    public final void setStgCmtPort(int stgcmtPort) {
        this.stgcmtPort = stgcmtPort;
    }

    public final void setStgCmtDelay(long delay) {
        this.stgcmtDelay = delay;
    }

    public final long getStgCmtDelay() {
        return stgcmtDelay;
    }

    public final int getStgCmtRetry() {
        return stgcmtRetry;
    }

    public final void setStgCmtRetry(int stgcmtRetry) {
        this.stgcmtRetry = stgcmtRetry;
    }

    public final long getStgCmtRetryPeriod() {
        return stgcmtRetryPeriod;
    }

    public final void setStgCmtRetryPeriod(long stgcmtRetryPeriod) {
        this.stgcmtRetryPeriod = stgcmtRetryPeriod;
    }

    public final String getStgCmtRetrieveAET() {
        return stgcmtRetrieveAET;
    }

    public final void setStgCmtRetrieveAET(String aet) {
        this.stgcmtRetrieveAET = aet;
    }


    public final String getStgCmtRetrieveAETs() {
        return stgcmtRetrieveAETs;
    }

    public final void setStgCmtRetrieveAETs(String aet) {
        this.stgcmtRetrieveAETs = aet;
    }

    final Executor executor() {
        return executor;
    }

    private static CommandLine parse(String[] args) {
        Options opts = new Options();

        OptionBuilder.withArgName("name");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set device name, use DCMRCV by default");
        opts.addOption(OptionBuilder.create("device"));

        OptionBuilder.withArgName("NULL|3DES|AES");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "enable TLS connection without, 3DES or AES encryption");
        opts.addOption(OptionBuilder.create("tls"));

        OptionGroup tlsProtocol = new OptionGroup();
        tlsProtocol.addOption(new Option("tls1",
                "disable the use of SSLv3 and SSLv2 for TLS connections"));
        tlsProtocol.addOption(new Option("ssl3",
                "disable the use of TLSv1 and SSLv2 for TLS connections"));
        tlsProtocol.addOption(new Option("no_tls1",
                "disable the use of TLSv1 for TLS connections"));
        tlsProtocol.addOption(new Option("no_ssl3",
                "disable the use of SSLv3 for TLS connections"));
        tlsProtocol.addOption(new Option("no_ssl2",
                "disable the use of SSLv2 for TLS connections"));
        opts.addOptionGroup(tlsProtocol);

        opts.addOption("noclientauth", false,
                "disable client authentification for TLS");        

        OptionBuilder.withArgName("file|url");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "file path or URL of P12 or JKS keystore, resource:tls/test_sys_2.p12 by default");
        opts.addOption(OptionBuilder.create("keystore"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for keystore file, 'secret' by default");
        opts.addOption(OptionBuilder.create("keystorepw"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for accessing the key in the keystore, keystore password by default");
        opts.addOption(OptionBuilder.create("keypw"));

        OptionBuilder.withArgName("file|url");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "file path or URL of JKS truststore, resource:tls/mesa_certs.jks by default");
        opts.addOption(OptionBuilder.create("truststore"));

        OptionBuilder.withArgName("password");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "password for truststore file, 'secret' by default");
        opts.addOption(OptionBuilder.create("truststorepw"));

        OptionBuilder.withArgName("dir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "store received objects into files in specified directory <dir>."
                        + " Do not store received objects by default.");
        opts.addOption(OptionBuilder.create("dest"));

        OptionBuilder.withArgName("file|url");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "file path or URL of properties for mapping Calling AETs to "
                + "sub-directories of the storage directory specified by "
                + "-dest, to separate the storage location dependend on "
                + "Calling AETs.");
        opts.addOption(OptionBuilder.create("calling2dir"));


        OptionBuilder.withArgName("file|url");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "file path or URL of properties for mapping Called AETs to "
                + "sub-directories of the storage directory specified by "
                + "-dest, to separate the storage location dependend on "
                + "Called AETs.");
        opts.addOption(OptionBuilder.create("called2dir"));

        OptionBuilder.withArgName("sub-dir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "storage sub-directory used for Calling AETs for which no "
                + " mapping is defined by properties specified by "
                + "-calling2dir, 'OTHER' by default.");
        opts.addOption(OptionBuilder.create("callingdefdir"));

        OptionBuilder.withArgName("sub-dir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "storage sub-directory used for Called AETs for which no "
                + " mapping is defined by properties specified by "
                + "-called2dir, 'OTHER' by default.");
        opts.addOption(OptionBuilder.create("calleddefdir"));

        OptionBuilder.withArgName("dir");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "register stored objects in cache journal files in specified directory <dir>."
                        + " Do not register stored objects by default.");
        opts.addOption(OptionBuilder.create("journal"));

        OptionBuilder.withArgName("pattern");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription("cache journal file path, with "
                + "'yyyy' will be replaced by the current year, "
                + "'MM' by the current month, 'dd' by the current date, "
                + "'HH' by the current hour and 'mm' by the current minute. "
                + "'yyyy/MM/dd/HH/mm' by default.");
        opts.addOption(OptionBuilder.create("journalfilepath"));

        opts.addOption("defts", false, "accept only default transfer syntax.");
        opts.addOption("bigendian", false,
                "accept also Explict VR Big Endian transfer syntax.");
        opts.addOption("native", false,
                "accept only transfer syntax with uncompressed pixel data.");

        OptionGroup scRetrieveAET = new OptionGroup();
        OptionBuilder.withArgName("aet");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Retrieve AE Title included in Storage Commitment " +
                "N-EVENT-REPORT in items of the Referenced SOP Sequence.");
        scRetrieveAET.addOption(OptionBuilder.create("scretraets"));
        OptionBuilder.withArgName("aet");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Retrieve AE Title included in Storage Commitment " +
                "N-EVENT-REPORT outside of the Referenced SOP Sequence.");
        scRetrieveAET.addOption(OptionBuilder.create("scretraet"));
        opts.addOptionGroup(scRetrieveAET);

        opts.addOption("screusefrom", false,
                "attempt to issue the Storage Commitment N-EVENT-REPORT on " +
                "the same Association on which the N-ACTION operation was " +
                "performed; use different Association for N-EVENT-REPORT by " +
                "default.");

        opts.addOption("screuseto", false,
                "attempt to issue the Storage Commitment N-EVENT-REPORT on " +
                "previous initiated Association to the Storage Commitment SCU; " +
                "initiate new Association for N-EVENT-REPORT by default.");

        OptionBuilder.withArgName("port");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "port of Storage Commitment SCU to connect to issue " +
                "N-EVENT-REPORT on different Association; 104 by default.");
        opts.addOption(OptionBuilder.create("scport"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms for N-EVENT-REPORT-RQ to Storage Commitment SCU, "
                        + "1s by default");
        opts.addOption(OptionBuilder.create("scdelay"));

        OptionBuilder.withArgName("retry");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "number of retries to issue N-EVENT-REPORT-RQ to Storage " +
                "Commitment SCU, 0 by default");
        opts.addOption(OptionBuilder.create("scretry"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "interval im ms between retries to issue N-EVENT-REPORT-RQ to" +
                "Storage Commitment SCU, 60s by default");
        opts.addOption(OptionBuilder.create("scretryperiod"));

        OptionBuilder.withArgName("maxops");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximum number of outstanding operations performed "
                        + "asynchronously, unlimited by default.");
        opts.addOption(OptionBuilder.create("async"));

        opts.addOption("pdv1", false, "send only one PDV in one P-Data-TF PDU, "
                                + "pack command and data PDV in one P-DATA-TF PDU by default.");
        opts.addOption("tcpdelay", false,
                "set TCP_NODELAY socket option to false, true by default");

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for TCP connect, no timeout by default");
        opts.addOption(OptionBuilder.create("connectTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create("rspTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving A-ASSOCIATE-AC, 5s by default");
        opts.addOption(OptionBuilder.create("acceptTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms for Socket close after sending A-ABORT, 50ms by default");
        opts.addOption(OptionBuilder.create("soclosedelay"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms for DIMSE-RSP; useful for testing asynchronous mode");
        opts.addOption(OptionBuilder.create("rspdelay"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving -ASSOCIATE-RQ, 5s by default");
        opts.addOption(OptionBuilder.create("requestTO"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        opts.addOption(OptionBuilder.create("releaseTO"));


        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "period in ms to check for outstanding DIMSE-RSP, 10s by default");
        opts.addOption(OptionBuilder.create("reaper"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "timeout in ms for receiving DIMSE-RQ, 60s by default");
        opts.addOption(OptionBuilder.create("idleTO"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximal length in KB of received P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("rcvpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximal length in KB of sent P-DATA-TF PDUs, 16KB by default");
        opts.addOption(OptionBuilder.create("sndpdulen"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_RCVBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sorcvbuf"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set SO_SNDBUF socket option to specified value in KB");
        opts.addOption(OptionBuilder.create("sosndbuf"));

        OptionBuilder.withArgName("KB");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "minimal buffer size to write received object to file, 1KB by default");
        opts.addOption(OptionBuilder.create("bufsize"));

        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmrcv: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption("V")) {
            Package p = DcmRcv.class.getPackage();
            System.out.println("dcmrcv v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption("h") || cl.getArgList().size() == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = parse(args);
        DcmRcv dcmrcv = new DcmRcv(cl.hasOption("device") 
                ? cl.getOptionValue("device") : "DCMRCV");
        final List<String> argList = cl.getArgList();
        String port = argList.get(0);
        String[] aetPort = split(port, ':', 1);
        dcmrcv.setPort(parseInt(aetPort[1], "illegal port number", 1, 0xffff));
        if (aetPort[0] != null) {
            String[] aetHost = split(aetPort[0], '@', 0);
            dcmrcv.setAEtitle(aetHost[0]);
            if (aetHost[1] != null) {
                dcmrcv.setHostname(aetHost[1]);
            }
        }

        if (cl.hasOption("dest"))
            dcmrcv.setDestination(cl.getOptionValue("dest"));
        if (cl.hasOption("calling2dir"))
            dcmrcv.setCalling2Dir(
                    loadProperties(cl.getOptionValue("calling2dir")));
        if (cl.hasOption("called2dir"))
            dcmrcv.setCalled2Dir(
                    loadProperties(cl.getOptionValue("called2dir")));
        if (cl.hasOption("callingdefdir"))
            dcmrcv.setCallingDefDir(cl.getOptionValue("callingdefdir"));
        if (cl.hasOption("calleddefdir"))
            dcmrcv.setCalledDefDir(cl.getOptionValue("calleddefdir"));
        if (cl.hasOption("journal"))
            dcmrcv.setJournal(cl.getOptionValue("journal"));
        if (cl.hasOption("journalfilepath"))
            dcmrcv.setJournalFilePathFormat(
                    cl.getOptionValue("journalfilepath"));
        
        if (cl.hasOption("defts"))
            dcmrcv.setTransferSyntax(ONLY_DEF_TS);
        else if (cl.hasOption("native"))
            dcmrcv.setTransferSyntax(cl.hasOption("bigendian") ? NATIVE_TS
                    : NATIVE_LE_TS);
        else if (cl.hasOption("bigendian"))
            dcmrcv.setTransferSyntax(NON_RETIRED_TS);
        if (cl.hasOption("scretraets"))
            dcmrcv.setStgCmtRetrieveAETs(cl.getOptionValue("scretraets"));
        if (cl.hasOption("scretraet"))
            dcmrcv.setStgCmtRetrieveAET(cl.getOptionValue("scretraet"));
        dcmrcv.setStgCmtReuseFrom(cl.hasOption("screusefrom"));
        dcmrcv.setStgCmtReuseTo(cl.hasOption("screuseto"));
        if (cl.hasOption("scport")) {
            dcmrcv.setStgCmtPort(parseInt(cl.getOptionValue("scport"),
                    "illegal port number", 1, 0xffff));
        }
        if (cl.hasOption("scdelay"))
            dcmrcv.setStgCmtDelay(parseInt(cl.getOptionValue("scdelay"),
                    "illegal argument of option -scdelay", 0,
                    Integer.MAX_VALUE));
        if (cl.hasOption("scretry"))
            dcmrcv.setStgCmtRetry(parseInt(cl.getOptionValue("scretry"),
                    "illegal argument of option -scretry", 0,
                    Integer.MAX_VALUE));
        if (cl.hasOption("scretryperiod"))
            dcmrcv.setStgCmtRetryPeriod(parseInt(cl.getOptionValue("scretryperiod"),
                    "illegal argument of option -scretryperiod", 1000,
                    Integer.MAX_VALUE));
        if (cl.hasOption("connectTO"))
            dcmrcv.setConnectTimeout(parseInt(cl.getOptionValue("connectTO"),
                    "illegal argument of option -connectTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("reaper"))
            dcmrcv.setAssociationReaperPeriod(parseInt(cl
                            .getOptionValue("reaper"),
                            "illegal argument of option -reaper", 1,
                            Integer.MAX_VALUE));
        if (cl.hasOption("rspTO"))
            dcmrcv.setDimseRspTimeout(parseInt(cl.getOptionValue("rspTO"),
                    "illegal argument of option -rspTO",
                    1, Integer.MAX_VALUE));
        if (cl.hasOption("acceptTO"))
            dcmrcv.setAcceptTimeout(parseInt(cl.getOptionValue("acceptTO"),
                    "illegal argument of option -acceptTO", 
                    1, Integer.MAX_VALUE));
        if (cl.hasOption("idleTO"))
            dcmrcv.setIdleTimeout(parseInt(cl.getOptionValue("idleTO"),
                            "illegal argument of option -idleTO", 1,
                            Integer.MAX_VALUE));
        if (cl.hasOption("requestTO"))
            dcmrcv.setRequestTimeout(parseInt(cl.getOptionValue("requestTO"),
                    "illegal argument of option -requestTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("releaseTO"))
            dcmrcv.setReleaseTimeout(parseInt(cl.getOptionValue("releaseTO"),
                    "illegal argument of option -releaseTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("soclosedelay"))
            dcmrcv.setSocketCloseDelay(parseInt(cl
                    .getOptionValue("soclosedelay"),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (cl.hasOption("rspdelay"))
            dcmrcv.setDimseRspDelay(parseInt(cl.getOptionValue("rspdelay"),
                    "illegal argument of option -rspdelay", 0, 10000));
        if (cl.hasOption("rcvpdulen"))
            dcmrcv.setMaxPDULengthReceive(parseInt(cl
                    .getOptionValue("rcvpdulen"),
                    "illegal argument of option -rcvpdulen", 1, 10000)
                    * KB);
        if (cl.hasOption("sndpdulen"))
            dcmrcv.setMaxPDULengthSend(parseInt(cl.getOptionValue("sndpdulen"),
                    "illegal argument of option -sndpdulen", 1, 10000)
                    * KB);
        if (cl.hasOption("sosndbuf"))
            dcmrcv.setSendBufferSize(parseInt(cl.getOptionValue("sosndbuf"),
                    "illegal argument of option -sosndbuf", 1, 10000)
                    * KB);
        if (cl.hasOption("sorcvbuf"))
            dcmrcv.setReceiveBufferSize(parseInt(cl.getOptionValue("sorcvbuf"),
                    "illegal argument of option -sorcvbuf", 1, 10000)
                    * KB);
        if (cl.hasOption("bufsize"))
            dcmrcv.setFileBufferSize(parseInt(cl.getOptionValue("bufsize"),
                    "illegal argument of option -bufsize", 1, 10000)
                    * KB);

        dcmrcv.setPackPDV(!cl.hasOption("pdv1"));
        dcmrcv.setTcpNoDelay(!cl.hasOption("tcpdelay"));
        if (cl.hasOption("async"))
            dcmrcv.setMaxOpsPerformed(parseInt(cl.getOptionValue("async"),
                    "illegal argument of option -async", 0, 0xffff));
        dcmrcv.initTransferCapability();
        if (cl.hasOption("tls")) {
            String cipher = cl.getOptionValue("tls");
            if ("NULL".equalsIgnoreCase(cipher)) {
                dcmrcv.setTlsWithoutEncyrption();
            } else if ("3DES".equalsIgnoreCase(cipher)) {
                dcmrcv.setTls3DES_EDE_CBC();
            } else if ("AES".equalsIgnoreCase(cipher)) {
                dcmrcv.setTlsAES_128_CBC();
            } else {
                exit("Invalid parameter for option -tls: " + cipher);
            }
            if (cl.hasOption("tls1")) {
                dcmrcv.setTlsProtocol(TLS1);
            } else if (cl.hasOption("ssl3")) {
                dcmrcv.setTlsProtocol(SSL3);
            } else if (cl.hasOption("no_tls1")) {
                dcmrcv.setTlsProtocol(NO_TLS1);
            } else if (cl.hasOption("no_ssl3")) {
                dcmrcv.setTlsProtocol(NO_SSL3);
            } else if (cl.hasOption("no_ssl2")) {
                dcmrcv.setTlsProtocol(NO_SSL2);
            }
            dcmrcv.setTlsNeedClientAuth(!cl.hasOption("noclientauth"));

            if (cl.hasOption("keystore")) {
                dcmrcv.setKeyStoreURL(cl.getOptionValue("keystore"));
            }
            if (cl.hasOption("keystorepw")) {
                dcmrcv.setKeyStorePassword(
                        cl.getOptionValue("keystorepw"));
            }
            if (cl.hasOption("keypw")) {
                dcmrcv.setKeyPassword(cl.getOptionValue("keypw"));
            }
            if (cl.hasOption("truststore")) {
                dcmrcv.setTrustStoreURL(
                        cl.getOptionValue("truststore"));
            }
            if (cl.hasOption("truststorepw")) {
                dcmrcv.setTrustStorePassword(
                        cl.getOptionValue("truststorepw"));
            }
            try {
                dcmrcv.initTLS();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to initialize TLS context:"
                        + e.getMessage());
                System.exit(2);
            }
        }
        try {
            dcmrcv.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTransferSyntax(String[] tsuids) {
        this.tsuids = tsuids;
    }

    public void initTransferCapability() {
        TransferCapability[] tc;
        if (isStgcmtEnabled()) {
            tc = new TransferCapability[CUIDS.length + 2];
            tc[tc.length -1 ] = new TransferCapability(
                    UID.StorageCommitmentPushModelSOPClass, ONLY_DEF_TS,
                    TransferCapability.SCP);
        } else {
            tc = new TransferCapability[CUIDS.length + 1];
        }
        tc[0] = new TransferCapability(UID.VerificationSOPClass, ONLY_DEF_TS,
                TransferCapability.SCP);
        for (int i = 0; i < CUIDS.length; i++)
            tc[i + 1] = new TransferCapability(CUIDS[i], tsuids,
                    TransferCapability.SCP);
        ae.setTransferCapability(tc);
    }

    public void setFileBufferSize(int size) {
        fileBufferSize = size;
    }

    public void setMaxOpsPerformed(int maxOps) {
        ae.setMaxOpsPerformed(maxOps);
    }

    public void setDestination(String filePath) {
        File f = new File(filePath);
        if ("/dev/null".equals(filePath)) {
            devnull = f;
            cache.setCacheRootDir(null);
        } else {
            devnull = null;
            cache.setCacheRootDir(f);
        }
    }

    public void setCalling2Dir(Properties calling2dir) {
        this.calling2dir = calling2dir;
    }

    public void setCalled2Dir(Properties called2dir) {
        this.called2dir = called2dir;
    }

    private static Properties loadProperties(String url) {
        Properties props = new Properties();
        try {
            InputStream inStream = openFileOrURL(url);
            try {
                props.load(inStream);
            } finally {
                inStream.close();
            }
        } catch (Exception e) {
            exit("Failed to load properties from " + url);
        }
        return props;
    }

    public void setCallingDefDir(String callingdefdir) {
        this.callingdefdir = callingdefdir;
    }

    public void setCalledDefDir(String calleddefdir) {
        this.calleddefdir = calleddefdir;
    }

    public void setJournal(String journalRootDir) {
        cache.setJournalRootDir(new File(journalRootDir));
        // Prefix JournalFileName to distinguish from journal files created
        // by other applications than DcmRcv
        cache.setJournalFileName("DcmRcv." + cache.getJournalFileName());
    }

    public void setJournalFilePathFormat(String format) {
        cache.setJournalFilePathFormat(format);
    }

    public void initTLS() throws GeneralSecurityException, IOException {
        KeyStore keyStore = loadKeyStore(keyStoreURL, keyStorePassword);
        KeyStore trustStore = loadKeyStore(trustStoreURL, trustStorePassword);
        device.initTLS(keyStore,
                keyPassword != null ? keyPassword : keyStorePassword,
                trustStore);
    }

    private static KeyStore loadKeyStore(String url, char[] password)
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

    private static InputStream openFileOrURL(String url) throws IOException {
        if (url.startsWith("resource:")) {
            return DcmRcv.class.getClassLoader().getResourceAsStream(
                    url.substring(9));
        }
        try {
            return new URL(url).openStream();
        } catch (MalformedURLException e) {
            return new FileInputStream(url);
        }
    }

    private static String toKeyStoreType(String fname) {
        return fname.endsWith(".p12") || fname.endsWith(".P12")
                 ? "PKCS12" : "JKS";
    }

    public void start() throws IOException {
        device.startListening(executor);
        System.out.println("Start Server listening on port " + nc.getPort());
    }

    public void stop() {
        if (device != null)
            device.stopListening();

        if (nc != null)
            System.out.println("Stop Server listening on port " + nc.getPort());
        else
            System.out.println("Stop Server");
    }

    private static String[] split(String s, char delim, int defPos) {
        String[] s2 = new String[2];
        s2[defPos] = s;
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmrcv -h' for more information.");
        System.exit(1);
    }

    private static int parseInt(String s, String errPrompt, int min, int max) {
        try {
            int i = Integer.parseInt(s);
            if (i >= min && i <= max)
                return i;
        } catch (NumberFormatException e) {
            // parameter is not a valid integer; fall through to exit
        }
        exit(errPrompt);
        throw new RuntimeException();
    }

    boolean isStoreFile() {
        return devnull != null || cache.getCacheRootDir() != null;
    }

    private boolean isStgcmtEnabled() {
        return ae.getAETitle() != null && cache.getCacheRootDir() != null;
    }

    void onCStoreRQ(Association as, int pcid, DicomObject rq,
            PDVInputStream dataStream, String tsuid, DicomObject rsp)
            throws IOException {
        String cuid = rq.getString(Tag.AffectedSOPClassUID);
        String iuid = rq.getString(Tag.AffectedSOPInstanceUID);
        File file = devnull != null ? devnull
                : new File(mkDir(as), iuid + ".part");
        LOG.info("M-WRITE {}", file);
        try {
            DicomOutputStream dos = new DicomOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(file),
                            fileBufferSize));
            try {
                BasicDicomObject fmi = new BasicDicomObject();
                fmi.initFileMetaInformation(cuid, iuid, tsuid);
                dos.writeFileMetaInformation(fmi);
                dataStream.copyTo(dos);
            } finally {
                CloseUtils.safeClose(dos);
            }
        } catch (IOException e) {
            if (devnull == null && file != null) {
                if (file.delete()) {
                    LOG.info("M-DELETE {}", file);
                }
            }
            throw new DicomServiceException(rq, Status.ProcessingFailure,
                    e.getMessage());
        }
        
        // Rename the file after it has been written. See DCM-279
        if (devnull == null && file != null) {
            File rename = new File(file.getParent(), iuid);
            LOG.info("M-RENAME {} to {}", file, rename);
            file.renameTo(rename);
            if (cache.getJournalRootDir() != null) {
                cache.record(rename);
            }
        }
    }

    private File getDir(Association as) {
        File dir = cache.getCacheRootDir();
        if (called2dir != null) {
            dir = new File(dir,
                    called2dir.getProperty(as.getCalledAET(), calleddefdir));
        }
        if (calling2dir != null) {
            dir = new File(dir,
                    calling2dir.getProperty(as.getCallingAET(), callingdefdir));
        }
        return dir;
    }

    private File mkDir(Association as) {
        File dir = getDir(as);
        if (dir.mkdirs()) {
            LOG.info("M-WRITE {}", dir);
        }
        return dir;
    }

    public void onNActionRQ(Association as, DicomObject rq, DicomObject info) {
        stgcmtTimer().schedule(new SendStgCmtResult(this, mkStgCmtAE(as),
                mkStgCmtResult(as, info)), stgcmtDelay, stgcmtRetryPeriod);
    }

    private NetworkApplicationEntity mkStgCmtAE(Association as) {
        NetworkApplicationEntity stgcmtAE = new NetworkApplicationEntity();
        NetworkConnection stgcmtNC = new NetworkConnection();
        stgcmtNC.setHostname(as.getSocket().getInetAddress().getHostAddress());
        stgcmtNC.setPort(stgcmtPort);
        stgcmtNC.setTlsCipherSuite(nc.getTlsCipherSuite());
        stgcmtAE.setNetworkConnection(stgcmtNC);
        stgcmtAE.setAETitle(as.getRemoteAET());
        stgcmtAE.setTransferCapability(new TransferCapability[]{
                new TransferCapability(
                        UID.StorageCommitmentPushModelSOPClass, ONLY_DEF_TS,
                        TransferCapability.SCU)});
        return stgcmtAE;
    }

    private DicomObject mkStgCmtResult(Association as, DicomObject rqdata) {
        DicomObject result = new BasicDicomObject();
        result.putString(Tag.TransactionUID, VR.UI,
                rqdata.getString(Tag.TransactionUID));
        DicomElement rqsq = rqdata.get(Tag.ReferencedSOPSequence);
        DicomElement resultsq = result.putSequence(Tag.ReferencedSOPSequence);
        if (stgcmtRetrieveAET != null)
            result.putString(Tag.RetrieveAETitle, VR.AE, stgcmtRetrieveAET);
        DicomElement failedsq = null;
        File dir = getDir(as);
        for (int i = 0, n = rqsq.countItems(); i < n; i++) {
            DicomObject rqItem = rqsq.getDicomObject(i);
            String uid = rqItem.getString(Tag.ReferencedSOPInstanceUID);
            DicomObject resultItem = new BasicDicomObject();
            rqItem.copyTo(resultItem);
            if (stgcmtRetrieveAETs != null)
                resultItem.putString(Tag.RetrieveAETitle, VR.AE,
                        stgcmtRetrieveAETs);
            File f = new File(dir, uid);
            if (f.isFile()) {
                resultsq.addDicomObject(resultItem);
            } else {
                resultItem.putInt(Tag.FailureReason, VR.US,
                        NO_SUCH_OBJECT_INSTANCE);
                if (failedsq == null)
                    failedsq = result.putSequence(Tag.FailedSOPSequence);
                failedsq.addDicomObject(resultItem);
            }
        }
        return result;
    }

    private synchronized Timer stgcmtTimer() {
        if (stgcmtTimer == null)
            stgcmtTimer = new Timer("SendStgCmtResult", true);
        return stgcmtTimer;
    }

    void sendStgCmtResult(NetworkApplicationEntity stgcmtAE,
            DicomObject result) throws Exception {
        synchronized(ae) {
            ae.setReuseAssocationFromAETitle(stgcmtReuseFrom 
                    ? new String[] { stgcmtAE.getAETitle() }
                    : new String[] {});
            ae.setReuseAssocationToAETitle(stgcmtReuseTo 
                    ? new String[] { stgcmtAE.getAETitle() }
                    : new String[] {});
           Association as = ae.connect(stgcmtAE, executor);
           as.nevent(UID.StorageCommitmentPushModelSOPClass,
                    UID.StorageCommitmentPushModelSOPInstance,
                    eventTypeIdOf(result), result, UID.ImplicitVRLittleEndian,
                    nEventReportRspHandler);
           if (!stgcmtReuseFrom && !stgcmtReuseTo)
               as.release(true);
        }
    }

    private static int eventTypeIdOf(DicomObject result) {
        return result.contains(Tag.FailedSOPInstanceUIDList) ? 2 : 1;
    }

}
