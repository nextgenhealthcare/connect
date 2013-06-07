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
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXList;

import com.mirth.connect.client.ui.panels.reference.ReferenceTable;
import com.mirth.connect.model.Connector;

public class VariableListHandler extends TransferHandler {

    public enum TransferMode {
        RAW("", ""), VELOCITY("${", "}"), JAVASCRIPT("$('", "')");

        private String prefix;
        private String suffix;

        private TransferMode(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getSuffix() {
            return suffix;
        }
    };

    private TransferMode transferMode;
    private Map<String, Integer> metaDataMap = new HashMap<String, Integer>();
    private static Map<String, String> staticJsReferences;
    private static Map<String, String> staticVelocityReferences;

    static {
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

        // these are used in DataPrunerPanel
        staticVelocityReferences.put("Server ID", "${message.serverId}");
        staticVelocityReferences.put("Channel ID", "${message.channelId}");
        staticVelocityReferences.put("Formatted Message Date", "${date.format('yyyy-MM-dd',$message.receivedDate)}");
        staticVelocityReferences.put("Formatted Current Date", "${date.get('yyyy-MM-dd')}");

        staticJsReferences = new HashMap<String, String>();
        staticJsReferences.put("Raw Data", "connectorMessage.getRawData()");
        staticJsReferences.put("Transformed Data", "connectorMessage.getTransformedData()");
        staticJsReferences.put("Message Type", "connectorMessage.getType()");
        staticJsReferences.put("Message Version", "connectorMessage.getVersion()");
        staticJsReferences.put("Message Source", "connectorMessage.getSource()");
        staticJsReferences.put("Message ID", "connectorMessage.getMessageId()");
        staticJsReferences.put("Encoded Data", "connectorMessage.getEncodedData()");
        staticJsReferences.put("Timestamp", "var dateString = DateUtil.getCurrentDate('yyyyMMddHHmmss');");
        staticJsReferences.put("Unique ID", "var uuid = UUIDGenerator.getUUID();");
        staticJsReferences.put("Date", "var date = DateUtil.getDate('pattern','date');");
        staticJsReferences.put("Original File Name", "$('originalFilename')");
        staticJsReferences.put("DICOM Message Raw Data", "var rawData = DICOMUtil.getDICOMRawData(connectorMessage);");
        staticJsReferences.put("Message with Attachments", "var rawData = AttachmentUtil.reAttachMessage(connectorMessage)");
        staticJsReferences.put("Formatted Date", "var dateString = DateUtil.getCurrentDate('yyyy-M-d H.m.s');");
        staticJsReferences.put("XML Entity Encoder", "var encodedMessage = XmlUtil.encode('message');");
        staticJsReferences.put("XML Pretty Printer", "var prettyPrintedMessage = XmlUtil.prettyPrint('message');");
    }

    public VariableListHandler(TransferMode transferMode) {
        this(transferMode, null);
    }

    public VariableListHandler(TransferMode transferMode, List<Connector> connectors) {
        this.transferMode = transferMode;
        populateConnectors(connectors);
    }

    public TransferMode getTransferMode() {
        return transferMode;
    }

    public void setTransferMode(TransferMode transferMode) {
        this.transferMode = transferMode;
    }

    public void populateConnectors(List<Connector> connectors) {
        if (connectors != null) {
            for (Connector connector : connectors) {
                metaDataMap.put(connector.getName(), connector.getMetaDataId());
            }
        }
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
                if (transferMode == TransferMode.VELOCITY && staticVelocityReferences.containsKey(text)) {
                    return new VariableTransferable(staticVelocityReferences.get(text), TransferMode.RAW, metaDataMap);
                } else if (transferMode == TransferMode.JAVASCRIPT && staticJsReferences.containsKey(text)) {
                    return new VariableTransferable(staticJsReferences.get(text), TransferMode.RAW, metaDataMap);
                }

                return new VariableTransferable(text, transferMode, metaDataMap);
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
