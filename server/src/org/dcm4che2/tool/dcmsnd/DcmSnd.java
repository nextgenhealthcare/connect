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

package org.dcm4che2.tool.dcmsnd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.dcm4che2.data.UIDDictionary;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;
import org.dcm4che2.io.DicomOutputStream;
import org.dcm4che2.io.StopTagInputHandler;
import org.dcm4che2.io.TranscoderInputHandler;
import org.dcm4che2.net.Association;
import org.dcm4che2.net.CommandUtils;
import org.dcm4che2.net.ConfigurationException;
import org.dcm4che2.net.Device;
import org.dcm4che2.net.DimseRSP;
import org.dcm4che2.net.DimseRSPHandler;
import org.dcm4che2.net.NetworkApplicationEntity;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NewThreadExecutor;
import org.dcm4che2.net.NoPresentationContextException;
import org.dcm4che2.net.PDVOutputStream;
import org.dcm4che2.net.TransferCapability;
import org.dcm4che2.net.UserIdentity;
import org.dcm4che2.net.service.StorageCommitmentService;
import org.dcm4che2.util.Anonymizer;
import org.dcm4che2.util.CloseUtils;
import org.dcm4che2.util.StringUtils;
import org.dcm4che2.util.UIDUtils;

/**
 * @author gunter zeilinger(gunterze@gmail.com)
 * @version $Revision: 17118 $ $Date:: 2012-09-13#$
 * @since Oct 13, 2005
 */
public class DcmSnd extends StorageCommitmentService {

    private static final int KB = 1024;

    private static final int MB = KB * KB;

    private static final int PEEK_LEN = 1024;

    private static final String USAGE = 
        "dcmsnd <aet>[@<host>[:<port>]] <file>|<directory>... [Options]";

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
        "\nExample: dcmsnd STORESCP@localhost:11112 image.dcm -stgcmt -L DCMSND:11113 \n"
      + "=> Start listening on local port 11113 for receiving Storage Commitment "
      + "results, send DICOM object image.dcm to Application Entity STORESCP, "
      + "listening on local port 11112, and request Storage Commitment in same association.";

    private static String[] TLS1 = { "TLSv1" };

    private static String[] SSL3 = { "SSLv3" };

    private static String[] NO_TLS1 = { "SSLv3", "SSLv2Hello" };

    private static String[] NO_SSL2 = { "TLSv1", "SSLv3" };

    private static String[] NO_SSL3 = { "TLSv1", "SSLv2Hello" };

    private static char[] SECRET = { 's', 'e', 'c', 'r', 'e', 't' };
    
    private static final String[] ONLY_IVLE_TS = { 
        UID.ImplicitVRLittleEndian
    };

    private static final String[] IVLE_TS = { 
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRLittleEndian, 
        UID.ExplicitVRBigEndian,
    };

    private static final String[] EVLE_TS = {
        UID.ExplicitVRLittleEndian,
        UID.ImplicitVRLittleEndian,
        UID.ExplicitVRBigEndian, 
    };

    private static final String[] EVBE_TS = { 
        UID.ExplicitVRBigEndian,
        UID.ExplicitVRLittleEndian, 
        UID.ImplicitVRLittleEndian, 
    };

    private static final int STG_CMT_ACTION_TYPE = 1;

    /** TransferSyntax: DCM4CHE URI Referenced */
    private static final String DCM4CHEE_URI_REFERENCED_TS_UID =
            "1.2.40.0.13.1.1.2.4.94";

    private final String name;

    private Executor executor;

    private final NetworkApplicationEntity remoteAE = new NetworkApplicationEntity();

    private NetworkApplicationEntity remoteStgcmtAE;

    protected NetworkConnection remoteConn;

    protected NetworkConnection remoteStgcmtConn;

    protected Device device;

    private final NetworkApplicationEntity ae = new NetworkApplicationEntity();

    protected NetworkConnection conn;

    private final Map<String, Set<String>> as2ts = new HashMap<String, Set<String>>();

    private final ArrayList<FileInfo> files = new ArrayList<FileInfo>();

    private Association assoc;

    private int priority = 0;
    
    private int transcoderBufferSize = 1024;

    private int filesSent = 0;

    private long totalSize = 0L;

    private boolean fileref = false;

    private boolean stgcmt = false;
    
    private long shutdownDelay = 1000L;
    
    private DicomObject stgCmtResult;

    private DicomObject coerceAttrs;

    private String[] suffixUID;

    private String keyStoreURL = "resource:tls/test_sys_1.p12";
    
    private char[] keyStorePassword = SECRET; 

    private char[] keyPassword; 
    
    private String trustStoreURL = "resource:tls/mesa_certs.jks";
    
    private char[] trustStorePassword = SECRET;
    
    private int batchSize = 0;
    
    private int lastSentFile = 0;

    private Anonymizer anonymizer;

    public DcmSnd() {
        this("DCMSND");
    }

    public DcmSnd(String name) {
        this(name, true);
    }

    public DcmSnd(String name, boolean init) {
        this.name = name;
        if (init) {
            init();
        }
    }

    protected void init() {
        conn = createNetworkConnection();
        remoteConn = createNetworkConnection();
        remoteStgcmtConn = createNetworkConnection();

        device = new Device(name);
        executor = new NewThreadExecutor(name);
        remoteAE.setInstalled(true);
        remoteAE.setAssociationAcceptor(true);
        remoteAE.setNetworkConnection(new NetworkConnection[] { remoteConn });

        device.setNetworkApplicationEntity(ae);
        device.setNetworkConnection(conn);
        ae.setNetworkConnection(conn);
        ae.setAssociationInitiator(true);
        ae.setAssociationAcceptor(true);
        ae.register(this);
        ae.setAETitle(name);
    }

    protected NetworkConnection createNetworkConnection() {
        return new NetworkConnection();
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

    public final void setRemoteStgcmtHost(String hostname) {
        remoteStgcmtConn.setHostname(hostname);
    }

    public final void setRemoteStgcmtPort(int port) {
        remoteStgcmtConn.setPort(port);
    }

    public final void setTlsProtocol(String[] tlsProtocol) {
        conn.setTlsProtocol(tlsProtocol);
    }

    public final void setTlsWithoutEncyrption() {
        conn.setTlsWithoutEncyrption();
        remoteConn.setTlsWithoutEncyrption();
        remoteStgcmtConn.setTlsWithoutEncyrption();
    }

    public final void setTls3DES_EDE_CBC() {
        conn.setTls3DES_EDE_CBC();
        remoteConn.setTls3DES_EDE_CBC();
        remoteStgcmtConn.setTls3DES_EDE_CBC();
    }

    public final void setTlsAES_128_CBC() {
        conn.setTlsAES_128_CBC();
        remoteConn.setTlsAES_128_CBC();
        remoteStgcmtConn.setTlsAES_128_CBC();
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

    public final void setSendFileRef(boolean fileref) {
        this.fileref = fileref;
    }

    public final void setStorageCommitment(boolean stgcmt) {
        this.stgcmt = stgcmt;
    }

    public final boolean isStorageCommitment() {
        return stgcmt;
    }

    public final void setStgcmtCalledAET(String called) {
        remoteStgcmtAE = new NetworkApplicationEntity();
        remoteStgcmtAE.setInstalled(true);
        remoteStgcmtAE.setAssociationAcceptor(true);
        remoteStgcmtAE.setNetworkConnection(
                new NetworkConnection[] { remoteStgcmtConn });
        remoteStgcmtAE.setAETitle(called);
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

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
    
    public int getBatchSize() {
        return batchSize;
    }
    
    public int getLastSentFile() {
        return lastSentFile;
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
    
    public List<FileInfo> getFileInfos() {
        return files;
    }

    private static CommandLine parse(String[] args) {
        Options opts = new Options();

        OptionBuilder.withArgName("name");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set device name, use DCMSND by default");
        opts.addOption(OptionBuilder.create("device"));

        OptionBuilder.withArgName("aet[@host][:port]");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "set AET, local address and listening port of local "
                + "Application Entity, use device name and pick up any valid "
                + "local address to bind the socket by default");
        opts.addOption(OptionBuilder.create("L"));

        opts.addOption("ts1", false, "offer Default Transfer Syntax in " +
                "separate Presentation Context. By default offered with " +
                "Explicit VR Little Endian TS in one PC.");

        opts.addOption("fileref", false,
                "send objects without pixel data, but with a reference to " +
                "the DICOM file using DCM4CHE URI Referenced Transfer Syntax " +
                "to import DICOM objects on a given file system to a DCM4CHEE " +
                "archive.");

        OptionBuilder.withArgName("username");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "enable User Identity Negotiation with specified username and "
                + " optional passcode");
        opts.addOption(OptionBuilder.create("username"));

        OptionBuilder.withArgName("passcode");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "optional passcode for User Identity Negotiation, "
                + "only effective with option -username");
        opts.addOption(OptionBuilder.create("passcode"));

        opts.addOption("uidnegrsp", false,
                "request positive User Identity Negotation response, "
                + "only effective with option -username");
                
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

        OptionBuilder.withArgName("aet@host:port");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "request storage commitment of (successfully) sent objects " +
                "afterwards in new association to specified remote " +
                "Application Entity");
        opts.addOption(OptionBuilder.create("stgcmtae"));

        opts.addOption("stgcmt", false,
                "request storage commitment of (successfully) sent objects " +
                "afterwards in same association");
        
        OptionBuilder.withArgName("attr=value");
        OptionBuilder.hasArgs();
        OptionBuilder.withValueSeparator('=');
        OptionBuilder.withDescription("Replace value of specified attribute " +
                "with specified value in transmitted objects. attr can be " +
                "specified by name or tag value (in hex), e.g. PatientName " +
                "or 00100010.");
        opts.addOption(OptionBuilder.create("set"));

        OptionBuilder.withArgName("salt");
        OptionBuilder.hasArgs();
        OptionBuilder.withDescription("Anonymize the files.  Set to 0 for a random anonymization (not repeatable) or 1 for a daily anonymization or another" +
            " value for a specific salt for reproducible anonymization (useful for allowing studies to be sent at a later date and still correctly named/associated)");
        OptionBuilder.withLongOpt("anonymize");
        opts.addOption(OptionBuilder.create("a"));

        OptionBuilder.withArgName("sx1[:sx2[:sx3]");
        OptionBuilder.hasArgs();
        OptionBuilder.withValueSeparator(':');
        OptionBuilder.withDescription("Suffix SOP [,Series [,Study]] " +
                "Instance UID with specified value[s] in transmitted objects.");
        opts.addOption(OptionBuilder.create("setuid"));

        OptionBuilder.withArgName("maxops");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "maximum number of outstanding operations it may invoke " + 
                "asynchronously, unlimited by default.");
        opts.addOption(OptionBuilder.create("async"));

        opts.addOption("pdv1", false,
                "send only one PDV in one P-Data-TF PDU, " + 
                "pack command and data PDV in one P-DATA-TF PDU by default.");
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
                "delay in ms for Socket close after sending A-ABORT, " +
                "50ms by default");
        opts.addOption(OptionBuilder.create("soclosedelay"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "delay in ms for closing the listening socket, " +
                "1000ms by default");
        opts.addOption(OptionBuilder.create("shutdowndelay"));

        OptionBuilder.withArgName("ms");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "period in ms to check for outstanding DIMSE-RSP, " +
                "10s by default");
        opts.addOption(OptionBuilder.create("reaper"));

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
                "timeout in ms for receiving A-RELEASE-RP, 5s by default");
        opts.addOption(OptionBuilder.create("releaseTO"));

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
                "transcoder buffer size in KB, 1KB by default");
        opts.addOption(OptionBuilder.create("bufsize"));
        
        OptionBuilder.withArgName("count");
        OptionBuilder.hasArg();
        OptionBuilder.withDescription(
                "Batch size - Number of files to be sent in each batch, " +
                "where a storage commit is done between batches ");
        opts.addOption(OptionBuilder.create("batchsize"));

        opts.addOption("lowprior", false,
                "LOW priority of the C-STORE operation, MEDIUM by default");
        opts.addOption("highprior", false,
                "HIGH priority of the C-STORE operation, MEDIUM by default");
        opts.addOption("h", "help", false, "print this message");
        opts.addOption("V", "version", false,
                "print the version information and exit");
        CommandLine cl = null;
        try {
            cl = new GnuParser().parse(opts, args);
        } catch (ParseException e) {
            exit("dcmsnd: " + e.getMessage());
            throw new RuntimeException("unreachable");
        }
        if (cl.hasOption('V')) {
            Package p = DcmSnd.class.getPackage();
            System.out.println("dcmsnd v" + p.getImplementationVersion());
            System.exit(0);
        }
        if (cl.hasOption('h') || cl.getArgList().size() < 2) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
            System.exit(0);
        }
        return cl;
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = parse(args);
        DcmSnd dcmsnd = new DcmSnd(cl.hasOption("device") 
                ? cl.getOptionValue("device") : "DCMSND");
        final List<String> argList = cl.getArgList();
        String remoteAE = argList.get(0);
        String[] calledAETAddress = split(remoteAE, '@');
        dcmsnd.setCalledAET(calledAETAddress[0]);
        if (calledAETAddress[1] == null) {
            dcmsnd.setRemoteHost("127.0.0.1");
            dcmsnd.setRemotePort(104);
        } else {
            String[] hostPort = split(calledAETAddress[1], ':');
            dcmsnd.setRemoteHost(hostPort[0]);
            dcmsnd.setRemotePort(toPort(hostPort[1]));
        }
        if (cl.hasOption("L")) {
            String localAE = cl.getOptionValue("L");
            String[] localPort = split(localAE, ':');
            if (localPort[1] != null) {
                dcmsnd.setLocalPort(toPort(localPort[1]));                
            }
            String[] callingAETHost = split(localPort[0], '@');
            dcmsnd.setCalling(callingAETHost[0]);
            if (callingAETHost[1] != null) {
                dcmsnd.setLocalHost(callingAETHost[1]);
            }
        }
        dcmsnd.setOfferDefaultTransferSyntaxInSeparatePresentationContext(
                cl.hasOption("ts1"));
        dcmsnd.setSendFileRef(cl.hasOption("fileref"));
        if (cl.hasOption("username")) {
            String username = cl.getOptionValue("username");
            UserIdentity userId;
            if (cl.hasOption("passcode")) {
                String passcode = cl.getOptionValue("passcode");
                userId = new UserIdentity.UsernamePasscode(username,
                        passcode.toCharArray());
            } else {
                userId = new UserIdentity.Username(username);
            }
            userId.setPositiveResponseRequested(cl.hasOption("uidnegrsp"));
            dcmsnd.setUserIdentity(userId);
        }
        dcmsnd.setStorageCommitment(cl.hasOption("stgcmt"));
        String remoteStgCmtAE = null;
        if (cl.hasOption("stgcmtae")) {
            try {
                remoteStgCmtAE = cl.getOptionValue("stgcmtae");
                String[] aet_hostport = split(remoteStgCmtAE, '@');
                String[] host_port = split(aet_hostport[1], ':');
                dcmsnd.setStgcmtCalledAET(aet_hostport[0]);
                dcmsnd.setRemoteStgcmtHost(host_port[0]);
                dcmsnd.setRemoteStgcmtPort(toPort(host_port[1]));
            } catch (Exception e) {
                exit("illegal argument of option -stgcmtae");
            }
        }
        if (cl.hasOption("set")) {
            String[] vals = cl.getOptionValues("set");
            for (int i = 0; i < vals.length; i++, i++) {
                dcmsnd.addCoerceAttr(Tag.toTag(vals[i]), vals[i+1]);
            }
        }
        if (cl.hasOption("setuid")) {
            dcmsnd.setSuffixUID(cl.getOptionValues("setuid"));
        }
        if (cl.hasOption("connectTO"))
            dcmsnd.setConnectTimeout(parseInt(cl.getOptionValue("connectTO"),
                    "illegal argument of option -connectTO", 1,
                    Integer.MAX_VALUE));
        if (cl.hasOption("reaper"))
            dcmsnd.setAssociationReaperPeriod(
                    parseInt(cl.getOptionValue("reaper"),
                    "illegal argument of option -reaper",
                    1, Integer.MAX_VALUE));
        if (cl.hasOption("rspTO"))
            dcmsnd.setDimseRspTimeout(parseInt(cl.getOptionValue("rspTO"),
                    "illegal argument of option -rspTO",
                    1, Integer.MAX_VALUE));
        if (cl.hasOption("acceptTO"))
            dcmsnd.setAcceptTimeout(parseInt(cl.getOptionValue("acceptTO"),
                    "illegal argument of option -acceptTO", 
                    1, Integer.MAX_VALUE));
        if (cl.hasOption("releaseTO"))
            dcmsnd.setReleaseTimeout(parseInt(cl.getOptionValue("releaseTO"),
                    "illegal argument of option -releaseTO",
                    1, Integer.MAX_VALUE));
        if (cl.hasOption("soclosedelay"))
            dcmsnd.setSocketCloseDelay(
                    parseInt(cl.getOptionValue("soclosedelay"),
                    "illegal argument of option -soclosedelay", 1, 10000));
        if (cl.hasOption("shutdowndelay"))
            dcmsnd.setShutdownDelay(
                    parseInt(cl.getOptionValue("shutdowndelay"),
                    "illegal argument of option -shutdowndelay", 1, 10000));
        if (cl.hasOption("anonymize"))
            dcmsnd.setAnonymize(Long.parseLong(cl.getOptionValue("anonymize")));
        if (cl.hasOption("rcvpdulen"))
            dcmsnd.setMaxPDULengthReceive(
                    parseInt(cl.getOptionValue("rcvpdulen"),
                    "illegal argument of option -rcvpdulen", 1, 10000) * KB);
        if (cl.hasOption("sndpdulen"))
            dcmsnd.setMaxPDULengthSend(parseInt(cl.getOptionValue("sndpdulen"),
                    "illegal argument of option -sndpdulen", 1, 10000) * KB);
        if (cl.hasOption("sosndbuf"))
            dcmsnd.setSendBufferSize(parseInt(cl.getOptionValue("sosndbuf"),
                    "illegal argument of option -sosndbuf", 1, 10000) * KB);
        if (cl.hasOption("sorcvbuf"))
            dcmsnd.setReceiveBufferSize(parseInt(cl.getOptionValue("sorcvbuf"),
                    "illegal argument of option -sorcvbuf", 1, 10000) * KB);
        if (cl.hasOption("bufsize"))
            dcmsnd.setTranscoderBufferSize(
                    parseInt(cl.getOptionValue("bufsize"),
                    "illegal argument of option -bufsize", 1, 10000) * KB);
        if (cl.hasOption("batchsize"))
            dcmsnd.setBatchSize(Integer.parseInt(cl.getOptionValue("batchsize")));
        dcmsnd.setPackPDV(!cl.hasOption("pdv1"));
        dcmsnd.setTcpNoDelay(!cl.hasOption("tcpdelay"));
        if (cl.hasOption("async"))
            dcmsnd.setMaxOpsInvoked(parseInt(cl.getOptionValue("async"),
                    "illegal argument of option -async", 0, 0xffff));
        if (cl.hasOption("lowprior"))
            dcmsnd.setPriority(CommandUtils.LOW);
        if (cl.hasOption("highprior"))
            dcmsnd.setPriority(CommandUtils.HIGH);
        System.out.println("Scanning files to send");
        long t1 = System.currentTimeMillis();
        for (int i = 1, n = argList.size(); i < n; ++i)
            dcmsnd.addFile(new File(argList.get(i)));
        long t2 = System.currentTimeMillis();
        if (dcmsnd.getNumberOfFilesToSend() == 0) {
            System.exit(2);
        }
        System.out.println("\nScanned " + dcmsnd.getNumberOfFilesToSend()
                + " files in " + ((t2 - t1) / 1000F) + "s (="
                + ((t2 - t1) / dcmsnd.getNumberOfFilesToSend()) + "ms/file)");
        dcmsnd.configureTransferCapability();
        if (cl.hasOption("tls")) {
            String cipher = cl.getOptionValue("tls");
            if ("NULL".equalsIgnoreCase(cipher)) {
                dcmsnd.setTlsWithoutEncyrption();
            } else if ("3DES".equalsIgnoreCase(cipher)) {
                dcmsnd.setTls3DES_EDE_CBC();
            } else if ("AES".equalsIgnoreCase(cipher)) {
                dcmsnd.setTlsAES_128_CBC();
            } else {
                exit("Invalid parameter for option -tls: " + cipher);
            }

            if (cl.hasOption("tls1")) {
                dcmsnd.setTlsProtocol(TLS1);
            } else if (cl.hasOption("ssl3")) {
                dcmsnd.setTlsProtocol(SSL3);
            } else if (cl.hasOption("no_tls1")) {
                dcmsnd.setTlsProtocol(NO_TLS1);
            } else if (cl.hasOption("no_ssl3")) {
                dcmsnd.setTlsProtocol(NO_SSL3);
            } else if (cl.hasOption("no_ssl2")) {
                dcmsnd.setTlsProtocol(NO_SSL2);
            }
            dcmsnd.setTlsNeedClientAuth(!cl.hasOption("noclientauth"));

            if (cl.hasOption("keystore")) {
                dcmsnd.setKeyStoreURL(cl.getOptionValue("keystore"));
            }
            if (cl.hasOption("keystorepw")) {
                dcmsnd.setKeyStorePassword(
                        cl.getOptionValue("keystorepw"));
            }
            if (cl.hasOption("keypw")) {
                dcmsnd.setKeyPassword(cl.getOptionValue("keypw"));
            }
            if (cl.hasOption("truststore")) {
                dcmsnd.setTrustStoreURL(
                        cl.getOptionValue("truststore"));
            }
            if (cl.hasOption("truststorepw")) {
                dcmsnd.setTrustStorePassword(
                        cl.getOptionValue("truststorepw"));
            }
            try {
                dcmsnd.initTLS();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to initialize TLS context:"
                        + e.getMessage());
                System.exit(2);
            }
        }
        while( dcmsnd.getLastSentFile() < dcmsnd.getNumberOfFilesToSend() ) {
            try {
                dcmsnd.start();
            } catch (Exception e) {
                System.err.println("ERROR: Failed to start server for receiving " +
                        "Storage Commitment results:" + e.getMessage());
                System.exit(2);
            }
            try {
                t1 = System.currentTimeMillis();
                try {
                    dcmsnd.open();
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to establish association:"
                            + e.getMessage());
                    System.exit(2);
                }
                t2 = System.currentTimeMillis();
                System.out.println("Connected to " + remoteAE + " in " 
                        + ((t2 - t1) / 1000F) + "s");
        
                t1 = System.currentTimeMillis();
                dcmsnd.send();
                t2 = System.currentTimeMillis();
                prompt(dcmsnd, (t2 - t1) / 1000F);
                if (dcmsnd.isStorageCommitment()) {
                    t1 = System.currentTimeMillis();
                    if (dcmsnd.commit()) {
                        t2 = System.currentTimeMillis();
                        System.out.println("Request Storage Commitment from " 
                                + remoteAE + " in " + ((t2 - t1) / 1000F) + "s");
                        System.out.println("Waiting for Storage Commitment Result..");
                        try {
                            DicomObject cmtrslt = dcmsnd.waitForStgCmtResult();
                            t1 = System.currentTimeMillis();
                            promptStgCmt(cmtrslt, ((t1 - t2) / 1000F));
                        } catch (InterruptedException e) {
                            System.err.println("ERROR:" + e.getMessage());
                        }
                    }
                 }
                dcmsnd.close();
                System.out.println("Released connection to " + remoteAE);
                if (remoteStgCmtAE != null) {
                    t1 = System.currentTimeMillis();
                    try {
                        dcmsnd.openToStgcmtAE();
                    } catch (Exception e) {
                        System.err.println("ERROR: Failed to establish association:"
                                + e.getMessage());
                        System.exit(2);
                    }
                    t2 = System.currentTimeMillis();
                    System.out.println("Connected to " + remoteStgCmtAE + " in " 
                            + ((t2 - t1) / 1000F) + "s");
                    t1 = System.currentTimeMillis();
                    if (dcmsnd.commit()) {
                        t2 = System.currentTimeMillis();
                        System.out.println("Request Storage Commitment from " 
                                + remoteStgCmtAE + " in " + ((t2 - t1) / 1000F) + "s");
                        System.out.println("Waiting for Storage Commitment Result..");
                        try {
                            DicomObject cmtrslt = dcmsnd.waitForStgCmtResult();
                            t1 = System.currentTimeMillis();
                            promptStgCmt(cmtrslt, ((t1 - t2) / 1000F));
                        } catch (InterruptedException e) {
                            System.err.println("ERROR:" + e.getMessage());
                        }
                    }
                    dcmsnd.close();
                    System.out.println("Released connection to " + remoteStgCmtAE);
                }
            } finally {
                dcmsnd.stop();
            }
        }
    }

    private void setAnonymize(long salt) {
        this.anonymizer = new Anonymizer(salt);
    }

    public void addCoerceAttr(int tag, String val) {
        if (coerceAttrs == null)
            coerceAttrs = new BasicDicomObject();
        if (val.length() == 0)
            coerceAttrs.putNull(tag, null);
        else
            coerceAttrs.putString(tag, null, val);
    }

    public void setSuffixUID(String[] suffix) {
        if (suffix.length > 3)
            throw new IllegalArgumentException(
                    "suffix.length: " + suffix.length);

        this.suffixUID = suffix.length > 0 ? suffix.clone() : null;
    }

    protected static void promptStgCmt(DicomObject cmtrslt, float seconds) {
        System.out.println("Received Storage Commitment Result after "
                + seconds + "s:");
        DicomElement refSOPSq = cmtrslt.get(Tag.ReferencedSOPSequence);
        System.out.print(refSOPSq.countItems());
        System.out.println(" successful");
        DicomElement failedSOPSq = cmtrslt.get(Tag.FailedSOPSequence);
        if (failedSOPSq != null) {
            System.out.print(failedSOPSq.countItems());
            System.out.println(" FAILED!");                       
        }
    }

    protected synchronized DicomObject waitForStgCmtResult() throws InterruptedException {
        while (stgCmtResult == null) wait();
        return stgCmtResult;
    }

    protected static void prompt(DcmSnd dcmsnd, float seconds) {
        System.out.print("\nSent ");
        System.out.print(dcmsnd.getNumberOfFilesSent());
        System.out.print(" objects (=");
        promptBytes(dcmsnd.getTotalSizeSent());
        System.out.print(") in ");
        System.out.print(seconds);
        System.out.print("s (=");
        promptBytes(dcmsnd.getTotalSizeSent() / seconds);
        System.out.println("/s)");
    }

    private static void promptBytes(float totalSizeSent) {
        if (totalSizeSent > MB) {
            System.out.print(totalSizeSent / MB);
            System.out.print("MB");
        } else {
            System.out.print(totalSizeSent / KB);
            System.out.print("KB");
        }
    }

    private static int toPort(String port) {
        return port != null ? parseInt(port, "illegal port number", 1, 0xffff)
                : 104;
    }

    private static String[] split(String s, char delim) {
        String[] s2 = { s, null };
        int pos = s.indexOf(delim);
        if (pos != -1) {
            s2[0] = s.substring(0, pos);
            s2[1] = s.substring(pos + 1);
        }
        return s2;
    }

    private static void exit(String msg) {
        System.err.println(msg);
        System.err.println("Try 'dcmsnd -h' for more information.");
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

    public void addFile(File f) {
        if (f.isDirectory()) {
            File[] fs = f.listFiles();
            if (fs == null || fs.length == 0)
                return;
            for (int i = 0; i < fs.length; i++)
                addFile(fs[i]);
            return;
        }

        if(f.isHidden())
            return;
        
        FileInfo info = new FileInfo(f);
        DicomObject dcmObj = new BasicDicomObject();
        DicomInputStream in = null;
        try {
            in = new DicomInputStream(f);
            in.setHandler(new StopTagInputHandler(Tag.StudyDate));
            in.readDicomObject(dcmObj, PEEK_LEN);
            info.tsuid = in.getTransferSyntax().uid();
            info.fmiEndPos = in.getEndOfFileMetaInfoPosition();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("WARNING: Failed to parse " + f + " - skipped.");
            System.out.print('F');
            return;
        } finally {
            CloseUtils.safeClose(in);
        }
        info.cuid = dcmObj.getString(Tag.MediaStorageSOPClassUID,
                dcmObj.getString(Tag.SOPClassUID));
        if (info.cuid == null) {
            System.err.println("WARNING: Missing SOP Class UID in " + f
                    + " - skipped.");
            System.out.print('F');
            return;
        }
        info.iuid = dcmObj.getString(Tag.MediaStorageSOPInstanceUID,
                dcmObj.getString(Tag.SOPInstanceUID));
        if (info.iuid == null) {
            System.err.println("WARNING: Missing SOP Instance UID in " + f
                    + " - skipped.");
            System.out.print('F');
            return;
        }
        if (suffixUID != null)
            info.iuid = info.iuid + suffixUID[0];
        addTransferCapability(info.cuid, info.tsuid);
        files.add(info);
        System.out.print('.');
    }

    public void addTransferCapability(String cuid, String tsuid) {
        Set<String> ts = as2ts.get(cuid);
        if (fileref) {
            if (ts == null) {
                as2ts.put(cuid,
                        Collections.singleton(DCM4CHEE_URI_REFERENCED_TS_UID));
            }
        } else {
            if (ts == null) {
                ts = new HashSet<String>();
                ts.add(UID.ImplicitVRLittleEndian);
                as2ts.put(cuid, ts);
            }
            ts.add(tsuid);
        }
    }

    public void configureTransferCapability() {
        int off = stgcmt || remoteStgcmtAE != null ? 1 : 0;
        TransferCapability[] tc = new TransferCapability[off + as2ts.size()];
        if (off > 0) {
            tc[0] = new TransferCapability(
                    UID.StorageCommitmentPushModelSOPClass,
                    ONLY_IVLE_TS, 
                    TransferCapability.SCU);
        }
        Iterator<Map.Entry<String, Set<String>>> iter = as2ts.entrySet().iterator();
        for (int i = off; i < tc.length; i++) {
            Map.Entry<String, Set<String>> e = iter.next();
            String cuid = e.getKey();
            Set<String> ts = e.getValue();
            tc[i] = new TransferCapability(cuid, 
                    ts.toArray(new String[ts.size()]),
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

    public void openToStgcmtAE() throws IOException, ConfigurationException,
            InterruptedException {
        assoc = ae.connect(remoteStgcmtAE, executor);
    }

    public void send() {
        int i = 0, n = files.size();
        for ( ; (i+lastSentFile) < n && (batchSize==0 || i < batchSize); ++i) {
            FileInfo info = files.get(i + lastSentFile);
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
                    fileref ? DCM4CHEE_URI_REFERENCED_TS_UID : info.tsuid);
            if (tsuid == null) {
                System.out.println();
                System.out.println(UIDDictionary.getDictionary().prompt(
                        info.cuid)
                        + " with "
                        + UIDDictionary.getDictionary().prompt(
                                fileref ? DCM4CHEE_URI_REFERENCED_TS_UID 
                                        : info.tsuid)
                        + " not supported by " + remoteAE.getAETitle());
                System.out.println("skip file " + info.f);
                continue;
            }
            try {
                DimseRSPHandler rspHandler = new DimseRSPHandler() {
                    @Override
                    public void onDimseRSP(Association as, DicomObject cmd,
                            DicomObject data) {
                        DcmSnd.this.onDimseRSP(cmd);
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
        lastSentFile += i;
        
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
            FileInfo info = files.get(i);
            if (info.transferred) {
                BasicDicomObject refSOP = new BasicDicomObject();
                refSOP.putString(Tag.ReferencedSOPClassUID, VR.UI, info.cuid);
                refSOP.putString(Tag.ReferencedSOPInstanceUID, VR.UI, info.iuid);
                refSOPSq.addDicomObject(refSOP);
            }
        }
        try {
            stgCmtResult = null;
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
        for (int j = 0; j < available.length; j++)
            if (available[j].equals(tsuid))
                return tsuid;
        return null;
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

    public static final class FileInfo {
        File f;

        String cuid;

        String iuid;

        String tsuid;

        long fmiEndPos;

        long length;
        
        boolean transferred;
        
        int status;

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
            if (coerceAttrs != null || suffixUID != null) {
                DicomObject attrs;
                DicomInputStream dis = new DicomInputStream(info.f);
                try {
                    dis.setHandler(new StopTagInputHandler(Tag.PixelData));
                    attrs = dis.readDicomObject();
                    suffixUIDs(attrs);
                    coerceAttrs(attrs);
                    anonymize(attrs);
                    DicomOutputStream dos = new DicomOutputStream(out);
                    dos.writeDataset(attrs, tsuid);
                    if (dis.tag() >= Tag.PixelData) {
                        copyPixelData(dis, dos, out);
                        // copy attrs after PixelData
                        dis.setHandler(dis);
                        attrs = dis.readDicomObject();
                        dos.writeDataset(attrs, tsuid);
                    }
                } finally {
                    dis.close();
                }
            } else if (tsuid.equals(info.tsuid)) {
                FileInputStream fis = new FileInputStream(info.f);
                try {
                    long skip = info.fmiEndPos;
                    while (skip > 0)
                        skip -= fis.skip(skip);
                    out.copyFrom(fis);
                } finally {
                    fis.close();
                }
            } else if (tsuid.equals(DCM4CHEE_URI_REFERENCED_TS_UID)) {
                DicomObject attrs;
                DicomInputStream dis = new DicomInputStream(info.f);
                try {
                    dis.setHandler(new StopTagInputHandler(Tag.PixelData));
                    attrs = dis.readDicomObject();
                } finally {
                    dis.close();
                }
                DicomOutputStream dos = new DicomOutputStream(out);
                attrs.putString(Tag.RetrieveURI, VR.UT, info.f.toURI().toString());
                dos.writeDataset(attrs, tsuid);
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

        private void anonymize(DicomObject attrs) {
            if( anonymizer!=null ) {
                anonymizer.anonymize(attrs);
            }
        }

    }

    private void suffixUIDs(DicomObject attrs) {
        if (suffixUID != null) {
            int[] uidTags = { Tag.SOPInstanceUID,
                    Tag.SeriesInstanceUID, Tag.StudyInstanceUID };
            for (int i = 0; i < suffixUID.length; i++)
                attrs.putString(uidTags[i], VR.UI,
                        attrs.getString(uidTags[i]) + suffixUID[i]);
        }
    }

    private void coerceAttrs(DicomObject attrs) {
        if (coerceAttrs != null)
            coerceAttrs.copyTo(attrs);
    }

    private void copyPixelData(DicomInputStream dis, DicomOutputStream dos,
            PDVOutputStream out) throws IOException {
        int vallen = dis.valueLength();
        dos.writeHeader(dis.tag(), dis.vr(), vallen);
        if (vallen == -1) {
            int tag;
            do {
                tag = dis.readHeader();
                vallen = dis.valueLength();
                dos.writeHeader(tag, null, vallen);
                out.copyFrom(dis, vallen);
            } while (tag == Tag.Item);
        } else {
            out.copyFrom(dis, vallen);
        }
    }

    private void promptErrRSP(String prefix, int status, FileInfo info,
            DicomObject cmd) {
        System.err.println(prefix + StringUtils.shortToHex(status) + "H for "
                + info.f + ", cuid=" + info.cuid + ", tsuid=" + info.tsuid);
        System.err.println(cmd.toString());
    }

    private void onDimseRSP(DicomObject cmd) {
        int status = cmd.getInt(Tag.Status);
        int msgId = cmd.getInt(Tag.MessageIDBeingRespondedTo);
        FileInfo info = files.get(msgId - 1);
        info.status = status;
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
    
    @Override
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
            return DcmSnd.class.getClassLoader().getResourceAsStream(
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
}
