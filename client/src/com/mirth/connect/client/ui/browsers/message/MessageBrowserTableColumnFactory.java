/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.browsers.message;

import java.awt.Component;
import java.text.SimpleDateFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.mirth.connect.client.ui.DateCellRenderer;
import com.mirth.connect.client.ui.NumberCellRenderer;
import com.mirth.connect.client.ui.components.MirthTreeTable;
import com.mirth.connect.donkey.model.channel.MetaDataColumnType;

public class MessageBrowserTableColumnFactory extends ColumnFactory {
    @Override
    public TableColumnExt createAndConfigureTableColumn(TableModel model, int index) {
        TableColumnExt column = super.createAndConfigureTableColumn(model, index);
        TableCellRenderer renderer;
        DateCellRenderer dateCellRenderer;

        switch (index) {
            case MessageBrowser.ID_COLUMN: // Message ID: Needs to be able to grow since it is a long
                renderer = new MessageBrowserNumberCellRenderer();
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                column.setToolTipText("<html><body>The message id.</body></html>");
                break;

            case MessageBrowser.CONNECTOR_COLUMN: // Connector Name
                renderer = new MessageBrowserTextCellRenderer();
                column.setMinWidth(90);
                column.setToolTipText("<html><body>The historic name of the connector at the time the message was processed.</body></html>");
                break;

            case MessageBrowser.STATUS_COLUMN: // Status
                renderer = new MessageBrowserTextCellRenderer();
                column.setMaxWidth(85);
                column.setMinWidth(85);
                column.setToolTipText("<html><body>The message status after being processed by the connector.</body></html>");
                break;

            case MessageBrowser.ORIGINAL_RECEIVED_DATE_COLUMN: // Received Date
                dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = dateCellRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                column.setToolTipText("<html><body>The date and time the original message was received. This value is<br>not updated when the message is reprocessed.</body></html>");
                break;

            case MessageBrowser.RECEIVED_DATE_COLUMN: // Received Date
                dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = dateCellRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                column.setToolTipText("<html><body>The date and time the message began processing through the connector.</body></html>");
                break;

            case MessageBrowser.SEND_ATTEMPTS_COLUMN: // Send Attempts:
                renderer = new MessageBrowserNumberCellRenderer();
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                column.setToolTipText("<html><body>Source Connector: The number of times the connector<br>attempted to send the response back to the point of origin.<br>Destination Connector: The number of times the connector<br>attempted to send the message to its recipient.</body></html>");
                break;

            case MessageBrowser.SEND_DATE_COLUMN: // Send Date
                dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = dateCellRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                column.setToolTipText("<html><body>Source Connector: N/A<br>Destination Connector: The date and time immediately before the most recent send attempt.</body></html>");
                break;

            case MessageBrowser.RESPONSE_DATE_COLUMN: // Response Date
                dateCellRenderer = new DateCellRenderer();
                dateCellRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = dateCellRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                column.setToolTipText("<html><body>Source Connector: The date and time immediately before the connector<br>attempted to send the response back to the point of origin.<br>Destination Connector: The date and time immediately after the server<br>receives a response from the connector, which may be empty.</body></html>");
                break;

            case MessageBrowser.ERRORS_COLUMN: // Error
                renderer = new MessageBrowserTextCellRenderer();
                column.setMaxWidth(85);
                column.setMinWidth(85);
                column.setToolTipText("<html><body>Indicates whether an error exists for this message. It is possible for<br>a message to have errors even if the message status is not ERROR.</body></html>");
                break;

            case MessageBrowser.SERVER_ID_COLUMN: // Server Id
                column.setMaxWidth(210);
                column.setMinWidth(210);
                renderer = new MessageBrowserTextCellRenderer();
                column.setToolTipText("<html><body>The id of the server that processed the message through the connector.</body></html>");
                break;

            case MessageBrowser.ORIGINAL_SERVER_ID_COLUMN: // Original Server Id
                column.setMaxWidth(210);
                column.setMinWidth(210);
                renderer = new MessageBrowserTextCellRenderer();
                column.setToolTipText("<html><body>The id of the server that received the message.</body></html>");
                break;

            case MessageBrowser.ORIGINAL_ID_COLUMN: // Original ID:
                renderer = new MessageBrowserTextCellRenderer(SwingConstants.RIGHT);
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                column.setToolTipText("<html><body>The original message id of a reprocessed message. This value<br>only exists for reprocessed messages.</body></html>");
                break;

            case MessageBrowser.IMPORT_ID_COLUMN: // Import ID:
                renderer = new MessageBrowserTextCellRenderer(SwingConstants.RIGHT);
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                column.setToolTipText("<html><body>The original message id of an imported message. This value<br>only exists for imported messages.</body></html>");
                break;

            case MessageBrowser.IMPORT_CHANNEL_ID_COLUMN: // Server Id
                column.setMaxWidth(210);
                column.setMinWidth(210);
                renderer = new MessageBrowserTextCellRenderer();
                column.setToolTipText("<html><body>The original channel id of an imported message. This value<br>only exists for messages imported from a different channel.</body></html>");
                break;

            default:
                renderer = new MessageBrowserTextCellRenderer();
                break;

        }
        column.setCellRenderer(new MessageBrowserItalicCellRenderer(renderer));
        return column;
    }

    public void configureCustomColumn(TableColumnExt column, MetaDataColumnType columnType) {
        TableCellRenderer renderer;

        switch (columnType) {
            case NUMBER:
                renderer = new MessageBrowserNumberCellRenderer();
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                break;

            case BOOLEAN:
                renderer = new MessageBrowserTextCellRenderer();
                column.setMaxWidth(500);
                column.setMinWidth(90);
                column.setPreferredWidth(90);
                break;

            case TIMESTAMP:
                DateCellRenderer timestampRenderer = new DateCellRenderer();
                timestampRenderer.setDateFormat(new SimpleDateFormat(MessageBrowser.DATE_FORMAT));
                renderer = timestampRenderer;
                column.setMaxWidth(140);
                column.setMinWidth(140);
                break;

            default:
                renderer = new MessageBrowserTextCellRenderer();
                break;
        }

        column.setCellRenderer(new MessageBrowserItalicCellRenderer(renderer));
    }

    private class MessageBrowserNumberCellRenderer extends NumberCellRenderer {
        private int alignment;

        public MessageBrowserNumberCellRenderer() {
            this(RIGHT);
        }

        public MessageBrowserNumberCellRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value == null) {
                label.setHorizontalAlignment(CENTER);
                label.setText("--");
            } else {
                label.setHorizontalAlignment(alignment);
            }

            return label;
        }
    }

    private class MessageBrowserTextCellRenderer extends DefaultTableCellRenderer {
        private int alignment;

        public MessageBrowserTextCellRenderer() {
            this(LEFT);
        }

        public MessageBrowserTextCellRenderer(int alignment) {
            this.alignment = alignment;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (value == null) {
                label.setHorizontalAlignment(CENTER);
                label.setText("--");
            } else {
                label.setHorizontalAlignment(alignment);
            }

            return label;
        }
    }

    private class MessageBrowserItalicCellRenderer implements TableCellRenderer {
        private TableCellRenderer delegateRenderer;

        public MessageBrowserItalicCellRenderer(TableCellRenderer renderer) {
            this.delegateRenderer = renderer;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = delegateRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            if (component instanceof JLabel) {
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
