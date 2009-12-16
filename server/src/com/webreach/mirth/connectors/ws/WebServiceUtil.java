package com.webreach.mirth.connectors.ws;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;

public class WebServiceUtil {
    public static URL getWsdlUrl(String wsdlUrl, String username, String password) throws Exception {
        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(wsdlUrl);

        int status = client.executeMethod(method);
        if ((status == HttpStatus.SC_UNAUTHORIZED) && (username != null) && (password != null)) {
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            status = client.executeMethod(method);

            if (status == HttpStatus.SC_OK) {
                String wsdl = method.getResponseBodyAsString();
                File tempFile = File.createTempFile("WebServiceSender", ".wsdl");
                tempFile.deleteOnExit();

                FileUtils.writeStringToFile(tempFile, wsdl);

                return tempFile.toURI().toURL();
            }
        }

        return new URL(wsdlUrl);
    }

    public static InputStream getUrlContents(URI uri, URI parentUri, String username, String password) throws Exception {
        URI baseUri = null;

        if (!uri.isAbsolute()) {
            baseUri = parentUri.resolve(uri);
        } else {
            baseUri = uri;
        }

        HttpClient client = new HttpClient();
        HttpMethod method = new GetMethod(baseUri.toString());

        int status = client.executeMethod(method);
        if ((status == HttpStatus.SC_UNAUTHORIZED) && (username != null) && (password != null)) {
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            status = client.executeMethod(method);

            if (status == HttpStatus.SC_OK) {
                return method.getResponseBodyAsStream();
            }
        }

        throw new Exception("Could not load url contents for url: " + baseUri.toString());
    }
}
