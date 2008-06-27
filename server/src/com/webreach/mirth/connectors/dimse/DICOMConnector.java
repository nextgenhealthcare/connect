package com.webreach.mirth.connectors.dimse;

import org.mule.config.i18n.Message;
import org.mule.umo.UMOComponent;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.provider.UMOMessageReceiver;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.MuleManager;
import org.mule.providers.AbstractServiceEnabledConnector;
import org.mule.impl.model.AbstractComponent;
import org.mule.management.stats.ComponentStatistics;
import org.dcm4che2.net.service.VerificationService;
import org.dcm4che2.net.NetworkConnection;
import org.dcm4che2.net.NetworkApplicationEntity;
import com.webreach.mirth.connectors.tcp.protocols.DefaultProtocol;
import com.webreach.mirth.server.controllers.SystemLogger;
import com.webreach.mirth.model.SystemEvent;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * Date: Jun 12, 2008
 * Time: 12:02:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMConnector extends AbstractServiceEnabledConnector {
    //--------------------------------
	// custom properties
    public static final String DICOM_TEMPLATE = "template";
    public static final String DICOM_HOST = "host";
    public static final String DICOM_PORT = "port";
    public static final String DICOM_ACCECPTTO = "accecptto";        // int
    public static final String DICOM_ASYNC = "async";                // int
    public static final String DICOM_BUFSIZE = "bufsize";            // int
    public static final String DICOM_CONNECTTO = "connectto";        // int
    public static final String DICOM_PRIORITY = "priority";          // string
    public static final String DICOM_KEYPW = "keypw";                // string
    public static final String DICOM_KEYSTORE = "keystore";          // string
    public static final String DICOM_KEYSTOREPW = "keystorepw";      // string
    public static final String DICOM_NOCLIENTAUTH = "noclientauth";  // boolean   
    public static final String DICOM_NOSSL2 = "nossl2";              // boolean
    public static final String DICOM_PASSCODE = "passcode";          // string
    public static final String DICOM_PDV1 = "pdv1";                  // boolean
    public static final String DICOM_RCVPDULEN = "rcvpdulen";        // int
    public static final String DICOM_REAPER = "reaper";              // int
    public static final String DICOM_RELEASETO = "releaseto";        // int
    public static final String DICOM_RSPTO = "rspto";                // int
    public static final String DICOM_SHUTDOWNDELAY = "shutdowndelay";// int
    public static final String DICOM_SNDPDULEN = "sndpdulen";        // int
    public static final String DICOM_SOCLOSEDELAY = "soclosedelay";  // int   
    public static final String DICOM_SORCVBUF = "sorcvbuf";          // int
    public static final String DICOM_SOSNDBUF = "sosndbuf";          // int
    public static final String DICOM_STGCMT = "stgcmt";              // boolean
    public static final String DICOM_TCPDELAY = "tcpdelay";          // boolean
    public static final String DICOM_TLS = "tls";                    // string
    public static final String DICOM_TRUSTSTORE = "truststore";      // string
    public static final String DICOM_TRUSTSTOREPW = "truststorepw";  // string
    public static final String DICOM_TS1 = "ts1";                    // boolean
    public static final String DICOM_UIDNEGRSP = "uidnegrsp";        // boolean
    public static final String DICOM_USERNAME = "username";          // string
    
    public static final String DICOM_REQUESTTO = "requestto";        // int
    public static final String DICOM_IDLETO = "idleto";              // int
    public static final String DICOM_RSPDELAY = "rspdelay";          // int
    public static final String DICOM_BIGENDIAN = "bigendian";        // boolean
    public static final String DICOM_DEFTS = "defts";                // boolean
    public static final String DICOM_DEST = "dest";                  // string
    public static final String DICOM_NATIVE = "nativeData";              // boolean
    
    // custom properties
    private NetworkConnection nc = new NetworkConnection();
    private NetworkApplicationEntity ae = new NetworkApplicationEntity();
    private String template = "message.encodedData";
    private String host = null;
    private String port = null;
    private int accecptto;
    private int async;
    private int bufsize;
    private int connectto;
    private String priority;
    private String keypw;
    private String keystore;
    private String keystorepw;
    private boolean noclientauth;
    private boolean nossl2;
    private String passcode;
    private boolean pdv1;
    private int rcvpdulen;
    private int reaper;
    private int releaseto;
    private int rspto;
    private int shutdowndelay;
    private int sndpdulen;
    private int soclosedelay;
    private int sorcvbuf;
    private int sosndbuf;
    private boolean stgcmt;
    private boolean tcpdelay;
    private String tls;
    private String truststore;
    private String truststorepw;
    private boolean ts1;
    private boolean uidnegrsp;
    private String username;
    
    private int requestto;
    private int idleto;
    private int rspdelay;        
    private boolean bigendian;        
    private boolean defts;        
    private String dest;        
    private boolean nativeData;        
            
    private UMOComponent component = null;

	// ast: encoding Charset
	public static final String PROPERTY_CHARSET_ENCODING = "charsetEncoding";
	public static final String CHARSET_KEY = "ca.uhn.hl7v2.llp.charset";
	public static final String DEFAULT_CHARSET_ENCODING = System.getProperty(CHARSET_KEY, java.nio.charset.Charset.defaultCharset().name());
	private String charsetEncoding = DEFAULT_CHARSET_ENCODING;

	// ast: overload of the creator, to allow the test of the charset Encoding
	public DICOMConnector() {
		super();
		// //ast: try to set the default encoding
		this.setCharsetEncoding(DEFAULT_CHARSET_ENCODING);
	}


	// //////////////////////////////////////////////////////////////////////
	public void doInitialise() throws InitialisationException {
		super.doInitialise();
        ae.setNetworkConnection(nc);
        ae.setAssociationAcceptor(true);
        ae.register(new VerificationService());
	}

    /**
     * @see org.mule.umo.provider.UMOConnector#getProtocol()
     */
    public String getProtocol()
    {
        return "dicom";
    }


	public boolean isRemoteSyncEnabled() {
		return true;
	}

	public char stringToChar(String source) {
		return source.charAt(0);
	}


	// ast: set the charset Encoding
	public void setCharsetEncoding(String charsetEncoding) {
		if ((charsetEncoding == null) || (charsetEncoding.equals("")) || (charsetEncoding.equalsIgnoreCase("DEFAULT_ENCODING")))
			charsetEncoding = DEFAULT_CHARSET_ENCODING;
		logger.debug("FileConnector: trying to set the encoding to " + charsetEncoding);
		try {
			byte b[] = { 20, 21, 22, 23 };
			String k = new String(b, charsetEncoding);
			this.charsetEncoding = charsetEncoding;
		} catch (Exception e) {
			// set the encoding to the default one: this charset can't launch an
			// exception
			this.charsetEncoding = java.nio.charset.Charset.defaultCharset().name();
			logger.error("Impossible to use [" + charsetEncoding + "] as the Charset Encoding: changing to the platform default [" + this.charsetEncoding + "]");
			SystemLogger systemLogger = SystemLogger.getInstance();
			SystemEvent event = new SystemEvent("Exception occured in channel.");
			event.setDescription("Impossible to use [" + charsetEncoding + "] as the Charset Encoding: changing to the platform default [" + this.charsetEncoding + "]");
			systemLogger.logSystemEvent(event);
		}
	}

	// ast: get the charset Encoding
	public String getCharsetEncoding() {
		if ((this.charsetEncoding == null) || (this.charsetEncoding.equals("")) || (this.charsetEncoding.equalsIgnoreCase("DEFAULT_ENCODING"))) {
			// Default Charset
			return DEFAULT_CHARSET_ENCODING;
		}
		return (this.charsetEncoding);
	}


	/*
	 * Overload method to avoid error startting the channel after an stop
	 * (non-Javadoc)
	 * 
	 * @see org.mule.providers.AbstractConnector#registerListener(org.mule.umo.UMOComponent,
	 *      org.mule.umo.endpoint.UMOEndpoint)
	 */
	public UMOMessageReceiver registerListener(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		UMOMessageReceiver r = null;
		this.component = component;
		try {
			r = super.registerListener(component, endpoint);
		} catch (org.mule.umo.provider.ConnectorException e) {
			logger.warn("Trying to reconnect a listener: this is not an error with this kind of router");
		}
		return r;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mule.umo.provider.UMOConnector#registerListener(org.mule.umo.UMOSession,
	 *      org.mule.umo.endpoint.UMOEndpoint)
	 */
	public UMOMessageReceiver createReceiver(UMOComponent component, UMOEndpoint endpoint) throws Exception {
		this.component = component;
		Map props = endpoint.getProperties();
		if (props != null) {
			// Override properties on the endpoint for the specific endpoint
			String template = (String) props.get(DICOM_TEMPLATE);
			if (template != null) {
				setTemplate(template);
			}
			String prop_host = (String) props.get(DICOM_HOST);
			if (prop_host != null) {
				setHost(prop_host);
			}
			String prop_port = (String) props.get(DICOM_PORT);
			if (prop_port != null && !prop_port.equals("")) {
				setPort(prop_port);
			}
		}    
    	return super.createReceiver(component, endpoint);
	}

	public void incErrorStatistics() {
		incErrorStatistics(component);
	}

	public void incErrorStatistics(UMOComponent umoComponent) {
		ComponentStatistics statistics = null;

		if (umoComponent != null)
			component = umoComponent;

		if (component == null) {
			return;
		}

		if (!(component instanceof AbstractComponent)) {
			return;
		}

		try {
			statistics = ((AbstractComponent) component).getStatistics();
			if (statistics == null) {
				return;
			}
			statistics.incExecutionError();
		} catch (Throwable t) {
			logger.error("Error setting statistics ");
		}
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public NetworkConnection getNc() {
        return nc;
    }

    public void setNc(NetworkConnection nc) {
        this.nc = nc;
    }

    public NetworkApplicationEntity getAe() {
        return ae;
    }

    public void setAe(NetworkApplicationEntity ae) {
        this.ae = ae;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAccecptto() {
        return accecptto;
    }

    public void setAccecptto(int accecptto) {
        this.accecptto = accecptto;
    }

    public int getAsync() {
        return async;
    }

    public void setAsync(int async) {
        this.async = async;
    }

    public int getBufsize() {
        return bufsize;
    }

    public void setBufsize(int bufsize) {
        this.bufsize = bufsize;
    }

    public int getConnectto() {
        return connectto;
    }

    public void setConnectto(int connectto) {
        this.connectto = connectto;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getKeypw() {
        return keypw;
    }

    public void setKeypw(String keypw) {
        this.keypw = keypw;
    }

    public String getKeystore() {
        return keystore;
    }

    public void setKeystore(String keystore) {
        this.keystore = keystore;
    }

    public String getKeystorepw() {
        return keystorepw;
    }

    public void setKeystorepw(String keystorepw) {
        this.keystorepw = keystorepw;
    }

    public boolean isNoclientauth() {
        return noclientauth;
    }

    public void setNoclientauth(boolean noclientauth) {
        this.noclientauth = noclientauth;
    }

    public boolean isNossl2() {
        return nossl2;
    }

    public void setNossl2(boolean nossl2) {
        this.nossl2 = nossl2;
    }

    public String getPasscode() {
        return passcode;
    }

    public void setPasscode(String passcode) {
        this.passcode = passcode;
    }

    public boolean isPdv1() {
        return pdv1;
    }

    public void setPdv1(boolean pdv1) {
        this.pdv1 = pdv1;
    }

    public int getRcvpdulen() {
        return rcvpdulen;
    }

    public void setRcvpdulen(int rcvpdulen) {
        this.rcvpdulen = rcvpdulen;
    }

    public int getReaper() {
        return reaper;
    }

    public void setReaper(int reaper) {
        this.reaper = reaper;
    }

    public int getReleaseto() {
        return releaseto;
    }

    public void setReleaseto(int releaseto) {
        this.releaseto = releaseto;
    }

    public int getRspto() {
        return rspto;
    }

    public void setRspto(int rspto) {
        this.rspto = rspto;
    }

    public int getShutdowndelay() {
        return shutdowndelay;
    }

    public void setShutdowndelay(int shutdowndelay) {
        this.shutdowndelay = shutdowndelay;
    }

    public int getSndpdulen() {
        return sndpdulen;
    }

    public void setSndpdulen(int sndpdulen) {
        this.sndpdulen = sndpdulen;
    }

    public int getSoclosedelay() {
        return soclosedelay;
    }

    public void setSoclosedelay(int soclosedelay) {
        this.soclosedelay = soclosedelay;
    }

    public int getSorcvbuf() {
        return sorcvbuf;
    }

    public void setSorcvbuf(int sorcvbuf) {
        this.sorcvbuf = sorcvbuf;
    }

    public int getSosndbuf() {
        return sosndbuf;
    }

    public void setSosndbuf(int sosndbuf) {
        this.sosndbuf = sosndbuf;
    }

    public boolean isStgcmt() {
        return stgcmt;
    }

    public void setStgcmt(boolean stgcmt) {
        this.stgcmt = stgcmt;
    }

    public boolean isTcpdelay() {
        return tcpdelay;
    }

    public void setTcpdelay(boolean tcpdelay) {
        this.tcpdelay = tcpdelay;
    }

    public String getTls() {
        return tls;
    }

    public void setTls(String tls) {
        this.tls = tls;
    }

    public String getTruststore() {
        return truststore;
    }

    public void setTruststore(String truststore) {
        this.truststore = truststore;
    }

    public String getTruststorepw() {
        return truststorepw;
    }

    public void setTruststorepw(String truststorepw) {
        this.truststorepw = truststorepw;
    }

    public boolean isTs1() {
        return ts1;
    }

    public void setTs1(boolean ts1) {
        this.ts1 = ts1;
    }

    public boolean isUidnegrsp() {
        return uidnegrsp;
    }

    public void setUidnegrsp(boolean uidnegrsp) {
        this.uidnegrsp = uidnegrsp;
    }

    public int getRequestto() {
        return requestto;
    }

    public void setRequestto(int requestto) {
        this.requestto = requestto;
    }

    public int getIdleto() {
        return idleto;
    }

    public void setIdleto(int idleto) {
        this.idleto = idleto;
    }

    public int getRspdelay() {
        return rspdelay;
    }

    public void setRspdelay(int rspdelay) {
        this.rspdelay = rspdelay;
    }

    public boolean isBigendian() {
        return bigendian;
    }

    public void setBigendian(boolean bigendian) {
        this.bigendian = bigendian;
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
}
