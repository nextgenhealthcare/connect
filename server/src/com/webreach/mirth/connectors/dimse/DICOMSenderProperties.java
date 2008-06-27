package com.webreach.mirth.connectors.dimse;

import com.webreach.mirth.model.ComponentProperties;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * Date: Jun 11, 2008
 * Time: 4:01:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMSenderProperties implements ComponentProperties {
	public static final String name = "DICOM Sender";
	
    public static final String DATATYPE = "DataType";
    public static final String DICOM_ADDRESS = "host";
    public static final String DICOM_PORT = "port";
    public static final String DICOM_TEMPLATE = "template";
    public static final String DICOM_ACCECPTTO = "accecptto";
    public static final String DICOM_ASYNC = "async";
    public static final String DICOM_BUFSIZE = "bufsize";
    public static final String DICOM_CONNECTTO = "connectto";
    public static final String DICOM_PRIORITY = "priority";
    public static final String DICOM_KEYPW = "keypw";    
    public static final String DICOM_KEYSTORE = "keystore";    
    public static final String DICOM_KEYSTOREPW = "keystorepw";   
    public static final String DICOM_NOCLIENTAUTH = "noclientauth";   
    public static final String DICOM_NOSSL2 = "nossl2";
    public static final String DICOM_PASSCODE = "passcode";
    public static final String DICOM_PDV1 = "pdv1"; 
    public static final String DICOM_RCVPDULEN = "rcvpdulen"; 
    public static final String DICOM_REAPER = "reaper";
    public static final String DICOM_RELEASETO = "releaseto";
    public static final String DICOM_RSPTO = "rspto";
    public static final String DICOM_SHUTDOWNDELAY = "shutdowndelay";
    public static final String DICOM_SNDPDULEN = "sndpdulen";
    public static final String DICOM_SOCLOSEDELAY = "soclosedelay";     
    public static final String DICOM_SORCVBUF = "sorcvbuf";
    public static final String DICOM_SOSNDBUF = "sosndbuf";
    public static final String DICOM_STGCMT = "stgcmt";
    public static final String DICOM_TCPDELAY = "tcpdelay";
    public static final String DICOM_TLS = "tls";
    public static final String DICOM_TRUSTSTORE = "truststore";
    public static final String DICOM_TRUSTSTOREPW = "truststorepw";
    public static final String DICOM_TS1 = "ts1";
    public static final String DICOM_UIDNEGRSP = "uidnegrsp";
    public static final String DICOM_USERNAME = "username";


    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(DICOM_ADDRESS, "127.0.0.1");
        properties.put(DICOM_PORT, "104");
        properties.put(DICOM_TEMPLATE, "${DICOMMESSAGE}");
        properties.put(DICOM_ACCECPTTO, "5");
        properties.put(DICOM_ASYNC, "0");
        properties.put(DICOM_BUFSIZE, "1");
        properties.put(DICOM_CONNECTTO, "0");
        properties.put(DICOM_PRIORITY, "med");
        properties.put(DICOM_KEYPW, "");
        properties.put(DICOM_KEYSTORE, "");
        properties.put(DICOM_KEYSTOREPW, "");
        properties.put(DICOM_NOCLIENTAUTH, "1");
        properties.put(DICOM_NOSSL2, "1");
        properties.put(DICOM_PASSCODE, "");
        properties.put(DICOM_PDV1, "0");
        properties.put(DICOM_RCVPDULEN, "16");
        properties.put(DICOM_REAPER, "10");
        properties.put(DICOM_RELEASETO, "5");
        properties.put(DICOM_RSPTO, "60");
        properties.put(DICOM_SHUTDOWNDELAY, "1000");
        properties.put(DICOM_SNDPDULEN, "16");
        properties.put(DICOM_SOCLOSEDELAY, "50");
        properties.put(DICOM_SORCVBUF, "0");
        properties.put(DICOM_SOSNDBUF, "0");
        properties.put(DICOM_STGCMT, "0");
        properties.put(DICOM_TCPDELAY, "1");
        properties.put(DICOM_TLS, "notls");
        properties.put(DICOM_TRUSTSTORE, "");
        properties.put(DICOM_TRUSTSTOREPW, "");
        properties.put(DICOM_TS1, "0");
        properties.put(DICOM_UIDNEGRSP, "0");
        properties.put(DICOM_USERNAME, "");
       
        return properties;
    }    
}
