/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.rtfviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;

import org.apache.commons.codec.binary.Base64;

import com.mirth.connect.model.Attachment;
import com.mirth.connect.plugins.AttachmentViewer;

public class RTFViewer extends AttachmentViewer {

    public RTFViewer(String name) {
        super(name);
    }

    @Override
    public String getViewerType() {
        return "RTF";
    }

    @Override
    public boolean handleMultiple() {
        return false;
    }

    @Override
    public void viewAttachments(List<String> attachmentIds) {
        // do viewing code

        Frame frame = new Frame("RTF Viewer");

        frame.setLayout(new BorderLayout());

        try {

            Attachment attachment = parent.mirthClient.getAttachment(attachmentIds.get(0));
            byte[] rawRTF = new Base64().decode(attachment.getData());
            JEditorPane jEditorPane = new JEditorPane("text/rtf", new String(rawRTF));

            if (jEditorPane.getDocument().getLength() == 0) {
                // decoded when it should not have been.  i.e.) the attachment data was not encoded.
                jEditorPane.setText(new String(attachment.getData()));
            }

            jEditorPane.setEditable(false);
            JScrollPane scrollPane = new javax.swing.JScrollPane();
            scrollPane.setViewportView(jEditorPane);
            frame.add(scrollPane);
            frame.addWindowListener(new WindowAdapter() {

                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                }
            });

            frame.setSize(600, 800);

            Dimension dlgSize = frame.getSize();
            Dimension frmSize = parent.getSize();
            Point loc = parent.getLocation();

            if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
                frame.setLocationRelativeTo(null);
            } else {
                frame.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
            }

            frame.setVisible(true);
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
