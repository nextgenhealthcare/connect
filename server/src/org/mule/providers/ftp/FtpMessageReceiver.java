/* 
 * $Header: /home/projects/mule/scm/mule/providers/ftp/src/java/org/mule/providers/ftp/FtpMessageReceiver.java,v 1.10 2005/09/27 16:21:40 aperepel Exp $
 * $Revision: 1.10 $
 * $Date: 2005/09/27 16:21:40 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 */
package org.mule.providers.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.mule.impl.MuleMessage;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOMessage;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.endpoint.UMOEndpointURI;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;

import javax.resource.spi.work.Work;
import java.io.ByteArrayOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:gnt@codehaus.org">Guillaume Nodet</a>
 * @version $Revision: 1.10 $
 */
public class FtpMessageReceiver extends PollingMessageReceiver {

    protected Set currentFiles = Collections.synchronizedSet(new HashSet());

    protected FtpConnector connector;

    private FilenameFilter filenameFilter = null;

    public FtpMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, Long frequency)
            throws InitialisationException {
        super(connector, component, endpoint, frequency);
        this.connector = (FtpConnector) connector;
        if (endpoint.getFilter() instanceof FilenameFilter) {
            filenameFilter = (FilenameFilter) endpoint.getFilter();
        }
    }

    public void poll() throws Exception {
        FTPFile[] files = listFiles();
        for (int i = 0; i < files.length; i++) {
            final FTPFile file = files[i];
            if (!currentFiles.contains(file.getName())) {
                getWorkManager().scheduleWork(new Work() {
                    public void run() {
                        try {
                            currentFiles.add(file.getName());
                            processFile(file);
                        } catch (Exception e) {
                            connector.handleException(e);
                        } finally {
                            currentFiles.remove(file.getName());
                        }
                    }

                    public void release() {
                    }
                });
            }
        }
    }

    protected FTPFile[] listFiles() throws Exception {
        FTPClient client = null;
        UMOEndpointURI uri = endpoint.getEndpointURI();
        try {
            client = connector.getFtp(uri);
            if (!client.changeWorkingDirectory(uri.getPath())) {
                throw new IOException("Ftp error: " +
                        client.getReplyCode());
            }
            FTPFile[] files = client.listFiles();
            if (!FTPReply.isPositiveCompletion(client.getReplyCode())) {
                throw new IOException("Ftp error: " +
                        client.getReplyCode());
            }
            if (files == null || files.length == 0) {
                return files;
            }
            List v = new ArrayList();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                       if (filenameFilter == null ||
                           filenameFilter.accept(null, files[i].getName())) {
                        v.add(files[i]);
                    }
                }
            }
            return (FTPFile[]) v.toArray(new FTPFile[v.size()]);

        } finally {
            connector.releaseFtp(uri, client);
        }
    }

    protected void processFile(FTPFile file) throws Exception {
        FTPClient client = null;
        UMOEndpointURI uri = endpoint.getEndpointURI();
        try {
            client = connector.getFtp(uri);
            if (!client.changeWorkingDirectory(endpoint.getEndpointURI().getPath())) {
                throw new IOException("Ftp error: " + client.getReplyCode());
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (!client.retrieveFile(file.getName(), baos)) {
                throw new IOException("Ftp error: " + client.getReplyCode());
            }
            UMOMessage message = new MuleMessage(connector.getMessageAdapter(baos.toByteArray()));
            message.setProperty(FtpConnector.PROPERTY_FILENAME, file.getName());
            routeMessage(message);
            if (!client.deleteFile(file.getName())) {
                throw new IOException("Ftp error: " + client.getReplyCode());
            }
        } finally {
            connector.releaseFtp(uri, client);
        }
    }

    public void doConnect() throws Exception {
        FTPClient client = connector.getFtp(getEndpointURI());
        connector.releaseFtp(getEndpointURI(), client);
    }

    public void doDisconnect() throws Exception {

    }

}
