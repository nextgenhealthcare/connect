package com.mirth.connect.webadmin.action;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import com.mirth.connect.webadmin.utils.Constants;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;

public class IndexActionBean extends BaseActionBean {
	private String httpsPort = "8443"; 
	private String httpPort = "8080"; 

	private boolean secureHttps;

	@DefaultHandler
	public Resolution init() {
        HttpServletRequest request = getContext().getRequest();

		InputStream mirthPropertiesStream = null;
		mirthPropertiesStream = this.getClass().getResourceAsStream("/mirth.properties");
		
		if(mirthPropertiesStream != null){
			Properties mirthProps = new Properties();
			try {
				mirthProps.load(mirthPropertiesStream);
				httpsPort = mirthProps.getProperty("https.port", "8443");
				httpPort = mirthProps.getProperty("http.port", "8080");

			} catch (IOException e) {
				// ignore
			}
		}		
		// Check if http or https
		secureHttps = request.isSecure();

		return new ForwardResolution(Constants.INDEX_JSP);
	}

	public String getHttpsPort() {
		return httpsPort;
	}

	public void setHttpsPort(String httpsPort) {
		this.httpsPort = httpsPort;
	}

	public boolean isSecureHttps() {
		return secureHttps;
	}

	public void setSecureHttps(boolean secureHttps) {
		this.secureHttps = secureHttps;
	}

	public String getHttpPort() {
		return httpPort;
	}

	public void setHttpPort(String httpPort) {
		this.httpPort = httpPort;
	}
}
