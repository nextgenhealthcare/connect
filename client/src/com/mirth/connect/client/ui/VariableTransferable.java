/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * 
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL license a copy of which has
 * been included with this distribution in the LICENSE.txt file.
 */

package com.mirth.connect.client.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Map;
import java.util.regex.Pattern;

import com.mirth.connect.client.ui.VariableListHandler.TransferMode;

/**
 * Package Database Variables for movement.
 */
public class VariableTransferable implements Transferable {

    private static DataFlavor[] flavors = null;
    private static final Pattern VALID_VELOCITY_VARIABLE_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_-]*");
    private String data = null;
    private TransferMode transferMode;
    private Map<String, Integer> metaDataMap;

    public VariableTransferable(String data, TransferMode transferMode) {
        this(data, transferMode, null);
    }

    /**
     * @param data
     *            the type of Ant element being transferred, e.g., target, task,
     *            type, etc.
     */
    public VariableTransferable(String data, TransferMode transferMode, Map<String, Integer> metaDataMap) {
        this.transferMode = transferMode;
        this.metaDataMap = metaDataMap;

        if (data.equals("CDATA Tag")) {
            this.data = "<![CDATA[]]>";
            this.transferMode = TransferMode.RAW;
        } else {
            this.data = data;
        }
        init();
    }

    /**
     * Set up the supported flavors: DataFlavor.stringFlavor for a raw string
     * containing an Ant element name (e.g. task, target, etc), or an
     * ElementFlavor containing an ElementPanel.
     */
    private void init() {
        try {
            flavors = new DataFlavor[1];
            flavors[0] = DataFlavor.stringFlavor;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param df
     *            the flavor type desired for the data. Acceptable value is
     *            DataFlavor.stringFlavor.
     * @return if df is DataFlavor.stringFlavor, returns a raw string containing
     *         an Ant element name.
     */
    public Object getTransferData(DataFlavor df) {
        if (df == null) {
            return null;
        }

        if (data != null) {
            String replacedData = data;
            String prefix = transferMode.getPrefix();
            String suffix = transferMode.getSuffix();

            if (transferMode != TransferMode.RAW) {
                // Replace connector names with metadata IDs
                if (metaDataMap != null && metaDataMap.containsKey(data)) {
                    replacedData = "d" + String.valueOf(metaDataMap.get(data));
                    if (transferMode == TransferMode.VELOCITY) {
                        suffix = ".message" + suffix;
                    }
                }

                if (transferMode == TransferMode.VELOCITY && !VALID_VELOCITY_VARIABLE_PATTERN.matcher(replacedData).matches()) {
                    prefix += "maps.get('";
                    suffix = "')" + suffix;
                }

                if (replacedData.contains("'")) {
                    prefix = prefix.replaceAll("'", "\"");
                    suffix = suffix.replaceAll("'", "\"");
                }
            }

            return prefix + replacedData + suffix;
        }
        return null;
    }

    /**
     * @return an array containing a single ElementFlavor.
     */
    public DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    /**
     * @param df
     *            the flavor to check
     * @return true if df is an ElementFlavor
     */
    public boolean isDataFlavorSupported(DataFlavor df) {
        if (df == null) {
            return false;
        }
        for (int i = 0; i < flavors.length; i++) {
            if (df.equals(flavors[i])) {
                return true;
            }
        }
        return false;
    }
}
