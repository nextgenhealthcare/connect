/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dicomviewer;

import ij.gui.ImageWindow;
import ij.plugin.DICOM;

import java.awt.Dimension;
import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.commons.io.IOUtils;

import com.mirth.connect.donkey.model.message.ConnectorMessage;
import com.mirth.connect.plugins.AttachmentViewer;

public class DICOMViewer extends AttachmentViewer {

    public DICOMViewer(String name) {
        super(name);
    }

    public boolean handleMultiple() {
        return true;
    }

    public void viewAttachments(String channelId, Long messageId, String attachmentId) {
        // do viewing code
        try {
            ConnectorMessage message = parent.messageBrowser.getSelectedConnectorMessage();
            byte[] rawImage = StringUtils.getBytesUsAscii(parent.mirthClient.getDICOMMessage(message));
            BufferedInputStream bis = new BufferedInputStream(new Base64InputStream(new ByteArrayInputStream(rawImage)));

            /*
             * Attempt to read in at least 128 bytes, this being the offset of "DCIM" in a valid
             * file. This is done so that DicomDecoder won't get stuck in an infinite loop trying to
             * read in a malformed attachment. If 128 bytes weren't successfully read abort the
             * operation.
             */
            bis.mark(128);
            if (bis.read(new byte[128], 0, 128) == 128) {
                bis.reset();
            } else {
                IOUtils.closeQuietly(bis);
                throw new Exception("Attachment is not a valid DICOM file.");
            }

            DICOM dcm = new DICOM(bis);
            // run() is required to create the dicom object. The argument serves multiple purposes. If it is null or empty, it opens a dialog to select a dicom file.
            // Otherwise, if dicom.show() is called, it is the title of the dialog. Since we are showing the dialog here, we pass the string we want to use as the title.
            dcm.run("DICOM Image Viewer");
            dcm.show();

            ImageWindow window = dcm.getWindow();

            if (window != null) {
                Dimension dlgSize = window.getSize();
                Dimension frmSize = parent.getSize();
                Point loc = parent.getLocation();

                if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
                    dcm.getWindow().setLocationRelativeTo(null);
                } else {
                    dcm.getWindow().setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
                }
            } else {
                parent.alertError(parent, "Unable to load DICOM attachment.");
            }
        } catch (Exception e) {
            parent.alertThrowable(parent, e);
        }

    }

    @Override
    public boolean isContentTypeViewable(String contentType) {
        return org.apache.commons.lang.StringUtils.containsIgnoreCase(contentType, "dicom");
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void reset() {}

    @Override
    public String getPluginPointName() {
        return "DICOM Viewer";
    }
}
