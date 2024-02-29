/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui.components;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;

import org.apache.commons.io.FilenameUtils;

import com.mirth.connect.client.ui.AbstractChannelTableNode;
import com.mirth.connect.model.Channel;

public abstract class ChannelTableTransferHandler extends TransferHandler {

    @Override
    protected Transferable createTransferable(JComponent c) {
        MirthTreeTable channelTable = (MirthTreeTable) c;

        int rows[] = channelTable.getSelectedModelRows();

        // Don't put anything on the clipboard if no rows are selected
        if (rows.length == 0) {
            return null;
        }

        List<Object> list = new ArrayList<Object>();

        for (int row : rows) {
            AbstractChannelTableNode node = (AbstractChannelTableNode) channelTable.getPathForRow(row).getLastPathComponent();

            if (node.isGroupNode()) {
                list.add(node.getGroupStatus().getGroup());
            } else {
                list.add(node.getChannelStatus().getChannel());
            }
        }

        if (list.isEmpty()) {
            return null;
        }

        return new ChannelTableTransferable(list);
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (canImport(support)) {
            try {
                if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    List<File> fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    final boolean showAlerts = (fileList.size() == 1);

                    // Submit each import task to a single-threaded executor so they always import one at a time.
                    Executor executor = Executors.newSingleThreadExecutor();

                    for (final File file : fileList) {
                        if (FilenameUtils.isExtension(file.getName(), "xml")) {
                            executor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    importFile(file, showAlerts);
                                }
                            });
                        }
                    }

                    return true;
                } else if (support.isDataFlavorSupported(ChannelTableTransferable.CHANNEL_DATA_FLAVOR)) {
                    List<Object> list = (List<Object>) support.getTransferable().getTransferData(ChannelTableTransferable.CHANNEL_DATA_FLAVOR);

                    List<Channel> channels = new ArrayList<Channel>();
                    for (Object obj : list) {
                        if (obj instanceof Channel) {
                            channels.add((Channel) obj);
                        } else {
                            return false;
                        }
                    }

                    if (support.getDropLocation() instanceof JTable.DropLocation) {
                        return moveChannels(channels, ((JTable.DropLocation) support.getDropLocation()).getRow());
                    }
                }
            } catch (Exception e) {
                // Let it return false
            }
        }

        return false;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        try {
            if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                List<File> fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                for (File file : fileList) {
                    if (!FilenameUtils.isExtension(file.getName(), "xml")) {
                        return false;
                    }
                }

                return true;
            } else if (support.isDataFlavorSupported(ChannelTableTransferable.CHANNEL_DATA_FLAVOR)) {
                List<Object> list = (List<Object>) support.getTransferable().getTransferData(ChannelTableTransferable.CHANNEL_DATA_FLAVOR);

                List<Channel> channels = new ArrayList<Channel>();
                for (Object obj : list) {
                    if (obj instanceof Channel) {
                        channels.add((Channel) obj);
                    } else {
                        return false;
                    }
                }

                if (support.getDropLocation() instanceof JTable.DropLocation) {
                    return canMoveChannels(channels, ((JTable.DropLocation) support.getDropLocation()).getRow());
                }
            }
        } catch (Exception e) {
            // Return true anyway until this bug is fixed:
            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6759788
            return true;
        }

        return false;
    }

    public abstract void importFile(File file, boolean showAlerts);

    public abstract boolean canMoveChannels(List<Channel> channels, int row);

    public abstract boolean moveChannels(List<Channel> channels, int row);
}