/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXList;

import com.mirth.connect.client.ui.panels.reference.ReferenceTable;

public class VariableListHandler extends TransferHandler {

    private String prefix, suffix;
    private Map<String, String> staticJsReferences;
    private Map<String, String> staticVelocityReferences;

    public VariableListHandler(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;

        staticVelocityReferences = new HashMap<String, String>();
        staticVelocityReferences.put("Raw Data", "${message.rawData}");
        staticVelocityReferences.put("Transformed Data", "${message.transformedData}");
        staticVelocityReferences.put("Message Type", "${message.type}");
        staticVelocityReferences.put("Message Version", "${message.version}");
        staticVelocityReferences.put("Message Source", "${message.source}");
        staticVelocityReferences.put("Message ID", "${message.messageId}");
        staticVelocityReferences.put("Encoded Data", "${message.encodedData}");
        staticVelocityReferences.put("Timestamp", "${SYSTIME}");
        staticVelocityReferences.put("Unique ID", "${UUID}");
        staticVelocityReferences.put("Date", "${DATE}");
        staticVelocityReferences.put("Original File Name", "${originalFilename}");
        staticVelocityReferences.put("Count", "${COUNT}");
        staticVelocityReferences.put("DICOM Message Raw Data", "${DICOMMESSAGE}");
        staticVelocityReferences.put("Formatted Date", "${date.get('yyyy-M-d H.m.s')}");
        staticVelocityReferences.put("XML Entity Encoder", "${XmlUtil.encode()}");
        staticVelocityReferences.put("XML Pretty Printer", "${XmlUtil.prettyPrint()}");

        staticJsReferences = new HashMap<String, String>();
        staticJsReferences.put("Raw Data", "messageObject.getRawData()");
        staticJsReferences.put("Transformed Data", "messageObject.getTransformedData()");
        staticJsReferences.put("Message Type", "messageObject.getType()");
        staticJsReferences.put("Message Version", "messageObject.getVersion()");
        staticJsReferences.put("Message Source", "messageObject.getSource()");
        staticJsReferences.put("Message ID", "messageObject.getMessageId()");
        staticJsReferences.put("Encoded Data", "messageObject.getEncodedData()");
        staticJsReferences.put("Timestamp", "var dateString = DateUtil.getCurrentDate('yyyyMMddHHmmss');");
        staticJsReferences.put("Unique ID", "var uuid = UUIDGenerator.getUUID();");
        staticJsReferences.put("Date", "var date = DateUtil.getDate('pattern','date');");
        staticJsReferences.put("Count", "var count = 0;\nif(globalMap.get('count') != undefined) {\n\tcount = globalMap.get('count');\n\tcount++;\n\tglobalMap.put('count', count);\n} else {\n\tcount=1;\n\tglobalMap.put('count',count);\n}\n");
        staticJsReferences.put("Original File Name", "$('originalFilename')");
        staticJsReferences.put("DICOM Message Raw Data", "var rawData = DICOMUtil.getDICOMRawData(messageObject);");
        staticJsReferences.put("Message with Attachments", "var rawData = AttachmentUtil.reAttachMessage(messageObject)");
        staticJsReferences.put("Formatted Date", "var dateString = DateUtil.getCurrentDate('yyyy-M-d H.m.s');");
        staticJsReferences.put("XML Entity Encoder", "var encodedMessage = XmlUtil.encode('message');");
        staticJsReferences.put("XML Pretty Printer", "var prettyPrintedMessage = XmlUtil.prettyPrint('message');");
    }

    protected Transferable createTransferable(JComponent c) {
        try {
            String text = "";
            if (c instanceof JXList) {
                JXList list = ((JXList) (c));
                if (list == null) {
                    return null;
                }
                text = (String) list.getSelectedValue();
            } else if (c instanceof ReferenceTable) {
                ReferenceTable reftable = ((ReferenceTable) (c));
                if (reftable == null) {
                    return null;
                }

                int currRow = reftable.getSelectedRow();

                if (currRow >= 0 && currRow < reftable.getRowCount()) {
                    text = (String) reftable.getValueAt(currRow, 0);
                }
            }

            if (text != null) {
                if (prefix.equals("${") && suffix.equals("}")) {
                    if (staticVelocityReferences.get(text) != null) {
                        return new VariableTransferable(staticVelocityReferences.get(text), "", "");
                    }
                } else {
                    if (staticJsReferences.get(text) != null) {
                        return new VariableTransferable(staticJsReferences.get(text), "", "");
                    }
                }
                return new VariableTransferable(text, prefix, suffix);
            }
            return null;
        } catch (ClassCastException cce) {
            return null;
        }
    }

    public int getSourceActions(JComponent c) {
        return COPY;
    }

    public boolean canImport(JComponent c, DataFlavor[] df) {
        return false;
    }
}
