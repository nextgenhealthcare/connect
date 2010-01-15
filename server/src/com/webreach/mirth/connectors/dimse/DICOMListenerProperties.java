package com.webreach.mirth.connectors.dimse;

import java.util.Properties;

import com.webreach.mirth.model.ComponentProperties;

/**
 * Created by IntelliJ IDEA.
 * Date: Jan 24, 2008
 * Time: 9:58:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class DICOMListenerProperties implements ComponentProperties {
	public static final String name = "DICOM Listener";
	
    public static final String DATATYPE = "DataType";
    public static final String DICOM_ADDRESS = "host";
    public static final String DICOM_PORT = "port";
    public static final String DICOM_APPENTITY = "applicationEntity";    
    public static final String DICOM_LOCALADDRESS = "localHost";
    public static final String DICOM_LOCALPORT = "localPort";
    public static final String DICOM_LOCALAPPENTITY = "localApplicationEntity";
    public static final String DICOM_SOCLOSEDELAY = "soclosedelay";     
    public static final String DICOM_RELEASETO = "releaseto";    
    public static final String DICOM_REQUESTTO = "requestto";
    public static final String DICOM_IDLETO = "idleto";
    public static final String DICOM_REAPER = "reaper";    
    public static final String DICOM_RSPDELAY = "rspdelay";
    public static final String DICOM_PDV1 = "pdv1";   
    public static final String DICOM_SNDPDULEN = "sndpdulen";
    public static final String DICOM_RCVPDULEN = "rcvpdulen"; 
    public static final String DICOM_ASYNC = "async";    
    public static final String DICOM_BIGENDIAN = "bigendian";
    public static final String DICOM_BUFSIZE = "bufsize";
    public static final String DICOM_DEFTS = "defts";
    public static final String DICOM_DEST = "dest";
    public static final String DICOM_NATIVE = "nativeData";
    public static final String DICOM_SORCVBUF = "sorcvbuf";
    public static final String DICOM_SOSNDBUF = "sosndbuf";
    public static final String DICOM_TCPDELAY = "tcpdelay";    
    
    public static final String DICOM_KEYPW = "keypw";    
    public static final String DICOM_KEYSTORE = "keystore";    
    public static final String DICOM_KEYSTOREPW = "keystorepw";   
    public static final String DICOM_NOCLIENTAUTH = "noclientauth";   
    public static final String DICOM_NOSSL2 = "nossl2";
    public static final String DICOM_TLS = "tls";
    public static final String DICOM_TRUSTSTORE = "truststore";
    public static final String DICOM_TRUSTSTOREPW = "truststorepw";
    

    public Properties getDefaults()
    {
        Properties properties = new Properties();
        properties.put(DATATYPE, name);
        properties.put(DICOM_ADDRESS, "127.0.0.1");
        properties.put(DICOM_PORT, "104");
        properties.put(DICOM_SOCLOSEDELAY, "50");        
        properties.put(DICOM_RELEASETO, "5");
        properties.put(DICOM_REQUESTTO, "5");
        properties.put(DICOM_IDLETO, "60");
        properties.put(DICOM_REAPER, "10");
        properties.put(DICOM_RSPDELAY, "0");
        properties.put(DICOM_PDV1, "0");
        properties.put(DICOM_SNDPDULEN, "16");
        properties.put(DICOM_RCVPDULEN, "16");
        properties.put(DICOM_ASYNC, "0");
        properties.put(DICOM_BIGENDIAN, "0");
        properties.put(DICOM_BUFSIZE, "1");
        properties.put(DICOM_DEFTS, "0");        
        properties.put(DICOM_DEST, "");        
        properties.put(DICOM_NATIVE, "0");  
        properties.put(DICOM_SORCVBUF, "0");
        properties.put(DICOM_SOSNDBUF, "0");
        properties.put(DICOM_TCPDELAY, "1");

        properties.put(DICOM_KEYPW, "");
        properties.put(DICOM_KEYSTORE, "");
        properties.put(DICOM_KEYSTOREPW, "");
        properties.put(DICOM_NOCLIENTAUTH, "1");
        properties.put(DICOM_NOSSL2, "1");
        properties.put(DICOM_TLS, "notls");
        properties.put(DICOM_TRUSTSTORE, "");
        properties.put(DICOM_TRUSTSTOREPW, "");    
        properties.put(DICOM_APPENTITY, "");

        properties.put(DICOM_LOCALADDRESS, "");
        properties.put(DICOM_LOCALPORT, "");        
        properties.put(DICOM_LOCALAPPENTITY, "");        
        
        return properties;
    }    
}
