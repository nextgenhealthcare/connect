/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import java.awt.Component;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.mirth.connect.client.ui.DateCellRenderer;
import com.mirth.connect.client.ui.NumberCellRenderer;
import com.mirth.connect.client.ui.components.MirthTreeTable;

public class MessageBrowserTableColumnFactory extends ColumnFactory {
    @Override
    public TableColumnExt createAndConfigureTableColumn(TableModel model, int index) {
        TableColumnExt column = super.createAndConfigureTableColumn(model, index);
        TableCellRenderer renderer;
        DateCellRenderer dateCellRenderer;

        switch (index) {
            case MessageBrowser.ID_COLUMN: // Message ID: Needs to be able to grow since it is a long
                renderer = new NumberCellRenderer();
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                column.setToolTipText("<html><body>The overall Id of the message.</body></html>");
                break;

            case MessageBrowser.CONNECTOR_COLUMN: // Connector Name
                renderer = new DefaultTableCellRenderer();
                column.setMinWidth(90);
                column.setToolTipText("<html><body>The historic name of the connector at the time the message was processed.</body></html>");
                break;

            case MessageBrowser.STATUS_COLUMN: // Status
                renderer = new DefaultTableCellRenderer();
                column.setMaxWidth(85);
                column.setMinWidth(85);
                column.setToolTipText("<html><body>The message status after being run through the connector.</body></html>");
                break;

            case MessageBrowser.ORIGINAL_RECEIVED_DATE_COLUMN: // Received Date
                dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = dateCellRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                column.setToolTipText("<html><body>The date the original message was received. This value is<br>not updated when the message is reprocessed.</body></html>");
                break;

            case MessageBrowser.RECEIVED_DATE_COLUMN: // Received Date
                dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = dateCellRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                column.setToolTipText("<html><body>The date the message began processing through the connector.</body></html>");
                break;

            case MessageBrowser.SEND_ATTEMPTS_COLUMN: // Send Attempts:
                renderer = new NumberCellRenderer();
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                column.setToolTipText("<html><body>Source Connector: this is the number of times the connector<br>attempted to send the response back to the point of origin.<br>Destination Connector: this is the number of times the connector<br>attempted to send the message to its recipient.</body></html>");
                break;

            case MessageBrowser.SEND_DATE_COLUMN: // Send Date
                dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = dateCellRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                column.setToolTipText("<html><body>Source Connector: this column is not used.<br>Destination Connector: this is the date right before the most recent send attempt.</body></html>");
                break;

            case MessageBrowser.RESPONSE_DATE_COLUMN: // Response Date
                dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = dateCellRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                column.setToolTipText("<html><body>Source Connector: this is the date right before the connector<br>attempted to send the response back to the point of origin.<br>Destination Connector: this is the date immediately after the server<br>receives a response from the connector, which may be empty.</body></html>");
                break;

            case MessageBrowser.ERRORS_COLUMN: // Error
                renderer = new DefaultTableCellRenderer();
                column.setMaxWidth(85);
                column.setMinWidth(85);
                column.setToolTipText("<html><body>Indicates whether an error exists for this message. It is possible for<br>a message to have errors even if the message status is not ERROR.</body></html>");
                break;

            case MessageBrowser.SERVER_ID_COLUMN: // Server Id
                column.setMaxWidth(210);
                column.setMinWidth(210);
                renderer = new DefaultTableCellRenderer();
                column.setToolTipText("<html><body>The Id of the server where the message was processed.</body></html>");
                break;

            case MessageBrowser.IMPORT_ID_COLUMN: // Import ID: Ditto
                renderer = new NumberCellRenderer();
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                column.setToolTipText("<html><body>If a message was imported, this column indicates the original<br>Message Id of the imported message.</body></html>");
                break;

            default:
                renderer = new DefaultTableCellRenderer();
                break;

        }
        column.setCellRenderer(new ItalicCellRenderer(renderer));
        return column;
    }

    private class ItalicCellRenderer implements TableCellRenderer {
        private TableCellRenderer delegateRenderer;

        public ItalicCellRenderer(TableCellRenderer renderer) {
            this.delegateRenderer = renderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value != null && component instanceof JLabel) {
                JLabel label = (JLabel) component;
                MessageBrowserTableNode messageNode = (MessageBrowserTableNode) ((MirthTreeTable) table).getPathForRow(row).getLastPathComponent();
                if (!messageNode.isProcessed()) {
                    label.setText("<html><i><font color='gray'>" + label.getText() + "</font></i></html>");
                }
            }
            return component;
        }

    }
}
