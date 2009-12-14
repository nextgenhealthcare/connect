package com.webreach.mirth.connectors.ws;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import javax.wsdl.xml.WSDLLocator;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;

public class AuthWsdlLocator implements WSDLLocator {
    
    private Logger logger = Logger.getLogger(this.getClass());
    
    private String wsdlUrl;
    private String username;
    private String password;
    private String lastImportUri;
    
    public AuthWsdlLocator(String wsdlUrl, String username, String password) {
        this.wsdlUrl = wsdlUrl;
        this.username = username;
        this.password = password;        
    }
    
    public void close() {
    }

    public InputSource getBaseInputSource() {
        InputSource inputSource = new InputSource();
        try {
            URI baseUri = null;
            
            try {
                baseUri = new URI(wsdlUrl);
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
            
            InputStream inputStream = WebServiceUtil.getUrlContents(baseUri, null, username, password);
            inputSource.setByteStream(inputStream);
        } catch (Exception e) {
            logger.error(e);
        }
        
        return inputSource;
    }

    public String getBaseURI() {
        return wsdlUrl;
    }

    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        InputSource inputSource = new InputSource();
        try {
            URI importUri = null;
            URI parentUri = null;
            
            try {
                importUri = new URI(importLocation);
                parentUri = new URI(parentLocation);
            } catch (URISyntaxException ex) {
                ex.printStackTrace();
            }
            
            InputStream inputStream = WebServiceUtil.getUrlContents(importUri, parentUri, username, password);
            lastImportUri = importLocation;
            inputSource.setByteStream(inputStream);
        } catch (Exception e) {
            logger.error(e);
        }
        
        return inputSource;
    }

    public String getLatestImportURI() {
        return lastImportUri;
    }

}
