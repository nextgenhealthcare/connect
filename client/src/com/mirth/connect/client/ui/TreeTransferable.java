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

import javax.swing.tree.TreeNode;

import com.mirth.connect.client.ui.components.MirthTree;
import com.mirth.connect.client.ui.editors.MessageTreePanel;

/**
 * Package TreeNodes for movement.
 */
public class TreeTransferable implements Transferable {

    public static final DataFlavor MAPPER_DATA_FLAVOR = new DataFlavor(MapperDropData.class, "MapperDropData");
    public static final DataFlavor MESSAGE_BUILDER_DATA_FLAVOR = new DataFlavor(MessageBuilderDropData.class, "MessageBuilderDropData");
    public static final DataFlavor RULE_DATA_FLAVOR = new DataFlavor(RuleDropData.class, "RuleDropData");
    private static DataFlavor[] flavors = null;
    private TreeNode data = null;
    private String prefix = MessageTreePanel.MAPPER_PREFIX;
    private String suffix = MessageTreePanel.MAPPER_SUFFIX;

    /**
     * @param data
     *            the type of Ant element being transferred, e.g., target, task,
     *            type, etc.
     */
    public TreeTransferable(TreeNode data, String prefix, String suffix, DataFlavor supportedDropFlavor) {
        this.data = data;
        this.prefix = prefix;
        this.suffix = suffix;
        init(supportedDropFlavor);
    }

    /**
     * Set up the supported flavors: DataFlavor.stringFlavor for a raw string
     * containing an Ant element name (e.g. task, target, etc), or an
     * ElementFlavor containing an ElementPanel.
     */
    private void init(DataFlavor supportedDropFlavor) {
        try {
            flavors = new DataFlavor[3];
            flavors[0] = DataFlavor.stringFlavor;
            flavors[1] = supportedDropFlavor;
            flavors[2] = RULE_DATA_FLAVOR;
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
            if (df == flavors[0]) {
                /*
                 * Always use a blank suffix if the accelerator key (CTRL on
                 * Windows) is pressed. This allows CTRL + Drag to be used to
                 * mapp into msg where we don't want to append a ".toString()".
                 */
                return MirthTree.constructPath(data.getParent(), prefix, (PlatformUI.MIRTH_FRAME.isAcceleratorKeyPressed() ? "" : suffix)).toString();
            }
            if (df == flavors[1]) {
                if (prefix.equals(MessageTreePanel.MAPPER_PREFIX)) {
                    return new MapperDropData(data, MirthTree.constructVariable(data.getParent()), MirthTree.constructPath(data.getParent(), prefix, suffix).toString());
                } else {
                    return new MessageBuilderDropData(data, MirthTree.constructPath(data.getParent(), prefix, suffix).toString(), "");
                }
            }
            if (df == flavors[2]) {
                return new RuleDropData(data, MirthTree.constructPath(data.getParent(), prefix, suffix).toString());
            }
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
