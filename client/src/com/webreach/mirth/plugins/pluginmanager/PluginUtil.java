package com.webreach.mirth.plugins.pluginmanager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JLabel;

public class PluginUtil {
	  public String getStringFromURL(String urlString){
	        try {
	            StringBuffer buffer = new StringBuffer();
	            // Create a URL for the desired page
	            URL url = new URL(urlString);

	            // Read all the text returned by the server
	            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
	            String str;
	            while ((str = in.readLine()) != null) {
	                buffer.append(str);
	                buffer.append("\r\n");
	            }
	            in.close();
	            return buffer.toString();
	        } catch (MalformedURLException e) {
	        } catch (IOException e) {
	        }
	        return "";
	    }
	  
	  public byte[] downloadFile(String address, JLabel statusLabel) {
	    	ByteArrayOutputStream out = null;
			URLConnection conn = null;
			InputStream  in = null;
			NumberFormat formatter = new DecimalFormat("#.00");
			try {
				URL url = new URL(address);
				out = new ByteArrayOutputStream(1024);
				conn = url.openConnection();
				in = conn.getInputStream();
				byte[] buffer = new byte[1024];
				int inread;
				float outwrite = 0;
				while ((inread = in.read(buffer)) != -1) {
					out.write(buffer, 0, inread);
					outwrite += inread;
					statusLabel.setText("Downloaded: " + formatter.format(outwrite/1000) + " Kbytes");
				}
				return out.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (in != null) {
						in.close();
					}
					if (out != null) {
						out.close();
					}
				} catch (IOException ioe) {
				}
			}
			return null;
		}
}
