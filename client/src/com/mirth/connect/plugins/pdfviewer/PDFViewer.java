/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.pdfviewer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.mirth.connect.model.Attachment;
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

    public void viewAttachments(List<String> attachmentIds) {

        try {
            Attachment attachment = parent.mirthClient.getAttachment(attachmentIds.get(0));
            byte[] rawData = attachment.getData();
            byte[] rawPDF = new Base64().decode(rawData);

            File temp = File.createTempFile(attachment.getAttachmentId(), ".pdf");
            temp.deleteOnExit();
            
            OutputStream out = new FileOutputStream(temp);
            out.write(rawPDF);
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
}
