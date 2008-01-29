package com.webreach.mirth.plugins.extensionmanager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.UUID;
import java.util.zip.ZipFile;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.webreach.mirth.client.ui.PlatformUI;

public class ExtensionUtil
{
    public String getStringFromURL(String urlString)
    {
        try
        {
            StringBuffer buffer = new StringBuffer();
            // Create a URL for the desired page
            URL url = new URL(urlString);
            
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str;
            while ((str = in.readLine()) != null)
            {
                buffer.append(str);
                buffer.append("\r\n");
            }
            in.close();
            return buffer.toString();
        }
        catch (MalformedURLException e)
        {
        }
        catch (IOException e)
        {
        }
        return "";
    }
    public String getDynamicURL(String url, String pluginVersion, String name) {
		return url.replaceAll("\\$\\{mirthVersion\\}", PlatformUI.SERVER_VERSION).replaceAll("\\$\\{version\\}", pluginVersion).replaceAll("\\$\\{name\\}", URLEncoder.encode(name)).replaceAll("\\$\\{serverid\\}", PlatformUI.SERVER_ID);
	}
    public String getDynamicURL(String url, String pluginVersion, String name, String id) {
		return url.replaceAll("\\$\\{mirthVersion\\}", PlatformUI.SERVER_VERSION).replaceAll("\\$\\{version\\}", pluginVersion).replaceAll("\\$\\{name\\}", URLEncoder.encode(name)).replaceAll("\\$\\{serverid\\}", PlatformUI.SERVER_ID).replaceAll("\\$\\{id\\}", id);
	}
    public byte[] downloadFile(String address, JLabel statusLabel, JProgressBar progressBar)
    {
        ByteArrayOutputStream out = null;
        URLConnection conn = null;
        InputStream  in = null;
        NumberFormat formatter = new DecimalFormat("#.00");
        try
        {
            URL url = new URL(address);
            out = new ByteArrayOutputStream(1024);
            conn = url.openConnection();
            int length = conn.getContentLength();
            in = conn.getInputStream();
            if (length != -1 )
            {
                progressBar.setMaximum(length);
            }
            else
            {
                progressBar.setIndeterminate(true);
            }
            byte[] buffer = new byte[1024];
            int inread;
            float outwrite = 0;
            while ((inread = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, inread);
                outwrite += inread;
                
                if (length != -1)
                {
                    progressBar.setValue(progressBar.getValue() + inread);
                    statusLabel.setText("Downloaded: " + formatter.format(outwrite/1000) + " Kbytes/" + formatter.format(length/1000) + " Kbytes");
                }
                else
                {
                    statusLabel.setText("Downloaded: " + formatter.format(outwrite/1000) + " Kbytes");
                }
            }
            if (length != -1)
            {
                progressBar.setValue(0);
            }
            else
            {
                progressBar.setIndeterminate(false);
            }
            return out.toByteArray();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException ioe)
            {
            }
        }
        return null;
    }
    
    public File downloadFileToDisk(String address, JLabel statusLabel, JProgressBar progressBar)
    {
        FileOutputStream out = null;
        URLConnection conn = null;
        InputStream  in = null;
        NumberFormat formatter = new DecimalFormat("#.00");
        String uniqueId = UUID.randomUUID().toString();
		ZipFile zipFile = null;
        try
        {
			File file = File.createTempFile(uniqueId, ".zip");
            URL url = new URL(address);
            out = new FileOutputStream(file);
            conn = url.openConnection();
            int length = conn.getContentLength();
            in = conn.getInputStream();
            if (length != -1 )
            {
                progressBar.setMaximum(length);
            }
            else
            {
                progressBar.setIndeterminate(true);
            }
            byte[] buffer = new byte[1024];
            int inread;
            float outwrite = 0;
            while ((inread = in.read(buffer)) != -1)
            {
                out.write(buffer, 0, inread);
                outwrite += inread;
                
                if (length != -1)
                {
                    progressBar.setValue(progressBar.getValue() + inread);
                    statusLabel.setText("Downloaded: " + formatter.format(outwrite/1000) + " Kbytes/" + formatter.format(length/1000) + " Kbytes");
                }
                else
                {
                    statusLabel.setText("Downloaded: " + formatter.format(outwrite/1000) + " Kbytes");
                }
            }
            if (length != -1)
            {
                progressBar.setValue(0);
            }
            else
            {
                progressBar.setIndeterminate(false);
            }
            return file;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException ioe)
            {
            }
        }
        return null;
    }
}
