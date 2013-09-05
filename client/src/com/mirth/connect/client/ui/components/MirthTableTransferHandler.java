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
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public abstract class MirthTableTransferHandler extends TransferHandler {

    private int primaryColumnIndex;
    private int secondaryColumnIndex;

    public MirthTableTransferHandler(int primaryColumnIndex, int secondaryColumnIndex) {
        this.primaryColumnIndex = primaryColumnIndex;
        this.secondaryColumnIndex = secondaryColumnIndex;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        MirthTable table = (MirthTable) c;
        int[] rows = table.getSelectedModelRows();

        // Don't put anything on the clipboard if no rows are selected
        if (rows.length == 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < rows.length; i++) {
            builder.append(table.getModel().getValueAt(rows[i], primaryColumnIndex));

            String secondaryValue = String.valueOf(table.getModel().getValueAt(rows[i], secondaryColumnIndex));
            if (StringUtils.isNotBlank(secondaryValue)) {
                builder.append(" (");
                builder.append(table.getModel().getValueAt(rows[i], secondaryColumnIndex));
                builder.append(")");
            }

            if (i != rows.length - 1) {
                builder.append("\n");
            }
        }

        return new StringSelection(builder.toString());
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferSupport support) {
        if (canImport(support)) {
            try {
                List<File> fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                boolean showAlerts = (fileList.size() == 1);

                for (File file : fileList) {
                    if (FilenameUtils.isExtension(file.getName(), "xml")) {
                        importFile(file, showAlerts);
                    }
                }

                return true;
            } catch (Exception e) {
                // Let it return false
            }
        }

        return false;
    }

    @Override
    public boolean canImport(TransferSupport support) {
        if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            try {
                List<File> fileList = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

                for (File file : fileList) {
                    if (!FilenameUtils.isExtension(file.getName(), "xml")) {
                        return false;
                    }
                }

                return true;
            } catch (Exception e) {
                // Return true anyway until this bug is fixed:
                // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6759788
                return true;
            }
        }

        return false;
    }

    public abstract void importFile(File file, boolean showAlerts);
}