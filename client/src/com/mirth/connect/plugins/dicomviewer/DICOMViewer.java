/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.dicomviewer;

import ij.plugin.DICOM;

import java.awt.Dimension;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.mirth.connect.model.MessageObject;
import com.mirth.connect.plugins.AttachmentViewer;

public class DICOMViewer extends AttachmentViewer {

    public DICOMViewer(String name) {
        super(name);
    }

    public String getViewerType() {
        return "DICOM";
    }

    public boolean handleMultiple() {
        return true;
    }

    public void viewAttachments(List<String> attachmentIds) {
        // do viewing code
        try {
            String messageId = parent.mirthClient.getAttachment(attachmentIds.get(0)).getMessageId();
            MessageObject message = parent.messageBrowser.getMessageObjectById(messageId);
            byte[] rawImage = new Base64().decode(parent.mirthClient.getDICOMMessage(message).getBytes());
            ByteArrayInputStream bis = new ByteArrayInputStream(rawImage);
            DICOM dcm = new DICOM(bis);
            dcm.run(message.getType());
            dcm.show();
            Dimension dlgSize = dcm.getWindow().getSize();
            Dimension frmSize = parent.getSize();
            Point loc = parent.getLocation();

            if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
                dcm.getWindow().setLocationRelativeTo(null);
            } else {
                dcm.getWindow().setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
            }

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
        return "DICOM Viewer";
    }
}
