/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.extensionmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JProgressBar;

import com.mirth.connect.client.ui.PlatformUI;

public class ExtensionUtil {

    public String getURLContents(String address) {
        StringBuilder builder = new StringBuilder();

        try {
            URL url = new URL(address);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String str = null;

            while ((str = in.readLine()) != null) {
                builder.append(str);
                builder.append("\r\n");
            }

            in.close();
        } catch (Exception e) {
            // could not load page contents
        }

        return builder.toString();
    }

    public String getDynamicURL(String url, String pluginVersion, String name) {
        return url.replaceAll("\\$\\{mirthVersion\\}", PlatformUI.SERVER_VERSION).replaceAll("\\$\\{version\\}", pluginVersion).replaceAll("\\$\\{name\\}", URLEncoder.encode(name)).replaceAll("\\$\\{serverid\\}", PlatformUI.SERVER_ID);
    }

    public String getDynamicURL(String url, String pluginVersion, String name, String id) {
        return url.replaceAll("\\$\\{mirthVersion\\}", PlatformUI.SERVER_VERSION).replaceAll("\\$\\{version\\}", pluginVersion).replaceAll("\\$\\{name\\}", URLEncoder.encode(name)).replaceAll("\\$\\{serverid\\}", PlatformUI.SERVER_ID).replaceAll("\\$\\{id\\}", id);
    }

    public File downloadFileToDisk(String address, JLabel statusLabel, JProgressBar progressBar) {
        InputStream in = null;
        FileOutputStream out = null;
        NumberFormat formatter = new DecimalFormat("#.00");

        try {
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".zip");
            URL url = new URL(address);
            out = new FileOutputStream(tempFile);
            URLConnection connection = url.openConnection();
            int length = connection.getContentLength();
            in = connection.getInputStream();

            if (length != -1) {
                progressBar.setMaximum(length);
            } else {
                progressBar.setIndeterminate(true);
            }

            byte[] buffer = new byte[1024];
            int input;
            float output = 0;

            while ((input = in.read(buffer)) != -1) {
                out.write(buffer, 0, input);
                output += input;

                if (length != -1) {
                    progressBar.setValue(progressBar.getValue() + input);
                    statusLabel.setText("Downloaded: " + formatter.format(output / 1000) + " Kbytes/" + formatter.format(length / 1000) + " Kbytes");
                } else {
                    statusLabel.setText("Downloaded: " + formatter.format(output / 1000) + " Kbytes");
                }
            }

            if (length != -1) {
                progressBar.setValue(0);
            } else {
                progressBar.setIndeterminate(false);
            }

            return tempFile;
        } catch (Exception e) {
            statusLabel.setText("Could not download file.");
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
