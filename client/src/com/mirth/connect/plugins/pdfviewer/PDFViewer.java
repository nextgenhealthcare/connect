/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.pdfviewer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.io.IOUtils;

import com.mirth.connect.donkey.model.message.attachment.Attachment;
import com.mirth.connect.plugins.AttachmentViewer;

public class PDFViewer extends AttachmentViewer {

    public PDFViewer(String name) {
        super(name);
    }

    public String getViewerType() {
        return "PDF";
    }

    public boolean handleMultiple() {
        return false;
    }

    public void viewAttachments(List<String> attachmentIds, String channelId) {

        try {
            Attachment attachment = parent.mirthClient.getAttachment(channelId, attachmentIds.get(0));
            byte[] rawData = attachment.getContent();
            Base64InputStream in = new Base64InputStream(new ByteArrayInputStream(rawData));

            File temp = File.createTempFile(attachment.getId(), ".pdf");
            temp.deleteOnExit();
            
            OutputStream out = new FileOutputStream(temp);
            IOUtils.copy(in, out);
            out.close();
            
            new MirthPDFViewer(true, temp);
            
        } catch (Exception e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }

    @Override
    public String getPluginPointName() {
        return "PDF Viewer";
    }
}
